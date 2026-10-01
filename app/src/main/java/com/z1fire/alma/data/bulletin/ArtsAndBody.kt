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

internal fun artsAndBodyDepartments(): List<TemplateDept> = listOf(philosophy(), art(), music(), kinesiology())

private fun philosophy() = department(
    "Philosophy", "PHIL", 1,
    "Logic, argument and the big questions — learn to reason well and think clearly about anything.",
) {
    course("101", "Introduction to Philosophy") {
        about = "The big questions and how philosophers argue about them: knowledge, mind, free will, God, morality and the meaning of life."
        objectives(
            "Reconstruct and evaluate classic philosophical arguments",
            "Explain major positions in epistemology, metaphysics and ethics",
            "Write a clear, charitable philosophical essay",
        )
        units(
            "What is philosophy?", "Knowledge & skepticism — Descartes", "Mind & body", "Free will & determinism",
            "Personal identity", "Arguments about God", "Ethics — consequences, duties, virtues", "Meaning & the good life",
        )
        book("Think: A Compelling Introduction to Philosophy", "Simon Blackburn", required = true)
        book("The Problems of Philosophy", "Bertrand Russell", required = false, notes = "Short, free, and a classic.")
        site("Stanford Encyclopedia of Philosophy", "Stanford University", "https://plato.stanford.edu/", notes = "Authoritative articles on every topic.")
        task(ESSAY, "Short paper: can we know anything for certain? (1,000 words)", 4)
        task(ESSAY, "Short paper: are we free? (1,000 words)", 9)
        task(ESSAY, "Final paper (2,000 words)", 15)
    }
    course("110", "Logic & the Art of Argument", weeks = 8) {
        about = "An introduction to reasoning well. We learn to find the argument hiding in everyday speech, test whether it holds up, name the ways it can go wrong, and build sound, charitable arguments of our own — first informally, then with the tools of propositional logic."
        objectives(
            "Identify premises and conclusions in everyday arguments",
            "Distinguish deductive validity from inductive strength",
            "Recognize and name common informal fallacies",
            "Symbolize arguments in propositional logic and test them with truth tables",
            "Write a clear, charitable, well-structured argumentative essay",
        )
        units(
            "What is an argument? — Premises, conclusions, and the difference between arguing and asserting.",
            "Mapping arguments — Standard form, argument diagrams, hidden premises.",
            "Deduction & induction — Validity, soundness, strength, cogency.",
            "Informal fallacies — Fallacies of relevance, ambiguity, and presumption.",
            "Propositional logic — Symbolization, connectives, truth tables.",
            "Testing validity — Truth-table tests and the basics of natural deduction.",
            "Reasoning under uncertainty — Analogy, causal reasoning, inference to the best explanation.",
            "Capstone: building your own case — Plan, draft, and critique an argumentative essay.",
        )
        book("A Rulebook for Arguments", "Anthony Weston", notes = "Short and practical — the backbone of the first half.")
        textbook("forall x: Calgary — An Introduction to Formal Logic", "P. D. Magnus, Tim Button et al.", "https://forallx.openlogicproject.org/", notes = "Free, open-access textbook for the formal units.")
        textbook("Introduction to Logic", "Irving M. Copi & Carl Cohen", required = false)
        book("Being Logical: A Guide to Good Thinking", "D. Q. McInerny", required = false)
        read(ResourceType.ARTICLE, "Informal Logic", "Stanford Encyclopedia of Philosophy", "https://plato.stanford.edu/entries/logic-informal/", required = false)
        site("The Fallacy Files", "Gary N. Curtis", "https://www.fallacyfiles.org/")
        task(READING, "Read the first half of A Rulebook for Arguments", 1)
        task(PROBLEM_SET, "Map five arguments from op-eds", 2)
        task(EXAM, "Midterm self-quiz: fallacies & validity", 4)
        task(PROJECT, "Fallacy field journal (10 entries)", 5)
        task(PROBLEM_SET, "Truth-table problem set", 6)
        task(ESSAY, "Final paper: 1,500-word argumentative essay", 8)
    }
    course("210", "Symbolic Logic", prereqs = listOf("110")) {
        about = "Natural deduction for propositional and first-order logic, quantifiers, identity, and an introduction to metalogic."
        objectives(
            "Construct natural-deduction proofs in propositional and predicate logic",
            "Translate English sentences with quantifiers into first-order logic",
            "Explain soundness and completeness informally",
        )
        units(
            "Review of propositional logic", "Natural deduction — basic rules", "Derived rules & proof strategy",
            "Predicate logic — symbolization", "Quantifier rules", "Identity & definite descriptions",
            "Interpretations & models", "Soundness & completeness",
        )
        textbook("forall x: Calgary — An Introduction to Formal Logic", "P. D. Magnus, Tim Button et al.", "https://forallx.openlogicproject.org/")
        site("Carnap.io", "Graham Leach-Krouse", "https://carnap.io/", notes = "Free proof checker that works with forall x.")
        task(PROBLEM_SET, "Propositional proofs (20 problems)", 4)
        task(PROBLEM_SET, "Quantifier proofs (20 problems)", 9)
        task(EXAM, "Final self-exam", 15)
    }
    course("215", "Critical Thinking & Cognitive Bias", prereqs = listOf("110")) {
        about = "Why smart people believe wrong things: heuristics and biases, probability errors, statistics in the news, and habits for thinking better."
        objectives(
            "Recognize common cognitive biases in yourself and others",
            "Evaluate statistical and scientific claims in the media",
            "Apply Bayesian reasoning to everyday beliefs",
        )
        units(
            "Two systems of thinking", "Heuristics & biases", "Probability & base rates", "Bayesian updating",
            "Statistics in the news", "Science vs pseudoscience", "Motivated reasoning", "Better decision habits",
        )
        book("Thinking, Fast and Slow", "Daniel Kahneman", required = true)
        book("The Scout Mindset", "Julia Galef", required = false)
        book("How to Lie with Statistics", "Darrell Huff", required = false)
        task(PROJECT, "Bias journal: catch yourself 10 times", 7)
        task(ESSAY, "Debunk a viral claim (1,200 words)", 12)
    }
    course("330", "Non-Classical Logic & Metalogic", prereqs = listOf("210")) {
        about = "Beyond classical logic: modal, many-valued, intuitionistic and relevant logics, and the limits of formal systems from Gödel's theorems."
        objectives(
            "Work with possible-world semantics and tableaux for modal logic",
            "Compare classical, intuitionistic and many-valued logics",
            "Explain Gödel's incompleteness theorems at a conceptual level",
        )
        units(
            "Tableaux for classical logic", "Modal logic — necessity & possibility", "Conditional logics",
            "Intuitionistic logic", "Many-valued logics", "Relevant logic", "Gödel's incompleteness theorems",
        )
        textbook("An Introduction to Non-Classical Logic", "Graham Priest")
        book("Gödel's Proof", "Ernest Nagel & James R. Newman", required = true)
        book("Logic: A Very Short Introduction", "Graham Priest", required = false)
        task(PROBLEM_SET, "Modal tableaux problems", 5)
        task(ESSAY, "Explain Gödel's first theorem (1,500 words)", 13)
    }
    program("Logic", ProgramKind.CERTIFICATE, "110", "210", "215", "330")
    program("Philosophy", ProgramKind.MINOR, "101", "110", "210", "215", "330")
}

private fun art() = department(
    "Art", "ARTS", 6,
    "Drawing, painting and art history — train your eye and your hand.",
) {
    course("101", "Drawing Fundamentals") {
        about = "Learn to see and draw: line, shape, perspective, proportion, value and form, built through daily practice."
        objectives(
            "Draw confidently with controlled line and shape",
            "Construct objects in one-, two- and three-point perspective",
            "Render form with value and light",
            "Build a daily sketchbook habit",
        )
        units(
            "Lines & ellipses", "Seeing shapes & negative space", "Perspective — boxes in space", "Proportion & measuring",
            "Value & shading", "Light on form", "Still life", "Drawing from life outdoors",
        )
        book("Drawing on the Right Side of the Brain", "Betty Edwards", required = true)
        book("Keys to Drawing", "Bert Dodson", required = false)
        site("Drawabox", "Irshad Karim", "https://drawabox.com/", required = true, notes = "Free structured lessons with homework.")
        task(PROJECT, "Sketchbook: 100 drawings", 15)
        task(PROJECT, "Before & after self-portrait", 2)
        task(PROJECT, "Finished still-life drawing", 10)
    }
    course("120", "Color & Painting", prereqs = listOf("101")) {
        about = "Color theory and paint handling in gouache, acrylic or oil: mixing, temperature, light and composition."
        objectives(
            "Mix accurate colors and control value and saturation",
            "Paint convincing light and atmosphere",
            "Complete finished paintings from life",
        )
        units(
            "Materials & setup", "Color wheel & mixing", "Value in color", "Color temperature", "Light & shadow",
            "Atmospheric perspective", "Plein air painting", "Final painting",
        )
        book("Color and Light: A Guide for the Realist Painter", "James Gurney", required = true)
        book("Alla Prima II", "Richard Schmid", required = false)
        task(PROJECT, "Color-mixing charts", 3)
        task(PROJECT, "Five plein air studies", 9)
        task(PROJECT, "Final finished painting", 15)
    }
    course("150", "Art History: A Global Survey") {
        about = "From cave paintings to contemporary art: how to look at art, and the stories of the people and cultures who made it."
        objectives(
            "Analyze artworks using visual vocabulary",
            "Place major works in their historical context",
            "Write a formal analysis of an artwork",
        )
        units(
            "How to look at art", "Prehistoric & ancient art", "Classical Greece & Rome", "Medieval & Islamic art",
            "Renaissance", "Baroque", "Asian art traditions", "Modernism", "Contemporary art",
        )
        book("The Story of Art", "E. H. Gombrich", required = true)
        book("Ways of Seeing", "John Berger", required = true)
        site("Smarthistory", "Smarthistory", "https://smarthistory.org/", required = true, notes = "Free videos and essays.")
        task(ESSAY, "Formal analysis of a painting (1,000 words)", 6)
        task(PROJECT, "Museum visit (in person or virtual) & reflection", 11)
        task(EXAM, "Final self-exam", 15)
    }
    course("210", "Figure Drawing & Anatomy", prereqs = listOf("101")) {
        about = "Draw the human figure: gesture, structure, anatomy and proportion, from timed poses to finished drawings."
        objectives(
            "Capture gesture in quick poses",
            "Construct the figure with simplified forms",
            "Draw major muscle groups convincingly",
        )
        units(
            "Gesture", "Proportion", "The head & face", "Torso & pelvis", "Arms & hands",
            "Legs & feet", "Clothing & drapery", "Long poses",
        )
        book("Figure Drawing: Design and Invention", "Michael Hampton", required = true)
        site("Proko", "Stan Prokopenko", "https://www.proko.com/", notes = "Figure and anatomy video lessons.")
        task(OTHER, "500 gesture drawings (30–60 seconds each)", 8)
        task(PROJECT, "Three finished long-pose drawings", 15)
    }
    program("Studio Art", ProgramKind.CERTIFICATE, "101", "120", "150", "210")
}

private fun music() = department(
    "Music", "MUSC", 7,
    "Guitar from first chords to improvisation, plus the theory that ties it together.",
) {
    course("110", "Guitar I: Foundations", credits = 2) {
        about = "Start playing: open chords, strumming, rhythm, smooth changes and your first songs — with good technique from day one."
        objectives(
            "Play the essential open chords cleanly",
            "Change chords in time with a metronome",
            "Strum common rhythm patterns",
            "Play 10 complete songs",
        )
        units(
            "Holding the guitar & tuning", "First chords — D, A, E", "One-minute changes", "Strumming patterns",
            "More chords — G, C, Em, Am, Dm", "Rhythm & the metronome", "Power chords", "Songs & performance",
        )
        online("Beginner Guitar Course", "Justin Sandercoe (JustinGuitar)", "https://www.justinguitar.com/", required = true, notes = "Free, structured, excellent.")
        book("Hal Leonard Guitar Method, Book 1", "Will Schmid & Greg Koch", required = false)
        task(OTHER, "Daily practice log (20 minutes a day)", 15)
        task(QUIZ, "One-minute changes: 40+ changes on three chord pairs", 6)
        task(PRESENTATION, "Record yourself playing three full songs", 15)
    }
    course("120", "Music Theory Fundamentals") {
        about = "Read and understand music: notes, rhythm, scales, keys, intervals, chords and progressions — applied on the fretboard."
        objectives(
            "Read rhythm and pitch notation",
            "Build major and minor scales, intervals and triads",
            "Analyze chord progressions with Roman numerals",
        )
        units(
            "Notes & the staff", "Rhythm & meter", "Major scales & key signatures", "Minor scales", "Intervals",
            "Triads & seventh chords", "Diatonic harmony", "Chord progressions & cadences",
        )
        site("musictheory.net", "Ricci Adams", "https://www.musictheory.net/", required = true, notes = "Lessons and drills.")
        book("Fretboard Theory", "Desi Serna", required = true)
        book("The Complete Idiot's Guide to Music Theory", "Michael Miller", required = false)
        task(QUIZ, "Key signatures drill (100% accuracy)", 4)
        task(PROBLEM_SET, "Analyze 5 songs with Roman numerals", 12)
        task(EXAM, "Final self-exam", 15)
    }
    course("210", "Guitar II: Intermediate Guitar", credits = 2, prereqs = listOf("110", "120")) {
        about = "Unlock the fretboard: barre chords, the CAGED system, pentatonic and major scales, and beginning lead playing."
        objectives(
            "Play barre chords anywhere on the neck",
            "Navigate the fretboard with CAGED",
            "Play the five pentatonic shapes and improvise over a blues",
        )
        units(
            "Barre chords — E and A shapes", "Notes on the neck", "The CAGED system", "Minor pentatonic — 5 shapes",
            "Bends, slides, vibrato", "12-bar blues & improvisation", "Major scale & modes intro", "Performance",
        )
        online("Intermediate Guitar Course", "Justin Sandercoe (JustinGuitar)", "https://www.justinguitar.com/", required = true)
        book("Fretboard Theory", "Desi Serna", required = true)
        task(PROJECT, "Record a 12-bar blues solo", 10)
        task(PRESENTATION, "Mini recital: 5 songs recorded", 15)
    }
    course("220", "Fingerstyle & Classical Guitar", credits = 2, prereqs = listOf("110")) {
        about = "Fingerpicking technique and classical repertoire: free and rest strokes, arpeggios, independence of thumb and fingers."
        objectives(
            "Play fingerstyle patterns with thumb independence",
            "Read standard notation for guitar",
            "Perform two classical or fingerstyle pieces",
        )
        units(
            "Right-hand technique — p i m a", "Free & rest strokes", "Arpeggio patterns", "Travis picking",
            "Reading notation", "Etudes", "Repertoire", "Performance",
        )
        book("Pumping Nylon", "Scott Tennant", required = true)
        task(PROJECT, "Learn a Carcassi or Sor étude", 8)
        task(PRESENTATION, "Record two pieces", 15)
    }
    course("310", "Guitar III: Improvisation & Jazz", credits = 2, prereqs = listOf("210")) {
        about = "Improvise with intention: chord tones, arpeggios, modes, jazz voicings and soloing over changes."
        objectives(
            "Target chord tones while soloing",
            "Play common jazz voicings and ii–V–I progressions",
            "Solo over a jazz standard",
        )
        units(
            "Arpeggios across the neck", "Chord-tone soloing", "Modes in context", "Jazz voicings — shell chords",
            "ii–V–I", "Learning a standard", "Transcription", "Recital",
        )
        book("The Advancing Guitarist", "Mick Goodrick", required = true)
        task(PROJECT, "Transcribe a short solo", 9)
        task(PRESENTATION, "Record a solo over a standard", 15)
    }
    program("Guitar Performance", ProgramKind.CERTIFICATE, "110", "120", "210", "220", "310")
}

private fun kinesiology() = department(
    "Kinesiology & Fitness", "KINE", 2,
    "Train your body like you train your mind: strength, endurance, mobility, nutrition and the science behind them.",
) {
    course("101", "Foundations of Strength Training", credits = 2, weeks = 12) {
        about = "Learn the big barbell lifts with good form and run a progressive program — squat, bench, deadlift, overhead press."
        objectives(
            "Perform the squat, bench press, deadlift and press with sound technique",
            "Run a linear-progression program and log every session",
            "Understand progressive overload, recovery and deloads",
        )
        units(
            "Safety, warm-up & equipment", "The squat", "The press", "The deadlift", "The bench press",
            "Linear progression", "Recovery & sleep", "Testing & next steps",
        )
        book("Starting Strength (3rd ed.)", "Mark Rippetoe", required = true)
        book("Practical Programming for Strength Training", "Mark Rippetoe & Andy Baker", required = false)
        task(OTHER, "Baseline: record starting lifts and body measurements", 1)
        task(PROJECT, "Training log: 36 sessions", 12)
        task(EXAM, "Final: test your lifts and film your form", 12)
    }
    course("110", "Cardiovascular Conditioning", credits = 2, weeks = 10) {
        about = "Build an aerobic base: running or cycling with heart-rate zones, mostly easy training, and a goal event at the end."
        objectives(
            "Train by heart-rate or effort zones",
            "Follow an 80/20 easy-to-hard training balance",
            "Complete a 5K run (or equivalent) at the end of the course",
        )
        units(
            "Heart-rate zones", "Building the base", "Running form", "Intervals", "Tempo work",
            "Injury prevention", "Taper", "Race week",
        )
        book("80/20 Running", "Matt Fitzgerald", required = true)
        site("Couch to 5K", "NHS", "https://www.nhs.uk/better-health/get-active/get-running-with-couch-to-5k/", notes = "A gentle start if you're new to running.")
        task(OTHER, "Baseline: 1-mile time trial", 1)
        task(PROJECT, "Training log: 30 sessions", 10)
        task(EXAM, "Finish a 5K", 10)
    }
    course("120", "Mobility, Flexibility & Yoga", credits = 1, weeks = 8) {
        about = "Move better and hurt less: daily mobility routines, flexibility work and a foundation in yoga."
        objectives(
            "Assess your mobility limitations",
            "Build a daily 15-minute mobility routine",
            "Practice foundational yoga postures safely",
        )
        units(
            "Mobility self-assessment", "Hips", "Shoulders & thoracic spine", "Ankles & feet",
            "Yoga foundations", "Breathing", "Building your routine", "Reassessment",
        )
        book("Becoming a Supple Leopard", "Kelly Starrett", required = true)
        book("Light on Yoga", "B. K. S. Iyengar", required = false)
        task(OTHER, "Mobility assessment (photos/video)", 1)
        task(PROJECT, "Daily routine log: 40 days", 8)
    }
    course("210", "Nutrition & Recovery", prereqs = listOf("101")) {
        about = "The evidence on eating, sleeping and recovering for health and performance — and how to tell good nutrition science from hype."
        objectives(
            "Explain macronutrients, energy balance and protein needs",
            "Evaluate supplement and diet claims against the evidence",
            "Design sleep and recovery habits that support training",
        )
        units(
            "Energy balance", "Protein, carbs & fat", "Micronutrients", "Hydration", "Supplements — what works",
            "Sleep", "Stress & recovery", "Building sustainable habits",
        )
        site("Examine.com", "Examine", "https://examine.com/", required = true, notes = "Independent analysis of nutrition research.")
        book("Why We Sleep", "Matthew Walker", required = false)
        task(PROJECT, "Two-week food & sleep log with analysis", 4)
        task(ESSAY, "Evaluate a popular diet against the evidence", 10)
    }
    course("220", "Exercise Physiology", prereqs = listOf("101", "BIOL 110")) {
        about = "How the body responds and adapts to exercise: energy systems, muscle, the cardiovascular and respiratory systems, and training adaptations."
        objectives(
            "Describe the three energy systems and when each dominates",
            "Explain muscle contraction and adaptations to strength training",
            "Explain cardiovascular and respiratory adaptations to endurance training",
        )
        units(
            "Energy systems", "Muscle structure & contraction", "Neural control of movement", "Cardiovascular system",
            "Respiratory system", "Adaptations to strength training", "Adaptations to endurance training", "Environment & performance",
        )
        textbook("Physiology of Sport and Exercise", "W. Larry Kenney, Jack Wilmore & David Costill")
        task(QUIZ, "Energy systems self-quiz", 4)
        task(ESSAY, "Explain your own training adaptations", 12)
        task(EXAM, "Final self-exam", 15)
    }
    program("Fitness & Health", ProgramKind.CERTIFICATE, "101", "110", "120", "210")
}
