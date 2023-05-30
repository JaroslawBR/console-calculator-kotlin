package calculator

import java.util.*
import kotlin.math.pow
import kotlin.system.exitProcess


val variablesList = mutableMapOf<String, Double>() //holds the values of all variables

class SystemInfo{
    val textHelp = "Program performs mathematical calculations"
    val textUnknown = "Unknown command"
    val textIdentifier = "Invalid identifier"
    val textExpression = "Invalid expression"
    val textAssignment =  "Invalid assignment"
    val textVariables = "Unknown variable"
    val textExit = "Bye!"
}

class ExpressionCheck {
    private val regex1 = Regex("^[+\\-\\d\\s/*()^.]+$") // Checks if the string contains only numbers and/or "-", "+", "*", "/", "(", ")", and "."
    private val regex2 = Regex(".*\\d+\\)?$") // Checks if the last element contains a number
    private val regex3 = Regex("\\D*\\d+\\s*?([-+*/()^]+\\s*?([-+*/()^]*\\d+\\s*?\\)?)*\\s*?)+") // Checks if the string contains characters between numbers
    private val regex4 = Regex("\\D*\\s*\\d+\\)?") // Checks if the string contains non-digit characters before a number, optionally followed by ")"
    private val regexVariables1 = Regex("^[+\\-\\w\\s/*()^.]+$") // Checks if the string contains only letters, numbers, "-", "+", "*", "/", "(", ")", and "."
    private val regexVariables2 = Regex(".*\\w+\\)?\$") // Checks if the last element contains a letter
    private val regexVariables3 = Regex("\\W*\\w+\\s*?([-+*/()^]+\\s*?([-+*/()^]*\\w+\\s*?\\)?)*\\s*?)+") // Checks if the string contains characters other than letters between letters
    private val regexVariables4 = Regex("\\W*\\s*\\w+\\)?") // Checks if the string contains characters other than letters before a letter, optionally followed by ")"

    val exit = Regex("/exit")
    val help = Regex("/help")
    val unknownCommend = Regex("/\\w+")

    fun checkRegex(input: String): Boolean {
        return regex1.matches(input) && regex2.matches(input.trim()) && (regex3.matches(input) || regex4.matches(input))
    }

    fun checkRegexVariables(input: String): Boolean {
        return regexVariables1.matches(input) && regexVariables2.matches(input.trim()) && (regexVariables3.matches(input) || regexVariables4.matches(input))
    }
}

class Variables {

    private val isAVariables = Regex("[a-z, A-Z](w)*.*")
    private val regexNum1 = Regex("[a-z, A-Z]([a-z, A-Z])*\\s*?=\\s*?[-+]?\\d+\\s?+")
    val check = Regex("[a-z, A-Z]([a-z, A-Z])*")

    fun isAVariables(input: String):Boolean {
        return isAVariables.matches(input)
    }

    fun checkVariables(input: String):Boolean {
        return regexNum1.matches(input)
    }

    fun addNewVariables(input: String) {
        val newVariables = format(input)
        variablesList[newVariables[0]] = newVariables[1].toInt().toDouble()
    }

    private fun format(input: String): List<String>{
        return input.replace(" ", "").trim().split("=")
    }

    fun compare(input: String){  //function checks if the variable exists
        if (variablesList.containsKey(input.trim())) {
            println(variablesList[input.trim()]?.toInt())
        } else println(SystemInfo().textVariables)
    }

    fun variablesToVariables(input: String){ //function is used to assign values to new variables using already existing ones
        val  test = format(input)
        val secondVariables = test[1]
        if (variablesList.containsKey(secondVariables)) {
            val newInput = test[0]+"="+ variablesList[secondVariables]!!.toInt()
            addNewVariables(newInput)
        } else{
            if (test[0].contains(Regex("\\d+"))) { println(SystemInfo().textIdentifier) }
            else if (test[1].contains(Regex("[a-z,A-Z]"))) { println(SystemInfo().textAssignment) }
            else println(SystemInfo().textVariables)
        }
    }

    fun changeVariablesToValue(input: String) { //change all use variables to the values they represent
        val inputList = input.replace(Regex("[^a-zA-Z]"), " ").split(" ").map { it.trim() }.filter { it.isNotEmpty() }
        var inputChange = input
        for (i in inputList) {
            if (variablesList.containsKey(i)) {
                inputChange = inputChange.replace(Regex(i), variablesList[i]?.toInt().toString())
            } else {
                println(SystemInfo().textVariables)
                return
            }
        }
        Calculate(inputChange).calculate() //if the process ends successfully, the function proceeds to calculations
    }
}

class Calculate(private val calculateList: String) {

    private val outputQueue =
        mutableListOf<Any>() //stores convert math expression in  RPN (correct order of operations)
    private val precedence = mapOf('^' to 3, '*' to 2, '/' to 2, '+' to 1, '&' to 1) // "&" == "-"

    fun calculate() {
        val expression =
            "0" + calculateList.replace(" ", "").replace(Regex("-+")) { if (it.value.length % 2 == 0) "+" else "-" }
                .replace("-", "&").replace(Regex("\\++"), "+")
                .replace("+&", "&") // first input formatting simplification of expression.
        // "0" in start is you use to handle the case when the expression starts with a negative number.
        if (expression.contains(Regex("\\*{2,}")) || expression.contains(Regex("/{2,}"))) {
            println(SystemInfo().textExpression)
            return main()
        }
        convertToRPN(expression)
        val result = evaluateRPN()
        println(result.toInt())
    }

    private fun convertToRPN(expression: String) { //  the function puts the mathematical expression in the correct order store in outputQueue
        val operators = mutableListOf<Char>() //store operators
        val output = mutableListOf<Any>()

        fun hasHigherPrecedence(op1: Char, op2: Char): Boolean {  //compare 2 operators precedence
            return precedence[op1]!! >= precedence[op2]!!
        }

        var index = 0
        while (index < expression.length) {
            val char = expression[index]

            when {
                char.isDigit() -> {
                    val number = expression.substring(index).takeWhile { it.isDigit() || it == '.' }
                    output.add(number.toDouble())
                    index += number.length - 1
                }

                isOperator(char) -> {
                    while (operators.isNotEmpty() && operators.last() != '(' && hasHigherPrecedence(
                            operators.last(),
                            char
                        )
                    ) {
                        output.add(operators.removeLast())
                    }
                    operators.add(char)
                }

                char == '(' -> {
                    operators.add(char)
                }

                char == ')' -> {
                    while (operators.isNotEmpty() && operators.last() != '(') {
                        output.add(operators.removeLast())
                    }

                    if (operators.isEmpty() || operators.last() != '(') {
                        println(SystemInfo().textExpression)
                        return main()
                    }

                    operators.removeLast() // remove '(' from stack
                }
            }
            index++
        }

        while (operators.isNotEmpty()) {
            if (operators.last() == '(') {
                println(SystemInfo().textExpression)
                return main()
            }
            output.add(operators.removeLast())
        }

        outputQueue.addAll(output)
    }

    private fun evaluateRPN(): Double { //Function takes the operands from the outputQueue and pushes them onto a stack.
        // When a character is encountered, the values of "operand2" and "operand1" are popped from the stack, and then the program proceeds  mathOperation.
        val stack = Stack<Double>()
        for (element in outputQueue) {
            when (element) {
                is Double -> stack.push(element)
                is Char -> {
                    val operand2 = stack.pop()
                    val operand1 = stack.pop()
                    val result = mathOperation(element, operand1, operand2)
                    stack.push(result)
                }
            }
        }
        return stack.pop()
    }

    private fun mathOperation(operator: Char, operand1: Double, operand2: Double): Double {
        return when (operator) {
            '+' -> operand1 + operand2 // addition
            '&' -> operand1 - operand2  // subtraction
            '*' -> operand1 * operand2 // multiplication
            '/' -> operand1 / operand2 // division
            '^' -> operand1.pow(operand2) // power
            else -> {
                println(SystemInfo().textExpression)
                return 0.0
            }
        }
    }

    private fun isOperator(char: Char): Boolean {  //checks if char is an operator
        return char in setOf('+', '&', '*', '/', '^')
    }
}

fun main() {
    while (true) {
        val input = readlnOrNull()
        when {
            input.isNullOrEmpty() -> continue
            input.matches(ExpressionCheck().exit) -> {
                println(SystemInfo().textExit)
                exitProcess(0)
            }
            input.matches(ExpressionCheck().help) -> println(SystemInfo().textHelp)
            input.matches(Regex(".*=.*=.*")) -> println(SystemInfo().textAssignment)
            ExpressionCheck().checkRegex(input) -> Calculate(input).calculate()
            input.matches(Variables().check) -> Variables().compare(input)
            ExpressionCheck().checkRegexVariables(input) -> Variables().changeVariablesToValue(input)
            input.matches(ExpressionCheck().unknownCommend) -> println(SystemInfo().textUnknown)
            Variables().isAVariables(input) -> {
                when {
                    Variables().checkVariables(input) -> Variables().addNewVariables(input)
                    input.contains("=") -> Variables().variablesToVariables(input)
                    else -> println(SystemInfo().textIdentifier)
                }
            }
            else -> println(SystemInfo().textExpression)
        }
    }
}