package ru.rabota.synthmigrate.migration.generate.variable

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName
import ru.rabota.synthmigrate.executeWrite
import ru.rabota.synthmigrate.migration.collect.hasRelevantParent
import ru.rabota.synthmigrate.migration.removePrefix
import ru.rabota.synthmigrate.migration.replace
import ru.rabota.synthmigrate.migration.templateReplaceType
import ru.rabota.synthmigrate.models.MigrateSettings


class GenerateBindingVariable(
    private val settings: MigrateSettings,
    private val addPrefixToViewExpression: AddPrefixToViewExpression
) {

    companion object {
        private const val VAL = "val"
        private const val LATE_INIT = "lateinit var"
        private const val SUPER_PREFIX = "super."
        private const val PRIVATE = "private"
        private const val OVERRIDE = "override"
    }

    operator fun invoke(psiFile: PsiFile): List<String> {
        val imports = mutableSetOf<String>()

        val ktClasses = PsiTreeUtil.findChildrenOfType(psiFile, KtClass::class.java)

        ktClasses.forEach { klass ->
            val isRelevantClass = klass.hasRelevantParent(settings.parentClassName)

            if (isRelevantClass) {
                println("Relevant class ${klass.name}")
                val hasBaseClass = klass.hasRelevantParent(settings.baseClassName, returnIfNull = false)
                println("Has base class $hasBaseClass")
                val viewBindingType = klass.generateBindingInClass(hasBaseClass)
                addPrefixToViewExpression.invoke(klass)
                val import = klass.module?.getDefaultPackage()
                if (viewBindingType != null && import != null) {
                    imports.add("$import.databinding.$viewBindingType")
                }
            }
        }

        return imports.toList()
    }

    private fun KtClass.generateBindingInClass(hasBaseClass: Boolean = false): String? {
        val modifier = if (hasBaseClass) OVERRIDE else PRIVATE
        val bindingBuilder = StringBuilder("$modifier $VAL")

        val viewBindingType = findViewBindingType() ?: return null
        val initExpression = settings.initTemplate.templateReplaceType(viewBindingType)

        if (settings.hasInitMethod) {
            if (!settings.isLocalVariable) {
                bindingBuilder.clear()
                bindingBuilder.append("$modifier $LATE_INIT")
            }
        }
        bindingBuilder.append(" ")
        bindingBuilder.append(settings.getNotNullBindingVariableName())

        if (!settings.hasInitMethod) {
            bindingBuilder.append(initExpression)
            writeInClass(bindingBuilder.toString())
        } else {
            if (settings.isLocalVariable) {
                bindingBuilder.append(initExpression)
                bindingBuilder.removePrefix(modifier)
                writeInFunction(bindingBuilder.toString())
            } else {
                bindingBuilder.append(" :$viewBindingType")
                writeInClass(bindingBuilder.toString())
                bindingBuilder.removePrefix(modifier)
                bindingBuilder.removePrefix(LATE_INIT)
                bindingBuilder.replace(":$viewBindingType", "")
                bindingBuilder.append(initExpression)
                writeInFunction(bindingBuilder.toString().trim(), inClassDeclared = true)
            }
        }
        return viewBindingType
    }

    private fun KtClass.writeInClass(expression: String) {
        val childAnchor = body?.children?.firstOrNull()
        val factory = KtPsiFactory(project)
        val psiElement = factory.createProperty(expression)
        project.executeWrite {
            body?.addBefore(psiElement, childAnchor)
        }
    }

    private fun KtClass.writeInFunction(expression: String, inClassDeclared: Boolean = false) {
        val func = findFunctionByName(settings.methodInitName ?: "") ?: return
        println("FUN ${func.name}")
        println(func.text)

        val functionBody = func.children.firstOrNull { it is KtBlockExpression }
        val childAnchor =
            functionBody?.children?.firstOrNull { it.text.contains(SUPER_PREFIX) } ?: functionBody?.firstChild

        print("Child anchor ${childAnchor?.text}")

        val factory = KtPsiFactory(project)
        val psiElement = if (inClassDeclared) {
            factory.createExpression(expression)
        } else {
            factory.createProperty(expression)
        }

        project.executeWrite {
            if (inClassDeclared) {
                val whiteSpace = factory.createWhiteSpace("\n")
                functionBody?.addAfter(whiteSpace, childAnchor)
            } else {
                functionBody?.addAfter(psiElement, childAnchor)
            }
        }
        if (inClassDeclared) {
            val sibling = childAnchor?.nextSibling
            project.executeWrite {
                functionBody?.addAfter(psiElement, sibling)
            }
        }
    }
}