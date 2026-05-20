package com.longanizacontrol.mx.data

import android.content.Context
import com.longanizacontrol.mx.model.BatchRecord
import com.longanizacontrol.mx.model.PremixConfig
import com.longanizacontrol.mx.model.RecipeConfig
import org.json.JSONArray
import org.json.JSONObject

class BatchStorage(context: Context) {
    private val prefs = context.getSharedPreferences("longaniza_prefs", Context.MODE_PRIVATE)

    fun saveRecord(record: BatchRecord) {
        val existing = loadRecords().toMutableList()
        existing.add(0, record)
        val array = JSONArray()
        existing.take(50).forEach { r ->
            val obj = JSONObject()
            obj.put("date", r.date)
            obj.put("meatKg", r.meatKg)
            obj.put("totalCost", r.totalCost)
            obj.put("costPerKg", r.costPerKg)
            obj.put("observations", r.observations)
            obj.put("garlic", r.recipeSnapshot.garlicGramsPerKg)
            obj.put("guajillo", r.recipeSnapshot.guajilloGramsPerKg)
            obj.put("paprika", r.recipeSnapshot.paprikaGramsPerKg)
            obj.put("spices", r.recipeSnapshot.spicesGramsPerKg)
            obj.put("water", r.recipeSnapshot.waterMlPerKg)
            obj.put("casing", r.recipeSnapshot.casingUnitsPerKg)
            obj.put("dose", r.recipeSnapshot.premix.doseGramsPerKgMeat)
            obj.put("saltPct", r.recipeSnapshot.premix.saltPercent)
            obj.put("sugarPct", r.recipeSnapshot.premix.sugarPercent)
            obj.put("curePct", r.recipeSnapshot.premix.curingSaltPercent)
            array.put(obj)
        }
        prefs.edit().putString("history", array.toString()).apply()
    }

    fun loadRecords(): List<BatchRecord> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            BatchRecord(
                date = o.getString("date"),
                meatKg = o.getDouble("meatKg"),
                totalCost = o.getDouble("totalCost"),
                costPerKg = o.getDouble("costPerKg"),
                observations = o.getString("observations"),
                recipeSnapshot = RecipeConfig(
                    garlicGramsPerKg = o.getDouble("garlic"),
                    guajilloGramsPerKg = o.getDouble("guajillo"),
                    paprikaGramsPerKg = o.getDouble("paprika"),
                    spicesGramsPerKg = o.getDouble("spices"),
                    waterMlPerKg = o.getDouble("water"),
                    casingUnitsPerKg = o.getDouble("casing"),
                    premix = PremixConfig(
                        doseGramsPerKgMeat = o.getDouble("dose"),
                        saltPercent = o.getDouble("saltPct"),
                        sugarPercent = o.getDouble("sugarPct"),
                        curingSaltPercent = o.getDouble("curePct")
                    )
                )
            )
        }
    }
}
