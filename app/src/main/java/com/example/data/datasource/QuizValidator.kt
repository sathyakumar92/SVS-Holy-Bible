package com.example.data.datasource

import com.example.data.model.QuizQuestion

object QuizValidator {

    data class ValidationReport(
        val totalQuestions: Int,
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>,
        val categoryCounts: Map<String, Int>,
        val difficultyCounts: Map<String, Int>
    )

    fun validateAllQuestions(questions: List<QuizQuestion> = BibleQuizBank.allQuestions): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val seenIds = mutableSetOf<String>()
        val seenEnglishQuestions = mutableSetOf<String>()
        val categoryCounts = mutableMapOf<String, Int>()
        val difficultyCounts = mutableMapOf<String, Int>()

        if (questions.size < 200) {
            errors.add("Question bank has ${questions.size} questions, which is less than the required 200.")
        }

        questions.forEachIndexed { index, q ->
            // 1. Unique ID
            if (q.id.isBlank()) {
                errors.add("Question at index $index has blank ID.")
            } else if (!seenIds.add(q.id)) {
                errors.add("Duplicate question ID found: '${q.id}' at index $index.")
            }

            // 2. Question Text Checks
            if (q.questionEn.isBlank()) {
                errors.add("Question '${q.id}' has blank English text.")
            }
            if (q.questionTa.isBlank()) {
                errors.add("Question '${q.id}' has blank Tamil text.")
            }

            // 3. Duplicate question wording check
            val normalizedEn = q.questionEn.trim().lowercase()
            if (!seenEnglishQuestions.add(normalizedEn)) {
                warnings.add("Possible duplicate question wording: '${q.questionEn}' in question ${q.id}.")
            }

            // 4. Options count and integrity
            if (q.optionsEn.size != 4) {
                errors.add("Question '${q.id}' has ${q.optionsEn.size} English options; exactly 4 required.")
            }
            if (q.optionsTa.size != 4) {
                errors.add("Question '${q.id}' has ${q.optionsTa.size} Tamil options; exactly 4 required.")
            }
            if (q.optionsEn.any { it.isBlank() }) {
                errors.add("Question '${q.id}' contains blank English option(s).")
            }
            if (q.optionsTa.any { it.isBlank() }) {
                errors.add("Question '${q.id}' contains blank Tamil option(s).")
            }

            // 5. Correct answer index validity
            if (q.correctIndex !in q.optionsEn.indices) {
                errors.add("Question '${q.id}' has invalid correctIndex ${q.correctIndex} (must be 0..${q.optionsEn.size - 1}).")
            }

            // 6. Scripture reference check
            if (q.scriptureRefEn.isBlank()) {
                errors.add("Question '${q.id}' has blank scriptureRefEn.")
            }
            if (q.scriptureRefTa.isBlank()) {
                errors.add("Question '${q.id}' has blank scriptureRefTa.")
            }

            // 7. Explanations
            if (q.explanationEn.isBlank()) {
                errors.add("Question '${q.id}' has blank explanationEn.")
            }
            if (q.explanationTa.isBlank()) {
                errors.add("Question '${q.id}' has blank explanationTa.")
            }

            // 8. Category & Difficulty tallies
            categoryCounts[q.category] = (categoryCounts[q.category] ?: 0) + 1
            difficultyCounts[q.difficulty.name] = (difficultyCounts[q.difficulty.name] ?: 0) + 1
        }

        return ValidationReport(
            totalQuestions = questions.size,
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            categoryCounts = categoryCounts,
            difficultyCounts = difficultyCounts
        )
    }
}
