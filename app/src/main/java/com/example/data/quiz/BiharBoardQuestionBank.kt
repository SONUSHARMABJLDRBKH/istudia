package com.example.data.quiz

import com.example.data.model.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

object BiharBoardQuestionBank {
    const val TARGET_QUESTIONS_PER_SUBJECT = 1000

    val allSubjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Hindi", "English")

    fun subjectsForClass(selectedClass: Int): List<String> = when (selectedClass) {
        9, 10 -> listOf("Mathematics", "Physics", "Chemistry", "Biology", "Hindi", "English")
        else -> listOf("Mathematics", "Physics", "Chemistry", "Biology", "Hindi", "English")
    }

    /**
     * Parses a verified Bihar Board question dataset JSON array imported by an Admin.
     * Supports bulk import up to 1,000+ questions per subject without fabricating exam years.
     */
    fun parseImportedJson(jsonText: String, adminUid: String): Result<List<QuizQuestion>> {
        return try {
            val array = JSONArray(jsonText.trim())
            val list = mutableListOf<QuizQuestion>()
            for (i in 0 until array.length()) {
                val obj: JSONObject = array.getJSONObject(i)
                val qClass = obj.optInt("questionClass", obj.optInt("class", 10)).coerceIn(9, 12)
                val subject = obj.optString("subject", "Mathematics").trim()
                val chapter = obj.optString("chapter", "General").trim()
                val questionText = obj.optString("questionText", obj.optString("question", "")).trim()
                val optionA = obj.optString("optionA", "").trim()
                val optionB = obj.optString("optionB", "").trim()
                val optionC = obj.optString("optionC", "").trim()
                val optionD = obj.optString("optionD", "").trim()
                val correct = obj.optString("correctOption", "A").trim().uppercase().take(1)
                val explanation = obj.optString("explanation", "Bihar Board syllabus concept.").trim()
                val source = obj.optString("source", "Bihar Board Verified Import").trim()
                val examYear = obj.optString("examYear", "").trim()
                val customId = obj.optString("id", "bseb_imp_${System.currentTimeMillis()}_$i")
                if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank() &&
                    optionC.isNotBlank() && optionD.isNotBlank() && correct in listOf("A", "B", "C", "D")
                ) {
                    list.add(
                        QuizQuestion(
                            id = customId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_"),
                            questionClass = qClass,
                            subject = subject,
                            chapter = chapter,
                            questionText = questionText,
                            optionA = optionA,
                            optionB = optionB,
                            optionC = optionC,
                            optionD = optionD,
                            correctOption = correct,
                            explanation = explanation,
                            source = source,
                            examYear = examYear,
                            createdBy = adminUid
                        )
                    )
                }
            }
            if (list.isEmpty()) Result.failure(IllegalArgumentException("No valid questions found in JSON"))
            else Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete Bihar Board (BSEB) VVI Objective Question Bank across Classes 9, 10, 11, and 12
     * and all 6 subjects (Mathematics, Physics, Chemistry, Biology, Hindi, English) —
     * 242 distinct verified VVI questions built-in, plus Admin JSON dataset import up to 1,000+ per subject.
     */
    fun starterQuestions(): List<QuizQuestion> = buildList {
        addAll(BsebMathVviBank.questions)
        addAll(BsebPhysicsVviBank.questions)
        addAll(BsebChemVviBank.questions)
        addAll(BsebBioVviBank.questions)
        addAll(BsebHindiVviBank.questions)
        addAll(BsebEnglishVviBank.questions)
    }
}
