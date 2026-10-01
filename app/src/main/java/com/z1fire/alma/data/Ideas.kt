package com.z1fire.alma.data

/** A starter course: tap it when creating a course to prefill a short "what to study" list. */
data class Idea(val title: String, val subject: String, val colorIndex: Int, val items: List<Pair<String, String>>) {
    fun toCourse(): Course = Course(
        title = title,
        subject = subject,
        colorIndex = colorIndex,
        items = items.map { (text, link) -> StudyItem(text = text, link = link) },
    )
}

private fun idea(title: String, subject: String, color: Int, vararg items: Pair<String, String>) =
    Idea(title, subject, color, items.toList())

private fun item(text: String, link: String = "") = text to link

val starterIdeas: List<Idea> = listOf(
    idea(
        "Mandarin Chinese", "Languages", 1,
        item("Pinyin and the four tones"),
        item("Integrated Chinese, Level 1 Part 1 — Yuehua Liu et al."),
        item("Pleco dictionary + flashcards", "https://www.pleco.com/"),
        item("Chinese Grammar Wiki (A1 points)", "https://resources.allsetlearning.com/chinese/grammar/"),
        item("First 150 characters by hand"),
        item("HSK 1 practice test"),
    ),
    idea(
        "Japanese", "Languages", 6,
        item("Hiragana", "https://www.tofugu.com/japanese/learn-hiragana/"),
        item("Katakana"),
        item("GENKI I — Eri Banno et al."),
        item("Tae Kim's Guide to Learning Japanese", "https://guidetojapanese.org/learn/"),
        item("Daily Anki reviews", "https://apps.ankiweb.net/"),
        item("JLPT N5 practice test"),
    ),
    idea(
        "Thai", "Languages", 3,
        item("The five tones and vowel length"),
        item("Thai consonants by class"),
        item("Tone rules"),
        item("Thai for Beginners — Benjawan Poomsan Becker"),
        item("thai-language.com dictionary", "http://www.thai-language.com/"),
        item("Order a meal and ask directions in Thai"),
    ),
    idea(
        "Spanish", "Languages", 7,
        item("Language Transfer: Complete Spanish", "https://www.languagetransfer.org/"),
        item("Dreaming Spanish — 50 hours of input", "https://www.dreamingspanish.com/"),
        item("Ser vs. estar"),
        item("Preterite vs. imperfect"),
        item("Practice Makes Perfect: Complete Spanish Grammar — Gilda Nissenberg"),
        item("A 10-minute conversation with a native speaker"),
    ),
    idea(
        "Cell Biology", "Biology", 2,
        item("OpenStax Biology 2e: cell structure chapters", "https://openstax.org/details/books/biology-2e"),
        item("Essential Cell Biology — Bruce Alberts et al."),
        item("Membranes and transport"),
        item("Protein sorting and vesicle traffic"),
        item("Cell signaling"),
        item("The cell cycle and cancer"),
    ),
    idea(
        "Molecular Biology", "Biology", 2,
        item("DNA replication and repair"),
        item("Transcription and gene regulation"),
        item("Translation and the genetic code"),
        item("Molecular Biology of the Gene — James D. Watson et al."),
        item("PCR, sequencing and CRISPR"),
        item("The Gene: An Intimate History — Siddhartha Mukherjee"),
    ),
    idea(
        "Logic", "Philosophy", 1,
        item("A Rulebook for Arguments — Anthony Weston"),
        item("Informal fallacies", "https://www.fallacyfiles.org/"),
        item("forall x: propositional logic", "https://forallx.openlogicproject.org/"),
        item("Truth tables and validity"),
        item("Natural deduction proofs"),
        item("Write a 1,500-word argumentative essay"),
    ),
    idea(
        "Drawing & Art", "Art", 6,
        item("Drawabox lessons", "https://drawabox.com/"),
        item("Drawing on the Right Side of the Brain — Betty Edwards"),
        item("Perspective: boxes in space"),
        item("Value and light on form"),
        item("The Story of Art — E. H. Gombrich"),
        item("Sketchbook: 100 drawings"),
    ),
    idea(
        "Guitar", "Music", 7,
        item("JustinGuitar beginner course", "https://www.justinguitar.com/"),
        item("Open chords: D, A, E, G, C, Em, Am"),
        item("One-minute chord changes"),
        item("Strumming patterns with a metronome"),
        item("Fretboard Theory — Desi Serna"),
        item("Learn 10 full songs"),
    ),
    idea(
        "Fitness", "Health", 2,
        item("Starting Strength — Mark Rippetoe"),
        item("Learn the squat, deadlift, bench and press"),
        item("12 weeks of linear progression"),
        item("80/20 Running — Matt Fitzgerald"),
        item("Run a 5K"),
        item("Daily 15-minute mobility routine"),
    ),
    idea(
        "Astronomy & Space", "Science", 9,
        item("OpenStax Astronomy 2e", "https://openstax.org/details/books/astronomy-2e"),
        item("Crash Course Astronomy (YouTube)"),
        item("Learn 20 constellations with Stellarium", "https://stellarium.org/"),
        item("Turn Left at Orion — Consolmagno & Davis"),
        item("Orbits, the rocket equation and Hohmann transfers"),
        item("The First Three Minutes — Steven Weinberg"),
    ),
    idea(
        "Electronics & Engineering", "Engineering", 8,
        item("Make: Electronics — Charles Platt"),
        item("Ohm's law and Kirchhoff's laws"),
        item("Build a 555 timer LED flasher"),
        item("Arduino basics", "https://docs.arduino.cc/"),
        item("CAD with Onshape", "https://learn.onshape.com/"),
        item("The Design of Everyday Things — Don Norman"),
    ),
    idea(
        "Computer Science", "Computing", 4,
        item("Automate the Boring Stuff with Python — Al Sweigart", "https://automatetheboringstuff.com/"),
        item("CS50x", "https://cs50.harvard.edu/x/"),
        item("Grokking Algorithms — Aditya Bhargava"),
        item("Data structures: lists, hash maps, trees, graphs"),
        item("Nand2Tetris", "https://www.nand2tetris.org/"),
        item("Build and ship a small project"),
    ),
)
