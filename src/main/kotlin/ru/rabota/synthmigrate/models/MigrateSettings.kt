package ru.rabota.synthmigrate.models

data class MigrateSettings(
    var parentClassName: String? = null,
    var baseClassName: String? = null,
    var bindingVariableName: String? = null,
    var methodInitName: String? = null,
    var isLocalVariable: Boolean = false,
    var initTemplate: String = "",
    var imports: String? = null
) {
    companion object {
        const val DEFAULT_BINDING_NAME = "binding"
    }

    val hasInitMethod: Boolean
        get() = !methodInitName.isNullOrBlank()

    fun getNotNullBindingVariableName(): String = bindingVariableName ?: DEFAULT_BINDING_NAME
}