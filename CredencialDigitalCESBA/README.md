# Credencial Digital CESBA

Proyecto Android nativo en **Java 17**, **layouts XML**, **Material Components con tema Material 3** y **ViewBinding**. Contiene exactamente cuatro pantallas: inicio, formulario, cámara y credencial de dos caras.

## Abrir y ejecutar

1. Extrae el ZIP y abre la carpeta **CredencialDigitalCESBA** desde **File → Open** en Android Studio.
2. Usa Android Studio Meerkat o posterior, con **Gradle JDK 17 o 21**. No uses JDK 26 para esta versión de Gradle.
3. Desde **SDK Manager**, instala **Android SDK Platform 35**, **Android SDK Build-Tools 36.0.0** y **Android SDK Platform-Tools**.
4. Permite la sincronización de Gradle. La primera sincronización necesita Internet; la app funciona sin conexión.
5. Selecciona un teléfono con **Android 7.0 / API 24 o superior**, o un emulador con cámara frontal o trasera habilitada, y pulsa **Run app**.
6. Entra a **Crear credencial**, completa los ocho campos de datos y toma la foto. **Generar credencial** se habilita con los nueve elementos válidos.
7. Toca la tarjeta para alternar frente y reverso. Regresa con el gesto o botón Atrás del sistema.

En Windows, desde la raíz del proyecto puedes ejecutar:

    .\gradlew.bat assembleDebug testDebugUnitTest lintDebug

En macOS/Linux:

    chmod +x gradlew
    ./gradlew assembleDebug testDebugUnitTest lintDebug

La APK de desarrollo se genera en **app/build/outputs/apk/debug/app-debug.apk**. Para distribución de producción, crea tu propia firma mediante **Build → Generate Signed App Bundle / APK**.

## Estructura

    app/src/main/
      java/mx/edu/cesba/credencial/
        ui/home/          HomeActivity: logotipo, nombre, autor y acción inicial
        ui/form/          StudentFormActivity: campos, validación, progreso y foto
        ui/camera/        CameraActivity: permiso, PreviewView y captura CameraX
        ui/credential/    CredentialActivity: caras, giro 3D, inclinación y brillo
        ui/common/        Insets, transiciones y microinteracciones
        model/            Datos de estudiante y colores de las 16 carreras
        validation/       Reglas compartidas por formulario y QR
        photo/            Archivos privados, muestreo y orientación EXIF
        qr/               JSON y codificación ZXing
      res/layout/         Las cuatro pantallas y los dos layouts de la tarjeta
      res/drawable-nodpi/  Los dos PNG oficiales descargados
      res/values/          Textos, colores y tema Material 3

## Ajustes solicitados

- **Autor:** sustituye **[TU NOMBRE]** en el recurso **author**, dentro de **app/src/main/res/values/strings.xml**.
- **Rango de matrícula:** modifica **ID_MIN_DIGITS** y **ID_MAX_DIGITS** en **StudentValidator.java**. El filtro de longitud utiliza esta misma constante.
- **Carreras y colores:** **CareerCatalog.java** conserva los 16 colores propuestos. **strings.xml** contiene las mismas 16 opciones del menú.
- **Identidad:** azul **#183D6E** y naranja **#F36B21**, medidos en las áreas sólidas del logotipo; fondo **#F7F6F2**, texto **#0E0F13**.
- El color de carrera se aplica a franjas y detalles, sin poner texto blanco sobre los acentos claros. Los datos siempre se muestran en tinta/azul sobre blanco o blanco sobre azul.

## Comportamiento

Los errores aparecen debajo del campo al editarlo o salir de él; los campos aún no visitados no muestran errores anticipados. La foto muestra su requisito desde el inicio. Un check se dibuja animado al validar un campo. El progreso cuenta nombre, matrícula, carrera, tipo de periodo, número, grupo, correo, tipo de usuario y foto.

La matrícula acepta exclusivamente **0–9**, conserva ceros iniciales y filtra también el pegado. Los periodos se seleccionan mediante dos menús, con número del **1 al 12**. Las selecciones se comprueban contra catálogos cerrados.

La cámara solicita permiso al abrirse. Si se niega, regresa al formulario con un mensaje debajo de la foto. Si Android bloquea nuevas solicitudes, el mismo mensaje explica cómo habilitar el permiso en Ajustes. No se añaden botones de configuración ni pantallas extra. La captura vuelve directamente al formulario, donde se previsualiza y puede repetirse.

Las fotos se guardan en el almacenamiento privado de la app. Se corrigen giro/reflejo EXIF y se reduce la decodificación para evitar cargar la foto a resolución completa. El borrador y la referencia de la foto sobreviven a la recreación de la Activity; no se añade historial de credenciales.

La tarjeta mantiene proporción horizontal **640:404** y contiene las dos franjas diagonales y las tres marcas de esquina pedidas. El giro está protegido contra toques repetidos. El acelerómetro se registra solo en primer plano; si no existe, la tarjeta mantiene su giro táctil. La animación respeta la desactivación de animaciones del sistema.

Antes de abrir la credencial se valida nuevamente el formulario; antes de codificar el QR se vuelven a validar todos los datos y se comprueba que la foto fue decodificada. El contenido del QR es JSON UTF-8 con estas claves:

    {"nombre":"María José Núñez","matricula":"000123456",
     "carrera":"Sistemas Computacionales","grupo":"6A"}

ZXing genera el QR negro sobre blanco, con margen de cuatro módulos y corrección de errores M. El reverso conserva el logo separado del QR.

## Dependencias

Versiones fijadas en **app/build.gradle**: AppCompat 1.7.1, Activity 1.10.1, Core 1.16.0, ConstraintLayout 2.2.1, Material 1.13.0, CameraX 1.4.2, ExifInterface 1.4.1 y ZXing 3.5.3. Android Gradle Plugin 8.9.2 y Gradle 8.11.1. Los repositorios son Google Maven, Maven Central y Gradle Plugin Portal.

El wrapper se descargó del repositorio oficial de Gradle y su SHA-256 coincide con el publicado por Gradle:

    2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046

El ZIP de distribución de Gradle también se verifica automáticamente con **distributionSha256Sum**.

## Fuentes oficiales

- [Logotipo CESBA](https://www.cesba-queretaro.edu.mx/wp-content/uploads/2021/06/logo_cesba.png)
- [Logotipo CESBA blanco](https://www.cesba-queretaro.edu.mx/wp-content/uploads/2021/07/LOGO-CESBA-BLANCO-1-300x141.png)
- [CameraX: captura de imagen](https://developer.android.com/media/camera/camerax/take-photo)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [Material Components 1.13.0](https://github.com/material-components/material-components-android/releases/tag/1.13.0)
- [Compatibilidad de AGP 8.9](https://developer.android.com/build/releases/agp-8-9-0-release-notes)
- [ZXing 3.5.3](https://github.com/zxing/zxing/releases/tag/zxing-3.5.3)

## Decisiones ante ambigüedades

Se conserva **[TU NOMBRE]** como autor editable porque no se proporcionó el nombre. Se usa una Activity por pantalla. La fotografía se revisa en el formulario y se repite con el mismo botón; no se añade una pantalla de confirmación. La cámara frontal se elige automáticamente y se utiliza la trasera si no hay frontal. El número de periodo y su tipo se eligen por separado. No se añadió ninguna otra función.

Consulta **VERIFICACION.md** para el resultado de las comprobaciones y los pasos de prueba.
