package mx.edu.cesba.credencial;

import org.junit.Test;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.validation.StudentValidator;
import mx.edu.cesba.credencial.validation.StudentValidator.Field;
import static org.junit.Assert.*;

public class StudentValidatorTest {
    private StudentData data(String name, String id, String career, String period, int number,
                             String group, String email, String type) {
        return new StudentData(name, id, career, period, number, group, email, type, "photo.jpg");
    }
    @Test public void namesAcceptSpanishAndDecomposedAccents() {
        for (String value : new String[] {"Ana López", "José Luis Muñoz", "  María  Núñez  ", "Jose\u0301 Mun\u0303oz"})
            assertTrue(value, StudentValidator.validName(value));
    }
    @Test public void namesRejectOneWordNumbersAndPunctuation() {
        for (String value : new String[] {"", "Ana", "Ana 7", "Ana-López", "Ana\nLópez", "Ana @López"})
            assertFalse(value, StudentValidator.validName(value));
    }
    @Test public void idBoundsAndLeadingZerosArePreserved() {
        assertTrue(StudentValidator.validId("000001"));
        assertTrue(StudentValidator.validId("123456789012"));
        assertFalse(StudentValidator.validId("12345"));
        assertFalse(StudentValidator.validId("1234567890123"));
    }
    @Test public void idRejectsOtherNumeralsAndNonDigits() {
        for (String value : new String[] {"12a456", "１２３４５６", "١٢٣٤٥٦", "+123456", "123 456", "123456\n"})
            assertFalse(value, StudentValidator.validId(value));
    }
    @Test public void emailAcceptsNormalAndTaggedAddresses() {
        assertTrue(StudentValidator.validEmail("ana.lopez+cesba@example.edu.mx"));
        assertTrue(StudentValidator.validEmail("alumno@cesba-queretaro.edu.mx"));
    }
    @Test public void emailRejectsMalformedAddresses() {
        for (String value : new String[] {"ana@", "@example.com", "ana example.com", "ana@localhost",
                "ana..lopez@example.com", ".ana@example.com", "ana.@example.com", "ana@-example.com"})
            assertFalse(value, StudentValidator.validEmail(value));
    }
    @Test public void groupHasAlphanumericBounds() {
        assertTrue(StudentValidator.validGroup("6A"));
        assertTrue(StudentValidator.validGroup("123456789A"));
        assertFalse(StudentValidator.validGroup(""));
        assertFalse(StudentValidator.validGroup("A B"));
        assertFalse(StudentValidator.validGroup(" 6A"));
        assertFalse(StudentValidator.validGroup("6A "));
        assertFalse(StudentValidator.validGroup("A-1"));
        assertFalse(StudentValidator.validGroup("12345678901"));
    }
    @Test public void allThreeUserTypesAndBothPeriodsWork() {
        for (String type : new String[] {"Alumno", "Docente", "Administrativo"})
            for (String period : new String[] {"Semestre", "Cuatrimestre"})
                for (int number : new int[] {1, 12})
                    assertTrue(StudentValidator.errors(data("Ana López", "123456", "Sistemas Computacionales",
                            period, number, "6A", "ana@example.com", type), true).isEmpty());
    }
    @Test public void missingOrForgedSelectionsCannotPass() {
        assertEquals(4, StudentValidator.errors(data("Ana López", "123456", "Otra carrera",
                "Bimestre", 13, "6A", "ana@example.com", "Invitado"), true).size());
    }
    @Test public void photoIsMandatoryEvenWithCompleteText() {
        assertEquals(Field.PHOTO, StudentValidator.errors(data("Ana López", "123456",
                "Sistemas Computacionales", "Semestre", 6, "6A", "ana@example.com", "Alumno"), false)
                .keySet().iterator().next());
    }
    @Test public void emptyFormHasExactlyNineInvalidFields() {
        assertEquals(StudentValidator.TOTAL_FIELDS, StudentValidator.errors(
                data("", "", "", "", 0, "", "", ""), false).size());
    }
}
