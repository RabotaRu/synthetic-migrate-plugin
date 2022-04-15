package ru.rabota.synthmigrate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class ShowMigrateSettingAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        val dialog = SynthMigrateDialog(project)
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
    }
}