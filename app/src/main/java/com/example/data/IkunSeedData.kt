package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object IkunSeedData {
    val categories = listOf(
        Category(
            id = 1,
            name = "篮球1V1",
            description = "这里是关于篮球、运动竞技以及练习生的经典趣味内容与常识盘点！",
            iconName = "sports_basketball"
        ),
        Category(
            id = 2,
            name = "多人唱跳",
            description = "多人及纯享舞台上的高能展示，考验你对唱跳名场面、细节知识的熟悉度！",
            iconName = "music_note"
        ),
        Category(
            id = 3,
            name = "rap对错",
            description = "硬核说唱热词、节奏押韵全解析，真金不怕红炉火，快来测测你是不是说唱大师！",
            iconName = "mic"
        )
    )

    private val typeMap = mapOf("单选题" to "SINGLE", "多选题" to "MULTI", "判断题" to "TF")
    private val categoryMap = mapOf("单选题" to 1, "多选题" to 2, "判断题" to 3)

    fun loadQuestionsFromAssets(context: Context): List<Question> {
        val json = context.assets.open("tiku_data.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("questions")
        val result = mutableListOf<Question>()
        for (i in 0 until arr.length()) {
            val q = arr.getJSONObject(i)
            val type = q.getString("type").trim()
            val mappedType = typeMap[type] ?: continue
            val catId = categoryMap[type] ?: continue
            val questionText = q.getString("question")
            val answer = q.getString("answer").trim()

            val options = q.getJSONArray("options")
            val kotlinOptions = if (mappedType == "TF") {
                listOf("A. 正确", "B. 错误")
            } else {
                val list = mutableListOf<String>()
                for (j in 0 until options.length()) {
                    val opt = options.getJSONObject(j)
                    list.add("${opt.getString("no")}. ${opt.optString("text", "")}")
                }
                list
            }

            val correct = when (mappedType) {
                "MULTI" -> answer.toCharArray().joinToString(",") { it.toString() }
                "TF" -> if (answer == "正确") "A" else "B"
                else -> answer
            }

            result.add(
                Question(
                    categoryId = catId,
                    type = mappedType,
                    content = questionText,
                    options = kotlinOptions,
                    correctAnswer = correct,
                    explanation = q.optString("resolve", "")
                )
            )
        }
        return result
    }
}
