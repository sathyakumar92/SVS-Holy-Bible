package com.example.data.datasource

import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizQuestion

object BibleQuizBank {

    val categories: List<String> = listOf(
        "Creation and Genesis",
        "Adam and Eve",
        "Noah and the Ark",
        "Abraham, Isaac, and Jacob",
        "Joseph and His Brothers",
        "Moses and the Exodus",
        "The Ten Commandments",
        "Joshua and the Promised Land",
        "Judges of Israel",
        "Kings Saul, David, and Solomon",
        "Psalms and Proverbs",
        "Elijah and Elisha",
        "Prophets of the Old Testament",
        "Daniel and His Friends",
        "Esther and Ruth",
        "The Birth and Childhood of Jesus",
        "The Teachings of Jesus",
        "Miracles of Jesus",
        "Parables of Jesus",
        "The Twelve Disciples",
        "The Last Supper and Crucifixion",
        "Resurrection of Jesus",
        "Acts of the Apostles",
        "Paul and His Missionary Journeys",
        "The Early Church",
        "The Letters of the New Testament",
        "Faith, Love, Hope, and Forgiveness",
        "Prayer and Worship",
        "Bible People and Their Families",
        "Books, Places, and Events in the Bible"
    )

    val allQuestions: List<QuizQuestion> by lazy {
        BibleQuizBankOT.questions + BibleQuizBankNT.questions
    }

    fun getQuestionsCount(): Int = allQuestions.size

    fun getQuestionById(id: String): QuizQuestion? {
        return allQuestions.find { it.id == id || it.questionId == id }
    }

    fun filterQuestions(
        difficulty: String = "MIXED", // "MIXED", "BEGINNER", "INTERMEDIATE", "ADVANCED"
        testament: String = "BOTH", // "BOTH", "OT", "NT"
        category: String = "ALL", // "ALL" or category name
        bookId: String? = null
    ): List<QuizQuestion> {
        return allQuestions.filter { q ->
            val matchDiff = when (difficulty.uppercase()) {
                "BEGINNER", "EASY" -> q.difficulty == QuizDifficulty.BEGINNER
                "INTERMEDIATE", "MEDIUM" -> q.difficulty == QuizDifficulty.INTERMEDIATE
                "ADVANCED", "HARD" -> q.difficulty == QuizDifficulty.ADVANCED
                else -> true
            }

            val matchTestament = when (testament.uppercase()) {
                "OT", "OLD TESTAMENT" -> q.testament == "Old Testament" || q.testament == "Both"
                "NT", "NEW TESTAMENT" -> q.testament == "New Testament" || q.testament == "Both"
                else -> true
            }

            val matchCategory = if (category.uppercase() == "ALL") {
                true
            } else {
                q.category.equals(category, ignoreCase = true)
            }

            val matchBook = if (bookId.isNullOrBlank() || bookId.uppercase() == "ALL") {
                true
            } else {
                q.bookId.equals(bookId, ignoreCase = true)
            }

            matchDiff && matchTestament && matchCategory && matchBook && q.activeStatus
        }
    }
}
