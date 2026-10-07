package mx.edu.cesba.credencial;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import mx.edu.cesba.credencial.model.CareerCatalog;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.photo.PhotoStore;
import mx.edu.cesba.credencial.ui.credential.CredentialActivity;
import mx.edu.cesba.credencial.ui.form.StudentFormActivity;
import mx.edu.cesba.credencial.ui.home.HomeActivity;
import static org.junit.Assert.*;

/** Pruebas de integración: vistas reales, recreación y credencial con foto local. */
@RunWith(AndroidJUnit4.class)
public class UiFlowTest {
    @Test public void emptyFormBlocksGenerationAndMatchesOfficialCatalog() {
        try (ActivityScenario<StudentFormActivity> scenario = ActivityScenario.launch(StudentFormActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(activity.findViewById(R.id.generateButton).isEnabled());
                assertEquals(16, activity.getResources().getStringArray(R.array.careers).length);
                for (String career : activity.getResources().getStringArray(R.array.careers))
                    assertTrue(CareerCatalog.contains(career));
                assertEquals("0 de 9 campos válidos", ((TextView) activity.findViewById(R.id.progressText)).getText().toString());
            });
        }
    }
    @Test public void pastedIdFiltersNonAsciiDigitsAndShowsErrorsImmediately() {
        try (ActivityScenario<StudentFormActivity> scenario = ActivityScenario.launch(StudentFormActivity.class)) {
            scenario.onActivity(activity -> {
                TextInputEditText id = activity.findViewById(R.id.idInput);
                id.setText("12a٣456");
                assertEquals("12456", id.getText().toString());
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.idLayout)).getError());
                id.setText("123456");
                assertNull(((TextInputLayout) activity.findViewById(R.id.idLayout)).getError());
                id.setText("1234567890123");
                assertEquals("123456789012", id.getText().toString());
                ((TextInputEditText) activity.findViewById(R.id.nameInput)).setText("Ana");
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.nameLayout)).getError());
                ((TextInputEditText) activity.findViewById(R.id.nameInput)).setText("Ana López");
                assertNull(((TextInputLayout) activity.findViewById(R.id.nameLayout)).getError());
            });
        }
    }
    @Test public void completeTextStillRequiresPhotoAndDraftSurvivesRecreation() {
        try (ActivityScenario<StudentFormActivity> scenario = ActivityScenario.launch(StudentFormActivity.class)) {
            scenario.onActivity(activity -> {
                ((TextInputEditText) activity.findViewById(R.id.nameInput)).setText("María José Núñez");
                ((TextInputEditText) activity.findViewById(R.id.idInput)).setText("000123456");
                ((MaterialAutoCompleteTextView) activity.findViewById(R.id.careerInput)).setText("Sistemas Computacionales", false);
                ((MaterialAutoCompleteTextView) activity.findViewById(R.id.periodTypeInput)).setText("Cuatrimestre", false);
                ((MaterialAutoCompleteTextView) activity.findViewById(R.id.periodNumberInput)).setText("12", false);
                ((TextInputEditText) activity.findViewById(R.id.groupInput)).setText("6A");
                ((TextInputEditText) activity.findViewById(R.id.emailInput)).setText("maria@example.com");
                ((MaterialAutoCompleteTextView) activity.findViewById(R.id.userTypeInput)).setText("Alumno", false);
                assertFalse(activity.findViewById(R.id.generateButton).isEnabled());
                assertEquals("8 de 9 campos válidos", ((TextView) activity.findViewById(R.id.progressText)).getText().toString());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals("María José Núñez", ((TextView) activity.findViewById(R.id.nameInput)).getText().toString());
                assertEquals("000123456", ((TextView) activity.findViewById(R.id.idInput)).getText().toString());
                assertEquals("Cuatrimestre", ((TextView) activity.findViewById(R.id.periodTypeInput)).getText().toString());
                assertFalse(activity.findViewById(R.id.generateButton).isEnabled());
            });
        }
    }
    @Test public void frontAndBackFlipAndSurviveRecreation() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File photo = PhotoStore.create(context);
        Bitmap image = Bitmap.createBitmap(400, 500, Bitmap.Config.ARGB_8888);
        image.eraseColor(0xFF8CA5C2);
        try (FileOutputStream stream = new FileOutputStream(photo)) { image.compress(Bitmap.CompressFormat.JPEG, 90, stream); }
        StudentData data = new StudentData("María José Núñez", "000123456", "Sistemas Computacionales",
                "Cuatrimestre", 12, "6A", "maria@example.com", "Alumno", photo.getAbsolutePath());
        Intent intent = new Intent(context, CredentialActivity.class).putExtra(StudentData.EXTRA, data);
        try (ActivityScenario<CredentialActivity> scenario = ActivityScenario.launch(intent)) {
            awaitQr(scenario);
            scenario.onActivity(activity -> {
                assertEquals("María José Núñez", ((TextView) activity.findViewById(R.id.name)).getText().toString());
                assertEquals("Grupo 6A · Cuatrimestre 12", ((TextView) activity.findViewById(R.id.groupPeriod)).getText().toString());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.front).getVisibility());
                activity.findViewById(R.id.card).performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            // La tarea Gradle desactiva animaciones: el cambio de cara es inmediato.
            scenario.onActivity(activity -> assertEquals("Reverso", ((TextView) activity.findViewById(R.id.faceLabel)).getText().toString()));
            scenario.recreate();
            awaitQr(scenario);
            scenario.onActivity(activity -> assertEquals("Reverso", ((TextView) activity.findViewById(R.id.faceLabel)).getText().toString()));
            saveScreenshot("credencial_reverso.png");
            scenario.onActivity(activity -> activity.findViewById(R.id.card).performClick());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            saveScreenshot("credencial_frente.png");
        } finally { photo.delete(); }
    }
    @Test public void homeContainsOnlyRequestedAction() throws Exception {
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {
            scenario.onActivity(activity -> {
                assertEquals("Crear credencial", ((TextView) activity.findViewById(R.id.createButton)).getText().toString());
                assertEquals("Autor · [TU NOMBRE]", ((TextView) activity.findViewById(R.id.author)).getText().toString());
            });
            saveScreenshot("inicio.png");
        }
    }
    private void awaitQr(ActivityScenario<CredentialActivity> scenario) throws InterruptedException {
        AtomicBoolean loaded = new AtomicBoolean();
        for (int attempt = 0; attempt < 60 && !loaded.get(); attempt++) {
            scenario.onActivity(activity -> loaded.set(((ImageView) activity.findViewById(R.id.qr)).getDrawable() instanceof BitmapDrawable));
            if (!loaded.get()) Thread.sleep(200);
        }
        assertTrue("La credencial debe cargar la foto y el QR", loaded.get());
    }
    private void saveScreenshot(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Bitmap screenshot = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        File folder = InstrumentationRegistry.getInstrumentation().getContext().getExternalFilesDir("screenshots");
        assertNotNull(folder);
        try (FileOutputStream out = new FileOutputStream(new File(folder, name))) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }
}
