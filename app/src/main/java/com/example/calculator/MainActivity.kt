package com.example.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.ui.theme.robotomonoFontFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Calculator()
            }
        }
    }
}


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator(modifier: Modifier = Modifier) {
    // Variables
    var calculatorInput by remember { mutableStateOf(mutableListOf<String>()) }
    var calculatorOutput by remember { mutableStateOf("") }
    var currentAnswer by remember { mutableStateOf("0") }
    var isEqualsButtonClicked by remember { mutableStateOf(false) }
    val isBlack = remember { mutableStateOf(true) }
    var calculatorInputIndex by remember { mutableIntStateOf(0) }
    var isShiftButtonClicked by remember { mutableStateOf(false) }
    var isAnglesUnitsRadians by remember { mutableStateOf(true) }
    val calculatorInputAndOutputPairs by remember { mutableStateOf(mutableListOf<Pair<MutableList<String>, String>>()) }
//    calculatorInputAndOutputPairs.add(Pair(mutableListOf(""), "0")) // Assume first Input is empty and first Output is "0"

    // Function to change color of calculatorInputIndexLine
    LaunchedEffect(calculatorInputIndex) {
        while (true) {
            isBlack.value = true
            delay(750)
            isBlack.value = false
            delay(250)
        }
    }

    
    // Functions for onClick parameter of different buttons
    fun onClickForNumbers(number: String) {
        if (isEqualsButtonClicked) {
            calculatorInput = mutableListOf(number)
            calculatorInputIndex = 1
            isEqualsButtonClicked = false
        }
        else {
            calculatorInput.add(calculatorInputIndex, number)
            calculatorInputIndex++
        }
    }
    fun onClickForOperators(operator: String) {
        if (isEqualsButtonClicked && calculatorOutput.isNotEmpty()) {
            calculatorInput = mutableListOf("Ans", operator)
            calculatorInputIndex = 2
        }
        else {
            calculatorInput.add(calculatorInputIndex, operator)
            calculatorInputIndex++
        }
        isEqualsButtonClicked = false
    }

    fun onClickForFunctions(function: String) {
        if (isEqualsButtonClicked && calculatorOutput.isNotEmpty()) {
            calculatorInput = mutableListOf(function)
            calculatorInputIndex = 1
        }
        else {
            calculatorInput.add(calculatorInputIndex, function)
            calculatorInputIndex++
        }
        isEqualsButtonClicked = false
    }

    fun onClickForPowerFunctions(power: String) {
        if (isEqualsButtonClicked && calculatorOutput.isNotEmpty()) {
            if (power.isEmpty()) {
                calculatorInput = mutableListOf("Ans", "^(")
                calculatorInputIndex = 2
            }
            else if (power == "-1") {
                calculatorInput = mutableListOf("Ans", "^(", "-", "1", ")")
                calculatorInputIndex = 5
            }
            else {
                calculatorInput = mutableListOf("Ans", "^(", power, ")")
                calculatorInputIndex = 4
            }
        }
        else {
            if (power.isEmpty()) {
                calculatorInput.add(calculatorInputIndex, "^(")
                calculatorInputIndex++
            }
            else if (power == "-1") {
                calculatorInput.addAll(calculatorInputIndex, mutableListOf("^(", "-", "1", ")"))
                calculatorInputIndex += 4
            }
            else {
                calculatorInput.addAll(calculatorInputIndex, mutableListOf("^(", power, ")"))
                calculatorInputIndex += 3
            }
        }
        isEqualsButtonClicked = false
    }


    // Function to evaluate calculatorInput expression
    fun evaluateExpression(expression: MutableList<String>): Double {

        var result = 0.0
        var openParenthesisIndex: Int
        var closeParenthesisIndex: Int
        var index = 0
        val specialStrings = listOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "log10", "ln", "√", "∛", "Abs",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh")

        while (index < expression.size) {

            if (expression[index] in specialStrings) {
                openParenthesisIndex = index + 1
                closeParenthesisIndex = findCloseParenthesisIndex(expression, openParenthesisIndex)
                val expressionSubList = expression.subList(openParenthesisIndex + 1, closeParenthesisIndex)

                val radDegConstant = if (isAnglesUnitsRadians) 1.0 else (Math.PI / 180.0)

                when (expression[index]) {
                    "sin" -> result = sin(evaluateExpression(expressionSubList) * radDegConstant)
                    "cos" -> result = cos(evaluateExpression(expressionSubList) * radDegConstant)
                    "tan" -> result = tan(evaluateExpression(expressionSubList) * radDegConstant)
                    "asin" -> result = asin(evaluateExpression(expressionSubList)) / radDegConstant
                    "acos" -> result = acos(evaluateExpression(expressionSubList)) / radDegConstant
                    "atan" -> result = atan(evaluateExpression(expressionSubList)) / radDegConstant
                    "log10" -> result = log10(evaluateExpression(expressionSubList))
                    "ln" -> result = ln(evaluateExpression(expressionSubList))
                    "√" -> result = sqrt(evaluateExpression(expressionSubList))
                    "∛" -> result = cbrt(evaluateExpression(expressionSubList))
                    "Abs" -> result = abs(evaluateExpression(expressionSubList))
                    "sinh" -> result = sinh(evaluateExpression(expressionSubList))
                    "cosh" -> result = cosh(evaluateExpression(expressionSubList))
                    "tanh" -> result = tanh(evaluateExpression(expressionSubList))
                    "asinh" -> result = asinh(evaluateExpression(expressionSubList))
                    "acosh" -> result = acosh(evaluateExpression(expressionSubList))
                    "atanh" -> result = atanh(evaluateExpression(expressionSubList))
                }
                index = closeParenthesisIndex
            }
            // Starting with a (
            else if (expression[index] == "(") {
                openParenthesisIndex = index
                closeParenthesisIndex = findCloseParenthesisIndex(expression, openParenthesisIndex)
                val expressionSubList = expression.subList(openParenthesisIndex + 1, closeParenthesisIndex)
                result = evaluateExpression(expressionSubList)
                index = closeParenthesisIndex
            }
            else if (expression[index] in listOf("+", "-", "*", "/", "^")) {
                if (expression[index + 1] == "(") {
                    openParenthesisIndex = index + 1
                    closeParenthesisIndex = findCloseParenthesisIndex(expression, openParenthesisIndex)
                    val expressionSubList = expression.subList(openParenthesisIndex + 1, closeParenthesisIndex)

                    when (expression[index]) {
                        "+" -> result += evaluateExpression(expressionSubList)
                        "-" -> result -= evaluateExpression(expressionSubList)
                        "*" -> result *= evaluateExpression(expressionSubList)
                        "/" -> result /= evaluateExpression(expressionSubList)
                        "^" -> result = customPow(result, evaluateExpression(expressionSubList))
                    }
                    index = closeParenthesisIndex
                }
                else if (expression[index + 1] in specialStrings) {
                    openParenthesisIndex = index + 2
                    closeParenthesisIndex = findCloseParenthesisIndex(expression, openParenthesisIndex)
                    val expressionSubList = expression.subList(openParenthesisIndex - 1, closeParenthesisIndex)

                    when (expression[index]) {
                        "+" -> result += evaluateExpression(expressionSubList)
                        "-" -> result -= evaluateExpression(expressionSubList)
                        "*" -> result *= evaluateExpression(expressionSubList)
                        "/" -> result /= evaluateExpression(expressionSubList)
                        "^" -> result = customPow(result, evaluateExpression(expressionSubList))
                    }
                    index = closeParenthesisIndex
                }
                else {
                    when (expression[index]) {
                        "+" -> result += evaluateExpression(mutableListOf(expression[index + 1]))
                        "-" -> result -= evaluateExpression(mutableListOf(expression[index + 1]))
                        "*" -> result *= evaluateExpression(mutableListOf(expression[index + 1]))
                        "/" -> result /= evaluateExpression(mutableListOf(expression[index + 1]))
                        "^" -> result = customPow(result, evaluateExpression(mutableListOf(expression[index + 1])))
                    }
                    index++
                }
            }
            else if (expression[index] == "!") {
                result = factorial(result)
            }
            else if (expression[index].toDoubleOrNull() != null) {
                result = expression[index].toDouble()
            }
            else {
                throw IllegalArgumentException("FAIL!")
            }
            index++
        }

        return result
    }

    fun evaluate(expression: MutableList<String>): String {
        try {
            val result = evaluateExpression(expression)
            return convertEvaluatedExpressionToString(result)
        } catch (e: Exception) {
            return "ERROR"
        }
    }



    Scaffold(modifier = modifier.fillMaxSize(),
        // TopAppBar
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "My Calculator")
                }
            )
        }) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .background(Color.LightGray)
        ) {
            // Calculator screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .padding(16.dp)
                    .background(Color.White)
            ) {
                // Syntax or Math error
                if (isEqualsButtonClicked && calculatorOutput in listOf("Syntax ERROR", "Math ERROR")) {
                    Column {
                        Row {
                            Text(
                                text = calculatorOutput,
                                style = TextStyle(
                                    fontSize = 20.sp
                                )
                            )
                        }
                        Row {
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = 8.sp
                                )
                            )
                        }
                        Row {
                            Text(
                                text = "[AC]  :Cancel",
                                fontFamily = robotomonoFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal,
                                fontSize = 20.sp
                            )
                        }
                        Row {
                            Text(
                                text = "[<][>]:Goto",
                                fontFamily = robotomonoFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                else {
                    Row (
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // calculatorInput is not empty
                        if (calculatorInput.isNotEmpty()) {
                            val startIndex = 0
                            val endIndex = calculatorInput.size - 1

                            for (index in startIndex..endIndex) {
                                val item = calculatorInput[index]
                                Box(
                                    modifier = Modifier
                                        .drawWithContent {
                                            drawContent()
                                            if (index == 0 && calculatorInputIndex == 0) {
                                                drawLine(
                                                    color = if (isBlack.value && !isEqualsButtonClicked) Color.Black else Color.White,
                                                    start = Offset(1.dp.toPx(), 0f),
                                                    end = Offset(1.dp.toPx(), size.height),
                                                    strokeWidth = 2.dp.toPx(),
                                                )
                                            }
                                            else if (index + 1 == calculatorInputIndex) {
                                                drawLine(
                                                    color = if (isBlack.value && !isEqualsButtonClicked) Color.Black else Color.White,
                                                    start = Offset(size.width - 1.dp.toPx(), 0f),
                                                    end = Offset(size.width - 1.dp.toPx(), size.height),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }
                                        }
                                ) {
                                    Text(
                                        text = item,
                                        fontFamily = robotomonoFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }
                        // calculatorInput is empty
                        else {
                            Box(
                                modifier = Modifier
                                    .drawWithContent {
                                        drawContent()
                                        drawLine(
                                            color = if (isBlack.value) Color.Black else Color.White,
                                            start = Offset(1.dp.toPx(), 0f),
                                            end = Offset(1.dp.toPx(), size.height),
                                            strokeWidth = 2.dp.toPx(),
                                        )
                                    }
                            ) {
                                Text(
                                    text = " ",
                                    fontFamily = robotomonoFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }
                    // Degrees or radians
                    Text(
                        text = if (isAnglesUnitsRadians) "Rad" else "Deg",
                        color = Color.Gray,
                        fontFamily = robotomonoFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(alignment = Alignment.BottomStart)
                    )
                    // calculatorOutput
                    Text(
                        text = calculatorOutput,
                        color = if (isEqualsButtonClicked) Color(color = 0xFF008000) else Color.Black,
                        fontFamily = robotomonoFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(alignment = Alignment.BottomEnd)
                    )
                }
            }
            // Calculator top key pad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SmallBlueSquareCalculatorButton(text = "<", onClick = {
                        if (isEqualsButtonClicked) {
                            if (calculatorOutput in listOf("Syntax ERROR", "Math ERROR")) {
                                calculatorOutput = ""
                            }
                            else {
                                calculatorInputIndex = calculatorInput.size
                            }
                        }
                        else {
                            calculatorInputIndex--
                            if (calculatorInputIndex < 0) {
                                calculatorInputIndex = calculatorInput.size
                            }
                        }
                        isEqualsButtonClicked = false
                    })
                    Spacer(modifier = Modifier.width(32.dp))
                    SmallBlueSquareCalculatorButton(text = ">", onClick = {
                        if (isEqualsButtonClicked) {
                            if (calculatorOutput in listOf("Syntax ERROR", "Math ERROR")) {
                                calculatorOutput = ""
                            }
                            else {
                                calculatorInputIndex = 0
                            }
                        }
                        else {
                            calculatorInputIndex++
                            if (calculatorInputIndex > calculatorInput.size) {
                                calculatorInputIndex = 0
                            }
                        }
                        isEqualsButtonClicked = false
                    })
                }
            }

            // Calculator middle key pad (small buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallColorChangingCalculatorButton(text = "Shift", onClick = {
                    isShiftButtonClicked = !isShiftButtonClicked
                }, isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "Deg", onClick = {
                    isAnglesUnitsRadians = isShiftButtonClicked
                }, shiftText = "Rad", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "(", onClick = { onClickForFunctions(function = "(") })
                SmallGrayCalculatorButton(text = ")", onClick = { onClickForFunctions(function = ")") })
                SmallGrayCalculatorButton(text = "e", onClick = { onClickForNumbers(number = "e") })
                SmallGrayCalculatorButton(text = "π", onClick = { onClickForNumbers(number = "π") })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallGrayCalculatorButton(text = "x^-1", onClick = { onClickForPowerFunctions(power = "-1") })
                SmallGrayCalculatorButton(text = "x!", onClick = { onClickForFunctions(function = "!") })
                SmallGrayCalculatorButton(text = "Abs", onClick = { onClickForFunctions(function = "Abs(") })
                SmallGrayCalculatorButton(text = "log10", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "log10(")
                    }
                    else {
                        onClickForFunctions(function = "*10^(")
                    }},
                    shiftText = "x10^", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "ln", onClick = { 
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "ln(")
                    }
                    else {
                        onClickForFunctions(function = "e^(")
                    }},
                    shiftText = "e^", isShiftButtonClicked = isShiftButtonClicked)
                SmallRedCalculatorButton(text = "log_", onClick = {  })
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallGrayCalculatorButton(text = "x^2", onClick = { onClickForPowerFunctions(power = "2") })
                SmallGrayCalculatorButton(text = "x^3", onClick = { onClickForPowerFunctions(power = "3") })
                SmallGrayCalculatorButton(text = "x^", onClick = { onClickForPowerFunctions(power = "") })
                SmallGrayCalculatorButton(text = "sin", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "sin(")
                    }
                    else {
                        onClickForFunctions(function = "asin(")
                    }},
                    shiftText = "asin", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "cos", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "cos(")
                    }
                    else {
                        onClickForFunctions(function = "acos(")
                    }},
                    shiftText = "acos", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "tan", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "tan(")
                    }
                    else {
                        onClickForFunctions(function = "atan(")
                    }},
                    shiftText = "atan", isShiftButtonClicked = isShiftButtonClicked)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallGrayCalculatorButton(text = "√x", onClick = { onClickForFunctions(function = "√(") })
                SmallGrayCalculatorButton(text = "∛x", onClick = { onClickForFunctions(function = "∛(") })
                SmallRedCalculatorButton(text = "^√x", onClick = {  })
                SmallGrayCalculatorButton(text = "sinh", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "sinh(")
                    }
                    else {
                        onClickForFunctions(function = "asinh(")
                    }},
                    shiftText = "asinh", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "cosh", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "cosh(")
                    }
                    else {
                        onClickForFunctions(function = "acosh(")
                    }},
                    shiftText = "acosh", isShiftButtonClicked = isShiftButtonClicked)
                SmallGrayCalculatorButton(text = "tanh", onClick = {
                    if (!isShiftButtonClicked) {
                        onClickForFunctions(function = "tanh(")
                    }
                    else {
                        onClickForFunctions(function = "atanh(")
                    }},
                    shiftText = "atanh", isShiftButtonClicked = isShiftButtonClicked)
            }
            // Calculator bottom key pad (big buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeBlackCalculatorButton(text = "7", onClick = { onClickForNumbers(number = "7") })
                LargeBlackCalculatorButton(text = "8", onClick = { onClickForNumbers(number = "8") })
                LargeBlackCalculatorButton(text = "9", onClick = { onClickForNumbers(number = "9") })
                LargeOrangeCalculatorButton(text = "DEL", onClick = {
                    if (calculatorInput.isNotEmpty() && !isEqualsButtonClicked) {
                        // Delete last char
                        if (calculatorInputIndex == calculatorInput.size) {
                            calculatorInput.removeAt(calculatorInput.lastIndex)
                            calculatorInputIndex--
                        }
                        // Delete to the left
                        else if (calculatorInputIndex >= 1 ) {
                            calculatorInput.removeAt(index = calculatorInputIndex - 1)
                            calculatorInputIndex--
                        }
                        // Delete first char as calculatorInputIndex == 0, presumably
                        else {
                            calculatorInput = calculatorInput.drop(1).toMutableList()
                        }
                    }
                })
                LargeOrangeCalculatorButton(text = "AC", onClick = {
                    calculatorInput = mutableListOf()
                    calculatorInputIndex = 0
                    calculatorOutput = ""
                    isEqualsButtonClicked = false
                })

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeBlackCalculatorButton(text = "4", onClick = { onClickForNumbers(number = "4") })
                LargeBlackCalculatorButton(text = "5", onClick = { onClickForNumbers(number = "5") })
                LargeBlackCalculatorButton(text = "6", onClick = { onClickForNumbers(number = "6") })
                LargeBlackCalculatorButton(text = "*", onClick = { onClickForOperators(operator = "*") })
                LargeBlackCalculatorButton(text = "/", onClick = { onClickForOperators(operator = "/") })
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeBlackCalculatorButton(text = "1", onClick = { onClickForNumbers(number = "1") })
                LargeBlackCalculatorButton(text = "2", onClick = { onClickForNumbers(number = "2") })
                LargeBlackCalculatorButton(text = "3", onClick = { onClickForNumbers(number = "3") })
                LargeBlackCalculatorButton(text = "+", onClick = { onClickForOperators(operator = "+") })
                LargeBlackCalculatorButton(text = "-", onClick = { onClickForOperators(operator = "-") })
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeBlackCalculatorButton(text = "0", onClick = { onClickForNumbers(number = "0") })
                LargeBlackCalculatorButton(text = ".", onClick = { onClickForNumbers(number = ".") })
                LargeBlackCalculatorButton(text = "Rand", onClick = {
                    // choose 5 decimal places
                    val randomValue = "%.5f".format(Random.nextDouble()).toList().map { it.toString() }.toMutableList()
                    if (isEqualsButtonClicked) {
                        calculatorInput = randomValue
                        calculatorInputIndex = 7
                        isEqualsButtonClicked = false
                    }
                    else {
                        calculatorInput.addAll(calculatorInputIndex, randomValue)
                        calculatorInputIndex += 7 // 5 decimal places and "0" and "."
                    }
                })
                LargeBlackCalculatorButton(text = "Ans", onClick = { onClickForNumbers(number = "Ans") })
                LargeBlackCalculatorButton(text = "=", onClick = {
                    if (calculatorInput.isNotEmpty()) {
                        isEqualsButtonClicked = true

                        val calculatorInputToEvaluate = calculatorInput.toMutableList()
                        val invalidIndex = findIndexWhenExpressionIsInvalid(calculatorInputToEvaluate)

                        // If invalidIndex does not equal -1, then the input is INVALID
                        if (invalidIndex != -1) {
                            calculatorInputIndex = invalidIndex + 1
                            calculatorOutput = "Syntax ERROR"
                        }
                        else {
                            separateMultiplyPowerFunctions(calculatorInputToEvaluate)
                            separateFunctionsAndBrackets(calculatorInputToEvaluate)
                            combineNumbers(calculatorInputToEvaluate)
                            insertImplicitMultiplication(calculatorInputToEvaluate)
                            insertImplicitZeroes(calculatorInputToEvaluate)
                            replaceConstants(calculatorInputToEvaluate, currentAnswer)
                            balanceParentheses(calculatorInputToEvaluate)

                            insertExtraParenthesisForOperator(calculatorInputToEvaluate, operator = "^")
                            insertExtraParenthesisForOperator(calculatorInputToEvaluate, operator = "!")
                            insertExtraParenthesisForOperator(calculatorInputToEvaluate, operator = "/")
                            insertExtraParenthesisForOperator(calculatorInputToEvaluate, operator = "*")

                            val evaluatedCalculatorInput = evaluate(calculatorInputToEvaluate)
                            if (evaluatedCalculatorInput in listOf("Infinity", "-Infinity", "NaN")) {
                                calculatorInputIndex = calculatorInput.size
                                calculatorOutput = "Math ERROR"
                            }
                            else {
                                calculatorOutput = evaluatedCalculatorInput
                                calculatorInputAndOutputPairs.add(Pair(calculatorInput, calculatorOutput))
                                currentAnswer = calculatorOutput
                            }
                        }
                    }
                })
            }
        }
    }
}


fun findIndexWhenExpressionIsInvalid(expression: MutableList<String>): Int {

    var currentNumber = ""

    var openParenthesesCount = 0
    var closeParenthesesCount = 0

    var currentItemGroup: Int
    var previousItemGroup = 0

    expression.forEachIndexed { index, item ->

        if (item in listOf("+", "-")) {
            currentItemGroup = 0
        }
        else if (item in listOf("*", "/", "^(", "*10^(")) {
            currentItemGroup = 1

            if (item in listOf("^(", "*10^(")) {
                openParenthesesCount++
            }
        }
        else if (item.matches(Regex("[0-9.]"))) {
            currentItemGroup = 2
            currentNumber += item
        }
        else if (item.contains("(")) {
            currentItemGroup = 3
            openParenthesesCount++
        }
        else if (item == ")") {
            currentItemGroup = 4
            closeParenthesesCount++
        }
        else if (item in listOf("Ans", "π", "e")) {
            currentItemGroup = 5
        }
        else if (item == "!") {
            currentItemGroup = 6
        }
        else {
            println("$index: $item")
            throw IllegalArgumentException("WHAT THE HELL???")
        }
        if (!item.matches(Regex("[0-9.]"))) {
            currentNumber = ""
        }


        // Check if number has more than one decimal place
        if (currentNumber.count { it == '.' } > 1) {
            return(index)
        }

        // Check if parentheses are valid
        if (closeParenthesesCount > openParenthesesCount) {
            return(index)
        }


        // Check first element of list is valid
        if (index == 0) {
            if (currentItemGroup !in listOf(0, 2, 3, 5)) {
                return(index)
            }
        }
        // Check elements in the middle of the list
        if (index > 0 && index <= expression.size - 1) {
            if (previousItemGroup in listOf(0, 1, 3) && currentItemGroup in listOf(1, 4, 6)) {
                return(index)
            }
        }
        // Check last element of list is valid
        if (index == expression.size - 1) {
            if (currentItemGroup !in listOf(2, 4, 5, 6)) {
                return(index)
            }
        }
        previousItemGroup = currentItemGroup
    }
    return(-1)
}




fun separateMultiplyPowerFunctions(expression: MutableList<String>) {
    val newList = mutableListOf<String>()
    expression.forEach { item ->
        when (item) {
            "*10^(" -> {
                newList.add("*")
                newList.add("1")
                newList.add("0")
                newList.add("^")
                newList.add("(")
            }
            "e^(" -> {
                newList.add("e")
                newList.add("^")
                newList.add("(")
            }
            else -> {
                newList.add(item)
            }
        }
    }
    expression.clear()
    expression.addAll(newList)
}


fun separateFunctionsAndBrackets(expression: MutableList<String>) {
    val newList = mutableListOf<String>()
    expression.forEach { item ->
        if (item.contains("(") && item.length > 1) {
            val word = item.substringBefore("(")
            val bracket = "("
            newList.add(word)
            newList.add(bracket)
        } else {
            newList.add(item)
        }
    }
    expression.clear()
    expression.addAll(newList)
}


fun combineNumbers(expression: MutableList<String>) {
    val newList = mutableListOf<String>()
    var currentNumber = ""

    expression.forEach { item ->
        if (item.matches(Regex("[0-9.]"))) {
            currentNumber += item
        } else {
            if (currentNumber.isNotEmpty()) {
                // A single decimal is the same as 0
                if (currentNumber == ".") {
                    currentNumber = "0"
                }
                newList.add(currentNumber)
                currentNumber = ""
            }
            newList.add(item)
        }
    }

    if (currentNumber.isNotEmpty()) {
        // A single decimal is the same as 0
        if (currentNumber == ".") {
            currentNumber = "0"
        }
        newList.add(currentNumber)
    }

    expression.clear()
    expression.addAll(newList)
}

fun insertImplicitMultiplication(expression: MutableList<String>) {
    var i = 0
    val specialStrings = listOf("(", "Ans", "π", "e",
        "sin", "cos", "tan", "asin", "acos", "atan",
        "log10", "ln", "√", "∛", "Abs",
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh")

    while (i < expression.size - 1) {
        val current = expression[i]
        val next = expression[i + 1]
        if ((current.toDoubleOrNull() != null) && next in specialStrings) {
            expression.add(i + 1, "*")
        }
        else if (current in listOf(")", "Ans", "π", "e", "!") && (next in specialStrings || next.matches(Regex("[0-9.]")))) {
            expression.add(i + 1, "*")
        }
        i++
    }
}


fun insertImplicitZeroes(expression: MutableList<String>) {
    var index = expression.size - 1

    while (index > 0) {

        if (expression[index] in listOf("+", "-") && expression[index - 1] in listOf("+", "-", "(")) {
            // Add a zero to the left of the "+" or "-" operator
            expression.add(index, "0")
            expression.add(index, "(")
        }

        index--
    }
    // Deal with first element separately
    if (expression[0] in listOf("+", "-")) {
        expression.add(index, "0")
        expression.add(index, "(")
    }
}


fun replaceConstants(expression: MutableList<String>, currentAnswer: String) {
    expression.replaceAll {
        when (it) {
            "Ans" -> currentAnswer
            "π" -> Math.PI.toString()
            "e" -> Math.E.toString()
            else -> it
        }
    }
}

fun balanceParentheses(expression: MutableList<String>) {
    var openParenthesesCount = 0
    var closeParenthesesCount = 0

    expression.forEach { item ->
        if (item == "(") {
            openParenthesesCount++
        } else if (item == ")") {
            closeParenthesesCount++
        }
    }

    val difference = openParenthesesCount - closeParenthesesCount

    if (difference > 0) {
        for (i in 0 until difference) {
            expression.add(")")
        }
    }
}


fun insertExtraParenthesisForOperator(expression: MutableList<String>, operator: String) {

    var openParenthesesCount: Int
    var closeParenthesesCount: Int
    var tempIndex: Int

    var index = 0
    while (index < expression.size) {

        if (expression[index] == operator) {
            // Traverse left
            tempIndex = index - 1

            // Deal with annoying case of factorial (only for left side)
            while (expression[tempIndex] == "!") {
                tempIndex--
            }
            // Traverse left to find where to put open parenthesis
            if (expression[tempIndex] != ")") {
                expression.add(tempIndex, "(")
            }
            else {
                openParenthesesCount = 0
                closeParenthesesCount = 0

                while (tempIndex >= 0) {
                    if (expression[tempIndex] == "(") {
                        openParenthesesCount++
                    }
                    else if (expression[tempIndex] == ")") {
                        closeParenthesesCount++
                    }
                    if (openParenthesesCount == closeParenthesesCount) {
                        expression.add(tempIndex, "(")
                        break
                    }
                    tempIndex--
                }
            }
            // Need to increment index as we added an extra element (left parenthesis)
            index++

            // Traverse right
            tempIndex = index + 1

            // Keep moving right until we found a "(" or number
            while (operator != "!" && expression[tempIndex] != "(" && expression[tempIndex].toDoubleOrNull() == null) {
                tempIndex++
            }
            // If the operator is "!", the close parenthesis should be placed immediately after the "!"
            if (operator == "!") {
                expression.add(tempIndex, ")")
            }
            // Traverse right to find where to put close parenthesis, if operator is not "!"
            else if (expression[tempIndex] != "(") {
                expression.add(tempIndex + 1, ")")
            }
            else {
                openParenthesesCount = 0
                closeParenthesesCount = 0

                while (tempIndex < expression.size) {
                    if (expression[tempIndex] == "(") {
                        openParenthesesCount++
                    }
                    else if (expression[tempIndex] == ")") {
                        closeParenthesesCount++
                    }
                    if (openParenthesesCount == closeParenthesesCount) {
                        expression.add(tempIndex + 1, ")")
                        break
                    }
                    tempIndex++
                }
            }
        }
        index++
    }
}


fun findCloseParenthesisIndex(expression: MutableList<String>, openParenthesisIndex: Int): Int {

    if (expression[openParenthesisIndex] != "(") {
        throw IllegalArgumentException("Element at openParenthesisIndex is not (")
    }

    var index = openParenthesisIndex
    var openParenthesesCount = 0
    var closeParenthesesCount = 0

    while (index < expression.size) {
        if (expression[index] == "(") {
            openParenthesesCount++
        }
        else if (expression[index] == ")") {
            closeParenthesesCount++
        }
        if (openParenthesesCount == closeParenthesesCount) {
            break
        }
        index++
    }
    return(index)
}

// Return NaN for 0^0 case
fun customPow(base: Double, exponent: Double): Double {
    return if (base == 0.0 && exponent == 0.0) {
        Double.NaN
    } else {
        base.pow(exponent)
    }
}

// Return NaN for negative or fractional cases
fun factorial(n: Double): Double {
    if (n < 0 || n % 1 != 0.0) {
        return Double.NaN
    }

    var result = 1.0
    for (i in 1..n.toInt()) {
        result *= i
    }
    return result
}


fun convertEvaluatedExpressionToString(evaluatedExpression: Double): String {

    if (!evaluatedExpression.isFinite()) {
        return (evaluatedExpression.toString())
    }

    // Big number, keep the first 10 digits
    val formattedValue = if (abs(evaluatedExpression) > Int.MAX_VALUE) {
        val numberString = evaluatedExpression.toString()
        val parts = numberString.split("E")
        val base = parts[0]
        val exponent = parts[1]

        // Keep only the first 10 digits of the base (if it has more than 10 digits, otherwise, keep everything)
        var count = 0
        val truncatedBase = base.takeWhile { char ->
            if (char.isDigit()) {
                count++
            }
            count <= 10
        }
        "${truncatedBase}E${exponent}"
    }
    // Small-ish integer
    else if (evaluatedExpression.toInt().toDouble() == evaluatedExpression) {
        evaluatedExpression.toInt().toString()
    }
    // Small-ish double
    else {
        // Double value
        val numberOfSignificantDigits = 10
        var numberOfIntegerDigits = floor(log10(abs(evaluatedExpression))).toInt() + 1
        if (numberOfIntegerDigits < 0) numberOfIntegerDigits = 0

        if (numberOfSignificantDigits > numberOfIntegerDigits) {
            val formatString = "%." + (numberOfSignificantDigits - numberOfIntegerDigits) + "f"
            removeTrailingZeros(formatString.format(evaluatedExpression))
        }
        // numberOfSignificantDigits == numberOfIntegerDigits
        else {
            evaluatedExpression.roundToInt().toString()
        }
    }
    return formattedValue
}


fun removeTrailingZeros(numberString: String): String {
    var i = numberString.length - 1
    while (i >= 0 && numberString[i] == '0') {
        i--
    }
    return if (i == numberString.length - 1) {
        numberString
    } else if (numberString[i] == '.') {
        numberString.substring(0, i)
    } else {
        numberString.substring(0, i + 1)
    }
}




@Composable
fun CalculatorButton(
    text: String,
    fontSize: Int,
    onClick: () -> Unit,
    color: Color,
    width: Int,
    height: Int,
    shiftText: String = "",
    shiftFontSize: Int = 0,
    shiftTextHeight: Int = 0,
    isShiftButtonClicked: Boolean = false) {

    val buttonText: String
    val aboveButtonText: String

    if (isShiftButtonClicked && shiftText != "") {
        buttonText = shiftText
        aboveButtonText = text
    }
    else {
        buttonText = text
        aboveButtonText = shiftText
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = aboveButtonText,
            fontSize = shiftFontSize.sp,
            modifier = Modifier.heightIn(max = shiftTextHeight.dp)
        )
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(color),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)) {
            Text(
                text = buttonText,
                fontSize = fontSize.sp
            )
        }
    }
}


@Composable
fun SmallBlueSquareCalculatorButton(text: String, onClick: () -> Unit) {
    CalculatorButton(
        text = text,
        fontSize = 15,
        onClick = onClick,
        color = Color.Blue,
        width = 40,
        height = 35
    )
}


@Composable
fun SmallGrayCalculatorButton(text: String,
                              onClick: () -> Unit,
                              shiftText: String = "",
                              isShiftButtonClicked: Boolean = false) {
    CalculatorButton(
        text = text,
        fontSize = 16,
        onClick = onClick,
        color = Color.Gray,
        width = 55,
        height = 35,
        shiftText = shiftText,
        shiftFontSize = 12,
        shiftTextHeight = 24,
        isShiftButtonClicked = isShiftButtonClicked
    )
}

@Composable
fun SmallRedCalculatorButton(text: String,
                             onClick: () -> Unit,
                             shiftText: String = "",
                             isShiftButtonClicked: Boolean = false) {
    CalculatorButton(
        text = text,
        fontSize = 16,
        onClick = onClick,
        color = Color.Red,
        width = 55,
        height = 35,
        shiftText = shiftText,
        shiftFontSize = 12,
        shiftTextHeight = 24,
        isShiftButtonClicked = isShiftButtonClicked
    )
}

@Composable
fun SmallColorChangingCalculatorButton(text: String,
                                       onClick: () -> Unit,
                                       isShiftButtonClicked: Boolean) {

    CalculatorButton(
        text = text,
        fontSize = 16,
        onClick = onClick,
        color = if (!isShiftButtonClicked) Color.Gray else Color.DarkGray,
        width = 55,
        height = 35,
        shiftText = "",
        shiftFontSize = 12,
        shiftTextHeight = 24
    )
}

@Composable
fun LargeBlackCalculatorButton(text: String, onClick: () -> Unit) {
    CalculatorButton(
        text = text,
        fontSize = 20,
        onClick = onClick,
        color = Color.Black,
        width = 65,
        height = 45
    )
}

@Composable
fun LargeOrangeCalculatorButton(text: String, onClick: () -> Unit) {
    CalculatorButton(
        text = text,
        fontSize = 20,
        onClick = onClick,
        color = Color(color = 0xFFF59E0B),
        width = 65,
        height = 45
    )
}



@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorTheme {
        Calculator()
    }
}