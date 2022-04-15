package ru.rabota.synthmigrate.migration.collect

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.EverythingGlobalScope
import ru.rabota.synthmigrate.findFiles

object AndroidLayoutUtils {

    fun getLayoutName(layout: String?): String? {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null // it's not layout identifier
        }
        val parts = layout.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size != 2) {
            null // not enough parts
        } else parts[1]
    }


    fun findLayoutResourceFile(element: PsiElement, project: Project, fileName: String): PsiFile? {

        val module = ModuleUtil.findModuleForPsiElement(element)

        var files: Array<PsiFile>? = null
        if (module != null) {
            // 在模块范围搜索文件
            files = module.getModuleWithDependenciesAndLibrariesScope(false).findFiles(project, fileName)
        }

        if (files == null || files.isEmpty()) {
            // 在整个工程范围搜索文件
            files = EverythingGlobalScope(project).findFiles(project, fileName)
        }

        if (files.isEmpty()) {// 没找到文件
            return null
        }

        // TODO - we have a problem here - we still can have multiple layouts (some coming from a dependency)
        // we need to resolve R class properly and find the proper layout for the R class
        return files[0]
    }
}