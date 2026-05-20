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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.longanizacontrol.mx.data.BatchStorage
import com.longanizacontrol.mx.model.BatchRecord
import com.longanizacontrol.mx.model.BatchResult
import com.longanizacontrol.mx.model.PremixConfig
import com.longanizacontrol.mx.model.PriceConfig
import com.longanizacontrol.mx.model.RecipeConfig
import com.longanizacontrol.mx.ui.components.DecimalInputField
import com.longanizacontrol.mx.ui.theme.AppTheme
import com.longanizacontrol.mx.util.Calculator
import com.longanizacontrol.mx.util.formatCurrency
import com.longanizacontrol.mx.util.formatKg
import com.longanizacontrol.mx.util.toDoubleOrNullSafe

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

    var meatInput by rememberSaveable { mutableStateOf("30") }
    var recipe by remember { mutableStateOf(RecipeConfig()) }
    var prices by remember { mutableStateOf(PriceConfig()) }
    var result by remember { mutableStateOf<BatchResult?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
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
                val kg = toDoubleOrNullSafe(meatInput)
                if (kg != null && kg > 0) {
                    result = Calculator.calculateBatch(kg, recipe, prices)
                }
            }, note, { note = it }, {
                result?.let {
                    storage.saveRecord(BatchRecord(meatKg = it.meatKg, recipeSnapshot = recipe, totalCost = it.totalCost, costPerKg = it.costPerEstimatedFinalKg, observations = note))
                    history.clear(); history.addAll(storage.loadRecords())
                    note = ""
                }
            })
            1 -> RecipeScreen(Modifier.padding(p), recipe, onSave = { recipe = it })
            2 -> CostScreen(Modifier.padding(p), prices, onSave = { prices = it })
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
fun RecipeScreen(modifier: Modifier, recipe: RecipeConfig, onSave: (RecipeConfig) -> Unit) {
    var garlic by rememberSaveable { mutableStateOf(recipe.garlicGramsPerKg.toString()) }
    var guajillo by rememberSaveable { mutableStateOf(recipe.guajilloGramsPerKg.toString()) }
    var paprika by rememberSaveable { mutableStateOf(recipe.paprikaGramsPerKg.toString()) }
    var spices by rememberSaveable { mutableStateOf(recipe.spicesGramsPerKg.toString()) }
    var water by rememberSaveable { mutableStateOf(recipe.waterMlPerKg.toString()) }
    var premixDose by rememberSaveable { mutableStateOf(recipe.premix.doseGramsPerKgMeat.toString()) }
    var salt by rememberSaveable { mutableStateOf(recipe.premix.saltPercent.toString()) }
    var sugar by rememberSaveable { mutableStateOf(recipe.premix.sugarPercent.toString()) }
    var curingSalt by rememberSaveable { mutableStateOf(recipe.premix.curingSaltPercent.toString()) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DecimalInputField(garlic, { garlic = it }, "Ajo g/kg", Modifier.fillMaxWidth())
        DecimalInputField(guajillo, { guajillo = it }, "Guajillo g/kg", Modifier.fillMaxWidth())
        DecimalInputField(paprika, { paprika = it }, "Pimentón g/kg", Modifier.fillMaxWidth())
        DecimalInputField(spices, { spices = it }, "Especias g/kg", Modifier.fillMaxWidth())
        DecimalInputField(water, { water = it }, "Agua ml/kg", Modifier.fillMaxWidth())
        DecimalInputField(premixDose, { premixDose = it }, "Dosis premix g/kg", Modifier.fillMaxWidth())
        DecimalInputField(salt, { salt = it }, "% Sal", Modifier.fillMaxWidth())
        DecimalInputField(sugar, { sugar = it }, "% Azúcar", Modifier.fillMaxWidth())
        DecimalInputField(curingSalt, { curingSalt = it }, "% Sal cura", Modifier.fillMaxWidth())
        Button(onClick = {
            val updated = recipe.copy(
                garlicGramsPerKg = toDoubleOrNullSafe(garlic) ?: recipe.garlicGramsPerKg,
                guajilloGramsPerKg = toDoubleOrNullSafe(guajillo) ?: recipe.guajilloGramsPerKg,
                paprikaGramsPerKg = toDoubleOrNullSafe(paprika) ?: recipe.paprikaGramsPerKg,
                spicesGramsPerKg = toDoubleOrNullSafe(spices) ?: recipe.spicesGramsPerKg,
                waterMlPerKg = toDoubleOrNullSafe(water) ?: recipe.waterMlPerKg,
                premix = PremixConfig(
                    doseGramsPerKgMeat = toDoubleOrNullSafe(premixDose) ?: recipe.premix.doseGramsPerKgMeat,
                    saltPercent = toDoubleOrNullSafe(salt) ?: recipe.premix.saltPercent,
                    sugarPercent = toDoubleOrNullSafe(sugar) ?: recipe.premix.sugarPercent,
                    curingSaltPercent = toDoubleOrNullSafe(curingSalt) ?: recipe.premix.curingSaltPercent
                )
            )
            val hadInvalid = listOf(garlic, guajillo, paprika, spices, water, premixDose, salt, sugar, curingSalt)
                .any { it.isBlank() || toDoubleOrNullSafe(it) == null }
            message = if (hadInvalid) "Se conservaron valores previos en campos vacíos o inválidos." else "Receta guardada correctamente."
            onSave(updated)
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar receta") }
        message?.let { Text(it) }
    }
}

@Composable
fun CostScreen(modifier: Modifier, prices: PriceConfig, onSave: (PriceConfig) -> Unit) {
    var meat by rememberSaveable { mutableStateOf(prices.meatPerKg.toString()) }
    var salt by rememberSaveable { mutableStateOf(prices.saltPerKg.toString()) }
    var curingSalt by rememberSaveable { mutableStateOf(prices.curingSaltPerKg.toString()) }
    var sugar by rememberSaveable { mutableStateOf(prices.sugarPerKg.toString()) }
    var garlic by rememberSaveable { mutableStateOf(prices.garlicPerKg.toString()) }
    var guajillo by rememberSaveable { mutableStateOf(prices.guajilloPerKg.toString()) }
    var paprika by rememberSaveable { mutableStateOf(prices.paprikaPerKg.toString()) }
    var spices by rememberSaveable { mutableStateOf(prices.spicesPerKg.toString()) }
    var water by rememberSaveable { mutableStateOf(prices.waterPerLiter.toString()) }
    var sale by rememberSaveable { mutableStateOf(prices.salePricePerKg.toString()) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DecimalInputField(meat, { meat = it }, "Carne $/kg", Modifier.fillMaxWidth())
        DecimalInputField(salt, { salt = it }, "Sal $/kg", Modifier.fillMaxWidth())
        DecimalInputField(curingSalt, { curingSalt = it }, "Sal cura $/kg", Modifier.fillMaxWidth())
        DecimalInputField(sugar, { sugar = it }, "Azúcar $/kg", Modifier.fillMaxWidth())
        DecimalInputField(garlic, { garlic = it }, "Ajo $/kg", Modifier.fillMaxWidth())
        DecimalInputField(guajillo, { guajillo = it }, "Guajillo $/kg", Modifier.fillMaxWidth())
        DecimalInputField(paprika, { paprika = it }, "Pimentón $/kg", Modifier.fillMaxWidth())
        DecimalInputField(spices, { spices = it }, "Especias $/kg", Modifier.fillMaxWidth())
        DecimalInputField(water, { water = it }, "Agua $/L", Modifier.fillMaxWidth())
        DecimalInputField(sale, { sale = it }, "Venta sugerida $/kg", Modifier.fillMaxWidth())
        Button(onClick = {
            val updated = prices.copy(
                meatPerKg = toDoubleOrNullSafe(meat) ?: prices.meatPerKg,
                saltPerKg = toDoubleOrNullSafe(salt) ?: prices.saltPerKg,
                curingSaltPerKg = toDoubleOrNullSafe(curingSalt) ?: prices.curingSaltPerKg,
                sugarPerKg = toDoubleOrNullSafe(sugar) ?: prices.sugarPerKg,
                garlicPerKg = toDoubleOrNullSafe(garlic) ?: prices.garlicPerKg,
                guajilloPerKg = toDoubleOrNullSafe(guajillo) ?: prices.guajilloPerKg,
                paprikaPerKg = toDoubleOrNullSafe(paprika) ?: prices.paprikaPerKg,
                spicesPerKg = toDoubleOrNullSafe(spices) ?: prices.spicesPerKg,
                waterPerLiter = toDoubleOrNullSafe(water) ?: prices.waterPerLiter,
                salePricePerKg = toDoubleOrNullSafe(sale) ?: prices.salePricePerKg
            )
            val hadInvalid = listOf(meat, salt, curingSalt, sugar, garlic, guajillo, paprika, spices, water, sale)
                .any { it.isBlank() || toDoubleOrNullSafe(it) == null }
            message = if (hadInvalid) "Se conservaron valores previos en campos vacíos o inválidos." else "Costos guardados correctamente."
            onSave(updated)
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar costos") }
        message?.let { Text(it) }
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
