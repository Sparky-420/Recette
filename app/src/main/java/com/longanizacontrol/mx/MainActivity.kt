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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.longanizacontrol.mx.util.Calculator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LonganizaControlApp() }
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
    var recipe by remember { mutableStateOf(RecipeConfig()) }
    var prices by remember { mutableStateOf(PriceConfig()) }
    var result by remember { mutableStateOf<BatchResult?>(null) }
    var note by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<BatchRecord>().apply { addAll(storage.loadRecords()) } }
    val checklist = remember { mutableStateListOf(
        "Pesar carne", "Preparar premix", "Hidratar/procesar guajillo", "Moler carne", "Mezclar ingredientes",
        "Reposar si aplica", "Embutir", "Orear", "Empacar", "Refrigerar o congelar"
    ) }
    val checked = remember { mutableStateListOf<Boolean>().apply { repeat(checklist.size) { add(false) } } }

    Scaffold(modifier = Modifier.fillMaxSize()) { p ->
        Column(Modifier.padding(p)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, title -> Tab(selected = idx == selectedTab, onClick = { selectedTab = idx }, text = { Text(title) }) }
            }
            when (selectedTab) {
                0 -> MainScreen(meatInput, onMeatChange = { meatInput = it }, result = result, onCalculate = {
                    val kg = meatInput.toDoubleOrNull() ?: 0.0
                    if (kg > 0) result = Calculator.calculateBatch(kg, recipe, prices)
                }, note = note, onNote = { note = it }, onSave = {
                    result?.let {
                        val record = BatchRecord(meatKg = it.meatKg, recipeSnapshot = recipe, totalCost = it.totalCost, costPerKg = it.costPerEstimatedFinalKg, observations = note)
                        storage.saveRecord(record)
                        history.clear(); history.addAll(storage.loadRecords())
                        note = ""
                    }
                })
                1 -> RecipeScreen(recipe) { recipe = it }
                2 -> CostScreen(prices) { prices = it }
                3 -> ChecklistScreen(checklist, checked)
                4 -> HistoryScreen(history)
            }
        }
    }
}

@Composable
fun MainScreen(meatInput: String, onMeatChange: (String) -> Unit, result: BatchResult?, onCalculate: () -> Unit, note: String, onNote: (String) -> Unit, onSave: () -> Unit) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = meatInput, onValueChange = onMeatChange, label = { Text("Kg de carne") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) { Text("Calcular lote") }
        result?.let {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Resumen", style = MaterialTheme.typography.titleMedium)
                Text("Kg carne: %.2f".format(it.meatKg))
                Text("Kg finales aprox: %.2f".format(it.estimatedFinalKg))
                Text("Costo total: $%.2f".format(it.totalCost))
                Text("Costo/kg final: $%.2f".format(it.costPerEstimatedFinalKg))
                Text("Utilidad posible total: $%.2f".format(it.utilityTotal))
            }}
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("Ingredientes"); it.ingredients.forEach { (k,v) -> Text("$k: %.2f".format(v)) } }}
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("Costos"); it.costs.forEach { (k,v) -> Text("$k: $%.2f".format(v)) } }}
            OutlinedTextField(value = note, onValueChange = onNote, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar en historial") }
        }
    }
}

@Composable
fun RecipeScreen(recipe: RecipeConfig, onChange: (RecipeConfig) -> Unit) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("Ajo g/kg", recipe.garlicGramsPerKg) { onChange(recipe.copy(garlicGramsPerKg = it)) }
        NumberField("Guajillo g/kg", recipe.guajilloGramsPerKg) { onChange(recipe.copy(guajilloGramsPerKg = it)) }
        NumberField("Pimentón g/kg", recipe.paprikaGramsPerKg) { onChange(recipe.copy(paprikaGramsPerKg = it)) }
        NumberField("Especias g/kg", recipe.spicesGramsPerKg) { onChange(recipe.copy(spicesGramsPerKg = it)) }
        NumberField("Agua ml/kg", recipe.waterMlPerKg) { onChange(recipe.copy(waterMlPerKg = it)) }
        NumberField("Tripa pzas/kg", recipe.casingUnitsPerKg) { onChange(recipe.copy(casingUnitsPerKg = it)) }
        Spacer(Modifier.height(8.dp)); Text("Premix", style = MaterialTheme.typography.titleMedium)
        NumberField("Dosis premix g/kg", recipe.premix.doseGramsPerKgMeat) { onChange(recipe.copy(premix = recipe.premix.copy(doseGramsPerKgMeat = it))) }
        NumberField("% Sal", recipe.premix.saltPercent) { onChange(recipe.copy(premix = recipe.premix.copy(saltPercent = it))) }
        NumberField("% Azúcar", recipe.premix.sugarPercent) { onChange(recipe.copy(premix = recipe.premix.copy(sugarPercent = it))) }
        NumberField("% Sal cura", recipe.premix.curingSaltPercent) { onChange(recipe.copy(premix = recipe.premix.copy(curingSaltPercent = it))) }
    }
}

@Composable
fun CostScreen(prices: PriceConfig, onChange: (PriceConfig) -> Unit) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("Carne $/kg", prices.meatPerKg) { onChange(prices.copy(meatPerKg = it)) }
        NumberField("Sal $/kg", prices.saltPerKg) { onChange(prices.copy(saltPerKg = it)) }
        NumberField("Sal cura $/kg", prices.curingSaltPerKg) { onChange(prices.copy(curingSaltPerKg = it)) }
        NumberField("Azúcar $/kg", prices.sugarPerKg) { onChange(prices.copy(sugarPerKg = it)) }
        NumberField("Ajo $/kg", prices.garlicPerKg) { onChange(prices.copy(garlicPerKg = it)) }
        NumberField("Guajillo $/kg", prices.guajilloPerKg) { onChange(prices.copy(guajilloPerKg = it)) }
        NumberField("Pimentón $/kg", prices.paprikaPerKg) { onChange(prices.copy(paprikaPerKg = it)) }
        NumberField("Especias $/kg", prices.spicesPerKg) { onChange(prices.copy(spicesPerKg = it)) }
        NumberField("Agua $/L", prices.waterPerLiter) { onChange(prices.copy(waterPerLiter = it)) }
        NumberField("Tripa $/pza", prices.casingPerUnit) { onChange(prices.copy(casingPerUnit = it)) }
        NumberField("Precio venta $/kg", prices.salePricePerKg) { onChange(prices.copy(salePricePerKg = it)) }
    }
}

@Composable
fun ChecklistScreen(steps: List<String>, checked: MutableList<Boolean>) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        steps.forEachIndexed { i, step ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(step, modifier = Modifier.weight(1f))
                Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
            }
        }
    }
}

@Composable
fun HistoryScreen(history: List<BatchRecord>) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        history.forEach { h ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text("Fecha: ${h.date}")
                Text("Kg carne: %.2f".format(h.meatKg))
                Text("Costo total: $%.2f".format(h.totalCost))
                Text("Costo/kg final: $%.2f".format(h.costPerKg))
                Text("Obs: ${h.observations}")
            }}
        }
    }
}

@Composable
fun NumberField(label: String, value: Double, onChange: (Double) -> Unit) {
    OutlinedTextField(value = value.toString(), onValueChange = { onChange(it.toDoubleOrNull() ?: 0.0) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
