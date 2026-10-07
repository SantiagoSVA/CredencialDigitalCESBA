package mx.edu.cesba.credencial.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Catálogo cerrado: el color de carrera se usa solo como acento. */
public final class CareerCatalog {
    private static final Map<String, Integer> COLORS;
    static {
        Map<String, Integer> colors = new LinkedHashMap<>();
        colors.put("Administración", 0xFF5C7CFA);
        colors.put("Arquitectura", 0xFF8B5E3C);
        colors.put("Comercio Internacional", 0xFF0F8B8D);
        colors.put("Contaduría Pública", 0xFF1E7B4F);
        colors.put("Contaduría y Finanzas", 0xFF1B2F6B);
        colors.put("Criminología", 0xFF3C4250);
        colors.put("Derecho", 0xFF6D1A36);
        colors.put("Diseño y Comunicación Audiovisual", 0xFFE5398A);
        colors.put("Enfermería", 0xFFD62839);
        colors.put("Fisioterapia", 0xFF2BB3E0);
        colors.put("Ingeniería Industrial", 0xFFF2B01E);
        colors.put("Mercadotecnia y Negocios", 0xFFE8505B);
        colors.put("Nutrición", 0xFF7CC23A);
        colors.put("Pedagogía", 0xFFC0A16B);
        colors.put("Psicología", 0xFFD96C9A);
        colors.put("Sistemas Computacionales", 0xFF7B3FE4);
        COLORS = Collections.unmodifiableMap(colors);
    }
    private CareerCatalog() {}
    public static boolean contains(String career) { return COLORS.containsKey(career); }
    public static int colorOf(String career) { return COLORS.getOrDefault(career, 0xFFF36B21); }
    public static Map<String, Integer> all() { return COLORS; }
}
