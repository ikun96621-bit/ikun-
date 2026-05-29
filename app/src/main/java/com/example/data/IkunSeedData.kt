package com.example.data

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

    val questions = listOf(
        // Category 1
        Question(
            id = 1,
            categoryId = 1,
            type = "SINGLE",
            content = "经典梗中，练习生蔡徐坤的练习时长是多少？",
            options = listOf("A. 两年半 (2年6个月)", "B. 两年整", "C. 三年半", "D. 一年零八个月"),
            correctAnswer = "A",
            explanation = "‘大家口口相传：我是练习时长两年半的个人练习生蔡徐坤，喜欢唱、跳、rap、篮球。’"
        ),
        Question(
            id = 2,
            categoryId = 1,
            type = "MULTI",
            content = "以下哪些属于网络狂欢中 'ikun' 的经典流行元素？（多选）",
            options = listOf("A. 鸡你太美", "B. 灰色中分头", "C. 吊带裤配中筒袜", "D. 唱、跳、rap、篮球"),
            correctAnswer = "A,B,C,D",
            explanation = "这些在视频剪辑和二创中被广泛讨论和娱乐，共同构成了标志性的ikun流行语体系。"
        ),
        Question(
            id = 3,
            categoryId = 1,
            type = "TF",
            content = "在那个经典的篮球秀舞蹈中，背景音乐《只因你太美》由于空耳常被调侃称为《鸡你太美》。",
            options = listOf("A. 正确", "B. 错误"),
            correctAnswer = "A",
            explanation = "《只因你太美》原唱是SWIN-S，歌词中‘只因你太美’唱得比较快而带有独特的律动音，因此被广大网友空耳演绎为‘鸡你太美’。"
        ),
        Question(
            id = 4,
            categoryId = 1,
            type = "SINGLE",
            content = "网络流行语 '真爆烈粉' / '真爱粉' 或 '小黑子'，其中的 '露露出黑脚儿' / '露出鸡脚' 常暗示什么？",
            options = listOf("A. 伪装成真爱粉的卧底露出了蛛丝马迹", "B. 练习篮球时不小心摔倒", "C. 纯粹的赞美词汇", "D. 表示对方歌唱水平极高"),
            correctAnswer = "A",
            explanation = "这是网络语境中的反串玩法，用来戏谑调侃自己伪装真爱粉的行为。"
        ),

        // Category 2
        Question(
            id = 5,
            categoryId = 2,
            type = "SINGLE",
            content = "在 Kotlin 语言中，推荐哪种方式声明一个‘一旦初始化后就不能被重新赋值’的只读局部变量？",
            options = listOf("A. var", "B. const val", "C. val", "D. let"),
            correctAnswer = "C",
            explanation = "val 用于声明局部或属性级别的只读变量；const val 用于编译期常量；var 用于可变变量。"
        ),
        Question(
            id = 6,
            categoryId = 2,
            type = "MULTI",
            content = "以下哪些属于 Jetpack Compose 特性所提倡的现代声明式 UI 的优势？（多选）",
            options = listOf("A. 状态向下一路单向传递、事件向上传递 (UDF)", "B. 相比传统 XML 大幅减少了模板代码", "C. 天然易于适配折叠屏与各种屏幕尺寸", "D. 必须先通过 findViewById 获取节点后才能更新其内容"),
            correctAnswer = "A,B,C",
            explanation = "Compose 摒弃了 findViewById 这种手动操作 DOM/视图树的陈旧机制，改由视图在状态（State）改变时自动进行重组（Recomposition）。"
        ),
        Question(
            id = 7,
            categoryId = 2,
            type = "TF",
            content = "在 Android 中，Room 内部默认会自动使用异步线程来调用 Kotlin 协程的 suspend 函数，即使我们直接在主线程中启动该 suspend 挂起函数也不会造成主线程 ANR 阻塞。",
            options = listOf("A. 正确", "B. 错误"),
            correctAnswer = "A",
            explanation = "是的，Room 对协程有内置的原生支持。所有使用了 `suspend` 关键字的 DAO 操作在执行的时候，都会自动派发到 Room 自己管理的后台线程池上运行。"
        ),
        Question(
            id = 8,
            categoryId = 2,
            type = "SINGLE",
            content = "为了监听 Jetpack Compose 中数据的并发读取和状态的生命周期安全观测，应当推荐使用哪一类方法？",
            options = listOf("A. collectAsStateWithLifecycle()", "B. observeAsState()", "C. Flow.collect()", "D. runBlocking { }"),
            correctAnswer = "A",
            explanation = "在 Compose 中推荐使用 `collectAsStateWithLifecycle()` 去安全订阅 Flow 数据流，它能确保当 Activity 处于后台不可见状态时自动停止监听，从而极大节省硬件能耗开销。"
        ),

        // Category 3
        Question(
            id = 9,
            categoryId = 3,
            type = "SINGLE",
            content = "大语言模型（LLM）取得突破性飞跃，全由于哪一篇著名的论文提出了 Transformer 机制？",
            options = listOf("A. Attention Is All You Need", "B. Deep Residual Learning for Image Recognition", "C. ImageNet Classification with Deep CNN", "D. Generative Adversarial Nets"),
            correctAnswer = "A",
            explanation = "Google 在 2017 年发表的《Attention Is All You Need》论文中首次引入了自注意力机制（Self-Attention）的 Transformer 架构，颠覆了自然语言处理领域。"
        ),
        Question(
            id = 10,
            categoryId = 3,
            type = "MULTI",
            content = "以下哪些属于 Prompt Engineering (提示词工程) 中推荐的高频进阶策略？（多选）",
            options = listOf("A. 融入 Few-Shot (少样本提供示例)", "B. 逻辑复杂的任务引导说：'Let's think step by step' (思维链)", "C. 限定输出格式、长度甚至使用 JSON 输出", "D. 完全不做指示，让模型全屏自己随意猜测"),
            correctAnswer = "A,B,C",
            explanation = "Few-Shot、思维链 (Chain of Thought)、限定结构化的 JSON 均能极大程度消除模型幻觉，使模型输出更为契合真实场景。"
        ),
        Question(
            id = 11,
            categoryId = 3,
            type = "TF",
            content = "在 API 应用中，直接在客户端代码中硬编码敏感的 API Key 是不安全的，因为 APK 极容易被逆向解包工具还原出原始 key，应当使用 BuildConfig 或安全的云端服务器中转代理。",
            options = listOf("A. 正确", "B. 错误"),
            correctAnswer = "A",
            explanation = "是的，硬编码 API 密钥是移动开发大忌，极易被反编译窃取。应当采用环境变量、混淆库，或者最安全的云端代发结构。"
        )
    )
}
