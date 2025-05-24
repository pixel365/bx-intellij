package com.github.pixel365.bxintellij

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class YamlConfigInspection : LocalInspectionTool() {
    private val requiredKeys = listOf(
        "name",
        "version",
        "account",
        "buildDirectory",
        "stages",
        "builds"
    )

    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<out ProblemDescriptor?>? {
        if (file !is YAMLFile) return emptyArray()
        if (!file.virtualFile.path.contains("/.bx/")) return emptyArray()

        val root = file.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return emptyArray()
        val problems = mutableListOf<ProblemDescriptor>()

        requiredKeys.forEach { key ->
            if (!root.keyValues.map { it.keyText }.contains(key)) {
               val problem = manager.createProblemDescriptor(
                   file,
                   TextRange(0, file.textLength),
                   "Required key '$key' does not exist",
                   ProblemHighlightType.WARNING,
                   true,
                   MissingKeyFix(key)
               )
                problems.add(problem)
            }
        }

        return problems.toTypedArray()
    }

    private class MissingKeyFix(private val key: String) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Add missing key"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile as? YAMLFile ?: return
            val root = file.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return
            val newKeyValue = createYAMLKeyValue(key, "", project)

            if (newKeyValue == null) return

            root.putKeyValue(newKeyValue)

            PsiDocumentManager.getInstance(project).commitDocument(file.viewProvider.document!!)
            DaemonCodeAnalyzer.getInstance(project).restart(file)
        }

        fun createYAMLKeyValue(key: String, value: String, project: Project): YAMLKeyValue? {
            val yamlText = "$key: $value"
            val yamlFile = PsiFileFactory.getInstance(project)
                .createFileFromText("temp.yaml", YAMLFileType.YML, yamlText) as? YAMLFile

            return yamlFile?.documents?.firstOrNull()
                ?.topLevelValue
                ?.children?.filterIsInstance<YAMLKeyValue>()
                ?.firstOrNull()
        }
    }
}
