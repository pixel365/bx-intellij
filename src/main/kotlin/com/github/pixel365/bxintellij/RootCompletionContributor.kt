package com.github.pixel365.bxintellij


import com.github.pixel365.bxintellij.completion.RootCompletionProvider
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import org.jetbrains.yaml.psi.YAMLMapping

class RootCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(YAMLMapping::class.java),
            RootCompletionProvider()
        )
    }
}
