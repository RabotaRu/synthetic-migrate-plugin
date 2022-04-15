package ru.rabota.synthmigrate.models

data class Element(
    val viewId: String,
    val viewName: String?,
    val parent: Element? = null,
    var layout: String? = null
)