package com.longanizacontrol.mx.util

import com.longanizacontrol.mx.model.BatchResult
import com.longanizacontrol.mx.model.PriceConfig
import com.longanizacontrol.mx.model.RecipeConfig

object Calculator {
    fun calculateBatch(meatKg: Double, recipe: RecipeConfig, prices: PriceConfig): BatchResult {
        val premixTotalG = meatKg * recipe.premix.doseGramsPerKgMeat
        val saltG = premixTotalG * (recipe.premix.saltPercent / 100.0)
        val sugarG = premixTotalG * (recipe.premix.sugarPercent / 100.0)
        val curingSaltG = premixTotalG * (recipe.premix.curingSaltPercent / 100.0)

        val garlicG = meatKg * recipe.garlicGramsPerKg
        val guajilloG = meatKg * recipe.guajilloGramsPerKg
        val paprikaG = meatKg * recipe.paprikaGramsPerKg
        val spicesG = meatKg * recipe.spicesGramsPerKg
        val waterMl = meatKg * recipe.waterMlPerKg
        val casingUnits = meatKg * recipe.casingUnitsPerKg

        val ingredients = linkedMapOf(
            "Premix total (g)" to premixTotalG,
            "Sal (g)" to saltG,
            "Azúcar (g)" to sugarG,
            "Sal cura (g)" to curingSaltG,
            "Ajo (g)" to garlicG,
            "Guajillo (g)" to guajilloG,
            "Pimentón (g)" to paprikaG,
            "Especias (g)" to spicesG,
            "Agua (ml)" to waterMl,
            "Tripa (pzas)" to casingUnits
        )

        val costs = linkedMapOf(
            "Carne" to meatKg * prices.meatPerKg,
            "Sal" to (saltG / 1000.0) * prices.saltPerKg,
            "Azúcar" to (sugarG / 1000.0) * prices.sugarPerKg,
            "Sal cura" to (curingSaltG / 1000.0) * prices.curingSaltPerKg,
            "Ajo" to (garlicG / 1000.0) * prices.garlicPerKg,
            "Guajillo" to (guajilloG / 1000.0) * prices.guajilloPerKg,
            "Pimentón" to (paprikaG / 1000.0) * prices.paprikaPerKg,
            "Especias" to (spicesG / 1000.0) * prices.spicesPerKg,
            "Agua" to (waterMl / 1000.0) * prices.waterPerLiter,
            "Tripa" to casingUnits * prices.casingPerUnit
        )

        val estimatedFinalKg = meatKg + (waterMl / 1000.0) + (garlicG + guajilloG + paprikaG + spicesG + premixTotalG) / 1000.0
        val totalCost = costs.values.sum()
        val costPerKgMeat = totalCost / meatKg
        val costPerFinalKg = totalCost / estimatedFinalKg
        val utilityPerKg = prices.salePricePerKg - costPerFinalKg

        return BatchResult(
            meatKg = meatKg,
            estimatedFinalKg = estimatedFinalKg,
            ingredients = ingredients,
            costs = costs,
            totalCost = totalCost,
            costPerKgMeat = costPerKgMeat,
            costPerEstimatedFinalKg = costPerFinalKg,
            utilityPerKg = utilityPerKg,
            utilityTotal = utilityPerKg * estimatedFinalKg
        )
    }
}
