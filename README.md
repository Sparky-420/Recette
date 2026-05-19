# LonganizaControl MX (Android, Kotlin, Jetpack Compose)

App nativa para calcular lotes de longaniza, ingredientes, costos, utilidad, checklist de producción e historial local.

## Estructura
- `app/src/main/java/com/longanizacontrol/mx/MainActivity.kt`: navegación por tabs y pantallas.
- `model/Models.kt`: modelos de receta, precios, resultados e historial.
- `util/Calculator.kt`: motor de cálculos de ingredientes y costeo.
- `data/BatchStorage.kt`: guardado local de historial usando SharedPreferences.

## Cómo abrir en Android Studio
1. Abre Android Studio (Hedgehog o superior recomendado).
2. `File > Open` y selecciona la carpeta raíz del proyecto.
3. Espera Sync de Gradle.
4. Conecta un dispositivo Android o usa emulador.
5. Ejecuta **Run app**.

## Requisitos
- Android Studio reciente
- SDK Android 35
- JDK 17

## Notas
- No requiere internet ni Firebase.
- Datos de historial se guardan localmente en el dispositivo.
