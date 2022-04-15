package ru.rabota.synthmigrate.migration.generate.variable

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtReferenceExpression
import ru.rabota.synthmigrate.executeWrite
import ru.rabota.synthmigrate.migration.snakeToLowerCamelCase
import ru.rabota.synthmigrate.models.MigrateSettings

class AddPrefixToViewExpression(
    private val migrateSettings: MigrateSettings
) {
    operator fun invoke(psiElement: PsiElement) {
        val expressions = PsiTreeUtil.findChildrenOfType(psiElement, KtReferenceExpression::class.java)
        expressions.forEach { referenceExpression ->
            referenceExpression.addViewBinding()
        }
    }

    private fun PsiElement.addViewBinding() {
        val isView = references.any { ref ->
            val resolve = ref.resolve()
            println(ref)
            println("element " + ref.element)
            println("resolve $resolve")

            println("----")
            resolve.elementType == XmlElementType.XML_ATTRIBUTE_VALUE
        }

        if (isView) {
            val newExpression = KtPsiFactory(project)
                .createExpression(
                    "${migrateSettings.getNotNullBindingVariableName()}.${text.snakeToLowerCamelCase()}"
                )
            project.executeWrite {
                replace(newExpression)
            }
        }
    }

}