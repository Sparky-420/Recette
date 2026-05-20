package com.longanizacontrol.mx.model

import java.time.LocalDate

data class PremixConfig(
    val doseGramsPerKgMeat: Double = 13.0,
    val saltPercent: Double = 85.0,
    val sugarPercent: Double = 9.0,
    val curingSaltPercent: Double = 6.0
)

data class RecipeConfig(
    val garlicGramsPerKg: Double = 8.0,
    val guajilloGramsPerKg: Double = 18.0,
    val paprikaGramsPerKg: Double = 4.0,
    val spicesGramsPerKg: Double = 2.5,
    val waterMlPerKg: Double = 120.0,
    val casingUnitsPerKg: Double = 0.9,
    val premix: PremixConfig = PremixConfig()
)

data class PriceConfig(
    val meatPerKg: Double = 95.0,
    val saltPerKg: Double = 14.0,
    val curingSaltPerKg: Double = 140.0,
    val sugarPerKg: Double = 28.0,
    val garlicPerKg: Double = 70.0,
    val guajilloPerKg: Double = 110.0,
    val paprikaPerKg: Double = 160.0,
    val spicesPerKg: Double = 180.0,
    val waterPerLiter: Double = 2.0,
    val casingPerUnit: Double = 5.5,
    val salePricePerKg: Double = 145.0
)

data class BatchResult(
    val meatKg: Double,
    val estimatedFinalKg: Double,
    val ingredients: Map<String, Double>,
    val costs: Map<String, Double>,
    val totalCost: Double,
    val costPerKgMeat: Double,
    val costPerEstimatedFinalKg: Double,
    val utilityPerKg: Double,
    val utilityTotal: Double
)

data class BatchRecord(
    val date: String = LocalDate.now().toString(),
    val meatKg: Double,
    val recipeSnapshot: RecipeConfig,
    val totalCost: Double,
    val costPerKg: Double,
    val observations: String
)
