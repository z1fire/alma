package com.z1fire.alma.data.bulletin

import com.z1fire.alma.data.AssignmentType.ESSAY
import com.z1fire.alma.data.AssignmentType.EXAM
import com.z1fire.alma.data.AssignmentType.OTHER
import com.z1fire.alma.data.AssignmentType.PRESENTATION
import com.z1fire.alma.data.AssignmentType.PROBLEM_SET
import com.z1fire.alma.data.AssignmentType.PROJECT
import com.z1fire.alma.data.AssignmentType.QUIZ
import com.z1fire.alma.data.AssignmentType.READING
import com.z1fire.alma.data.ProgramKind
import com.z1fire.alma.data.ResourceType

internal fun languageDepartments(): List<TemplateDept> = listOf(mandarin(), japanese(), thai(), spanish())

private fun mandarin() = department(
    "Mandarin Chinese", "CHIN", 1,
    "Spoken and written Mandarin from first tones to reading real texts, aligned with the HSK levels.",
) {
    course("101", "Elementary Mandarin I", credits = 4) {
        about = "A first course in Modern Standard Mandarin: pinyin and the four tones, survival conversation, and your first 150 characters. Daily speaking practice is the heart of the course."
        objectives(
            "Pronounce all pinyin initials, finals and tones accurately, including tone sandhi",
            "Hold short conversations about yourself, family, dates, time and hobbies",
            "Recognize and handwrite about 150 common characters with correct stroke order",
            "Pass a practice HSK 1 exam",
        )
        units(
            "Pinyin & the four tones — initials, finals, tone marks, 3rd-tone and 不/一 sandhi",
            "Greetings & names — 你好, 请问, 贵姓; basic question words",
            "Family — 有/没有, measure word 个, numbers 1–99",
            "Dates & age — 年月日, days of the week, 几 vs 多少",
            "Hobbies — 喜欢, verb-object phrases, 也 and 都",
            "Visiting friends — 在, 吗/呢 questions, polite expressions",
            "Making appointments — clock time, 要, 有空, 时候",
            "Studying Chinese — 的 for possession, 得 complements preview",
            "Character foundations — radicals, stroke order, building a writing habit",
            "Review & HSK 1 practice",
        )
        textbook("Integrated Chinese, Level 1 Part 1 (4th ed.)", "Yuehua Liu, Tao-chung Yao et al.", notes = "Textbook + workbook; lessons 1–5.")
        textbook("HSK Standard Course 1", "Jiang Liping", required = false, notes = "Official-style prep for the HSK 1 exam.")
        site("Chinese Grammar Wiki", "AllSet Learning", "https://resources.allsetlearning.com/chinese/grammar/", notes = "Look up every grammar point at the A1 level.")
        site("Pleco Chinese Dictionary", "Pleco Software", "https://www.pleco.com/", required = true, notes = "Dictionary + flashcards on your phone.")
        task(OTHER, "Tone drill recording: all four tones on 20 syllables", 2)
        task(QUIZ, "Pinyin dictation self-quiz", 3)
        task(PROJECT, "Character writing log — 150 characters by hand", 12)
        task(PRESENTATION, "Two-minute recorded self-introduction", 8)
        task(EXAM, "HSK 1 practice exam", 15)
    }
    course("102", "Elementary Mandarin II", credits = 4, prereqs = listOf("101")) {
        about = "Continue building everyday Mandarin: shopping, transportation, weather, dining and directions, with a growing vocabulary of about 300 characters."
        objectives(
            "Handle common transactions: shopping, ordering food, asking directions",
            "Use aspect particles 了 and 过 and resultative complements correctly",
            "Read short dialogues and notes written in characters without pinyin",
            "Pass a practice HSK 2 exam",
        )
        units(
            "Shopping — prices, colors, 一点儿, comparing with 比",
            "Transportation — 坐/骑/开, 还是 vs 或者, 先…再…",
            "Weather — 会, 比 comparisons, 越来越",
            "Dining — ordering, 了 for completed action, measure words",
            "Asking directions — locations, 离, 往, 一…就…",
            "Birthday party — 过 experience, 又…又…",
            "Seeing a doctor — body parts, 把 construction intro",
            "Dating & relationships — duration of time, 得 complements",
            "Renting an apartment — 除了…以外, potential complements",
            "Review & HSK 2 practice",
        )
        textbook("Integrated Chinese, Level 1 Part 2 (4th ed.)", "Yuehua Liu, Tao-chung Yao et al.")
        textbook("HSK Standard Course 2", "Jiang Liping", required = false)
        read(ResourceType.BOOK, "Mandarin Companion graded readers, Level 1", "Mandarin Companion", required = false, notes = "Easy novels using ~300 characters — start your extensive reading.")
        task(QUIZ, "了 vs 过 grammar self-quiz", 4)
        task(PROJECT, "Role-play: order a full meal in Mandarin (record it)", 6)
        task(READING, "Finish one Level 1 graded reader", 11)
        task(EXAM, "HSK 2 practice exam", 15)
    }
    course("201", "Intermediate Mandarin", credits = 4, prereqs = listOf("102")) {
        about = "Bridge from textbook dialogues to real-world Chinese: longer readings, opinions and narration, complex complements, and roughly 600 characters."
        objectives(
            "Narrate past events and express opinions in connected paragraphs",
            "Use 把 and 被 sentences, directional and potential complements",
            "Read graded readers and short authentic texts with a dictionary",
            "Pass a practice HSK 3 exam",
        )
        units(
            "Campus life — narrating routines, sequence connectors",
            "Choosing courses — 对…感兴趣, 不但…而且…",
            "Living on campus vs off — comparing options, 要是…就…",
            "Food & health — 被 passive, 越…越…",
            "Travel plans — directional complements, 打算",
            "Chinese holidays — culture readings, 一边…一边…",
            "Gender & family — expressing opinions politely",
            "Shopping online — 连…都…, 不管…都…",
            "Writing practice — 200-character diary entries",
            "Review & HSK 3 practice",
        )
        textbook("Integrated Chinese, Level 2 Part 1 (4th ed.)", "Yuehua Liu, Tao-chung Yao et al.")
        textbook("HSK Standard Course 3", "Jiang Liping", required = false)
        read(ResourceType.BOOK, "Mandarin Companion graded readers, Level 2", "Mandarin Companion", required = false)
        read(ResourceType.PODCAST, "Mandarin listening podcast of your choice", "", required = false, notes = "Aim for 20 minutes of listening a day.")
        task(PROJECT, "Weekly 200-character diary (10 entries)", 12)
        task(READING, "Read two graded readers", 13)
        task(PRESENTATION, "5-minute talk on a Chinese holiday", 9)
        task(EXAM, "HSK 3 practice exam", 15)
    }
    course("250", "Reading & Listening Lab", credits = 2, weeks = 10, prereqs = listOf("201")) {
        about = "An immersion lab: extensive reading and listening with graded readers, podcasts and shows, tracked by hours and characters read."
        objectives(
            "Read 50,000 characters of graded or native material",
            "Follow slow native podcasts and subtitled shows",
            "Mine new vocabulary into a spaced-repetition deck",
        )
        units(
            "Setting up — Pleco reader, flashcard deck, tracking sheet",
            "Graded readers I", "Graded readers II", "Podcasts for learners",
            "Chinese TV with Chinese subtitles", "Native short stories",
            "Shadowing practice", "Final reflection & next-level plan",
        )
        read(ResourceType.BOOK, "Mandarin Companion graded readers, Levels 1–2", "Mandarin Companion")
        site("Du Chinese", "Du Chinese", "https://www.duchinese.net/", notes = "Graded lessons with audio.")
        task(PROJECT, "Reading log: 50,000 characters", 10)
        task(OTHER, "Shadow a 5-minute audio clip (record before & after)", 7)
    }
    program("Mandarin Chinese", ProgramKind.CERTIFICATE, "101", "102", "201")
    program("Chinese Language", ProgramKind.MINOR, "101", "102", "201", "250")
}

private fun japanese() = department(
    "Japanese", "JAPN", 6,
    "Kana to kanji, polite speech to casual conversation — a full beginner-to-intermediate sequence built around Genki.",
) {
    course("101", "Elementary Japanese I", credits = 4) {
        about = "Learn hiragana and katakana, the polite です/ます style, and the grammar to introduce yourself, shop, make plans and describe your day."
        objectives(
            "Read and write all hiragana and katakana",
            "Conjugate verbs and adjectives in polite present, past and negative forms",
            "Recognize about 100 kanji",
            "Hold simple polite conversations on everyday topics",
        )
        units(
            "Hiragana — the 46 basic characters, dakuten, small つ/ゃゅょ",
            "Katakana — loanwords, long vowels",
            "Greetings & self-introduction — です, の, question か",
            "Shopping — これ/それ/あれ, numbers, prices",
            "Making plans — verb conjugation, particles を/に/で/へ",
            "The first date — past tense, あります/います",
            "A trip to Okinawa — adjectives, likes and dislikes",
            "A day in Robert's life — て-form, ～てもいいです",
            "Family pictures — ～ている, describing people",
            "Review & JLPT N5 vocabulary",
        )
        textbook("GENKI I: An Integrated Course in Elementary Japanese (3rd ed.)", "Eri Banno et al.", notes = "Lessons 1–6 with the workbook.")
        site("Learn Hiragana: The Ultimate Guide", "Tofugu", "https://www.tofugu.com/japanese/learn-hiragana/", required = true)
        site("Tae Kim's Guide to Learning Japanese", "Tae Kim", "https://guidetojapanese.org/learn/", notes = "Free grammar guide; great second explanation.")
        site("Anki", "Damien Elmes", "https://apps.ankiweb.net/", notes = "Spaced-repetition flashcards for kana and vocabulary.")
        task(QUIZ, "Hiragana chart from memory", 2)
        task(QUIZ, "Katakana chart from memory", 3)
        task(PRESENTATION, "Recorded self-introduction (自己紹介)", 5)
        task(EXAM, "Genki I lessons 1–6 review test", 15)
    }
    course("102", "Elementary Japanese II", credits = 4, prereqs = listOf("101")) {
        about = "Finish the elementary foundation: plain/short forms, casual speech, giving reasons, comparisons and wishes — and prepare for JLPT N5."
        objectives(
            "Switch between polite and casual (short-form) speech",
            "Explain reasons, make comparisons, and talk about wants and plans",
            "Recognize about 300 kanji",
            "Pass a practice JLPT N5 exam",
        )
        units(
            "Barbecue — short forms, casual speech",
            "Kabuki — past short forms, qualifying nouns with verbs",
            "Winter vacation plans — comparisons, ～つもり",
            "After the vacation — ～たい, ～たり～たりする",
            "Feeling ill — ～んです, ～すぎる, ～ほうがいい",
            "Kanji sprint — 150 new characters",
            "Listening practice — slow podcasts and anime with subtitles",
            "JLPT N5 practice",
        )
        textbook("GENKI I (3rd ed.), lessons 7–12", "Eri Banno et al.")
        site("JLPT official site — sample questions", "Japan Foundation", "https://www.jlpt.jp/e/samples/forlearners.html")
        task(PROJECT, "Write a 300-character diary entry in casual Japanese", 6)
        task(QUIZ, "Kanji self-test (150 characters)", 10)
        task(EXAM, "JLPT N5 practice exam", 15)
    }
    course("201", "Intermediate Japanese", credits = 4, prereqs = listOf("102")) {
        about = "Genki II: potential and volitional forms, giving and receiving, honorifics, and reading longer passages on the way to JLPT N4."
        objectives(
            "Use potential, volitional, passive and causative forms",
            "Use keigo (honorific and humble speech) in common situations",
            "Read short essays and stories with kanji",
            "Pass a practice JLPT N4 exam",
        )
        units(
            "Part-time jobs — potential verbs, ～し",
            "Shopping & giving gifts — あげる/くれる/もらう",
            "Travel — volitional form, ～ておく",
            "Lost & found — transitivity pairs, ～てしまう",
            "Honorific & humble speech — 尊敬語 and 謙譲語",
            "Thank-you letters — reading and writing formally",
            "Passive & causative — complaints and favors",
            "JLPT N4 practice",
        )
        textbook("GENKI II: An Integrated Course in Elementary Japanese (3rd ed.)", "Eri Banno et al.")
        book("Remembering the Kanji, Vol. 1", "James W. Heisig", required = false, notes = "Mnemonic approach to 2,200 kanji meanings.")
        task(PROJECT, "Write a formal thank-you letter (お礼の手紙)", 7)
        task(READING, "Read three NHK News Web Easy articles a week", 12)
        task(EXAM, "JLPT N4 practice exam", 15)
    }
    course("210", "Kanji Foundations", credits = 2, weeks = 12, prereqs = listOf("101")) {
        about = "A focused kanji course: radicals, mnemonics and spaced repetition to learn the 500 most common kanji and their key vocabulary."
        objectives(
            "Identify common radicals and use them to remember kanji",
            "Learn 500 kanji meanings and common readings",
            "Keep a daily spaced-repetition review habit",
        )
        units(
            "How kanji work — radicals, on'yomi vs kun'yomi",
            "Kanji 1–100", "Kanji 101–200", "Kanji 201–300", "Kanji 301–400", "Kanji 401–500",
            "Vocabulary from kanji compounds", "Final review",
        )
        site("WaniKani", "Tofugu", "https://www.wanikani.com/", notes = "Radical → kanji → vocabulary SRS.")
        book("Remembering the Kanji, Vol. 1", "James W. Heisig", required = false)
        task(QUIZ, "Monthly kanji self-test", 4)
        task(QUIZ, "Monthly kanji self-test", 8)
        task(EXAM, "500-kanji final", 12)
    }
    program("Japanese Language", ProgramKind.CERTIFICATE, "101", "102", "201")
}

private fun thai() = department(
    "Thai", "THAI", 3,
    "Thai script, tones and everyday conversation for travel, friendships and living in Thailand.",
) {
    course("101", "Elementary Thai I", credits = 4) {
        about = "Crack the Thai writing system and the five tones while learning survival conversation: greetings, food, numbers, directions and polite particles."
        objectives(
            "Read the 44 consonants by class and the main vowel forms",
            "Apply the tone rules to read unfamiliar words aloud",
            "Use polite particles ครับ/ค่ะ and everyday phrases naturally",
            "Order food, bargain, take taxis and ask directions",
        )
        units(
            "Sounds of Thai — five tones, vowel length, aspirated consonants",
            "Consonants I — middle class",
            "Consonants II — high and low class",
            "Vowels — before, after, above and below the consonant",
            "Tone rules — consonant class × live/dead syllables × tone marks",
            "Greetings & politeness — ครับ/ค่ะ, wai, names and nicknames",
            "Numbers, money & time — the Thai six-hour clock",
            "Food & markets — ordering, spicy levels, bargaining",
            "Getting around — taxis, tuk-tuks, directions",
            "Review — reading signs and menus",
        )
        book("Thai for Beginners", "Benjawan Poomsan Becker", notes = "Paiboon Publishing; script, tones and dialogues with audio.")
        site("thai-language.com", "Thai Language", "http://www.thai-language.com/", notes = "Dictionary with tone-marked transliteration and audio.")
        task(QUIZ, "Write all 44 consonants by class from memory", 5)
        task(PROBLEM_SET, "Tone-rule worksheet: 50 words", 6)
        task(PROJECT, "Read and translate 10 real menu items or signs", 9)
        task(EXAM, "Script & survival-phrases final", 15)
    }
    course("102", "Elementary Thai II", credits = 4, prereqs = listOf("101")) {
        about = "Build sentences and fluency: verbs of motion, classifiers, time expressions, feelings, and reading short texts."
        objectives(
            "Use classifiers, time markers and serial verbs correctly",
            "Talk about past, present and future activities",
            "Read short paragraphs aloud with correct tones",
        )
        units(
            "Classifiers — ตัว, คน, อัน and counting things",
            "Time & aspect — แล้ว, กำลัง, จะ",
            "Serial verbs & directions — ไป, มา, ขึ้น, ลง",
            "Feelings & opinions — คิดว่า, รู้สึก",
            "Daily routines", "Travel in Thailand",
            "Reading short stories", "Conversation review",
        )
        book("Thai for Beginners", "Benjawan Poomsan Becker", notes = "Finish the book.")
        book("Thai Reference Grammar", "James Higbie & Snea Thinsan", required = false, notes = "Excellent explanations with examples.")
        task(PROJECT, "Record a 3-minute conversation with a tutor or language partner", 8)
        task(READING, "Read a children's story aloud (record it)", 12)
        task(EXAM, "Elementary Thai final", 15)
    }
    course("201", "Intermediate Thai", credits = 4, prereqs = listOf("102")) {
        about = "Move toward real-world Thai: conversational speed, register and slang, and reading news headlines and short articles."
        objectives(
            "Follow natural-speed conversation on familiar topics",
            "Distinguish formal and informal registers and common slang",
            "Read headlines and short articles with a dictionary",
        )
        units(
            "Natural speech — dropped pronouns, sentence-final particles",
            "Formal vs informal Thai", "Health & body", "Work & business",
            "Thai culture & festivals", "Reading the news", "Thai music & film", "Final project",
        )
        book("Thai for Intermediate Learners", "Benjawan Poomsan Becker")
        book("Thai Reference Grammar", "James Higbie & Snea Thinsan", required = false)
        task(READING, "Summarize 5 Thai news headlines a week", 10)
        task(PRESENTATION, "10-minute talk in Thai on a Thai festival", 14)
    }
    program("Thai Language", ProgramKind.CERTIFICATE, "101", "102", "201")
}

private fun spanish() = department(
    "Spanish", "SPAN", 7,
    "From first words to fluent conversation, with comprehensible input, solid grammar and the cultures of the Spanish-speaking world.",
) {
    course("101", "Elementary Spanish I", credits = 4) {
        about = "Start speaking from day one: pronunciation, the present tense, ser vs estar, and everyday topics — backed by lots of comprehensible input."
        objectives(
            "Pronounce Spanish clearly and spell what you hear",
            "Use the present tense of regular, stem-changing and key irregular verbs",
            "Choose correctly between ser and estar",
            "Talk about yourself, family, routine, food and plans",
        )
        units(
            "Sounds & greetings — vowels, rolled r, accents",
            "Identity — ser, nationalities, gender & number",
            "Family & description — tener, possessives, adjectives",
            "Daily routine — reflexive verbs",
            "Place & state — estar, location, feelings",
            "Food & ordering — gustar, querer, poder",
            "Free time — ir a + infinitive, weather",
            "Stem-changing verbs — e→ie, o→ue, e→i",
            "Shopping & numbers to 1,000",
            "Review & oral interview",
        )
        online("Complete Spanish", "Language Transfer", "https://www.languagetransfer.org/", required = true, notes = "Free audio course — think your way into Spanish.")
        book("Madrigal's Magic Key to Spanish", "Margarita Madrigal", required = false)
        site("Dreaming Spanish", "Dreaming Spanish", "https://www.dreamingspanish.com/", required = true, notes = "Comprehensible-input videos sorted by level. Log your hours.")
        site("SpanishDict", "SpanishDict", "https://www.spanishdict.com/", notes = "Dictionary, conjugation tables and grammar lessons.")
        task(OTHER, "Log 30 hours of Dreaming Spanish input", 15)
        task(QUIZ, "Ser vs estar self-quiz", 6)
        task(PRESENTATION, "Recorded 2-minute 'my daily routine'", 8)
        task(EXAM, "Oral interview (record with a tutor or partner)", 15)
    }
    course("102", "Elementary Spanish II", credits = 4, prereqs = listOf("101")) {
        about = "Talk about the past and give advice: preterite vs imperfect, object pronouns, commands, and longer conversations."
        objectives(
            "Narrate in the past choosing preterite vs imperfect",
            "Use direct and indirect object pronouns together",
            "Give commands and advice",
        )
        units(
            "Preterite — regular and irregular",
            "Imperfect — descriptions and habits",
            "Preterite vs imperfect — storytelling",
            "Object pronouns — lo, le, se lo",
            "Commands — tú and usted",
            "Travel & directions", "Health & the body", "Review & storytelling project",
        )
        book("Practice Makes Perfect: Complete Spanish Grammar", "Gilda Nissenberg")
        book("Short Stories in Spanish for Beginners", "Olly Richards", required = false)
        site("Destinos", "Annenberg Learner", "https://www.learner.org/series/destinos-an-introduction-to-spanish/", notes = "Classic 52-episode telenovela course.")
        task(PROJECT, "Write and record a 3-minute childhood story", 9)
        task(OTHER, "Log 50 more hours of input", 15)
        task(EXAM, "Past tenses final", 15)
    }
    course("201", "Intermediate Spanish", credits = 4, prereqs = listOf("102")) {
        about = "The subjunctive and beyond: express wishes, doubts and emotions, hypotheticals with the conditional, and read authentic texts."
        objectives(
            "Use the present and imperfect subjunctive in common triggers",
            "Form hypotheticals with si-clauses and the conditional",
            "Read short stories and news articles",
            "Hold 20-minute conversations on familiar topics",
        )
        units(
            "Present subjunctive — wishes and requests",
            "Subjunctive of emotion and doubt",
            "Subjunctive in adjective and adverb clauses",
            "Future & conditional",
            "Imperfect subjunctive & si-clauses",
            "Reading Latin American short stories",
            "News & current events",
            "Conversation marathon",
        )
        book("Practice Makes Perfect: Complete Spanish Grammar", "Gilda Nissenberg")
        book("A New Reference Grammar of Modern Spanish", "John Butt & Carmen Benjamin", required = false)
        site("Dreaming Spanish — intermediate videos", "Dreaming Spanish", "https://www.dreamingspanish.com/", required = true)
        task(QUIZ, "Subjunctive triggers self-quiz", 5)
        task(ESSAY, "500-word essay: '¿Qué harías si…?'", 11)
        task(EXAM, "Recorded 20-minute conversation", 15)
    }
    course("301", "Spanish Conversation & Culture", credits = 3, prereqs = listOf("201")) {
        about = "Fluency through culture: film, music, literature and history of Spain and Latin America, discussed entirely in Spanish."
        objectives(
            "Discuss films, songs and short literature in Spanish",
            "Recognize major regional accents and vocabulary",
            "Present a researched topic for 10 minutes without notes",
        )
        units(
            "Regional Spanish — Spain, Mexico, the Caribbean, the Southern Cone",
            "Film I — Spanish cinema", "Film II — Latin American cinema",
            "Music — from flamenco to reggaetón", "Short fiction — Borges, Cortázar, García Márquez",
            "History — the Spanish Civil War", "History — independence movements",
            "Final presentation",
        )
        read(ResourceType.BOOK, "Ficciones", "Jorge Luis Borges", required = false)
        read(ResourceType.PODCAST, "Radio Ambulante", "NPR", required = true, notes = "Long-form Latin American stories, with transcripts.")
        task(ESSAY, "Film review in Spanish (600 words)", 6)
        task(PRESENTATION, "10-minute talk on a cultural topic", 15)
    }
    program("Spanish", ProgramKind.MINOR, "101", "102", "201", "301")
}
