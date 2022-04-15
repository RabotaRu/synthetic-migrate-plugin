package ru.rabota.synthmigrate.dialog

import com.intellij.openapi.project.Project
import ru.rabota.synthmigrate.migration.MigrateHelper
import ru.rabota.synthmigrate.migration.ReplaceHelper
import ru.rabota.synthmigrate.models.MigrateSettings
import ru.rabota.synthmigrate.models.ReplaceModel
import java.lang.ref.WeakReference

class SynthMigrateDialogPresenter(
    private val project: Project
) {

    companion object {
        private const val VALIDATE_ERROR_TITLE = "Ошибка валидации"
    }

    private var weakView: WeakReference<SynthMigrateDialogView>? = null

    private val view: SynthMigrateDialogView?
        get() = weakView?.get()

    private var currentTab: Tab = Tab.MIGRATE

    private val replaceHelper = ReplaceHelper(project)

    fun attachView(dialogView: SynthMigrateDialogView) {
        this.weakView = WeakReference(dialogView)
    }

    fun detachView() {
        weakView = null
    }

    fun onClearClick() {
        when (currentTab) {
            Tab.MIGRATE -> view?.clearMigrateInputs()
            Tab.REPLACE -> view?.clearReplaceInputs()
        }
    }

    fun onMigrateClick(migrateSettings: MigrateSettings) {
        val isValid = validateFields(migrateSettings)
        if (isValid) {
            val migrateHelper = MigrateHelper(project, migrateSettings)
            migrateHelper.startMigrate()
            view?.showSuccess("Успешно мигрировали")
        }
    }

    fun onReplaceClick(replaceModel: ReplaceModel) {
        val result = replaceHelper.replace(replaceModel)
        val errMsg = result.errorMessage
        if (errMsg == null) {
            view?.showSuccess("Успешно заменили ${result.replaceCount} наследований")
        } else {
            view?.showError("Ошибка", errMsg)
        }
    }

    fun onCancelClick() {
        view?.dispose()
    }

    fun onTabChange() {
        currentTab = when (currentTab) {
            Tab.REPLACE -> Tab.MIGRATE
            Tab.MIGRATE -> Tab.REPLACE
        }
    }

    private fun validateFields(replaceModel: ReplaceModel): Boolean {
        var isValid = true
        val errorMessage = StringBuilder()
        if (replaceModel.replaceFrom.isBlank()) {
            errorMessage.append("Заполните поле \"Что заменяем\"")
            isValid = false
        }

        if (replaceModel.replaceTo.isBlank()) {
            errorMessage.append("\n")
            errorMessage.append("Заполните поле \"На что заменяем\"")
            isValid = false
        }
        if (!isValid) {
            view?.showError(VALIDATE_ERROR_TITLE, errorMessage.toString())
        }
        return isValid
    }

    private fun validateFields(migrateSettings: MigrateSettings): Boolean {
        var isValid = true
        val errorMessage = StringBuilder()
        if (migrateSettings.initTemplate.isBlank()) {
            errorMessage.append("Заполните поле \"Шаблон инициализации\"")
            isValid = false
        }
        if (migrateSettings.isLocalVariable && migrateSettings.methodInitName.isNullOrBlank()) {
            errorMessage.append("\n")
            errorMessage.append("Заполните поле \"Метод инициализации\" или уберите галочку с локальной переменной")
            isValid = false
        }

        if (!isValid) {
            view?.showError(VALIDATE_ERROR_TITLE, errorMessage.toString())
        }

        return isValid
    }
}