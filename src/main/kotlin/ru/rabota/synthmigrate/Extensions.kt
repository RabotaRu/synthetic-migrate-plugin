package ru.rabota.synthmigrate

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

fun Project.executeWrite(runnable: Runnable) {
    WriteCommandAction.runWriteCommandAction(this, runnable)
}

fun GlobalSearchScope.findFiles(project: Project, fileName: String): Array<PsiFile> {
    return FilenameIndex.getFilesByName(project, fileName, this)
}
