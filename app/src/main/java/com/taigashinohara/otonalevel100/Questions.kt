package com.taigashinohara.otonalevel100

enum class QType { QUIZ, CARD }

data class Question(
    val id: String,
    val category: String,
    val type: QType,
    val prompt: String,
    val choices: List<String> = emptyList(),
    val answerIndex: Int = 0,
    /** For CARD: the back side text. For QUIZ: unused. */
    val cardBack: String = "",
    val explanation: String,
    val scene: String,
    val source: String,
)

/**
 * Prototype pool: 20 questions (4 per P0 category), matching
 * docs/サンプル問題_v0.1.md plus 10 more in the same format.
 * The full product targets ~600 P0 questions built from primary sources.
 */
object Questions {
    val all: List<Question> = listOf(
        // ---- 敬語・言葉づかい ----
        Question("keigo1", "敬語", QType.QUIZ, "上司の行動を言うとき、正しいのは？",
            listOf("部長がおっしゃいました", "部長が申されました", "部長がご覧になられました", "部長が参られました"), 0,
            explanation = "「おっしゃる」は「言う」の尊敬語。「申す」は自分の行為に使う謙譲語なので目上には使わない。",
            scene = "会議で上司の発言を引用するとき、ここを間違えると一番目立つ。", source = "文化庁「敬語の指針」"),
        Question("keigo2", "敬語", QType.QUIZ, "目上の人に使うべきではない挨拶は？",
            listOf("お疲れ様です", "ご苦労様です", "お世話になっております", "よろしくお願いいたします"), 1,
            explanation = "「ご苦労様」は目下へのねぎらいの言葉。目上には「お疲れ様」が無難。",
            scene = "取引先へのメール冒頭で使うと、失礼だと受け取られることがある。", source = "文化庁「敬語の指針」"),
        Question("keigo3", "敬語", QType.CARD, "「言う」の謙譲語は？（自分の行為）",
            cardBack = "申し上げる",
            explanation = "目上の人に自分の「言う」を伝えるときは「申し上げる」。相手の「言う」は尊敬語「おっしゃる」。",
            scene = "「部長が申し上げました」と混ぜると、その場で違和感を持たれる。", source = "文化庁「敬語の指針」"),
        Question("keigo4", "敬語", QType.QUIZ, "二重敬語になっているのは？",
            listOf("お帰りになりました", "ご覧になられました", "おっしゃいました", "召し上がりました"), 1,
            explanation = "「ご覧になる」＋「られる」で敬語が二重になっている。正しくは「ご覧になりました」。",
            scene = "丁寧にしようとして重ねるほど、かえって目立つ間違い。", source = "文化庁「敬語の指針」"),
        // ---- マナー・冠婚葬祭 ----
        Question("manner1", "マナー", QType.QUIZ, "結婚式のご祝儀で避けるべき金額は？",
            listOf("1万円", "2万円", "3万円", "5万円"), 1,
            explanation = "2万円は「割れる＝別れる」を連想させるため慶事では避けるのが一般的。包む場合は品物を添えるなどで調整する。",
            scene = "受付で渡す金額は、後からでも長く覚えられる。", source = "一般的な冠婚葬祭マナー"),
        Question("manner2", "マナー", QType.QUIZ, "葬儀で香典袋の表書きとして一般的なのは？",
            listOf("御祝", "御霊前", "お見舞い", "御中元"), 1,
            explanation = "通夜・葬儀では「御霊前」が広く使われる（宗派により「御仏前」を使う場合がある）。",
            scene = "受付で表書きを見られる場面。宗派が分かる場合はそれに合わせる。", source = "一般的な冠婚葬祭マナー"),
        Question("manner3", "マナー", QType.QUIZ, "名刺交換で正しいのは？",
            listOf("相手より高い位置で渡す", "両手で、文字が相手から読める向きで渡す", "座ったまま渡す", "片手でポケットから出して渡す"), 1,
            explanation = "名刺は相手への自己紹介。読める向き・両手が基本で、受け取ったら机の上に丁寧に置く。",
            scene = "初対面の最初の10秒で、扱いの丁寧さが見られている。", source = "一般的なビジネスマナー"),
        Question("manner4", "マナー", QType.CARD, "目上の人の車に同乗するとき、上座はどこ？",
            cardBack = "後部座席の右側（運転席の後ろ）",
            explanation = "車の上座は後部座席・運転席の後ろ。助手席は末席とされ、目下が座るのが一般的。",
            scene = "「どうぞ」と促されて助手席に座ると、気づかれることがある。", source = "一般的なビジネスマナー"),
        // ---- 漢字・日本語 ----
        Question("kanji1", "漢字", QType.QUIZ, "「月極駐車場」の読みは？",
            listOf("げっきょく", "つきぎめ", "げっき", "つききょく"), 1,
            explanation = "「月極」は「つきぎめ（月ごとに決める）」の熟字訓。",
            scene = "不動産の話で「げっきょく」と読むと、その場で訂正されがち。", source = "常用漢字表・辞書類"),
        Question("kanji2", "漢字", QType.QUIZ, "「脆弱」の読みは？",
            listOf("きじゃく", "ぜいじゃく", "さいじゃく", "もろじゃく"), 1,
            explanation = "「脆」は音読みで「ゼイ」。「脆弱性（ぜいじゃくせい）」は仕事でも頻出。",
            scene = "会議の資料読み上げでつまずくと、内容以前に目立つ。", source = "常用漢字表"),
        Question("kanji3", "漢字", QType.CARD, "「早急」の読みは？",
            cardBack = "さっきゅう",
            explanation = "「早急」は「さっきゅう」が本来の読み。「そうきゅう」と読まれることも多いが、読み合わせでは「さっきゅう」が安全。",
            scene = "会議で読み上げた瞬間に、静かに気づかれるタイプの間違い。", source = "辞書類"),
        Question("kanji4", "漢字", QType.QUIZ, "「重複」の読みとして本来のものは？",
            listOf("じゅうふく", "ちょうふく", "じゅうとく", "ちょうとく"), 1,
            explanation = "「重」は「チョウ」とも読み、「重複」は「ちょうふく」が本来の読み。「じゅうふく」も広く使われる慣用読み。",
            scene = "アナウンスや読み合わせの場面で差が出る。", source = "辞書類"),
        // ---- 道路標識・交通 ----
        Question("sign1", "標識・交通", QType.QUIZ, "青い円に白い矢印が描かれた標識の意味は？",
            listOf("一方通行", "指定方向以外進行禁止", "車両通行止め", "左折可"), 1,
            explanation = "矢印の方向以外へは進めないことを示す規制標識。",
            scene = "矢印と逆方向に進むと違反になるだけでなく危険。", source = "警察庁 道路標識"),
        Question("sign2", "標識・交通", QType.QUIZ, "「駐車」と「停車」の違いで正しいのは？",
            listOf("停車は5分以内なら何をしてもよい", "停車は人の乗り降りなど、すぐに運転できる状態での短時間の停止", "駐車禁止の場所では停車も常に禁止", "停車中はエンジンを切らなければならない"), 1,
            explanation = "停車は「直ちに運転できる状態での短時間の停止」。駐車禁止場所でも停車は可能な場合が多い。",
            scene = "「ちょっとだけ」のつもりの駐車が違反になる典型パターン。", source = "道路交通法"),
        Question("sign3", "標識・交通", QType.CARD, "赤い三角（逆三角形）の標識の意味は？",
            cardBack = "止まれ（一時停止）",
            explanation = "逆三角形は一時停止の標識。停止線で止まり、安全を確認してから進む。",
            scene = "見落とすと違反だけでなく、出会い頭の事故につながる。", source = "警察庁 道路標識"),
        Question("sign4", "標識・交通", QType.QUIZ, "黄色いひし形の標識が示すのは？",
            listOf("規制（してはいけないこと）", "警戒（この先の注意）", "案内（目的地の方向）", "指示（してもよいこと）"), 1,
            explanation = "黄色いひし形は警戒標識。「この先に何があるか」を知らせて注意を促す形と色。",
            scene = "形と色だけで種類が分かると、標識を読むスピードが上がる。", source = "警察庁 道路標識"),
        // ---- 社会・お金・手続き ----
        Question("society1", "社会・お金", QType.QUIZ, "「源泉徴収」とは？",
            listOf("給与から税金があらかじめ差し引かれる仕組み", "年末に自分で税金を一括で払う仕組み", "副業収入だけにかかる税金", "住民税の別名"), 0,
            explanation = "会社が給与から所得税を天引きして国に納める仕組み。年末調整はその精算。",
            scene = "給与明細の話題で「手取りとの差」を説明できないと詰まる。", source = "国税庁"),
        Question("society2", "社会・お金", QType.QUIZ, "国民年金（基礎年金）の受給開始年齢は原則？",
            listOf("60歳", "63歳", "65歳", "70歳"), 2,
            explanation = "原則65歳。希望すれば60〜64歳への繰上げ（減額）や66〜75歳への繰下げ（増額）が選べる。",
            scene = "親の年金や自分の将来の話で、土台の数字がずれていると話が噛み合わない。", source = "厚生労働省・日本年金機構"),
        Question("society3", "社会・お金", QType.QUIZ, "衆議院議員の任期は？",
            listOf("3年", "4年", "5年", "6年"), 1,
            explanation = "衆議院は任期4年（解散あり）。参議院は6年で、3年ごとに半数が改選される。",
            scene = "選挙のニュースで「任期・解散」の話についていけるかの土台。", source = "総務省・憲法"),
        Question("society4", "社会・お金", QType.CARD, "「年末調整」とは何をするもの？",
            cardBack = "1年分の所得税の過不足を、会社が年末に精算すること",
            explanation = "毎月の源泉徴収は概算。年末に正確な税額と比べて、払いすぎは戻り、不足は追加で引かれる。",
            scene = "12月の給与が増えて驚く／減って不安になる理由が説明できるようになる。", source = "国税庁"),
    )

    val categories: List<String> = listOf("敬語", "マナー", "漢字", "標識・交通", "社会・お金")
    fun byId(id: String): Question? = all.firstOrNull { it.id == id }
}

/**
 * Shuffles a quiz question's choices (UX review IR-5: the sample data had the
 * correct answer in position 2 for 12 of 15 quizzes). Returns the shuffled
 * choices and the new index of the correct answer.
 */
fun shuffledChoices(q: Question, random: kotlin.random.Random): Pair<List<String>, Int> {
    require(q.choices.size > 1) { "not a quiz question" }
    val order = q.choices.indices.shuffled(random)
    return order.map { q.choices[it] } to order.indexOf(q.answerIndex)
}
