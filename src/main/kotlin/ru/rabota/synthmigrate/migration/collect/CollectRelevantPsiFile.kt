package ru.rabota.synthmigrate.migration.collect

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import ru.rabota.synthmigrate.models.MigrateSettings

class CollectRelevantPsiFile(
    private val migrateSettings: MigrateSettings
) {

    companion object {
        const val PART_SYNTHETIC_IMPORT = "kotlinx.android.synthetic"
    }

    operator fun invoke(rootDirectory: PsiDirectory): MutableList<PsiFile> {
        val relevantFiles = mutableListOf<PsiFile>()
        rootDirectory.accept(object : KtTreeVisitorVoid() {
            override fun visitImportList(importList: KtImportList) {
                super.visitImportList(importList)
                //обходим импорты и ищем синтетику
                if (importList.text.contains(PART_SYNTHETIC_IMPORT)) {
                    //проверяем importList.parent, тобишь файл в котором эти импорты содержаться, на наличие
                    //того что классы там наследуется от класса из migrateSettings.parentClassName
                    if (importList.containingKtFile.hasRelevantParent(migrateSettings.parentClassName)) {
                        relevantFiles.add(importList.parent as PsiFile)
                    }
                }
            }
        })

        return relevantFiles
    }
}