package mx.edu.cesba.credencial.ui.credential;

import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.IntentCompat;
import com.google.zxing.common.BitMatrix;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mx.edu.cesba.credencial.R;
import mx.edu.cesba.credencial.databinding.ActivityCredentialBinding;
import mx.edu.cesba.credencial.model.CareerCatalog;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.photo.PhotoStore;
import mx.edu.cesba.credencial.qr.QrEncoder;
import mx.edu.cesba.credencial.ui.common.BaseActivity;
import mx.edu.cesba.credencial.ui.common.Motion;
import mx.edu.cesba.credencial.validation.StudentValidator;

/** Frente/reverso con giro 3D y acelerómetro registrado únicamente en primer plano. */
public final class CredentialActivity extends BaseActivity implements SensorEventListener {
    private ActivityCredentialBinding binding;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private SensorManager sensors;
    private Sensor accelerometer;
    private boolean reverse, flipping, ready;
    private float tiltX, tiltY;
    private float neutralX, neutralY;
    private boolean calibrated;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityCredentialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fitSystemBars(binding.getRoot(), false);
        binding.stage.setVisibility(View.INVISIBLE);
        StudentData data = IntentCompat.getSerializableExtra(getIntent(), StudentData.EXTRA, StudentData.class);
        if (!StudentValidator.errors(data, data != null && PhotoStore.isReadable(this, data.photoPath)).isEmpty()) {
            fail(R.string.invalid_credential); return;
        }
        reverse = state != null && state.getBoolean("reverse");
        binding.back.getRoot().setVisibility(reverse ? View.VISIBLE : View.GONE);
        binding.front.getRoot().setVisibility(reverse ? View.GONE : View.VISIBLE);
        binding.card.setCameraDistance(9000 * getResources().getDisplayMetrics().density);
        binding.card.setOnClickListener(view -> flip());
        int color = CareerCatalog.colorOf(data.career);
        binding.front.decor.configure(color, false); binding.back.decor.configure(color, true);
        binding.front.name.setText(data.name);
        binding.front.studentId.setText(getString(R.string.student_id) + "  " + data.studentId);
        binding.front.career.setText(data.career);
        binding.front.groupPeriod.setText(getString(R.string.group_format, data.group) + " · "
                + getString(R.string.period_format, data.periodType, data.periodNumber));
        binding.front.email.setText(data.email); binding.front.userType.setText(data.userType);
        updateFace();
        sensors = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensors == null ? null : sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Decodificar la foto y el QR fuera del hilo de interfaz evita pausas en la entrada.
        worker.execute(() -> {
            try {
                Bitmap photo = PhotoStore.load(getApplicationContext(), data.photoPath, 1200);
                BitMatrix matrix = QrEncoder.encode(data, true, 900);
                Bitmap qr = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
                int[] pixels = new int[matrix.getWidth() * matrix.getHeight()];
                for (int y = 0; y < matrix.getHeight(); y++) for (int x = 0; x < matrix.getWidth(); x++)
                    pixels[y * matrix.getWidth() + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                qr.setPixels(pixels, 0, matrix.getWidth(), 0, 0, matrix.getWidth(), matrix.getHeight());
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) { photo.recycle(); qr.recycle(); return; }
                    binding.front.photo.setImageBitmap(photo); binding.back.qr.setImageBitmap(qr);
                    binding.stage.setVisibility(View.VISIBLE);
                    ready = true;
                    if (state == null) Motion.enter(binding.stage, 40);
                });
            } catch (Exception exception) { runOnUiThread(() -> { if (!isDestroyed()) fail(R.string.qr_error); }); }
        });
    }
    private void fail(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
    private void flip() {
        if (!ready || flipping) return;
        if (!Motion.enabled(binding.card)) { reverse = !reverse; updateFace(); return; }
        flipping = true;
        binding.card.animate().rotationX(0).rotationY(90f).setDuration(180).withEndAction(() -> {
            if (isDestroyed()) return;
            reverse = !reverse; updateFace();
            binding.card.setRotationY(-90f);
            binding.card.animate().rotationY(0).setDuration(220).withEndAction(() -> flipping = false).start();
        }).start();
    }
    private void updateFace() {
        binding.front.getRoot().setVisibility(reverse ? View.GONE : View.VISIBLE);
        binding.back.getRoot().setVisibility(reverse ? View.VISIBLE : View.GONE);
        binding.sheen.setReverse(reverse);
        binding.faceLabel.setText(reverse ? R.string.credential_back : R.string.credential_front);
        binding.card.setContentDescription(getString(reverse ? R.string.credential_back : R.string.credential_front)
                + ". " + getString(R.string.flip_accessibility));
    }
    @Override protected void onResume() {
        super.onResume();
        calibrated = false;
        if (sensors != null && accelerometer != null && Motion.enabled(binding.card))
            sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }
    @Override protected void onPause() {
        if (sensors != null) sensors.unregisterListener(this);
        binding.card.animate().cancel();
        flipping = false;
        binding.card.setRotationX(0f); binding.card.setRotationY(0f);
        super.onPause();
    }
    @Override public void onSensorChanged(SensorEvent event) {
        if (!ready || flipping) return;
        float x = event.values[0], y = event.values[1];
        int rotation = binding.card.getDisplay() == null ? Surface.ROTATION_0 : binding.card.getDisplay().getRotation();
        if (rotation == Surface.ROTATION_90) { float temp = x; x = -y; y = temp; }
        else if (rotation == Surface.ROTATION_270) { float temp = x; x = y; y = -temp; }
        else if (rotation == Surface.ROTATION_180) { x = -x; y = -y; }
        if (!calibrated) { neutralX = x; neutralY = y; calibrated = true; }
        x -= neutralX; y -= neutralY;
        // Filtro de paso bajo y límite de ±4 grados para no comprometer la legibilidad.
        tiltX += (Math.max(-4f, Math.min(4f, y * 0.45f)) - tiltX) * 0.1f;
        tiltY += (Math.max(-4f, Math.min(4f, -x * 0.45f)) - tiltY) * 0.1f;
        binding.card.setRotationX(tiltX); binding.card.setRotationY(tiltY);
        binding.sheen.setTilt(tiltY);
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("reverse", reverse);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }
}
