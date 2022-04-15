package ru.rabota.synthmigrate.migration.generate.variable

import com.android.tools.idea.npw.project.getPackageForApplication
import com.intellij.openapi.module.Module
import org.jetbrains.android.facet.AndroidFacet

fun Module.getDefaultPackage(): String? {
    return AndroidFacet.getInstance(this)?.getPackageForApplication()
}