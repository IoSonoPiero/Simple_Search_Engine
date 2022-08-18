package search

import java.io.File

enum class Strategy {
    ALL, ANY, NONE
}

// store people information
var people = mutableListOf<String>()

// store the index
val invertedIndex = mutableMapOf<String, MutableList<Int>>()

// start program
fun main(args: Array<String>) {
    parseInputData(args)
    createInvertedIndex(people)
    printMenu()
}

fun createInvertedIndex(people: MutableList<String>) {
    // for each line of the list of people extract single words

    // the key is the word
    // the values are the lines in the file where word are matched
    for (index in people.indices) {
        val listOfWords = people[index].split(" ").toMutableList()
        addWordToInvertedIndex(listOfWords, index)
    }
}

fun addWordToInvertedIndex(listOfWords: MutableList<String>, line: Int) {
    for (word in listOfWords) {
        // check if word is already in the index
        if (invertedIndex.contains(word.lowercase())) {
            // if yes, then add the line to the MutableList as new element of the List
            val anIntList: MutableList<Int>? = invertedIndex[word.lowercase()]
            if (anIntList != null) {
                anIntList += line + 1
                invertedIndex[word.lowercase()] = anIntList
            }
        } else {
            // if no, then add a new key to Map and a new Element to List
            val anIntList = mutableListOf(line + 1)
            invertedIndex[word.lowercase()] = anIntList
        }
    }
}

// parse input data file
fun parseInputData(args: Array<String>) {
    // if there are arguments from command line, try to parse them
    if (args.size == 2 && args[0] == "--data") {
        val fileRead = File(args[1])
        // add each row to our List of People
        if (fileRead.exists()) {
            fileRead.forEachLine { people.add(it) }
        }
    }
}

fun getAbsolutePath() {
    // get the working directory
    val workingDirectory = System.getProperty("user.dir")
    // determine separator from OS
    val separator = File.separator
    // return the absolute path to file
    println("$workingDirectory$separator$")
}

fun printMenu() {
    var choice: Int

    // loop until user choose exit
    do {
        println("\n=== Menu ===")
        println("1. Find a person")
        println("2. Print all people")
        println("0. Exit")
        choice = readln().toInt()
        when (choice) {
            // exit
            0 -> {
                println("Bye!")
                break
            }
            // find a person
            1 -> findPeople()

            // print contents of list
            2 -> printAllPeople()
            else -> println("\nIncorrect option! Try again.")
        }
    } while (true)
}

fun printAllPeople() {
    println("\n=== List of people ===")
    people.forEach { println(it) }
}

fun findPeople() {

    println("Select a matching strategy: ${Strategy.values().joinToString()}")
    val strategy = readln()

    // get the strategy
    val strategyType: Strategy?
    try {
        strategyType = Strategy.valueOf(strategy.uppercase())
    } catch (e: IllegalArgumentException) {
        return
    }

    // get the terms to search for
    println("\nEnter a name or email to search all matching people.")
    val listOfWord = readln().lowercase().split(" ").toList()

    // create a copy of the index to be filtered with search terms
    val filteredInvertedIndex: MutableMap<String, MutableList<Int>> = invertedIndex.toMutableMap()

    // filter the map with keys entered and print a message if result is empty
    filteredInvertedIndex.keys.retainAll(listOfWord.toSet())
    // no match found
    if (filteredInvertedIndex.isEmpty()) {
        println("No matching people found.")
        return
    }

    // get a list from map keys
    val listFromMapKeys: MutableList<String> = filteredInvertedIndex.keys.toMutableList()

    // create a set from the listFromMapKeys[0]
    var theSet: MutableSet<Int> = invertedIndex[listFromMapKeys[0]]!!.toMutableSet()

    for (index in listFromMapKeys.indices) {
        val aSet: MutableSet<Int> = invertedIndex[listFromMapKeys[index]]!!.toMutableSet()
        theSet = when (strategyType) {
            // uses intersections of sets
            Strategy.ALL -> (theSet.intersect(aSet)).toMutableSet()
            // uses unions of sets
            Strategy.ANY -> (theSet.union(aSet)).toMutableSet()
            // to obtain the opposite of unions of sets, first does the union
            Strategy.NONE -> (theSet.union(aSet)).toMutableSet()
        }
    }
    if (strategyType == Strategy.NONE) {
        // then subtract the all values from the subset
        val aSet: MutableSet<Int> = people.indices.map { (it + 1) }.toMutableSet()
        theSet = aSet.subtract(theSet).toMutableSet()
    }

    // print the people found
    val numberOfPeopleFound = theSet.size
    println("$numberOfPeopleFound person${if (numberOfPeopleFound > 1) "s" else ""} found:")

    // print the list of people found
    for (value in theSet) {
        val theRow = value - 1
        println(people[theRow])
    }
}