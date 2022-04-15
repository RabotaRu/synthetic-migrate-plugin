package ru.rabota.synthmigrate.migration

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import ru.rabota.synthmigrate.migration.collect.CollectRelevantPsiFile
import ru.rabota.synthmigrate.migration.generate.variable.AddPrefixToViewExpression
import ru.rabota.synthmigrate.migration.generate.variable.GenerateBindingVariable
import ru.rabota.synthmigrate.migration.importutill.DeleteSynthImport
import ru.rabota.synthmigrate.migration.importutill.InsertNewImport
import ru.rabota.synthmigrate.models.MigrateSettings

class MigrateHelper(
    private val project: Project,
    private val migrateSettings: MigrateSettings
) {

    private val relevantFiles = mutableListOf<PsiFile>()

    private lateinit var rootDirectory: PsiDirectory

    fun startMigrate() {
        val projectDir = project.guessProjectDir() ?: return
        rootDirectory = PsiManager.getInstance(project).findDirectory(projectDir) ?: return

        val collectRelevantPsiFile = CollectRelevantPsiFile(migrateSettings)
        relevantFiles.addAll(collectRelevantPsiFile.invoke(rootDirectory))


        val insertNewImport = InsertNewImport()
        val deleteSynthImport = DeleteSynthImport()

        val generateBindingVariable = GenerateBindingVariable(
            migrateSettings,
            AddPrefixToViewExpression(migrateSettings)
        )

        relevantFiles.forEach { psiFile ->
            val imports = generateBindingVariable.invoke(psiFile)
            insertNewImport(imports, migrateSettings.imports, psiFile)
            deleteSynthImport.invoke(psiFile)
        }
    }
}