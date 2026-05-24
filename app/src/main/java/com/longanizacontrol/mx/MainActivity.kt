package com.longanizacontrol.mx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.longanizacontrol.mx.data.BatchStorage
import com.longanizacontrol.mx.model.BatchRecord
import com.longanizacontrol.mx.model.BatchResult
import com.longanizacontrol.mx.model.PriceConfig
import com.longanizacontrol.mx.model.RecipeConfig
import com.longanizacontrol.mx.ui.components.DecimalInputField
import com.longanizacontrol.mx.ui.theme.AppTheme
import com.longanizacontrol.mx.util.Calculator
import com.longanizacontrol.mx.util.formatCurrency
import com.longanizacontrol.mx.util.formatKg
import com.longanizacontrol.mx.util.toDoubleOrZero

private data class RecipeInput(
    val garlicGramsPerKg: String,
    val guajilloGramsPerKg: String,
    val paprikaGramsPerKg: String,
    val spicesGramsPerKg: String,
    val waterMlPerKg: String,
    val premixDoseGramsPerKgMeat: String,
    val premixSaltPercent: String,
    val premixSugarPercent: String,
    val premixCuringSaltPercent: String
) {
    companion object {
        fun fromRecipe(recipe: RecipeConfig): RecipeInput = RecipeInput(
            garlicGramsPerKg = recipe.garlicGramsPerKg.toString(),
            guajilloGramsPerKg = recipe.guajilloGramsPerKg.toString(),
            paprikaGramsPerKg = recipe.paprikaGramsPerKg.toString(),
            spicesGramsPerKg = recipe.spicesGramsPerKg.toString(),
            waterMlPerKg = recipe.waterMlPerKg.toString(),
            premixDoseGramsPerKgMeat = recipe.premix.doseGramsPerKgMeat.toString(),
            premixSaltPercent = recipe.premix.saltPercent.toString(),
            premixSugarPercent = recipe.premix.sugarPercent.toString(),
            premixCuringSaltPercent = recipe.premix.curingSaltPercent.toString()
        )
    }
}

private data class PriceInput(
    val meatPerKg: String,
    val saltPerKg: String,
    val curingSaltPerKg: String,
    val sugarPerKg: String,
    val garlicPerKg: String,
    val guajilloPerKg: String,
    val paprikaPerKg: String,
    val spicesPerKg: String,
    val waterPerLiter: String,
    val salePricePerKg: String
) {
    companion object {
        fun fromPrices(prices: PriceConfig): PriceInput = PriceInput(
            meatPerKg = prices.meatPerKg.toString(),
            saltPerKg = prices.saltPerKg.toString(),
            curingSaltPerKg = prices.curingSaltPerKg.toString(),
            sugarPerKg = prices.sugarPerKg.toString(),
            garlicPerKg = prices.garlicPerKg.toString(),
            guajilloPerKg = prices.guajilloPerKg.toString(),
            paprikaPerKg = prices.paprikaPerKg.toString(),
            spicesPerKg = prices.spicesPerKg.toString(),
            waterPerLiter = prices.waterPerLiter.toString(),
            salePricePerKg = prices.salePricePerKg.toString()
        )
    }
}

private fun RecipeInput.toRecipeConfig(): RecipeConfig = RecipeConfig(
    garlicGramsPerKg = toDoubleOrZero(garlicGramsPerKg),
    guajilloGramsPerKg = toDoubleOrZero(guajilloGramsPerKg),
    paprikaGramsPerKg = toDoubleOrZero(paprikaGramsPerKg),
    spicesGramsPerKg = toDoubleOrZero(spicesGramsPerKg),
    waterMlPerKg = toDoubleOrZero(waterMlPerKg),
    premix = RecipeConfig().premix.copy(
        doseGramsPerKgMeat = toDoubleOrZero(premixDoseGramsPerKgMeat),
        saltPercent = toDoubleOrZero(premixSaltPercent),
        sugarPercent = toDoubleOrZero(premixSugarPercent),
        curingSaltPercent = toDoubleOrZero(premixCuringSaltPercent)
    )
)

private fun PriceInput.toPriceConfig(): PriceConfig = PriceConfig(
    meatPerKg = toDoubleOrZero(meatPerKg),
    saltPerKg = toDoubleOrZero(saltPerKg),
    curingSaltPerKg = toDoubleOrZero(curingSaltPerKg),
    sugarPerKg = toDoubleOrZero(sugarPerKg),
    garlicPerKg = toDoubleOrZero(garlicPerKg),
    guajilloPerKg = toDoubleOrZero(guajilloPerKg),
    paprikaPerKg = toDoubleOrZero(paprikaPerKg),
    spicesPerKg = toDoubleOrZero(spicesPerKg),
    waterPerLiter = toDoubleOrZero(waterPerLiter),
    salePricePerKg = toDoubleOrZero(salePricePerKg)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { LonganizaControlApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LonganizaControlApp() {
    val tabs = listOf("Calculadora", "Receta", "Costeo", "Proceso", "Historial")
    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val storage = remember { BatchStorage(context) }

    var meatInput by remember { mutableStateOf("30") }
    val baseRecipe = remember { RecipeConfig() }
    val basePrices = remember { PriceConfig() }
    var recipeInput by remember { mutableStateOf(RecipeInput.fromRecipe(baseRecipe)) }
    var priceInput by remember { mutableStateOf(PriceInput.fromPrices(basePrices)) }
    var recipe by remember { mutableStateOf(baseRecipe) }
    var prices by remember { mutableStateOf(basePrices) }
    var result by remember { mutableStateOf<BatchResult?>(null) }
    var note by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<BatchRecord>().apply { addAll(storage.loadRecords()) } }
    val checklist = remember { mutableStateListOf("Pesar carne", "Preparar premix", "Moler carne", "Mezclar", "Embutir", "Empacar") }
    val checked = remember { mutableStateListOf<Boolean>().apply { repeat(checklist.size) { add(false) } } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("LonganizaControl MX") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { idx, label ->
                    NavigationBarItem(selected = idx == selectedTab, onClick = { selectedTab = idx }, icon = {}, label = { Text(label) })
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { p ->
        when (selectedTab) {
            0 -> MainScreen(Modifier.padding(p), meatInput, { meatInput = it }, result, {
                val kg = toDoubleOrZero(meatInput)
                recipe = recipeInput.toRecipeConfig()
                prices = priceInput.toPriceConfig()
                if (kg > 0) result = Calculator.calculateBatch(kg, recipe, prices)
            }, note, { note = it }, {
                result?.let {
                    storage.saveRecord(BatchRecord(meatKg = it.meatKg, recipeSnapshot = recipe, totalCost = it.totalCost, costPerKg = it.costPerEstimatedFinalKg, observations = note))
                    history.clear(); history.addAll(storage.loadRecords())
                    note = ""
                }
            })
            1 -> RecipeScreen(Modifier.padding(p), recipeInput) { recipeInput = it }
            2 -> CostScreen(Modifier.padding(p), priceInput) { priceInput = it }
            3 -> ChecklistScreen(Modifier.padding(p), checklist, checked)
            else -> HistoryScreen(Modifier.padding(p), history)
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier, meatInput: String, onMeatChange: (String) -> Unit, result: BatchResult?, onCalculate: () -> Unit, note: String, onNote: (String) -> Unit, onSave: () -> Unit) {
    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DecimalInputField(meatInput, onMeatChange, "Kg de carne", Modifier.fillMaxWidth())
        Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) { Text("Calcular lote") }
        result?.let {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Resumen")
                Text("Costo total: ${formatCurrency(it.totalCost)}")
                Text("Costo/kg final: ${formatCurrency(it.costPerEstimatedFinalKg)}")
                Text("Kg finales: ${formatKg(it.estimatedFinalKg)}")
                Text("Utilidad: ${formatCurrency(it.utilityTotal)}")
            } }
            OutlinedTextField(value = note, onValueChange = onNote, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar en historial") }
        }
    }
}

@Composable
fun RecipeScreen(modifier: Modifier, recipe: RecipeInput, onChange: (RecipeInput) -> Unit) {
    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Field("Ajo g/kg", recipe.garlicGramsPerKg) { onChange(recipe.copy(garlicGramsPerKg = it)) }
        Field("Guajillo g/kg", recipe.guajilloGramsPerKg) { onChange(recipe.copy(guajilloGramsPerKg = it)) }
        Field("Pimentón g/kg", recipe.paprikaGramsPerKg) { onChange(recipe.copy(paprikaGramsPerKg = it)) }
        Field("Especias g/kg", recipe.spicesGramsPerKg) { onChange(recipe.copy(spicesGramsPerKg = it)) }
        Field("Agua ml/kg", recipe.waterMlPerKg) { onChange(recipe.copy(waterMlPerKg = it)) }
        Field("Dosis premix g/kg", recipe.premixDoseGramsPerKgMeat) { onChange(recipe.copy(premixDoseGramsPerKgMeat = it)) }
        Field("% Sal", recipe.premixSaltPercent) { onChange(recipe.copy(premixSaltPercent = it)) }
        Field("% Azúcar", recipe.premixSugarPercent) { onChange(recipe.copy(premixSugarPercent = it)) }
        Field("% Sal cura", recipe.premixCuringSaltPercent) { onChange(recipe.copy(premixCuringSaltPercent = it)) }
    }
}

@Composable
fun CostScreen(modifier: Modifier, prices: PriceInput, onChange: (PriceInput) -> Unit) {
    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Field("Carne $/kg", prices.meatPerKg) { onChange(prices.copy(meatPerKg = it)) }
        Field("Sal $/kg", prices.saltPerKg) { onChange(prices.copy(saltPerKg = it)) }
        Field("Sal cura $/kg", prices.curingSaltPerKg) { onChange(prices.copy(curingSaltPerKg = it)) }
        Field("Azúcar $/kg", prices.sugarPerKg) { onChange(prices.copy(sugarPerKg = it)) }
        Field("Ajo $/kg", prices.garlicPerKg) { onChange(prices.copy(garlicPerKg = it)) }
        Field("Guajillo $/kg", prices.guajilloPerKg) { onChange(prices.copy(guajilloPerKg = it)) }
        Field("Pimentón $/kg", prices.paprikaPerKg) { onChange(prices.copy(paprikaPerKg = it)) }
        Field("Especias $/kg", prices.spicesPerKg) { onChange(prices.copy(spicesPerKg = it)) }
        Field("Agua $/L", prices.waterPerLiter) { onChange(prices.copy(waterPerLiter = it)) }
        Field("Venta sugerida $/kg", prices.salePricePerKg) { onChange(prices.copy(salePricePerKg = it)) }
    }
}

@Composable
fun ChecklistScreen(modifier: Modifier, steps: List<String>, checked: MutableList<Boolean>) {
    val done = checked.count { it }
    val progress = if (steps.isNotEmpty()) done.toFloat() / steps.size else 0f
    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Progreso: $done/${steps.size}")
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        steps.forEachIndexed { i, step ->
            Row(Modifier.fillMaxWidth()) {
                androidx.compose.material3.Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
                Spacer(Modifier.width(8.dp))
                Text(step)
            }
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier, history: List<BatchRecord>) {
    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (history.isEmpty()) Text("No hay lotes guardados todavía.")
        history.forEach { h ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text("Fecha: ${h.date}")
                Text("Carne: ${formatKg(h.meatKg)}")
                Text("Costo total: ${formatCurrency(h.totalCost)}")
                Text("Costo/kg: ${formatCurrency(h.costPerKg)}")
            } }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit) {
    DecimalInputField(value, onChange, label, Modifier.fillMaxWidth())
}
