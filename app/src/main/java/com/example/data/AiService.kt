package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrieve API settings from SharedPreferences
    private fun getApiSettings(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences("ikun_prefs", Context.MODE_PRIVATE)
        
        // 1. Base URL
        var baseUrl = prefs.getString("base_url", "https://generativelanguage.googleapis.com/") ?: ""
        if (baseUrl.isEmpty()) {
            baseUrl = "https://generativelanguage.googleapis.com/"
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }

        // 2. Model Name
        var modelName = prefs.getString("model_name", "gemini-3.5-flash") ?: ""
        if (modelName.isEmpty()) {
            modelName = "gemini-3.5-flash"
        }

        // 3. API Key (Fallback to BuildConfig if empty/null)
        var apiKey = prefs.getString("api_key", "") ?: ""
        if (apiKey.isEmpty()) {
            apiKey = BuildConfig.GEMINI_API_KEY
        }

        return Triple(apiKey, baseUrl, modelName)
    }

    /**
     * Call the Gemini API to get an AI-generated explanation
     */
    suspend fun fetchExplanation(
        context: Context,
        categoryName: String,
        question: Question,
        userAnswer: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (apiKey, baseUrl, modelName) = getApiSettings(context)
            
            if (apiKey.isEmpty()) {
                return@withContext Result.failure(Exception("AI 接口未配置！请前往 [设置] 页面填写您的 Gemini API Key 或检查默认密钥。"))
            }

            // Construct Prompt
            val optionsStr = question.options.joinToString("\n")
            val feedbackPrompt = if (userAnswer != null) {
                "用户的答题反馈：用户选择了解答 [$userAnswer], 回答错误。"
            } else {
                "用户想了解这道题。请详细拆解这道题目。"
            }

            val prompt = """
                你是一位风趣、幽默、热心又傲娇的名师 AI，目前在"ikun刷题"应用中指导用户。
                
                请对这道题进行一键精要解析：
                【分类】：$categoryName
                【题型】：${when(question.type) {
                    "SINGLE" -> "单选题"
                    "MULTI" -> "多选题"
                    else -> "判断题"
                }}
                【题目内容】：${question.content}
                【备选项/判断】：
                $optionsStr
                【正确答案】：${question.correctAnswer}
                $feedbackPrompt

                请按如下部分逐步解答（多用中文中分中括号醒目标识）：
                1. 【名师破题】：为什么正确答案是 ${question.correctAnswer}？精简透彻讲解这一核心考点。
                2. 【避坑指路】：如果用户做错了，可能有哪些认知偏差？避开哪些坑？
                3. 【Kuner专属趣味碎碎念】：结合练习生、两年半、背带裤、篮球、唱跳、Rap、鸡你太美、露出鸡脚等ikun梗来进行一两句趣味励志解说，调节刷题氛围。
                
                字数控制在 150-400字 之间，排版精美整洁，充满爱与正能量。
            """.trimIndent()

            // Construct JSON request manually to avoid serialization issues
            // structure: { "contents": [{ "parts": [{ "text": "..." }] }] }
            val requestBodyJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // Dynamic Endpoint: e.g. https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=YOUR_KEY
            val url = "${baseUrl}v1beta/models/$modelName:generateContent?key=$apiKey"

            val okRequest = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(okRequest).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                return@withContext Result.failure(Exception("API请求失败 (${response.code})。错误详情: $errorBody"))
            }

            val responseBody = response.body?.string() ?: ""
            if (responseBody.isEmpty()) {
                return@withContext Result.failure(Exception("收到空响应"))
            }

            // Parse response json
            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("无法获取解析候选（可能有安全过滤或API密钥限制）"))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val resultText = parts?.getJSONObject(0)?.optString("text")

            if (resultText != null) {
                Result.success(resultText)
            } else {
                Result.failure(Exception("解析响应失败：无法提取解析文本。"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
