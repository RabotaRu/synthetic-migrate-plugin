package ru.rabota.synthmigrate.migration

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath
import ru.rabota.synthmigrate.executeWrite
import ru.rabota.synthmigrate.migration.generate.variable.findViewBindingType
import ru.rabota.synthmigrate.models.ReplaceModel
import ru.rabota.synthmigrate.models.ReplaceResult

class ReplaceHelper(
    private val project: Project
) {

    private val replaceResult = ReplaceResult()

    fun replace(replaceModel: ReplaceModel): ReplaceResult {
        val notFoundResult = ReplaceResult(errorMessage = "Не найденана директория проекта")
        val projectDir = project.guessProjectDir() ?: return notFoundResult
        val rootDirectory = PsiManager.getInstance(project).findDirectory(projectDir) ?: return notFoundResult

        rootDirectory.accept(object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                super.visitClass(klass)
                klass.superTypeListEntries.forEach { superType ->
                    val superKElement = superType.typeAsUserType?.referenceExpression?.resolve() as? KtElement
                    if (superKElement?.name == replaceModel.replaceFrom &&
                        klass.isWritable
                    ) {
                        println("Start replace ${superType.text}")
                        println("for class ${klass.name}")
                        klass.replaceSupertype(superType, replaceModel)
                        println("------")
                    }
                }
            }
        })
        return replaceResult
    }

    private fun KtClass.replaceSupertype(
        ktSuperTypeEntry: KtSuperTypeListEntry,
        replaceModel: ReplaceModel
    ) {
        val factory = KtPsiFactory(project)
        val viewBindingType = findViewBindingType()
        val formattedReplaceTo =
            replaceModel.replaceTo.templateReplaceOldType(ktSuperTypeEntry.text, viewBindingType)
        println("Formatted $formattedReplaceTo")
        replaceResult.replaceCount++
        val newSuperType = kotlin.runCatching { factory.createSuperTypeCallEntry(formattedReplaceTo) }
            .getOrNull() ?: factory.createSuperTypeEntry(formattedReplaceTo)

        project.executeWrite {
            ktSuperTypeEntry.replace(newSuperType)
        }

        containingKtFile.replaceImport(replaceModel)
    }

    private fun KtFile.replaceImport(replaceModel: ReplaceModel) {
        if (replaceModel.import.isNullOrBlank()) return
        val factory = KtPsiFactory(project)
        importDirectives.forEach { import ->
            if (import.text.contains(replaceModel.replaceFrom)) {
                val newImport = factory.createImportDirective(
                    ImportPath.fromString(replaceModel.import ?: String())
                )
                project.executeWrite {
                    import.replace(newImport)
                }
            }
        }
    }
}