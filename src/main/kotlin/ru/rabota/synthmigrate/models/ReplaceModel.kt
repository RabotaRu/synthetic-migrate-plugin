package ru.rabota.synthmigrate.models

data class ReplaceModel(
    var replaceFrom: String = "",
    var replaceTo: String = "",
    var import: String? = null
)