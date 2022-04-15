package ru.rabota.synthmigrate.migration.importutill

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath
import ru.rabota.synthmigrate.executeWrite

class InsertNewImport {

    operator fun invoke(listImport: List<String>, psiFile: PsiFile) {
        val ktImports = PsiTreeUtil.findChildOfType(psiFile, KtImportList::class.java)

        val listImportDirective = createListImportDirective(psiFile.project, listImport)
        val lastImport = ktImports?.imports?.lastOrNull()
        psiFile.project.executeWrite {
            listImportDirective.forEach { ktImports?.addBefore(it, lastImport) }
        }
    }

    operator fun invoke(newImports: List<String>, newImportsString: String?, psiFile: PsiFile) {
        val listString = newImportsString?.split("\n") ?: emptyList()
        var result = newImports.toMutableList()
        result.addAll(listString)
        result = result.filter { it.isNotBlank() }.distinct().toMutableList()
        if (result.isNotEmpty()) invoke(result, psiFile)
    }


    private fun createListImportDirective(project: Project, listImport: List<String>): List<KtImportDirective> {
        if (listImport.isEmpty()) return emptyList()
        return listImport.mapNotNull { importPath ->
            if (importPath.isNotBlank()) {
                KtPsiFactory(project).createImportDirective(ImportPath.fromString(importPath))
            } else null
        }
    }
}