package com.github.pixel365.bxintellij


import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class YamlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(YAMLMapping::class.java),
            RootCompletionProvider()
        )
    }

    private class RootCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val file = position.containingFile as? YAMLFile ?: return
            val root = file.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return
            val rootMapping = PsiTreeUtil.getParentOfType(position, YAMLMapping::class.java, false) ?: return
            val keyValue = PsiTreeUtil.getParentOfType(position, YAMLKeyValue::class.java, false)

            if (keyValue != null) return
            if (rootMapping.parent !is YAMLDocument) return

            val existingKeys = root.keyValues.map { it.keyText }
            val rootKeys = listOf("name", "version", "account", "buildDirectory", "logDirectory",
                "variables", "stages", "callbacks", "ignore")
            rootKeys.filterNot {existingKeys.contains(it) }
                .forEach{ key ->
                result.addElement(LookupElementBuilder.create(key).withBoldness(true))
            }
        }
    }
}