package com.example.data.quiz

import com.example.data.model.QuizQuestion

object BsebEnglishVviBank {
    private fun q(
        id: String,
        cls: Int,
        ch: String,
        text: String,
        a: String,
        b: String,
        c: String,
        d: String,
        ans: String,
        exp: String
    ) = QuizQuestion(
        id = id,
        questionClass = cls,
        subject = "English",
        chapter = ch,
        questionText = text,
        optionA = a,
        optionB = b,
        optionC = c,
        optionD = d,
        correctOption = ans,
        explanation = exp,
        source = "BSEB Class $cls English VVI"
    )

    val questions: List<QuizQuestion> = listOf(
        // --- CLASS 9 ENGLISH (10 VVI) ---
        q("b9_e1", 9, "Articles", "Fill in the blank: He is ___ honest man.", "a", "an", "the", "no article", "B", "'Honest' begins with a silent 'h' and a vowel sound, so 'an' is used."),
        q("b9_e2", 9, "Panorama Prose", "Who wrote 'Dharm Juddha' in Bihar Board Class 9 English?", "Arjun Dev Charan", "Mahadevi Verma", "Premchand", "R. K. Narayan", "A", "Arjun Dev Charan wrote the play 'Dharm Juddha'."),
        q("b9_e3", 9, "Tenses", "Choose the correct verb: The sun ___ in the east.", "rise", "rises", "rising", "rose", "B", "Universal truths take Simple Present Tense; singular subject 'The sun' takes 'rises'."),
        q("b9_e4", 9, "Prepositions", "Fill in the blank: The cat jumped ___ the table.", "in", "upon", "at", "by", "B", "Movement onto an elevated surface uses 'upon' or 'onto'."),
        q("b9_e5", 9, "Synonyms", "Choose the synonym of 'Brave':", "Coward", "Courageous", "Weak", "Lazy", "B", "'Courageous' means fearless or brave."),
        q("b9_e6", 9, "Antonyms", "Choose the antonym of 'Ancient':", "Old", "Modern", "Past", "Historic", "B", "The opposite of 'Ancient' (very old) is 'Modern'."),
        q("b9_e7", 9, "Pronouns", "Choose the correct pronoun: Neither Ram nor Shyam brought ___ book.", "their", "his", "her", "them", "B", "Singular subjects joined by 'neither...nor' take a singular pronoun 'his'."),
        q("b9_e8", 9, "Modals", "___ I come in, Sir? (Formal permission)", "Must", "May", "Should", "Will", "B", "'May' is used for polite/formal permission."),
        q("b9_e9", 9, "Spelling", "Choose the correctly spelt word:", "Recieve", "Receive", "Receeve", "Recive", "B", "Following 'i before e except after c', the correct spelling is 'Receive'."),
        q("b9_e10", 9, "Panorama Poetry", "Who wrote 'The Grandmother' poem in Class 9 Panorama?", "Ray Young Bear", "W. B. Yeats", "John Keats", "Sarojini Naidu", "A", "'The Grandmother' is written by Ray Young Bear."),

        // --- CLASS 10 ENGLISH (10 VVI) ---
        q("b10_e1", 10, "Grammar – Number", "What is the plural form of 'Child'?", "Childs", "Children", "Childes", "Childrens", "B", "'Child' takes the irregular plural form 'Children'."),
        q("b10_e2", 10, "Active & Passive Voice", "Passive Voice of: 'Ram writes a letter.'", "A letter is written by Ram.", "A letter was written by Ram.", "A letter has written by Ram.", "Ram is writing a letter.", "A", "Simple Present converts to 'is/are + V3 + by'."),
        q("b10_e3", 10, "Panorama Prose", "Who is the author of 'The Pace for Living'?", "R. C. Hutchinson", "Mahadevi Verma", "Leo Tolstoy", "Satyajit Ray", "A", "'The Pace for Living' is an essay by R. C. Hutchinson."),
        q("b10_e4", 10, "Panorama Prose", "In 'Gillu', what kind of creature was Gillu?", "A parrot", "A tiny squirrel", "A kitten", "A rabbit", "B", "Mahadevi Verma rescued an injured baby squirrel named Gillu."),
        q("b10_e5", 10, "Panorama Prose", "Who is the Nobel Prize winner who wrote 'Acceptance Speech'?", "Aung San Suu Kyi", "Toni Morrison", "Malala Yousafzai", "Mother Teresa", "A", "Aung San Suu Kyi's son Alexander Aris delivered her Nobel Acceptance Speech."),
        q("b10_e6", 10, "Panorama Poetry", "Who wrote the poem 'God Made the Country'?", "William Cowper", "Alexander Pope", "William Wordsworth", "Durga Prasad Panda", "A", "'God Made the Country, and Man Made the Town' was written by William Cowper."),
        q("b10_e7", 10, "Prepositions", "He died ___ cholera last month.", "from", "of", "by", "with", "B", "Death caused by a disease takes the preposition 'of' ('died of cholera')."),
        q("b10_e8", 10, "Narration", "Indirect speech: He said, 'I am busy.'", "He said that he is busy.", "He said that he was busy.", "He says he was busy.", "He told he is busy.", "B", "Present 'am' shifts to past 'was' when reporting verb 'said' is in past."),
        q("b10_e9", 10, "Articles", "The Ganga is ___ sacred river.", "a", "an", "the", "no article", "A", "'Sacred river' starts with a consonant sound, so 'a' is used."),
        q("b10_e10", 10, "Subject-Verb", "Slow and steady ___ the race.", "win", "wins", "winning", "won", "B", "'Slow and steady' expresses a single unified idea, so it takes singular 'wins'."),

        // --- CLASS 11 ENGLISH (10 VVI) ---
        q("b11_e1", 11, "Prepositions", "She is fond ___ classical music.", "on", "in", "of", "at", "C", "The adjective 'fond' always takes the preposition 'of'."),
        q("b11_e2", 11, "Rainbow Part I", "Who wrote 'Animals in Prison' in Class 11 Rainbow?", "Mahatma Gandhi", "Jawaharlal Nehru", "Dr. Rajendra Prasad", "Mulkh Raj Anand", "B", "Pandit Jawaharlal Nehru wrote 'Animals in Prison' while in Dehradun jail."),
        q("b11_e3", 11, "Rainbow Part I", "Who wrote 'Nalanda: Ancient Seat of Learning'?", "Dr. Rajendra Prasad", "Dr. Zakir Hussain", "R. K. Narayan", "Ruskin Bond", "A", "India's first President Dr. Rajendra Prasad wrote this prose piece."),
        q("b11_e4", 11, "Tenses", "If it ___, we shall stay at home.", "rained", "rains", "will rain", "raining", "B", "First conditional uses Simple Present in the 'if' clause."),
        q("b11_e5", 11, "Voice", "Passive Voice of: 'Open the door.'", "Let the door be opened.", "The door is opened.", "Let open the door.", "You open the door.", "A", "Imperative commands convert to 'Let + object + be + V3'."),
        q("b11_e6", 11, "One-Word Substitution", "A person who knows many languages is called:", "Illiterate", "Polyglot", "Orator", "Optimist", "B", "A polyglot is someone fluent in several languages."),
        q("b11_e7", 11, "Idioms", "The idiom 'To burn the midnight oil' means:", "To waste fuel", "To study or work late into the night", "To sleep early", "To start a fire", "B", "It means working or studying hard late at night."),
        q("b11_e8", 11, "Articles", "He plays ___ harmonium very well.", "a", "an", "the", "no article", "C", "Musical instruments take the definite article 'the' when played."),
        q("b11_e9", 11, "Grammar", "Choose the correct auxiliary: You ___ obey your parents.", "ought to", "might", "can", "dare", "A", "'Ought to' expresses moral duty and obligation."),
        q("b11_e10", 11, "Rainbow Poetry", "Who wrote 'Where the Mind is Without Fear'?", "Rabindranath Tagore", "Sarojini Naidu", "Toru Dutt", "Nissim Ezekiel", "A", "Nobel Laureate Rabindranath Tagore wrote this iconic poem in Gitanjali."),

        // --- CLASS 12 ENGLISH (10 VVI) ---
        q("b12_e1", 12, "Rainbow Part II", "Who wrote the essay 'Indian Civilization and Culture'?", "Dr. Zakir Hussain", "Mahatma Gandhi", "Jawaharlal Nehru", "Bertrand Russell", "B", "Mahatma Gandhi wrote 'Indian Civilization and Culture'."),
        q("b12_e2", 12, "Rainbow Part II", "Who delivered the speech 'Bharat is My Home'?", "Mahatma Gandhi", "Dr. Zakir Hussain", "Manohar Malgaonkar", "Shiga Naoya", "B", "Dr. Zakir Hussain delivered 'Bharat is My Home' as President in 1967."),
        q("b12_e3", 12, "Rainbow Part II", "Who is the main funny uncle character in 'A Pinch of Snuff'?", "Nanukaka", "Sohanlal", "Ratiram", "Barkat Ali", "A", "Manohar Malgaonkar's 'A Pinch of Snuff' revolves around Nanukaka."),
        q("b12_e4", 12, "Rainbow Part II", "Who delivered the famous speech 'I Have a Dream'?", "Abraham Lincoln", "Martin Luther King Jr.", "Nelson Mandela", "Bertrand Russell", "B", "Martin Luther King Jr. delivered 'I Have a Dream' in 1963."),
        q("b12_e5", 12, "Rainbow Part II", "Who is the author of 'Ideas That Have Helped Mankind'?", "Bertrand Russell", "Pearl S. Buck", "Germaine Greer", "H. E. Bates", "A", "British philosopher Bertrand Russell wrote 'Ideas That Have Helped Mankind'."),
        q("b12_e6", 12, "Rainbow Part II", "'The Artist' is a short story written by:", "Shiga Naoya", "Anton Chekhov", "Dorothy L. Sayers", "Pearl S. Buck", "A", "Japanese writer Shiga Naoya wrote 'The Artist' (about Seibei and gourds)."),
        q("b12_e7", 12, "Rainbow Poetry", "Who wrote the poem 'Sweetest Love, I Do Not Goe'?", "John Donne", "Walt Whitman", "W. H. Auden", "John Keats", "A", "Metaphysical poet John Donne wrote 'Sweetest Love, I Do Not Goe'."),
        q("b12_e8", 12, "Rainbow Poetry", "Who wrote 'Song of Myself'?", "Walt Whitman", "T. S. Eliot", "Rupert Brooke", "Kamala Das", "A", "American poet Walt Whitman wrote 'Song of Myself'."),
        q("b12_e9", 12, "Rainbow Poetry", "'Ode to Autumn' is composed by:", "John Keats", "D. H. Lawrence", "Walter de la Mare", "Keki N. Daruwalla", "A", "Romantic poet John Keats wrote 'To Autumn' / 'Ode to Autumn'."),
        q("b12_e10", 12, "Rainbow Poetry", "Who wrote 'The Soldier', a famous World War I sonnet?", "Rupert Brooke", "W. H. Auden", "T. S. Eliot", "John Donne", "A", "Rupert Brooke wrote the patriotic sonnet 'The Soldier'.")
    )
}
