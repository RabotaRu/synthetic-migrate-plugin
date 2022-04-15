package ru.rabota.synthmigrate.dialog

interface SynthMigrateDialogView {

    fun clearMigrateInputs()

    fun clearReplaceInputs()

    fun showError(title: String, message: String)

    fun showSuccess(text: String)

    fun dispose()
}