package com.github.shyiko.ktlint.ruleset.standard

import com.github.shyiko.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class ClassLayoutRule : Rule(RULE_ID) {
    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node.elementType == KtStubElementTypes.CLASS_BODY) {
            var layoutState: LayoutState = LayoutState.initialState
            for (child in node.children()) {
                try {
                    layoutState = layoutState.nextState(child)
                } catch (exception: LayoutState.IllegalLayoutState) {
                    emit(child.startOffset, exception.message, false)
                }
            }
        }
    }

    private sealed class LayoutState {

        protected abstract fun accept(node: ASTNode): Boolean

        fun nextState(node: ASTNode): LayoutState {
            val nextStateCoordinate = orderedLayoutStateList.find { it.accept(node) } ?: return this

            if (orderedLayoutStateList.indexOf(this) <= orderedLayoutStateList.indexOf(nextStateCoordinate)) {
                return nextStateCoordinate
            } else {
                throw IllegalLayoutState("${nextStateCoordinate.toString().capitalize()} should be before $this")
            }
        }

        private object PropertyOrInitializer : LayoutState() {
            override fun accept(node: ASTNode): Boolean =
                setOf(KtStubElementTypes.PROPERTY, KtStubElementTypes.CLASS_INITIALIZER).contains(node.elementType)

            override fun toString(): String = "property or initializer"
        }

        private object SecondaryConstructor : LayoutState() {
            override fun accept(node: ASTNode): Boolean =
                node.elementType == KtStubElementTypes.SECONDARY_CONSTRUCTOR

            override fun toString(): String = "secondary constructor"
        }

        private object Method : LayoutState() {
            override fun accept(node: ASTNode): Boolean =
                node.elementType == KtStubElementTypes.FUNCTION

            override fun toString(): String = "method"
        }

        private object CompanionObject : LayoutState() {
            override fun accept(node: ASTNode): Boolean {
                val psi = node.psi
                return psi is KtObjectDeclaration && psi.isCompanion()
            }

            override fun toString(): String = "companion object"
        }

        class IllegalLayoutState(override val message: String) : IllegalStateException(message)

        companion object {
            private val orderedLayoutStateList = listOf(
                PropertyOrInitializer,
                SecondaryConstructor,
                Method,
                CompanionObject
            )

            val initialState: LayoutState = orderedLayoutStateList.first()
        }
    }

    companion object {
        private const val RULE_ID = "class-layout"
    }
}
