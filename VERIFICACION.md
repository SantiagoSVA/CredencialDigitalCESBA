# Verificación

## Comprobaciones automatizadas incluidas

- **StudentValidatorTest:** nombres con acentos y Unicode descompuesto; rechazo de una palabra, cifras y puntuación; extremos de matrícula y ceros iniciales; rechazo de dígitos de otros alfabetos; correos válidos e inválidos; grupo; todos los tipos de usuario; periodos 1 y 12; selección obligatoria y fotografía obligatoria.
- **QrEncoderTest:** genera y decodifica un QR real para las 16 carreras, recupera el JSON y verifica los acentos y la matrícula; rechaza datos inválidos y foto ausente antes de codificar.
- **UiFlowTest:** formulario vacío, bloqueo de caracteres al pegar, errores en tiempo real, foto obligatoria, restauración del borrador, ambos lados de la credencial, recreación y pantalla de inicio.
- **CameraFlowTest:** apertura de CameraX, captura, retorno y previsualización en el formulario, posibilidad de repetir la foto.

Pruebas locales y análisis estático:

    .\gradlew.bat testDebugUnitTest lintDebug

Con un teléfono/emulador conectado y cámara habilitada:

    .\gradlew.bat connectedDebugAndroidTest

Las pruebas de cámara usan permiso previamente concedido. Los casos de denegación se comprueban manualmente con la secuencia de abajo.

## Revisión manual de interacción y hardware

1. Inicio: observa entrada escalonada de logo, título y botón. Pulsa Crear credencial.
2. Prueba nombres como **Ana**, **Ana 7**, **Ana López**, matrícula de 5/6/12 dígitos y pegado con letras. Comprueba mensajes, shake y check.
3. Prueba grupo vacío, **6A**, **A-1** y correo incompleto. Completa todos los menús. El progreso debe quedar en **8 de 9** y Generar deshabilitado.
4. Toca Tomar foto y niega el permiso. Comprueba el mensaje debajo de la foto y que los datos se conservan.
5. Repite y niega hasta que Android bloquee la solicitud. Comprueba las instrucciones de Ajustes. Habilita CAMERA desde los ajustes del sistema y vuelve a la app.
6. Captura una foto. Revisa su orientación y reflejo; debe aparecer animada y activar el progreso **9 de 9**.
7. Pulsa Volver a tomar foto y cancela con Atrás: la foto anterior permanece. Captura otra: la previsualización se sustituye.
8. Genera la credencial y revisa todos los datos del frente. Tócala varias veces; debe cambiar de cara sin superponer animaciones.
9. Escanea el QR con un lector y comprueba las cuatro claves JSON, los acentos y los ceros iniciales.
10. Inclina el teléfono: el giro se limita a cuatro grados y el brillo es discreto. En un dispositivo sin acelerómetro, el giro al tocar sigue disponible.
11. Cambia la carrera, observa la transición de color y genera nuevamente. Las carreras claras deben mantener contraste.
12. Rota el teléfono durante el formulario, al revisar la foto y en el reverso. Los datos y la cara seleccionada deben mantenerse. Revisa también teclado, navegación del sistema y texto grande.

## Resultado de esta entrega

- **14 pruebas locales aprobadas**, incluyendo lectura real de QR de las 16 carreras, acentos, matrícula con ceros iniciales y las reglas del formulario.
- El proyecto generó una APK de desarrollo y compiló las pruebas instrumentadas.
- Las pruebas de interfaz, cámara y acelerómetro **no se ejecutaron en un dispositivo**: el emulador disponible no pudo iniciar por falta de aceleración de virtualización utilizable.
- El informe de Lint encontró una restricción de escritura del entorno; no se afirma que Lint haya finalizado satisfactoriamente.
- Para verificar la compilación se utilizó una firma de depuración dentro del espacio de trabajo y una adaptación local del empaquetado de recursos: AAPT2 oficial enlazó los recursos a un directorio y Java creó el ZIP. Esta adaptación no se incluye en la configuración del proyecto entregado, que conserva el flujo habitual de Android Studio.
