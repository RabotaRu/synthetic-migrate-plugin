package ru.rabota.synthmigrate.migration.collect

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

fun KtFile.hasRelevantParent(
    className: String?,
    nameCheckClass: String? = null,
    returnIfNull: Boolean = true
): Boolean {
    val parentClassName = if (!className.isNullOrBlank()) {
        className
    } else return returnIfNull

    return classes.filter { psiClass -> nameCheckClass?.let { psiClass.name == it } ?: true }.any { psiClass ->
        println()
        println(psiClass.name)
        var superClass = psiClass.superClass
        while (superClass != null && superClass.name != parentClassName) {
            println(superClass.name)
            superClass = superClass.superClass
        }
        println(superClass?.name)
        return superClass != null
    }
}

fun KtClass.hasRelevantParent(className: String?, returnIfNull: Boolean = true): Boolean {
    return containingKtFile.hasRelevantParent(className, name, returnIfNull)
}