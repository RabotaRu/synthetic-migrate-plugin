package ru.rabota.synthmigrate.migration

fun String.templateReplaceType(viewBinding: String): String {
    return replace("<type>", viewBinding.snakeToUpperCamelCase(), ignoreCase = true)
}

fun String.templateReplaceOldType(oldSuperType: String, viewBinding: String?): String {
    val viewBindingType = viewBinding ?: ""
    val indexOpenGeneric = oldSuperType.indexOf("<")
    val indexCloseGeneric = oldSuperType.indexOf(">")
    val oldGenericParams =  if(indexOpenGeneric != -1) {
        oldSuperType.substring(indexOpenGeneric + 1, indexCloseGeneric).split(",")
    } else {
        emptyList()
    }

    val indexStartParams = oldSuperType.indexOf("(")
    val indexEndParams = oldSuperType.indexOf(")")

    val oldParams = if(indexStartParams > 0) {
        oldSuperType.substring(indexStartParams + 1, indexEndParams).split(",")
    } else {
        emptyList()
    }

    var result = this
    oldGenericParams.forEachIndexed { index, s ->
        result = result.replace("<$index>", s)
    }

    if(result.contains("(")) {
        oldParams.forEachIndexed { index, s ->
            result = result.replace("[$index]", s)
        }
    } else {
        result += "()"
    }

    result = result.replace(Regex("\\[[0-9]+\\],?"),"")
    result = result.replace(Regex("<[0-9]+>,?"),"")
    result = result.replace(Regex("<\\s?>"), "")

    return result.templateReplaceType(viewBindingType)
}

fun String.snakeToLowerCamelCase(): String {
    val snakeRegex = "_[a-zA-Z]".toRegex()
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .toUpperCase()
    }
}

fun String.snakeToUpperCamelCase(): String {
    return this.snakeToLowerCamelCase().capitalize()
}

fun StringBuilder.removePrefix(prefix: String) {
    replace(0, length, (this as CharSequence).removePrefix(prefix).toString())
}

fun StringBuilder.replace(oldValue: String, newValue: String) {
    replace(0, length, (this as CharSequence).replace(oldValue.toRegex(), newValue))
}