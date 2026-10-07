package mx.edu.cesba.credencial.model;

import java.io.Serializable;

/** Datos inmutables transportados únicamente entre las Activities internas. */
public final class StudentData implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String EXTRA = "mx.edu.cesba.credencial.STUDENT";
    public final String name, studentId, career, periodType, group, email, userType, photoPath;
    public final int periodNumber;
    public StudentData(String name, String studentId, String career, String periodType,
                       int periodNumber, String group, String email, String userType, String photoPath) {
        this.name = name; this.studentId = studentId; this.career = career;
        this.periodType = periodType; this.periodNumber = periodNumber; this.group = group;
        this.email = email; this.userType = userType; this.photoPath = photoPath;
    }
}
