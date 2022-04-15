package ru.rabota.synthmigrate

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import ru.rabota.synthmigrate.dialog.SynthMigrateDialogPresenter
import ru.rabota.synthmigrate.dialog.SynthMigrateDialogView
import ru.rabota.synthmigrate.models.MigrateSettings
import ru.rabota.synthmigrate.models.ReplaceModel
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class SynthMigrateDialog(
    private val project: Project
) : JDialog(), SynthMigrateDialogView {

    private lateinit var contentPane: JPanel
    private lateinit var buttonMigrate: JButton
    private lateinit var buttonCancel: JButton
    private lateinit var parentClassName: JTextField
    private lateinit var baseClassName: JTextField
    private lateinit var bindingVariableName: JTextField
    private lateinit var methodInitName: JTextField
    private lateinit var isLocalVariableCheckBox: JCheckBox
    private lateinit var initTemplate: JTextField
    private lateinit var imports: JTextArea
    private lateinit var replaceFrom: JTextField
    private lateinit var replaceTo: JTextField
    private lateinit var replacePanel: JPanel
    private lateinit var buttonClear: JButton
    private lateinit var allContent: JPanel
    private lateinit var btnReplace: JButton
    private lateinit var btnClearReplace: JButton
    private lateinit var replaceImports: JTextField
    private lateinit var tabPanel: JTabbedPane

    private val presenter by lazy { SynthMigrateDialogPresenter(project) }


    init {
        setContentPane(contentPane)

        presenter.attachView(this)

        initUi()
    }

    private fun initUi() {
        isModal = true
        getRootPane().defaultButton = buttonMigrate

        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                presenter.onCancelClick()
            }
        })

        contentPane.registerKeyboardAction(
            { presenter.onCancelClick() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        tabPanel.addChangeListener { presenter.onTabChange() }


        btnReplace.addActionListener { presenter.onReplaceClick(createReplaceModel()) }
        btnClearReplace.addActionListener { presenter.onClearClick() }
        buttonCancel.addActionListener { presenter.onCancelClick() }
        buttonMigrate.addActionListener { presenter.onMigrateClick(createSetting()) }
        buttonClear.addActionListener { presenter.onClearClick() }
    }

    override fun showSuccess(text: String) {
        Messages.showInfoMessage(project, text, "Успех")
    }

    override fun clearMigrateInputs() {
        parentClassName.text = String()
        baseClassName.text = String()
        bindingVariableName.text = String()
        methodInitName.text = String()
        isLocalVariableCheckBox.isSelected = false
        initTemplate.text = String()
        imports.text = String()
    }

    override fun clearReplaceInputs() {
        replaceFrom.text = String()
        replaceTo.text = String()
        replaceImports.text = String()
    }

    override fun showError(title: String, message: String) {
        Messages.showWarningDialog(project, message, title)
    }

    private fun createSetting(): MigrateSettings {
        return MigrateSettings(
            parentClassName = parentClassName.text.takeIf { !it.isNullOrBlank() },
            baseClassName = baseClassName.text.takeIf { !it.isNullOrBlank() },
            bindingVariableName = bindingVariableName.text.takeIf { !it.isNullOrBlank() },
            methodInitName = methodInitName.text.takeIf { !it.isNullOrBlank() },
            isLocalVariable = isLocalVariableCheckBox.isSelected,
            initTemplate = initTemplate.text,
            imports = imports.text.takeIf { !it.isNullOrBlank() }
        )
    }

    private fun createReplaceModel(): ReplaceModel {
        return ReplaceModel(
            replaceFrom = replaceFrom.text,
            replaceTo = replaceTo.text,
            import = replaceImports.text.takeIf { !it.isNullOrBlank() }
        )
    }

    override fun dispose() {
        super.dispose()
        presenter.detachView()
    }
}