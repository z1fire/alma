package com.z1fire.alma.data.bulletin

import com.z1fire.alma.data.AssignmentType.ESSAY
import com.z1fire.alma.data.AssignmentType.EXAM
import com.z1fire.alma.data.AssignmentType.PROBLEM_SET
import com.z1fire.alma.data.AssignmentType.PROJECT
import com.z1fire.alma.data.AssignmentType.QUIZ
import com.z1fire.alma.data.AssignmentType.READING
import com.z1fire.alma.data.ProgramKind

internal fun scienceDepartments(): List<TemplateDept> = listOf(biology(), chemistry())

private fun biology() = department(
    "Biology", "BIOL", 2,
    "Life from molecules to cells to organisms, with a deep track in cellular and molecular biology.",
) {
    course("110", "Principles of Biology", credits = 4) {
        about = "The foundation for everything else in biology: the chemistry of life, cells, energy, genetics, evolution and ecology."
        objectives(
            "Explain the structure and function of the major biological macromolecules",
            "Describe how cells harvest and use energy",
            "Apply Mendelian genetics and explain the central dogma",
            "Explain natural selection and read a phylogenetic tree",
        )
        units(
            "The chemistry of life — water, carbon, macromolecules",
            "Cell structure — prokaryotes, eukaryotes, organelles",
            "Membranes & transport",
            "Metabolism — enzymes, cellular respiration",
            "Photosynthesis",
            "Cell division — mitosis and meiosis",
            "Mendelian genetics",
            "DNA, RNA & protein synthesis",
            "Evolution & natural selection",
            "Ecology — populations, communities, ecosystems",
        )
        textbook("Biology 2e", "OpenStax (Clark, Douglas & Choi)", "https://openstax.org/details/books/biology-2e", notes = "Free textbook.")
        textbook("Campbell Biology", "Lisa Urry, Michael Cain et al.", required = false, notes = "The classic intro text.")
        online("Biology", "Khan Academy", "https://www.khanacademy.org/science/biology", notes = "Videos and practice exercises.")
        task(QUIZ, "Macromolecules self-quiz", 2)
        task(PROBLEM_SET, "Genetics problem set (crosses & pedigrees)", 8)
        task(EXAM, "Midterm self-exam", 7)
        task(ESSAY, "Explain one evolutionary adaptation in 1,000 words", 12)
        task(EXAM, "Final self-exam", 15)
    }
    course("220", "Cell Biology", credits = 4, prereqs = listOf("110", "CHEM 101")) {
        about = "How the cell works: membranes and transport, the cytoskeleton, protein sorting, signaling, the cell cycle and cell death."
        objectives(
            "Describe how proteins are sorted to organelles and secreted",
            "Explain the major cell-signaling pathways and their logic",
            "Describe the cytoskeleton and molecular motors",
            "Explain regulation of the cell cycle and how it fails in cancer",
            "Read and summarize a primary research paper",
        )
        units(
            "Cells & the tools to study them — microscopy, fractionation",
            "Membrane structure & transport",
            "Intracellular compartments & protein sorting",
            "Vesicular traffic — ER, Golgi, endocytosis",
            "Cell signaling — receptors, second messengers, kinases",
            "Cytoskeleton & molecular motors",
            "The cell cycle & its control",
            "Apoptosis",
            "Cell junctions & the extracellular matrix",
            "Stem cells & cancer",
        )
        textbook("Essential Cell Biology (5th ed.)", "Bruce Alberts et al.")
        textbook("Molecular Biology of the Cell", "Bruce Alberts et al.", required = false, notes = "The deeper reference.")
        lectures("iBiology lecture library", "iBiology", "https://www.ibiology.org/", notes = "Talks by the scientists who made the discoveries.")
        task(READING, "Summarize a primary research paper on protein trafficking", 5)
        task(PROBLEM_SET, "Signaling pathway diagrams", 8)
        task(EXAM, "Midterm self-exam", 8)
        task(PROJECT, "Illustrated poster: life cycle of a secreted protein", 12)
        task(EXAM, "Final self-exam", 15)
    }
    course("230", "Molecular Biology", credits = 4, prereqs = listOf("220")) {
        about = "The molecular machinery of heredity and gene expression — replication, transcription, translation, gene regulation and the lab methods that reveal them."
        objectives(
            "Explain DNA replication, repair and recombination at the molecular level",
            "Describe transcription and its regulation in prokaryotes and eukaryotes",
            "Explain RNA processing and translation",
            "Interpret classic molecular-biology experiments and modern techniques (PCR, sequencing, CRISPR)",
        )
        units(
            "DNA structure & chromatin",
            "DNA replication",
            "DNA repair & recombination",
            "Transcription in bacteria — the lac and trp operons",
            "Eukaryotic transcription & epigenetics",
            "RNA splicing & processing",
            "Translation & the genetic code",
            "Regulatory RNAs",
            "Molecular tools — cloning, PCR, sequencing",
            "Genome editing — CRISPR-Cas9",
        )
        textbook("Molecular Biology of the Gene (7th ed.)", "James D. Watson et al.")
        textbook("Lewin's GENES", "Jocelyn Krebs, Elliott Goldstein & Stephen Kilpatrick", required = false)
        book("The Double Helix", "James D. Watson", required = false, notes = "A first-person (and controversial) account of the discovery.")
        book("The Gene: An Intimate History", "Siddhartha Mukherjee", required = false)
        task(PROBLEM_SET, "Classic experiments: Meselson–Stahl, Hershey–Chase", 3)
        task(PROBLEM_SET, "Operon regulation problems", 5)
        task(EXAM, "Midterm self-exam", 8)
        task(ESSAY, "CRISPR: mechanism and ethics (1,500 words)", 14)
        task(EXAM, "Final self-exam", 15)
    }
    course("240", "Genetics & Genomics", credits = 3, prereqs = listOf("110")) {
        about = "Inheritance from Mendel to the genome: linkage, population genetics, human genetics and what we can read in our own DNA."
        objectives(
            "Solve problems in Mendelian, sex-linked and linked inheritance",
            "Apply Hardy–Weinberg and basic population genetics",
            "Explain how genomes are sequenced, assembled and analyzed",
        )
        units(
            "Mendel revisited", "Chromosomes & linkage mapping", "Mutation",
            "Population genetics", "Quantitative traits", "Human genetics & disease",
            "Genome sequencing", "Comparative genomics & evolution",
        )
        textbook("Introduction to Genetic Analysis", "Anthony Griffiths et al.")
        task(PROBLEM_SET, "Linkage mapping problem set", 4)
        task(PROBLEM_SET, "Hardy–Weinberg problem set", 7)
        task(EXAM, "Final self-exam", 15)
    }
    program("Biology", ProgramKind.MAJOR, "110", "220", "230", "240")
    program("Cellular & Molecular Biology", ProgramKind.CERTIFICATE, "220", "230", "240")
}

private fun chemistry() = department(
    "Chemistry", "CHEM", 5,
    "The chemistry that biology, medicine and engineering are built on.",
) {
    course("101", "General Chemistry", credits = 4) {
        about = "Atoms, bonding, reactions, stoichiometry, gases, thermochemistry and equilibrium — the toolkit for biology and engineering."
        objectives(
            "Balance equations and solve stoichiometry problems",
            "Predict molecular shape and polarity from Lewis structures",
            "Apply equilibrium and acid–base concepts quantitatively",
        )
        units(
            "Measurement & the mole", "Atomic structure & periodic trends", "Chemical bonding",
            "Molecular geometry", "Reactions & stoichiometry", "Gases", "Thermochemistry",
            "Solutions", "Chemical equilibrium", "Acids & bases",
        )
        textbook("Chemistry 2e", "OpenStax", "https://openstax.org/details/books/chemistry-2e", notes = "Free textbook.")
        lectures("Crash Course Chemistry", "CrashCourse (YouTube)", notes = "Short, fast episodes that cover the whole course.")
        task(PROBLEM_SET, "Stoichiometry problem set", 5)
        task(PROBLEM_SET, "Equilibrium problem set", 12)
        task(EXAM, "Final self-exam", 15)
    }
    course("230", "Biochemistry", credits = 4, prereqs = listOf("101", "BIOL 110")) {
        about = "The molecules of life in action: protein structure, enzyme kinetics, metabolism and its regulation."
        objectives(
            "Relate protein structure to function",
            "Analyze enzyme kinetics with Michaelis–Menten",
            "Trace glycolysis, the citric acid cycle and oxidative phosphorylation",
        )
        units(
            "Amino acids & proteins", "Protein folding & structure", "Enzymes & kinetics",
            "Carbohydrates & lipids", "Glycolysis", "Citric acid cycle",
            "Oxidative phosphorylation", "Metabolic regulation",
        )
        textbook("Lehninger Principles of Biochemistry", "David L. Nelson & Michael M. Cox")
        task(PROBLEM_SET, "Enzyme kinetics problems", 5)
        task(PROJECT, "Hand-drawn metabolic map", 10)
        task(EXAM, "Final self-exam", 15)
    }
    program("Chemistry", ProgramKind.MINOR, "101", "230")
}
