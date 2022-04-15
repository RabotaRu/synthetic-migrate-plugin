package ru.rabota.synthmigrate.migration.collect

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlFile
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import ru.rabota.synthmigrate.models.Element
import javax.xml.parsers.SAXParserFactory

class CollectLayoutIds {

    companion object {
        private const val PART_PATH_TO_LAYOUT = "res/layout"
    }

    //layout_name - список айдишек
    operator fun invoke(rootDirectory: PsiDirectory): Map<String, List<Element>> {
        val result = mutableMapOf<String, List<Element>>()

        rootDirectory.accept(object : XmlRecursiveElementVisitor() {

            override fun visitXmlFile(file: XmlFile?) {
                super.visitXmlFile(file)
                if (file == null) return
                if (file.isPhysical && file.virtualFile.path.contains(PART_PATH_TO_LAYOUT)) {
                    val ids = file.getAndroidViewElements()
                    println(file.name)
                    println(ids)
                    if (ids.isNotEmpty()) {
                        result[file.name] = file.getAndroidViewElements()
                    }
                }
            }

        })

        return result
    }
}

private fun PsiFile.getAndroidViewElements(parent: Element? = null): List<Element> {

    val elements = mutableListOf<Element>()

    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()

    val handler = object : DefaultHandler() {
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            // get element ID
            val id = attributes?.getValue("android:id")
                ?: ""  // missing android:id attribute
            // check if there is defined custom class
            var name: String? = qName
            val clazz = attributes?.getValue("class")
            if (clazz != null) {
                name = clazz
            }
            var element: Element? = null
            try {
                id.split("/").lastOrNull()?.let { clearId ->
                    element = Element(clearId, name, parent)
                    elements.add(element!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            if ("include".equals(qName, ignoreCase = true)) {
                val layout = attributes?.getValue("layout")

                if (layout != null) {
                    val project = this@getAndroidViewElements.project
                    val layoutName = AndroidLayoutUtils.getLayoutName(layout)
                    element?.layout = layoutName
                    val include = if (layoutName == null) null else
                        AndroidLayoutUtils.findLayoutResourceFile(this@getAndroidViewElements, project, "$layoutName.xml")

                    if (include != null) {
                        elements.addAll(include.getAndroidViewElements(element))
                    }
                }
            }


        }
    }

    kotlin.runCatching { parser.parse(this.text.byteInputStream(), handler) }

    return elements
}