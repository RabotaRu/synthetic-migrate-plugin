package ru.rabota.synthmigrate.migration.importutill

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtImportList
import ru.rabota.synthmigrate.executeWrite
import ru.rabota.synthmigrate.migration.collect.CollectRelevantPsiFile.Companion.PART_SYNTHETIC_IMPORT

class DeleteSynthImport {

    operator fun invoke(psiFile: PsiFile) {
        val ktImports = PsiTreeUtil.findChildOfType(psiFile, KtImportList::class.java)

        ktImports?.imports?.forEach { psiImportStatementBase ->
            println(psiImportStatementBase.text)
            if (psiImportStatementBase.text.contains(PART_SYNTHETIC_IMPORT)) {
                psiFile.project.executeWrite {
                    psiImportStatementBase.delete()
                }
            }
        }
    }
}