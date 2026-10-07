package mx.edu.cesba.credencial.ui.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import mx.edu.cesba.credencial.R;
import mx.edu.cesba.credencial.databinding.ActivityCameraBinding;
import mx.edu.cesba.credencial.photo.PhotoStore;
import mx.edu.cesba.credencial.ui.common.BaseActivity;
import mx.edu.cesba.credencial.ui.common.Motion;

/** CameraX en Java. Solicita únicamente CAMERA y devuelve la foto al formulario. */
public final class CameraActivity extends BaseActivity {
    public static final String EXTRA_PHOTO = "photo_path";
    public static final String EXTRA_ERROR = "camera_error";
    private ActivityCameraBinding binding;
    private ProcessCameraProvider provider;
    private ImageCapture imageCapture;
    private boolean frontCamera;
    private File pendingFile;
    private boolean resultDelivered;

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else returnError(getString(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                        ? R.string.camera_denied : R.string.camera_denied_permanently));
            });
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fitSystemBars(binding.getRoot(), true);
        Motion.press(binding.captureButton);
        binding.captureButton.setOnClickListener(view -> capture());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            startCamera();
        else if (state == null) permissionLauncher.launch(Manifest.permission.CAMERA);
        // El contrato ActivityResult conserva una solicitud de permiso pendiente durante una rotación.
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            if (isDestroyed() || isFinishing()) return;
            try {
                provider = future.get();
                frontCamera = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
                CameraSelector selector = frontCamera ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
                if (!provider.hasCamera(selector)) { returnError(getString(R.string.camera_unavailable)); return; }
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.preview.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                provider.unbindAll();
                provider.bindToLifecycle(this, selector, preview, imageCapture);
                binding.captureButton.setEnabled(true);
                binding.status.setText(R.string.camera_instruction);
                Motion.enter(binding.preview, 0);
            } catch (Exception exception) { returnError(getString(R.string.camera_unavailable)); }
        }, ContextCompat.getMainExecutor(this));
    }
    private void capture() {
        if (imageCapture == null || !binding.captureButton.isEnabled()) return;
        binding.captureButton.setEnabled(false);
        try {
            pendingFile = PhotoStore.create(this);
            if (binding.preview.getDisplay() != null)
                imageCapture.setTargetRotation(binding.preview.getDisplay().getRotation());
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(frontCamera);
            File file = pendingFile;
            ImageCapture.OutputFileOptions output = new ImageCapture.OutputFileOptions.Builder(file)
                    .setMetadata(metadata).build();
            imageCapture.takePicture(output, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                    if (isDestroyed() || isFinishing()) { file.delete(); return; }
                    resultDelivered = true;
                    setResult(RESULT_OK, new Intent().putExtra(EXTRA_PHOTO, file.getAbsolutePath()));
                    finish();
                }
                @Override public void onError(@NonNull ImageCaptureException exception) {
                    file.delete();
                    if (isDestroyed() || isFinishing()) return;
                    binding.status.setText(R.string.camera_capture_error);
                    binding.captureButton.setEnabled(true);
                    Motion.shake(binding.preview);
                }
            });
        } catch (Exception exception) {
            binding.status.setText(R.string.camera_capture_error);
            binding.captureButton.setEnabled(true);
        }
    }
    private void returnError(String message) {
        setResult(RESULT_CANCELED, new Intent().putExtra(EXTRA_ERROR, message));
        finish();
    }
    @Override protected void onDestroy() {
        if (provider != null) provider.unbindAll();
        if (!resultDelivered && pendingFile != null) pendingFile.delete();
        super.onDestroy();
    }
}
