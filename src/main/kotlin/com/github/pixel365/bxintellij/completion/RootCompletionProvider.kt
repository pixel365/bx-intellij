package com.github.pixel365.bxintellij.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class RootCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val rootKeys = listOf(
        "name",
        "version",
        "label",
        "account",
        "buildDirectory",
        "logDirectory",
        "repository",
        "variables",
        "changelog",
        "stages",
        "callbacks",
        "builds",
        "run",
        "ignore"
    )

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
        rootKeys.filterNot {existingKeys.contains(it) }
            .forEach{ key ->
                result.addElement(LookupElementBuilder.create(key).withBoldness(true))
            }
    }
}
