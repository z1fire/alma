package com.z1fire.alma.data.bulletin

import com.z1fire.alma.data.AssignmentType.ESSAY
import com.z1fire.alma.data.AssignmentType.EXAM
import com.z1fire.alma.data.AssignmentType.OTHER
import com.z1fire.alma.data.AssignmentType.PRESENTATION
import com.z1fire.alma.data.AssignmentType.PROBLEM_SET
import com.z1fire.alma.data.AssignmentType.PROJECT
import com.z1fire.alma.data.AssignmentType.QUIZ
import com.z1fire.alma.data.ProgramKind
import com.z1fire.alma.data.ResourceType

internal fun stemDepartments(): List<TemplateDept> =
    listOf(mathematics(), physics(), computerScience(), engineering(), astronomy())

private fun mathematics() = department(
    "Mathematics", "MATH", 0,
    "The language of science and engineering, from precalculus through linear algebra and statistics.",
) {
    course("110", "Precalculus") {
        about = "Functions, graphs, polynomials, exponentials, logarithms and trigonometry — the runway for calculus."
        objectives(
            "Analyze and transform functions and their graphs",
            "Solve exponential and logarithmic equations",
            "Use trigonometric functions and identities",
        )
        units(
            "Functions & graphs", "Linear & quadratic models", "Polynomial & rational functions",
            "Exponential & logarithmic functions", "Right-triangle trigonometry", "The unit circle",
            "Trigonometric identities", "Vectors & polar coordinates",
        )
        textbook("Precalculus 2e", "OpenStax", "https://openstax.org/details/books/precalculus-2e")
        online("Precalculus", "Khan Academy", "https://www.khanacademy.org/math/precalculus")
        task(PROBLEM_SET, "Functions problem set", 3)
        task(PROBLEM_SET, "Exponentials & logs problem set", 7)
        task(PROBLEM_SET, "Trigonometry problem set", 11)
        task(EXAM, "Final self-exam", 15)
    }
    course("151", "Calculus I", credits = 4, prereqs = listOf("110")) {
        about = "Limits, derivatives and integrals of a single variable — the mathematics of change, with applications to physics and optimization."
        objectives(
            "Compute limits and derivatives using the rules of differentiation",
            "Apply derivatives to rates of change and optimization",
            "Evaluate definite integrals and apply the Fundamental Theorem of Calculus",
        )
        units(
            "Limits & continuity", "The derivative", "Differentiation rules", "Chain rule & implicit differentiation",
            "Related rates", "Optimization & curve sketching", "Antiderivatives", "The definite integral",
            "Fundamental Theorem of Calculus", "Substitution",
        )
        textbook("Calculus Volume 1", "OpenStax (Strang & Herman)", "https://openstax.org/details/books/calculus-volume-1")
        lectures("Essence of Calculus", "3Blue1Brown", "https://www.3blue1brown.com/topics/calculus", required = true, notes = "Watch before each unit for intuition.")
        online("18.01SC Single Variable Calculus", "MIT OpenCourseWare", "https://ocw.mit.edu/courses/18-01sc-single-variable-calculus-fall-2010/")
        task(PROBLEM_SET, "Limits problem set", 3)
        task(PROBLEM_SET, "Derivatives problem set", 6)
        task(EXAM, "Midterm self-exam", 8)
        task(PROBLEM_SET, "Optimization problem set", 10)
        task(EXAM, "Final self-exam", 15)
    }
    course("152", "Calculus II", credits = 4, prereqs = listOf("151")) {
        about = "Integration techniques, applications of integrals, sequences and series, and parametric and polar curves."
        objectives(
            "Apply integration by parts, partial fractions and trig substitution",
            "Use integrals for area, volume, arc length and work",
            "Determine convergence of series and build Taylor series",
        )
        units(
            "Areas & volumes", "Integration by parts", "Trigonometric integrals & substitution", "Partial fractions",
            "Improper integrals", "Differential equations intro", "Sequences", "Series & convergence tests",
            "Power & Taylor series", "Parametric & polar curves",
        )
        textbook("Calculus Volume 2", "OpenStax (Strang & Herman)", "https://openstax.org/details/books/calculus-volume-2")
        task(PROBLEM_SET, "Integration techniques problem set", 5)
        task(PROBLEM_SET, "Series problem set", 11)
        task(EXAM, "Final self-exam", 15)
    }
    course("240", "Linear Algebra", prereqs = listOf("151")) {
        about = "Vectors, matrices, linear transformations, eigenvalues and least squares — essential for engineering, physics and machine learning."
        objectives(
            "Solve linear systems and interpret them geometrically",
            "Work with vector spaces, bases, rank and null space",
            "Compute and interpret eigenvalues, eigenvectors and the SVD",
        )
        units(
            "Vectors & linear combinations", "Solving Ax = b — elimination", "Matrix operations & inverses",
            "Vector spaces & subspaces", "Rank & the four fundamental subspaces", "Orthogonality & least squares",
            "Determinants", "Eigenvalues & eigenvectors", "Diagonalization", "Singular value decomposition",
        )
        textbook("Introduction to Linear Algebra", "Gilbert Strang")
        online("18.06 Linear Algebra", "MIT OpenCourseWare (Gilbert Strang)", "https://ocw.mit.edu/courses/18-06-linear-algebra-spring-2010/", required = true)
        lectures("Essence of Linear Algebra", "3Blue1Brown", "https://www.3blue1brown.com/topics/linear-algebra")
        task(PROBLEM_SET, "Elimination & inverses problem set", 4)
        task(PROBLEM_SET, "Subspaces problem set", 7)
        task(PROBLEM_SET, "Eigenvalues problem set", 11)
        task(EXAM, "Final self-exam", 15)
    }
    course("250", "Probability & Statistics", prereqs = listOf("151")) {
        about = "Thinking with uncertainty: probability, distributions, sampling, estimation, hypothesis tests and regression."
        objectives(
            "Compute probabilities with counting, conditioning and Bayes' rule",
            "Work with common discrete and continuous distributions",
            "Build confidence intervals, run hypothesis tests and fit regressions",
        )
        units(
            "Descriptive statistics", "Probability rules & Bayes", "Discrete distributions", "Continuous distributions",
            "Sampling & the Central Limit Theorem", "Confidence intervals", "Hypothesis testing", "Linear regression",
        )
        textbook("Introductory Statistics", "OpenStax", "https://openstax.org/details/books/introductory-statistics")
        site("Seeing Theory", "Brown University", "https://seeing-theory.brown.edu/", notes = "Beautiful interactive visualizations.")
        task(PROBLEM_SET, "Probability problem set", 4)
        task(PROJECT, "Analyze a real dataset of your choice", 13)
        task(EXAM, "Final self-exam", 15)
    }
    program("Mathematics", ProgramKind.MINOR, "151", "152", "240", "250")
}

private fun physics() = department(
    "Physics", "PHYS", 8,
    "The fundamental laws of motion, energy and fields that engineering and astronomy depend on.",
) {
    course("101", "Physics I: Mechanics", credits = 4, prereqs = listOf("MATH 151")) {
        about = "Calculus-based mechanics: kinematics, Newton's laws, energy, momentum, rotation, gravitation and oscillations."
        objectives(
            "Apply Newton's laws with free-body diagrams",
            "Use conservation of energy and momentum to solve problems",
            "Analyze rotational motion, gravitation and simple harmonic motion",
        )
        units(
            "Kinematics in 1D & 2D", "Newton's laws", "Friction & circular motion", "Work & energy",
            "Momentum & collisions", "Rotation & torque", "Angular momentum", "Gravitation & orbits",
            "Oscillations", "Waves",
        )
        textbook("University Physics Volume 1", "OpenStax", "https://openstax.org/details/books/university-physics-volume-1")
        site("The Feynman Lectures on Physics", "Richard Feynman", "https://www.feynmanlectures.caltech.edu/", notes = "Free online; read Vol. I alongside.")
        task(PROBLEM_SET, "Newton's laws problem set", 4)
        task(PROBLEM_SET, "Energy & momentum problem set", 8)
        task(PROJECT, "Home lab: measure g with a pendulum", 10)
        task(EXAM, "Final self-exam", 15)
    }
    course("102", "Physics II: Electricity & Magnetism", credits = 4, prereqs = listOf("101", "MATH 152")) {
        about = "Electric and magnetic fields, circuits, induction and electromagnetic waves — the physics behind all of electronics."
        objectives(
            "Use Gauss's law and potential to analyze electric fields",
            "Analyze DC and simple AC circuits",
            "Explain induction and Maxwell's equations conceptually",
        )
        units(
            "Electric charge & fields", "Gauss's law", "Electric potential", "Capacitance",
            "Current & resistance", "DC circuits", "Magnetic fields", "Induction", "AC circuits", "Maxwell's equations & light",
        )
        textbook("University Physics Volume 2", "OpenStax", "https://openstax.org/details/books/university-physics-volume-2")
        task(PROBLEM_SET, "Electrostatics problem set", 5)
        task(PROBLEM_SET, "Circuits problem set", 9)
        task(EXAM, "Final self-exam", 15)
    }
    course("201", "Modern Physics", prereqs = listOf("102")) {
        about = "Relativity and the quantum revolution: the physics of the very fast and the very small."
        objectives(
            "Apply special relativity to time dilation, length contraction and energy",
            "Explain wave–particle duality and the Schrödinger equation",
            "Describe atomic, nuclear and particle physics at an introductory level",
        )
        units(
            "Special relativity", "Photons & the photoelectric effect", "Matter waves", "The Schrödinger equation",
            "The hydrogen atom", "Nuclear physics", "Particle physics & the Standard Model",
        )
        textbook("University Physics Volume 3", "OpenStax", "https://openstax.org/details/books/university-physics-volume-3")
        book("QED: The Strange Theory of Light and Matter", "Richard Feynman", required = false)
        task(PROBLEM_SET, "Relativity problem set", 4)
        task(ESSAY, "Explain quantum tunneling to a friend (1,000 words)", 10)
        task(EXAM, "Final self-exam", 15)
    }
    program("Physics", ProgramKind.MINOR, "101", "102", "201")
}

private fun computerScience() = department(
    "Computer Science", "CSCI", 4,
    "Programming, algorithms, systems and machine learning — from your first program to building real software.",
) {
    course("101", "Introduction to Programming (Python)", credits = 4) {
        about = "Learn to think computationally and write real programs in Python: variables, control flow, functions, data structures, files and automation."
        objectives(
            "Write, test and debug Python programs",
            "Decompose problems into functions",
            "Use lists, dictionaries and files to process data",
            "Automate a real task from your own life",
        )
        units(
            "Expressions, variables & types", "Conditionals", "Loops", "Functions", "Lists & strings",
            "Dictionaries & sets", "Files & exceptions", "Modules & libraries", "Automation projects", "Final project",
        )
        book("Automate the Boring Stuff with Python", "Al Sweigart", required = true, notes = "Free to read online at automatetheboringstuff.com.")
        book("Python Crash Course", "Eric Matthes", required = false)
        online("CS50x: Introduction to Computer Science", "Harvard University", "https://cs50.harvard.edu/x/", notes = "Free and excellent; a broader alternative path.")
        task(PROBLEM_SET, "Control-flow exercises", 3)
        task(PROBLEM_SET, "Functions & lists exercises", 6)
        task(PROJECT, "Build a text-based game", 9)
        task(PROJECT, "Final project: automate something you actually do", 15)
    }
    course("150", "Discrete Mathematics", prereqs = listOf("MATH 110")) {
        about = "The math of computer science: logic, proofs, sets, induction, counting, graphs and number theory."
        objectives(
            "Write clear direct, contrapositive, contradiction and induction proofs",
            "Solve counting and discrete probability problems",
            "Reason about graphs, trees and relations",
        )
        units(
            "Propositional & predicate logic", "Proof techniques", "Sets, functions & relations", "Induction",
            "Number theory & modular arithmetic", "Counting", "Discrete probability", "Graphs & trees",
        )
        textbook("Mathematics for Computer Science", "Eric Lehman, F. Thomson Leighton & Albert R. Meyer", notes = "Free MIT text.")
        task(PROBLEM_SET, "Proofs problem set", 4)
        task(PROBLEM_SET, "Counting problem set", 9)
        task(EXAM, "Final self-exam", 15)
    }
    course("201", "Data Structures & Algorithms", credits = 4, prereqs = listOf("101", "150")) {
        about = "How to organize data and design efficient algorithms: complexity, lists, trees, hashing, graphs, sorting and dynamic programming."
        objectives(
            "Analyze algorithms with Big-O notation",
            "Implement core data structures from scratch",
            "Apply graph algorithms, divide-and-conquer and dynamic programming",
        )
        units(
            "Complexity & Big-O", "Arrays, linked lists, stacks, queues", "Recursion", "Sorting & searching",
            "Hash tables", "Trees & binary search trees", "Heaps & priority queues", "Graphs — BFS & DFS",
            "Shortest paths & minimum spanning trees", "Dynamic programming",
        )
        book("Grokking Algorithms", "Aditya Bhargava", required = true)
        textbook("Introduction to Algorithms (CLRS)", "Cormen, Leiserson, Rivest & Stein", required = false)
        site("Open Data Structures", "Pat Morin", "https://opendatastructures.org/", notes = "Free textbook with code.")
        task(PROJECT, "Implement a hash map and a BST from scratch", 6)
        task(PROBLEM_SET, "Solve 30 algorithm practice problems", 12)
        task(EXAM, "Final self-exam", 15)
    }
    course("220", "Computer Systems: From NAND to Tetris", credits = 4, prereqs = listOf("101")) {
        about = "Build a computer from first principles — logic gates, an ALU, a CPU, an assembler, a virtual machine and a compiler."
        objectives(
            "Design combinational and sequential logic from NAND gates",
            "Explain how a CPU executes machine code",
            "Build an assembler and understand compilation",
        )
        units(
            "Boolean logic & gates", "Arithmetic — adders & the ALU", "Memory — flip-flops & RAM",
            "Machine language", "Computer architecture", "The assembler", "Virtual machine", "Compiler basics",
        )
        book("The Elements of Computing Systems (2nd ed.)", "Noam Nisan & Shimon Schocken")
        site("Nand2Tetris", "Nisan & Schocken", "https://www.nand2tetris.org/", required = true, notes = "Projects and software tools.")
        book("Code: The Hidden Language of Computer Hardware and Software", "Charles Petzold", required = false)
        task(PROJECT, "Projects 1–3: gates, ALU, memory", 6)
        task(PROJECT, "Projects 4–5: the Hack CPU", 10)
        task(PROJECT, "Project 6: the assembler", 13)
    }
    course("230", "Web Development", prereqs = listOf("101")) {
        about = "Build for the web: HTML, CSS, JavaScript, the DOM, APIs, and deploying a full project."
        objectives(
            "Build accessible, responsive pages with HTML and CSS",
            "Write interactive JavaScript that talks to APIs",
            "Deploy a complete web project",
        )
        units(
            "How the web works", "HTML & accessibility", "CSS layout — flexbox & grid", "JavaScript fundamentals",
            "The DOM & events", "Fetching data from APIs", "Git & deployment", "Capstone",
        )
        online("The Odin Project — Foundations", "The Odin Project", "https://www.theodinproject.com/", required = true)
        site("MDN Web Docs", "Mozilla", "https://developer.mozilla.org/")
        task(PROJECT, "Responsive landing page", 5)
        task(PROJECT, "JavaScript app using a public API", 10)
        task(PROJECT, "Deployed capstone site", 15)
    }
    course("250", "Mobile App Development (Android)", prereqs = listOf("101")) {
        about = "Build native Android apps in Kotlin with Jetpack Compose — UI, state, navigation, persistence and publishing."
        objectives(
            "Write idiomatic Kotlin",
            "Build Compose UIs with state and navigation",
            "Persist data and ship a signed APK",
        )
        units(
            "Kotlin basics", "Compose layouts", "State & recomposition", "Navigation", "Architecture & ViewModels",
            "Persistence", "Networking", "Publishing & signing",
        )
        online("Android Basics with Compose", "Google", "https://developer.android.com/courses/android-basics-compose/course", required = true)
        task(PROJECT, "Build a tip-calculator app", 4)
        task(PROJECT, "Ship your own app idea", 15)
    }
    course("310", "Operating Systems", prereqs = listOf("201", "220")) {
        about = "Processes, threads, scheduling, virtual memory, concurrency and file systems — how the OS makes one machine look like many."
        objectives(
            "Explain how the OS virtualizes the CPU and memory",
            "Write correct concurrent code with locks and condition variables",
            "Describe file-system design and crash consistency",
        )
        units(
            "Processes & the process API", "CPU scheduling", "Address spaces & paging", "TLBs & swapping",
            "Threads & locks", "Condition variables & semaphores", "Concurrency bugs", "File systems", "Crash consistency & journaling",
        )
        textbook("Operating Systems: Three Easy Pieces", "Remzi & Andrea Arpaci-Dusseau", "https://pages.cs.wisc.edu/~remzi/OSTEP/", notes = "Free online.")
        task(PROJECT, "Write a tiny shell", 4)
        task(PROJECT, "Concurrent producer–consumer queue", 9)
        task(EXAM, "Final self-exam", 15)
    }
    course("340", "Machine Learning", prereqs = listOf("201", "MATH 240", "MATH 250")) {
        about = "How machines learn from data: regression, classification, neural networks and deep learning, with hands-on projects."
        objectives(
            "Train, evaluate and tune supervised learning models",
            "Explain gradient descent and backpropagation",
            "Build and train a neural network on a real dataset",
        )
        units(
            "The ML landscape", "Linear & logistic regression", "Gradient descent", "Evaluation & overfitting",
            "Trees & ensembles", "Unsupervised learning", "Neural networks & backpropagation",
            "Convolutional networks", "Sequence models & transformers", "Capstone project",
        )
        book("Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow", "Aurélien Géron")
        online("Machine Learning Specialization", "Andrew Ng — DeepLearning.AI & Stanford (Coursera)", required = true)
        lectures("Neural Networks", "3Blue1Brown", "https://www.3blue1brown.com/topics/neural-networks")
        task(PROJECT, "Regression model on a real dataset", 5)
        task(PROJECT, "Train an image classifier", 10)
        task(PROJECT, "Capstone ML project with write-up", 15)
    }
    program("Computer Science", ProgramKind.MAJOR, "101", "150", "201", "220", "310")
    program("Software Development", ProgramKind.CERTIFICATE, "101", "230", "250")
}

private fun engineering() = department(
    "Engineering", "ENGR", 3,
    "Design, build and understand machines and electronics — from breadboards and microcontrollers to CAD and mechanics.",
) {
    course("101", "Introduction to Engineering & Design") {
        about = "How engineers think: the design process, failure, trade-offs and human-centered design, with hands-on mini projects."
        objectives(
            "Apply an iterative design process to a real problem",
            "Analyze failures and design for safety and usability",
            "Communicate a design with sketches and a short report",
        )
        units(
            "What engineers do", "The design process", "Sketching & communication", "Human-centered design",
            "Materials & manufacturing", "Failure & safety", "Estimation & back-of-envelope math", "Design project",
        )
        book("The Design of Everyday Things", "Don Norman", required = true)
        book("To Engineer Is Human: The Role of Failure in Successful Design", "Henry Petroski", required = true)
        task(ESSAY, "Analyze a badly designed everyday object", 4)
        task(PROJECT, "Design & build a solution to a household problem", 14)
        task(PRESENTATION, "Design review presentation", 15)
    }
    course("120", "Electronics & Circuits", prereqs = listOf("101")) {
        about = "Hands-on electronics: voltage, current, resistance, components, breadboarding, soldering and reading schematics."
        objectives(
            "Apply Ohm's and Kirchhoff's laws to real circuits",
            "Build and debug circuits with a multimeter",
            "Use transistors, capacitors and ICs in simple designs",
        )
        units(
            "Safety & tools — multimeter, breadboard", "Voltage, current & Ohm's law", "Series & parallel circuits",
            "Capacitors & RC timing", "Diodes & LEDs", "Transistors as switches", "Integrated circuits — the 555 timer",
            "Soldering", "Final build",
        )
        book("Make: Electronics", "Charles Platt", required = true, notes = "Learning by discovery — do every experiment.")
        book("Practical Electronics for Inventors", "Paul Scherz & Simon Monk", required = false)
        book("The Art of Electronics", "Paul Horowitz & Winfield Hill", required = false, notes = "The bible, for later.")
        task(PROJECT, "Build an LED flasher with a 555 timer", 7)
        task(PROJECT, "Solder a kit project", 10)
        task(PROJECT, "Final build: design your own circuit", 15)
    }
    course("130", "Arduino & Embedded Systems", prereqs = listOf("120", "CSCI 101")) {
        about = "Program microcontrollers to sense and act on the world: digital and analog I/O, sensors, motors, communication protocols."
        objectives(
            "Write Arduino programs that read sensors and drive outputs",
            "Use PWM, interrupts, I²C and SPI",
            "Design and build a complete embedded project",
        )
        units(
            "Arduino setup & blink", "Digital I/O & buttons", "Analog input & sensors", "PWM, servos & motors",
            "Serial communication", "I²C & SPI devices", "Interrupts & timing", "Power & enclosures", "Final project",
        )
        book("Exploring Arduino", "Jeremy Blum", required = true)
        site("Arduino Documentation", "Arduino", "https://docs.arduino.cc/", required = true)
        task(PROJECT, "Weather station with a temperature/humidity sensor", 6)
        task(PROJECT, "Motor-controlled robot or gadget", 11)
        task(PROJECT, "Final embedded project", 15)
    }
    course("140", "CAD & 3D Printing") {
        about = "Model parts in parametric CAD and turn them into real objects with 3D printing — tolerances, materials and design for manufacturing."
        objectives(
            "Create parametric parts and assemblies",
            "Design for 3D printing — tolerances, supports, orientation",
            "Iterate a design from sketch to working print",
        )
        units(
            "Sketches & constraints", "Extrudes, revolves & fillets", "Parametric design", "Assemblies & joints",
            "3D printer setup & slicing", "Tolerances & fits", "Materials", "Final design project",
        )
        site("Onshape Learning Center", "Onshape", "https://learn.onshape.com/", required = true)
        site("FreeCAD", "FreeCAD project", "https://www.freecad.org/", notes = "Free, open-source alternative.")
        task(PROJECT, "Model and print a phone stand", 5)
        task(PROJECT, "Print-in-place hinge or snap-fit test", 9)
        task(PROJECT, "Final functional part", 15)
    }
    course("210", "Statics & Mechanics of Materials", prereqs = listOf("PHYS 101", "MATH 152")) {
        about = "Forces in equilibrium, trusses, beams, stress and strain — how structures carry loads without failing."
        objectives(
            "Solve 2D and 3D equilibrium problems",
            "Analyze trusses, frames and beams",
            "Compute stress, strain and deflection",
        )
        units(
            "Force vectors", "Particle equilibrium", "Moments & couples", "Rigid-body equilibrium",
            "Trusses & frames", "Shear & moment diagrams", "Stress & strain", "Beam bending",
        )
        textbook("Engineering Mechanics: Statics", "J. L. Meriam & L. G. Kraige")
        task(PROBLEM_SET, "Equilibrium problem set", 4)
        task(PROJECT, "Design and load-test a popsicle-stick bridge", 10)
        task(EXAM, "Final self-exam", 15)
    }
    program("Electronics & Embedded Systems", ProgramKind.CERTIFICATE, "120", "130", "140")
    program("Engineering", ProgramKind.MINOR, "101", "120", "130", "140", "210")
}

private fun astronomy() = department(
    "Astronomy & Space Science", "ASTR", 9,
    "The universe, the night sky and how we explore it — planets, stars, galaxies, spaceflight and cosmology.",
) {
    course("101", "Introduction to Astronomy") {
        about = "A tour of the universe: the sky, light and telescopes, the solar system, stars, galaxies and the Big Bang."
        objectives(
            "Explain the motions of the sky, seasons and lunar phases",
            "Describe how astronomers learn from light",
            "Trace the life cycles of stars",
            "Describe the structure and history of the universe",
        )
        units(
            "The sky — celestial sphere, seasons, phases", "Gravity & orbits", "Light & telescopes",
            "The solar system", "The Sun", "Measuring stars", "Stellar life & death",
            "Black holes", "Galaxies", "Cosmology & the Big Bang",
        )
        textbook("Astronomy 2e", "OpenStax", "https://openstax.org/details/books/astronomy-2e")
        lectures("Crash Course Astronomy", "Phil Plait (CrashCourse)", required = true, notes = "Short, fun episodes for every unit.")
        book("Cosmos", "Carl Sagan", required = false)
        task(OTHER, "Observe and sketch the Moon on 5 nights", 4)
        task(QUIZ, "Stars & stellar evolution self-quiz", 9)
        task(ESSAY, "What would we need to find life elsewhere? (1,000 words)", 13)
        task(EXAM, "Final self-exam", 15)
    }
    course("120", "Observational Astronomy & Stargazing", credits = 2, weeks = 10) {
        about = "Learn the night sky: constellations, star-hopping, binoculars and telescopes, and keeping an observing log."
        objectives(
            "Identify 20 constellations and the bright stars",
            "Star-hop to deep-sky objects with binoculars or a telescope",
            "Keep a detailed observing log",
        )
        units(
            "Dark adaptation & sky charts", "Seasonal constellations", "Planets & the Moon", "Binocular astronomy",
            "Telescope basics", "Star-hopping to Messier objects", "Astrophotography with a phone", "Observing night",
        )
        book("Turn Left at Orion", "Guy Consolmagno & Dan M. Davis", required = true)
        book("NightWatch: A Practical Guide to Viewing the Universe", "Terence Dickinson", required = false)
        site("Stellarium", "Stellarium project", "https://stellarium.org/", required = true, notes = "Free planetarium software.")
        task(PROJECT, "Observing log: 10 sessions", 10)
        task(PROJECT, "Find and sketch 10 Messier objects", 9)
    }
    course("210", "Spaceflight & Orbital Mechanics", prereqs = listOf("PHYS 101", "MATH 151")) {
        about = "How we get to space and move around once there: rockets, orbits, transfers, rendezvous and the history of space exploration."
        objectives(
            "Use the rocket equation and Δv budgets",
            "Describe orbits with Kepler's laws and orbital elements",
            "Plan Hohmann transfers and explain gravity assists",
        )
        units(
            "History of spaceflight", "The rocket equation", "Kepler's laws & orbital elements", "Orbital maneuvers",
            "Hohmann transfers", "Rendezvous & docking", "Gravity assists & interplanetary travel", "Reentry & landing",
        )
        textbook("Fundamentals of Astrodynamics", "Roger Bate, Donald Mueller & Jerry White")
        read(ResourceType.ONLINE_COURSE, "Basics of Space Flight", "NASA Jet Propulsion Laboratory", required = true, notes = "Free online primer.")
        read(ResourceType.OTHER, "Kerbal Space Program", "Squad / Private Division", required = false, notes = "A surprisingly good orbital-mechanics lab.")
        task(PROBLEM_SET, "Δv budget for a Moon mission", 5)
        task(PROJECT, "Fly a Hohmann transfer in a simulator", 9)
        task(ESSAY, "Case study: a mission that failed, and why", 13)
    }
    course("220", "Planetary Science & Astrobiology", prereqs = listOf("101")) {
        about = "Worlds beyond Earth: planetary formation, surfaces and atmospheres, moons, exoplanets and the search for life."
        objectives(
            "Explain how planetary systems form",
            "Compare the geology and atmospheres of the planets",
            "Evaluate the habitability of Mars, Europa, Enceladus and exoplanets",
        )
        units(
            "Solar system formation", "Rocky planets", "Giant planets", "Moons & ocean worlds",
            "Small bodies — asteroids & comets", "Exoplanet detection", "Origins of life", "Searching for biosignatures",
        )
        book("Astrobiology: A Very Short Introduction", "David C. Catling", required = true)
        textbook("Astronomy 2e (solar system chapters)", "OpenStax", "https://openstax.org/details/books/astronomy-2e")
        task(PRESENTATION, "Pitch a mission to an ocean world", 10)
        task(EXAM, "Final self-exam", 15)
    }
    course("230", "Cosmology", prereqs = listOf("101")) {
        about = "The history and fate of the universe: expansion, the cosmic microwave background, dark matter, dark energy and the earliest moments."
        objectives(
            "Explain the evidence for the Big Bang",
            "Describe dark matter and dark energy and how we infer them",
            "Narrate the timeline of the early universe",
        )
        units(
            "Expansion & Hubble's law", "The cosmic microwave background", "Big Bang nucleosynthesis",
            "Dark matter", "Dark energy", "Inflation", "Structure formation", "The fate of the universe",
        )
        book("The First Three Minutes", "Steven Weinberg", required = true)
        book("A Brief History of Time", "Stephen Hawking", required = false)
        task(ESSAY, "The evidence for dark matter (1,500 words)", 9)
        task(EXAM, "Final self-exam", 15)
    }
    course("310", "Astrophysics", credits = 4, prereqs = listOf("101", "PHYS 102", "MATH 152")) {
        about = "The physics of stars and galaxies: radiation, stellar structure, nucleosynthesis, compact objects and galactic dynamics."
        objectives(
            "Apply blackbody radiation and spectroscopy to stars",
            "Model stellar structure and energy generation",
            "Describe white dwarfs, neutron stars and black holes quantitatively",
        )
        units(
            "Radiation & spectra", "Binary stars & stellar masses", "Stellar atmospheres", "Stellar interiors",
            "Nuclear fusion", "Stellar evolution", "Compact objects", "The Milky Way", "Galaxies & AGN",
        )
        textbook("An Introduction to Modern Astrophysics", "Bradley Carroll & Dale Ostlie")
        task(PROBLEM_SET, "Stellar structure problem set", 6)
        task(PROBLEM_SET, "Compact objects problem set", 11)
        task(EXAM, "Final self-exam", 15)
    }
    program("Space Science", ProgramKind.CERTIFICATE, "101", "120", "210", "220")
    program("Astronomy", ProgramKind.MINOR, "101", "120", "220", "230", "310")
}
