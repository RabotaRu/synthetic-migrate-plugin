package ru.rabota.synthmigrate.migration.generate.variable

import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.KtClass
import ru.rabota.synthmigrate.migration.snakeToUpperCamelCase

private const val VIEW_BINDING_POSTFIX = "Binding"
private const val LAYOUT_PREFIX = "R.layout."

//ищем R.layout в текущем классе и выдергиваем название лэйаута в камел кейс + ViewBinding,
// если нет идем в суперклассы и там ищем
//takeWhile сделано чтобы если например после лэйаута есть какие то посторонние символы
// типо закрытия скобок мы их случайно в название не притащили
fun KtClass.findViewBindingType(): String? {
    val layout = body?.children?.firstOrNull { it.text.contains(LAYOUT_PREFIX) }?.text

    return layout?.substringAfter(LAYOUT_PREFIX)?.takeWhile { char ->
        char.isLetterOrDigit() || char == '_'
    }?.snakeToUpperCamelCase()?.toViewBindingName() ?: findViewBindingInSuperClass()
}

//смотрим суперклассы на предмет R.layout
fun KtClass.findViewBindingInSuperClass(): String? {
    var viewBinding: String?
    superTypeListEntries.forEach { superType ->
        val userType = superType.typeAsUserType
        val resolvedReference = userType?.referenceExpression?.resolve() as? KtClass
        viewBinding = resolvedReference?.findViewBindingType()
        if (viewBinding != null) return viewBinding
    }
    return null
}

private fun String.toViewBindingName(): String {
    return "$this${VIEW_BINDING_POSTFIX}"
}