package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini API with the given prompt.
     * Returns the raw text response or throws an exception.
     */
    suspend fun generateText(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is empty or placeholder. Using fallback.")
            throw IllegalStateException("API_KEY_MISSING")
        }

        val url = "$BASE_URL?key=$apiKey"
        
        // Construct standard Gemini request structure
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Add generation configuration to request JSON
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed with code ${response.code}: $bodyString")
                throw Exception("API_ERROR_${response.code}")
            }

            try {
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.getJSONArray("candidates")
                val parts = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Gemini response: ${e.message}", e)
                throw Exception("PARSE_ERROR")
            }
        }
    }

    /**
     * Safe entrypoint for generating PPT content.
     */
    suspend fun generatePPT(topic: String, stage: String, subject: String): String {
        val prompt = """
            قم بإنشاء خطة عرض بوربوينت احترافية لدرس عن '$topic' لمادة '$subject' للمرحلة '$stage' في التعليم السعودي.
            يجب أن ترجع النتيجة فقط في صيغة ملف JSON صالح يحتوي على مصفوفة باسم 'slides' تحتوي على 6 شرائح كحد أدنى.
            كل عنصر في مصفوفة 'slides' يجب أن يحتوي بالضبط على المفاتيح التالية:
            - 'title': عنوان الشريحة باللغة العربية
            - 'content': قائمة من 3 إلى 5 نقاط رئيسية تشرح الموضوع باختصار ووضوح
            - 'notes': ملاحظات توجيهية للمعلم لشرح هذه الشريحة بالفصل بصوت مسموع
            أرجع فقط كود الـ JSON دون أي نصوص برمجية أخرى أو علامات Markdown مثل ```json.
        """.trimIndent()

        return try {
            generateText(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "PPT generation failed, invoking local Arabic generator", e)
            generateFallbackPPT(topic, stage, subject)
        }
    }

    /**
     * Safe entrypoint for generating Quiz content.
     */
    suspend fun generateQuiz(topic: String, stage: String, subject: String, count: Int = 5): String {
        val prompt = """
            قم بإنشاء اختبار شامل وتفاعلي لدرس عن '$topic' لمادة '$subject' للمرحلة '$stage' في التعليم السعودي يحتوي على $count أسئلة اختيار من متعدد.
            يجب أن ترجع النتيجة فقط في صيغة ملف JSON صالح يحتوي على مصفوفة باسم 'questions'.
            كل عنصر في مصفوفة 'questions' يجب أن يحتوي بالضبط على المفاتيح التالية:
            - 'question': نص السؤال باللغة العربية
            - 'options': مصفوفة تحتوي على 4 خيارات اختيار من متعدد باللغة العربية
            - 'correct': رقم الخيار الصحيح (عدد صحيح من 0 إلى 3 يمثل الفهرس في مصفوفة options)
            - 'explanation': شرح مفصل لسبب صحة هذا الخيار وتفسير مبسط للطلاب
            أرجع فقط كود الـ JSON دون أي نصوص برمجية أخرى أو علامات Markdown مثل ```json.
        """.trimIndent()

        return try {
            generateText(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Quiz generation failed, invoking local Arabic generator", e)
            generateFallbackQuiz(topic, stage, subject, count)
        }
    }

    /**
     * Safe entrypoint for generating Worksheets.
     */
    suspend fun generateWorksheet(topic: String, stage: String, subject: String): String {
        val prompt = """
            صمم ورقة عمل دراسية شاملة عن موضوع '$topic' لمادة '$subject' للمرحلة '$stage' في المملكة العربية السعودية.
            يجب أن ترجع النتيجة فقط في صيغة ملف JSON صالح يحتوي على الكائنات التالية:
            - 'title': عنوان ورقة العمل باللغة العربية
            - 'instructions': إرشادات واضحة للطلاب قبل البدء بالحل
            - 'tasks': مصفوفة من الأسئلة والتمارين العملية المتنوعة (أكمل الفراغ، صح أو خطأ، أسئلة مقالية قصيرة). كل عنصر يحتوي على 'id' و 'type' و 'question' (نص السؤال) و 'grade' (الدرجة المستحقة).
            - 'answer_key': نموذج الإجابة النموذجي لكل سؤال لمساعدة المعلم في التصحيح السريع.
            أرجع فقط كود الـ JSON دون أي نصوص برمجية أخرى أو علامات Markdown مثل ```json.
        """.trimIndent()

        return try {
            generateText(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Worksheet generation failed, invoking local Arabic generator", e)
            generateFallbackWorksheet(topic, stage, subject)
        }
    }

    /**
     * Safe entrypoint for generating Remedial or Enrichment Plans.
     */
    suspend fun generatePlan(topic: String, stage: String, subject: String, isRemedial: Boolean): String {
        val planName = if (isRemedial) "خطة علاجية لدعم الطلاب ذوي الأداء الضعيف وتجاوز الصعوبات" else "خطة إثرائية لتطوير مهارات الطلاب المتفوقين وتوسيع مداركهم"
        val prompt = """
            صمم '$planName' لدرس عن '$topic' لمادة '$subject' للمرحلة '$stage' في المملكة العربية السعودية.
            يجب أن ترجع النتيجة فقط في صيغة ملف JSON صالح يحتوي على الهيكل التالي:
            - 'title': عنوان الخطة التعليمية باللغة العربية
            - 'target_group': الفئة المستهدفة للطلاب باللغة العربية
            - 'objectives': قائمة من 3 أهداف تعليمية مخصصة ومصاغة بذكاء
            - 'activities': مصفوفة تحتوي على 3 أنشطة أو أوراق تدريب عملية مفصلة لتنفيذ الخطة. كل نشاط يحتوي على 'title' و 'description' و 'duration' (المدة الزمنية بالدقائق).
            - 'evaluation_method': طريقة قياس الأثر وتقييم مدى نجاح الخطة
            أرجع فقط كود الـ JSON دون أي نصوص برمجية أخرى أو علامات Markdown مثل ```json.
        """.trimIndent()

        return try {
            generateText(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Plan generation failed, invoking local Arabic generator", e)
            generateFallbackPlan(topic, stage, subject, isRemedial)
        }
    }

    /**
     * Safe entrypoint for Student Activity Plans.
     */
    suspend fun generateActivityPlan(topic: String, stage: String, subject: String): String {
        val prompt = """
            قم بإعداد خطة نشاط لا صفي/طلابي منظم وممتع عن موضوع '$topic' لمادة '$subject' للمرحلة '$stage' في المدارس السعودية.
            يجب أن ترجع النتيجة فقط في صيغة ملف JSON صالح يحتوي على الهيكل التالي:
            - 'title': عنوان النشاط الطلابي باللغة العربية
            - 'description': نبذة مختصرة عن فكرة النشاط وأهميته التربوية
            - 'objectives': قائمة تحتوي على 3 أهداف تربوية وسلوكية للنشاط
            - 'phases': مصفوفة من مراحل التنفيذ (مثل التخطيط، الانطلاق، الحفل/التقييم)، كل مرحلة بها 'name' و 'tasks' (مهام التنفيذ)
            - 'requirements': المستلزمات والميزانية التقديرية المقترحة لتنفيذ النشاط مجاناً وبأبسط الأدوات
            أرجع فقط كود الـ JSON دون أي نصوص برمجية أخرى أو علامات Markdown مثل ```json.
        """.trimIndent()

        return try {
            generateText(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Activity plan generation failed, invoking local Arabic generator", e)
            generateFallbackActivityPlan(topic, stage, subject)
        }
    }

    // ---------------- LOCAL ARABIC FALLBACK GENERATORS ----------------

    private fun generateFallbackPPT(topic: String, stage: String, subject: String): String {
        val json = JSONObject()
        val slides = JSONArray()

        slides.put(JSONObject().apply {
            put("title", "شريحة 1: المقدمة والتمهيد")
            put("content", JSONArray().apply {
                put("الترحيب بالطلاب في درس اليوم عن: $topic")
                put("مراجعة المفهوم السابق المرتبط بالموضوع")
                put("استعراض أهداف الدرس الأساسية لمادة $subject")
            })
            put("notes", "ابدأ الدرس بابتسامة ترحيبية واعرض العنوان على السبورة الذكية، ثم اطرح سؤالاً تشويقياً عما يعرفه الطلاب سابقاً عن $topic.")
        })

        slides.put(JSONObject().apply {
            put("title", "شريحة 2: المفهوم الأساسي")
            put("content", JSONArray().apply {
                put("تعريف مبسط وشامل لمفهوم: $topic")
                put("أمثلة حية وواقعية من البيئة المحيطة بنا في المملكة")
                put("لماذا ندرس هذا الموضوع وأهميته في حياتنا اليومية")
            })
            put("notes", "استخدم أسلوب العصف الذهني واطلب من الطلاب تقديم أمثلة من حياتهم اليومية، وركز على المفهوم بأسلوب تفاعلي يتناسب مع مرحلة $stage.")
        })

        slides.put(JSONObject().apply {
            put("title", "شريحة 3: التحليل والتفاصيل")
            put("content", JSONArray().apply {
                put("مكونات وعناصر درس $topic بالتفصيل")
                put("الروابط المشتركة والخصائص المميزة لكل عنصر")
                put("فهم الآلية أو الطريقة العلمية لعمل المفهوم")
            })
            put("notes", "اشرح العناصر ببطء ودع الطلاب يقرؤون النقاط المكتوبة على الشريحة بصوت مسموع، واستخدم السبورة أو النماذج لتبسيط الفكرة.")
        })

        slides.put(JSONObject().apply {
            put("title", "شريحة 4: نشاط جماعي وتطبيقي")
            put("content", JSONArray().apply {
                put("توزيع الطلاب إلى مجموعات تعاونية صغيرة")
                put("حل تدريب جماعي سريع لتطبيق المفهوم المشروح")
                put("مناقشة الحلول ومشاركة المخرجات مع الفصل")
            })
            put("notes", "امنح الطلاب 5 دقائق للعمل الجماعي ومناقشة الحل، وتجول بين المجموعات لتقديم الدعم والتشجيع وتذليل أي صعوبات.")
        })

        slides.put(JSONObject().apply {
            put("title", "شريحة 5: التقييم السريع والأسئلة")
            put("content", JSONArray().apply {
                put("سؤال تفاعلي لقياس مدى فهم الدرس")
                put("فتح المجال لاستفسارات وأسئلة الطلاب")
                put("توضيح المفاهيم التي قد تكون غامضة")
            })
            put("notes", "اطرح سؤال الشريحة على الطلاب بشكل فردي واستمع للإجابات لتقييم مدى تحقيق أهداف الدرس ومدى استيعاب الفصل.")
        })

        slides.put(JSONObject().apply {
            put("title", "شريحة 6: الخاتمة والواجب المنزلي")
            put("content", JSONArray().apply {
                put("ملخص سريع لأبرز ما تم تعلمه اليوم")
                put("شكر الطلاب على تفاعلهم المتميز وحسن مشاركتهم")
                put("الواجب المنزلي المطلوب وتحديد الدرس القادم")
            })
            put("notes", "لخص الدرس في دقيقتين، وسجل التكليف المنزلي على نظام نور أو الدفتر، وودع الطلاب بحفاوة.")
        })

        json.put("slides", slides)
        return json.toString()
    }

    private fun generateFallbackQuiz(topic: String, stage: String, subject: String, count: Int): String {
        val json = JSONObject()
        val questions = JSONArray()

        val sampleQuestions = listOf(
            Triple(
                "ما هو المفهوم الأساسي لـ $topic مادة $subject؟",
                listOf("عملية مستمرة ومترابطة", "حدث فردي عابر", "أمر لا علاقة له بالتعليم", "مفهوم افتراضي غير موجود"),
                0
            ),
            Triple(
                "أي مما يلي يمثل مثالاً واقعياً وصحيحاً على $topic في بيئتنا؟",
                listOf("المثال الأول المرتبط مباشرة بالدرس", "مثال غير دقيق وغير علمي", "الظواهر العشوائية فقط", "لا توجد أمثلة في بيئتنا"),
                0
            ),
            Triple(
                "كيف يؤثر فهم $topic على تحسين أدائنا وتفاعلنا؟",
                listOf("من خلال تبسيط المهام وزيادة الفهم والوعي", "من خلال إهمال الواجبات المدرسية", "لا توجد أي فائدة أو تأثير يذكر", "عن طريق تكرار الأخطاء السابقة دون معالجة"),
                0
            ),
            Triple(
                "ما هي الخطوة الأولى والأساسية لتطبيق مفاهيم $topic مادة $subject؟",
                listOf("التحليل والتخطيط الجيد وفهم الأساسيات", "البدء الفوري العشوائي دون إعداد", "تجاهل الشرح والاعتماد على التخمين", "إلغاء الدرس بالكامل والتركيز على مواضيع أخرى"),
                0
            ),
            Triple(
                "لماذا يعتبر موضوع $topic مهماً لطلاب مرحلة $stage تحديداً؟",
                listOf("لأنه يبني المهارات الأساسية التراكمية في هذه المرحلة", "لأنه موضوع اختياري لا يؤثر على المعدل", "لصعوبته الشديدة التي تتطلب الحفظ التلقائي", "لعدم توفر بدائل تعليمية أخرى"),
                0
            )
        )

        val finalCount = if (count <= sampleQuestions.size) count else sampleQuestions.size
        for (i in 0 until finalCount) {
            val q = sampleQuestions[i]
            questions.put(JSONObject().apply {
                put("question", q.first)
                put("options", JSONArray(q.second))
                put("correct", q.third)
                put("explanation", "الخيار الأول هو الصحيح لأن $topic يتطلب تعلماً تدريجياً وتطبيقاً للمفاهيم الأساسية والمهارات التي تم شرحها في هذه المرحلة ($stage).")
            })
        }

        json.put("questions", questions)
        return json.toString()
    }

    private fun generateFallbackWorksheet(topic: String, stage: String, subject: String): String {
        val json = JSONObject()
        json.put("title", "ورقة عمل تفاعلية: $topic")
        json.put("instructions", "عزيزي الطالب/الطالبة في مرحلة $stage، يرجى قراءة الأسئلة بعناية قبل الإجابة والاستعانة بما تم شرحه في الفصل بمادة $subject.")

        val tasks = JSONArray()
        tasks.put(JSONObject().apply {
            put("id", 1)
            put("type", "أكمل الفراغ")
            put("question", "يعتبر ___________ من الركائز الأساسية التي يبنى عليها درس $topic.")
            put("grade", "درجتان")
        })
        tasks.put(JSONObject().apply {
            put("id", 2)
            put("type", "صح أو خطأ")
            put("question", "يؤثر فهم موضوع $topic إيجابياً على التحصيل الدراسي الشامل لطلاب $stage في مادة $subject.")
            put("grade", "درجة واحدة")
        })
        tasks.put(JSONObject().apply {
            put("id", 3)
            put("type", "سؤال مقالي")
            put("question", "اذكر اثنين من التطبيقات العملية لـ $topic في حياتك اليومية كطالب سعودي متميز.")
            put("grade", "3 درجات")
        })
        json.put("tasks", tasks)

        val answers = JSONArray()
        answers.put("السؤال الأول: المفهوم الأساسي أو القاعدة العلمية التي تم شرحها.")
        answers.put("السؤال الثاني: صح (العبارة صحيحة تماماً وتدعمها الدراسات التربوية).")
        answers.put("السؤال الثالث: 1- تحسين مهارات التفكير المنظم، 2- تطبيق المعرفة لحل مشكلات واقعية محيطة بنا.")
        json.put("answer_key", answers)

        return json.toString()
    }

    private fun generateFallbackPlan(topic: String, stage: String, subject: String, isRemedial: Boolean): String {
        val json = JSONObject()
        if (isRemedial) {
            json.put("title", "خطة علاجية مخصصة: $topic")
            json.put("target_group", "الطلاب ذوي التحصيل الدراسي المتعثر في درس $topic")
            json.put("objectives", JSONArray().apply {
                put("تمكين الطالب من التعرف على المفاهيم والأسس البسيطة لـ $topic")
                put("معالجة مواطن الضعف وتكرار الأخطاء الشائعة في مادة $subject")
                put("رفع ثقة الطالب بنفسه من خلال تدرج صعوبة التدريبات")
            })

            val activities = JSONArray()
            activities.put(JSONObject().apply {
                put("title", "النشاط 1: تبسيط المفاهيم")
                put("description", "جلسة شرح فردي مصغر مستعينين بوسائل بصرية ومجسمات تفاعلية لفهم فكرة $topic بصورة مبسطة.")
                put("duration", "15 دقيقة")
            })
            activities.put(JSONObject().apply {
                put("title", "النشاط 2: أوراق التدريب المتدرجة")
                put("description", "حل ورقة عمل سهلة جداً تركز على سؤال واحد مباشر، ثم ورقة متوسطة بمساعدة وتوجيه مستمر من المعلم.")
                put("duration", "20 دقيقة")
            })
            activities.put(JSONObject().apply {
                put("title", "النشاط 3: التقييم الداعم والتغذية الراجعة")
                put("description", "لعبة تفاعلية سريعة لقياس الأثر وإعطاء تشجيع فوري وهدايا رمزية لتعزيز ثقة الطالب وتحفيزه.")
                put("duration", "10 دقيقة")
            })
            json.put("activities", activities)
            json.put("evaluation_method", "مقارنة نتيجة الاختبار التشخيصي البعدي السريع مع نتيجة الاختبار القبلي لتوثيق التحسن والتقدم.")
        } else {
            json.put("title", "خطة إثرائية متميزة: $topic")
            json.put("target_group", "الطلاب المتفوقين والمتميزين في درس $topic لمادة $subject")
            json.put("objectives", JSONArray().apply {
                put("تنمية مهارات التفكير العليا (التحليل والتركيب والابتكار) لدى الطالب")
                put("ربط موضوع الدرس بمشاريع علمية وتطبيقات تكنولوجية متقدمة")
                put("إتاحة الفرصة للتعلم الذاتي وتدريب الأقران في الفصل")
            })

            val activities = JSONArray()
            activities.put(JSONObject().apply {
                put("title", "النشاط 1: البحث والقصة الإبداعية")
                put("description", "تكليف الطالب بالبحث عن تطبيقات حديثة لمفهوم $topic وإعداد عرض تقديمي مصغر لشرحه لزملائه في دقيقتين.")
                put("duration", "15 دقيقة")
            })
            activities.put(JSONObject().apply {
                put("title", "النشاط 2: التفكير خارج الصندوق")
                put("description", "طرح مشكلة معقدة أو لغز علمي يحتاج إلى دمج مفهوم $topic مع معارف أخرى واقتراح حل مبتكر وغير تقليدي.")
                put("duration", "20 دقيقة")
            })
            activities.put(JSONObject().apply {
                put("title", "النشاط 3: تحدي قيادة التعلم")
                put("description", "إسناد دور 'المعلم الصغير' للطالب المتفوق ليقود جزءاً من مراجعة الدرس التعاونية مع مجموعة من زملائه بالفصل.")
                put("duration", "10 دقيقة")
            })
            json.put("activities", activities)
            json.put("evaluation_method", "تقييم جودة المنتج الإبداعي أو العرض التقديمي ومدى توظيف مهارات التفكير العليا والبحث العلمي.")
        }

        return json.toString()
    }

    private fun generateFallbackActivityPlan(topic: String, stage: String, subject: String): String {
        val json = JSONObject()
        json.put("title", "خطة نشاط لا صفي: $topic")
        json.put("description", "نشاط تفاعلي متميز لربط المعرفة النظرية لدرس $topic في مادة $subject بالتطبيقات الحياتية والمشاريع الترفيهية الهادفة لطلاب مرحلة $stage.")

        val objectives = JSONArray().apply {
            put("تعزيز الروح الرياضية والعمل الجماعي التعاوني بين الطلاب")
            put("تطبيق ما تعلمه الطالب نظرياً في قالب ترفيهي وتطبيقي مشوق")
            put("اكتشاف مواهب الطلاب ومهاراتهم القيادية والعملية")
        }
        json.put("objectives", objectives)

        val phases = JSONArray()
        phases.put(JSONObject().apply {
            put("name", "أولاً: مرحلة التخطيط والإعداد")
            put("tasks", "تشكيل لجنة النشاط، تقسيم المهام وتحديد المشرفين وتجهيز الأدوات البسيطة مسبقاً قبل الموعد.")
        })
        phases.put(JSONObject().apply {
            put("name", "ثانياً: مرحلة التنفيذ والفعالية")
            put("tasks", "تجمع الطلاب بملعب المدرسة أو ساحة الأنشطة، إجراء المسابقات والفقرات، وعمل المعرض المصغر.")
        })
        phases.put(JSONObject().apply {
            put("name", "ثالثاً: مرحلة التقييم والتكريم")
            put("tasks", "تسليم الدروع والشهادات للفرق الفائزة، تنظيف المكان بشكل تعاوني، وكتابة التقرير الختامي.")
        })
        json.put("phases", phases)

        json.put("requirements", "لوحات كرتونية ملونة، أدوات رسم وتلوين، جوائز تشجيعية بسيطة جداً، وتنسيق مع إدارة المدرسة لتوفير حصة النشاط.")

        return json.toString()
    }
}
