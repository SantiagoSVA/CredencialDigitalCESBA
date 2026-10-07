package mx.edu.cesba.credencial.validation;

import mx.edu.cesba.credencial.model.CareerCatalog;
import mx.edu.cesba.credencial.model.StudentData;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Una única fuente de reglas para el formulario y la comprobación previa al QR. */
public final class StudentValidator {
    // Cambia únicamente estas constantes para ajustar el rango de matrícula.
    public static final int ID_MIN_DIGITS = 6;
    public static final int ID_MAX_DIGITS = 12;
    public static final int TOTAL_FIELDS = 9;
    public enum Field { NAME, ID, CAREER, PERIOD_TYPE, PERIOD_NUMBER, GROUP, EMAIL, USER_TYPE, PHOTO }
    private static final Pattern NAME = Pattern.compile(
            "^\\p{L}[\\p{L}\\p{M}]*(?: +\\p{L}[\\p{L}\\p{M}]*)+$");
    private static final Pattern GROUP = Pattern.compile("^[A-Za-z0-9]{1,10}$");
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_\\x60{|}~-]+@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");
    private StudentValidator() {}
    public static String clean(String text) { return text == null ? "" : text.trim(); }
    public static boolean validName(String name) { return NAME.matcher(clean(name)).matches(); }
    public static boolean validId(String id) {
        return id != null && id.matches("[0-9]{" + ID_MIN_DIGITS + "," + ID_MAX_DIGITS + "}");
    }
    public static boolean validGroup(String group) { return group != null && GROUP.matcher(group).matches(); }
    public static boolean validEmail(String email) {
        String value = clean(email);
        int at = value.indexOf('@');
        return at > 0 && at <= 64 && value.length() <= 254
                && !value.startsWith(".") && value.charAt(at - 1) != '.'
                && !value.substring(0, at).contains("..") && EMAIL.matcher(value).matches();
    }
    public static Map<Field, String> errors(StudentData data, boolean photoReadable) {
        Map<Field, String> errors = new EnumMap<>(Field.class);
        if (data == null) {
            for (Field field : Field.values()) errors.put(field, "Completa este campo.");
            return errors;
        }
        if (!validName(data.name)) errors.put(Field.NAME,
                "Escribe al menos dos palabras, solo con letras y espacios.");
        if (!validId(data.studentId)) errors.put(Field.ID,
                "La matrícula debe tener entre " + ID_MIN_DIGITS + " y " + ID_MAX_DIGITS + " dígitos (0–9).");
        if (!CareerCatalog.contains(data.career)) errors.put(Field.CAREER, "Selecciona una carrera.");
        if (!"Semestre".equals(data.periodType) && !"Cuatrimestre".equals(data.periodType))
            errors.put(Field.PERIOD_TYPE, "Selecciona semestre o cuatrimestre.");
        if (data.periodNumber < 1 || data.periodNumber > 12)
            errors.put(Field.PERIOD_NUMBER, "Selecciona un número del 1 al 12.");
        if (!validGroup(data.group)) errors.put(Field.GROUP,
                "Usa de 1 a 10 letras o números, sin espacios.");
        if (!validEmail(data.email)) errors.put(Field.EMAIL, "Escribe un correo electrónico válido.");
        if (!"Alumno".equals(data.userType) && !"Docente".equals(data.userType)
                && !"Administrativo".equals(data.userType))
            errors.put(Field.USER_TYPE, "Selecciona un tipo de usuario.");
        if (!photoReadable) errors.put(Field.PHOTO, "Toma una foto para generar tu credencial.");
        return errors;
    }
}
