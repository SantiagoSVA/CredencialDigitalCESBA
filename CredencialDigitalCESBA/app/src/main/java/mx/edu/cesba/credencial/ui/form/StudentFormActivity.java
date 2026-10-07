package mx.edu.cesba.credencial.ui.form;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.os.BundleCompat;
import androidx.core.app.ActivityOptionsCompat;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mx.edu.cesba.credencial.R;
import mx.edu.cesba.credencial.databinding.ActivityStudentFormBinding;
import mx.edu.cesba.credencial.model.CareerCatalog;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.photo.PhotoStore;
import mx.edu.cesba.credencial.ui.camera.CameraActivity;
import mx.edu.cesba.credencial.ui.common.BaseActivity;
import mx.edu.cesba.credencial.ui.common.Motion;
import mx.edu.cesba.credencial.ui.credential.CredentialActivity;
import mx.edu.cesba.credencial.validation.StudentValidator;
import mx.edu.cesba.credencial.validation.StudentValidator.Field;

/** Formulario con validación inmediata y borrador conservado al rotar/recrear la Activity. */
public final class StudentFormActivity extends BaseActivity {
    private ActivityStudentFormBinding binding;
    private final Map<Field, TextInputLayout> layouts = new EnumMap<>(Field.class);
    private final EnumSet<Field> touched = EnumSet.noneOf(Field.class);
    private final EnumSet<Field> previousValid = EnumSet.noneOf(Field.class);
    private final ExecutorService photoExecutor = Executors.newSingleThreadExecutor();
    private String photoPath = "";
    private boolean photoReady;
    private int photoRequest;
    private int accent = 0xFFF36B21;
    private ValueAnimator accentAnimator;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nextPath = result.getData().getStringExtra(CameraActivity.EXTRA_PHOTO);
                    loadPhoto(nextPath, true);
                } else if (result.getData() != null) {
                    // El error se queda debajo de la fotografía y la captura anterior sigue válida.
                    String error = result.getData().getStringExtra(CameraActivity.EXTRA_ERROR);
                    if (error != null) {
                        binding.photoError.setText(error);
                        binding.photoError.setTextColor(getColor(R.color.error));
                        Motion.shake(binding.photoPreview);
                    }
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityStudentFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fitSystemBars(binding.getRoot(), false);
        layouts.put(Field.NAME, binding.nameLayout); layouts.put(Field.ID, binding.idLayout);
        layouts.put(Field.CAREER, binding.careerLayout); layouts.put(Field.PERIOD_TYPE, binding.periodTypeLayout);
        layouts.put(Field.PERIOD_NUMBER, binding.periodNumberLayout); layouts.put(Field.GROUP, binding.groupLayout);
        layouts.put(Field.EMAIL, binding.emailLayout); layouts.put(Field.USER_TYPE, binding.userTypeLayout);

        // Filtra tanto escritura como pegado; los límites se toman de las constantes de validación.
        binding.idInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        binding.idInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(StudentValidator.ID_MAX_DIGITS) });
        dropdown(binding.careerInput, getResources().getStringArray(R.array.careers), Field.CAREER);
        dropdown(binding.periodTypeInput, getResources().getStringArray(R.array.period_types), Field.PERIOD_TYPE);
        String[] numbers = new String[12];
        for (int i = 0; i < numbers.length; i++) numbers[i] = String.valueOf(i + 1);
        dropdown(binding.periodNumberInput, numbers, Field.PERIOD_NUMBER);
        dropdown(binding.userTypeInput, getResources().getStringArray(R.array.user_types), Field.USER_TYPE);

        if (state != null) {
            StudentData saved = BundleCompat.getSerializable(state, "draft", StudentData.class);
            if (saved != null) restoreDraft(saved);
            String[] fields = state.getStringArray("touched");
            if (fields != null) for (String field : fields) touched.add(Field.valueOf(field));
        }
        for (Map.Entry<Field, TextInputLayout> entry : layouts.entrySet()) watch(entry.getKey(), entry.getValue());
        Motion.press(binding.photoButton); Motion.press(binding.generateButton);
        binding.photoButton.setOnClickListener(view -> cameraLauncher.launch(new Intent(this, CameraActivity.class),
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.screen_enter, R.anim.screen_exit)));
        binding.generateButton.setOnClickListener(view -> generate());
        validate();
        if (!photoPath.isEmpty()) loadPhoto(photoPath, false);
        if (state == null) Motion.enter(binding.progress, 50);
    }
    private void dropdown(MaterialAutoCompleteTextView input, String[] items, Field field) {
        input.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items));
        input.setOnItemClickListener((parent, view, position, id) -> {
            touched.add(field);
            if (field == Field.CAREER) animateAccent(CareerCatalog.colorOf(text(input)));
            validate();
        });
    }
    private void watch(Field field, TextInputLayout layout) {
        if (!(layout.getEditText() instanceof TextView)) return;
        TextView input = layout.getEditText();
        if (!(input instanceof MaterialAutoCompleteTextView)) {
            layout.setEndIconVisible(false);
            layout.setEndIconContentDescription(R.string.valid_field);
        }
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable value) { touched.add(field); validate(); }
        });
        input.setOnFocusChangeListener((view, focused) -> {
            if (Motion.enabled(layout)) layout.animate().scaleX(focused ? 1.012f : 1f)
                    .scaleY(focused ? 1.012f : 1f).setDuration(180).start();
            if (!focused) { touched.add(field); validate(); }
        });
    }
    private String text(TextView view) { return StudentValidator.clean(view.getText().toString()); }
    private StudentData snapshot() {
        int number = 0;
        try { number = Integer.parseInt(text(binding.periodNumberInput)); } catch (NumberFormatException ignored) {}
        return new StudentData(text(binding.nameInput), text(binding.idInput), text(binding.careerInput),
                text(binding.periodTypeInput), number, binding.groupInput.getText().toString(), text(binding.emailInput),
                text(binding.userTypeInput), photoPath);
    }
    private void restoreDraft(StudentData data) {
        binding.nameInput.setText(data.name); binding.idInput.setText(data.studentId);
        binding.careerInput.setText(data.career, false); binding.periodTypeInput.setText(data.periodType, false);
        binding.periodNumberInput.setText(data.periodNumber == 0 ? "" : String.valueOf(data.periodNumber), false);
        binding.groupInput.setText(data.group); binding.emailInput.setText(data.email);
        binding.userTypeInput.setText(data.userType, false);
        photoPath = data.photoPath == null ? "" : data.photoPath;
        accent = CareerCatalog.colorOf(data.career);
        binding.progress.setIndicatorColor(accent);
    }
    private Map<Field, String> validate() {
        boolean photoValid = photoReady && PhotoStore.isReadable(this, photoPath);
        Map<Field, String> errors = StudentValidator.errors(snapshot(), photoValid);
        for (Map.Entry<Field, TextInputLayout> entry : layouts.entrySet()) {
            Field field = entry.getKey();
            TextInputLayout layout = entry.getValue();
            String error = touched.contains(field) ? errors.get(field) : null;
            CharSequence previousError = layout.getError();
            layout.setError(error);
            if (error != null && (previousError == null || !error.contentEquals(previousError)))
                Motion.shake(layout);
            boolean valid = !errors.containsKey(field);
            // Los menús conservan su flecha; el check aparece en el extremo izquierdo.
            boolean newlyValid = valid && !previousValid.contains(field);
            Drawable check = newlyValid ? getDrawable(R.drawable.animated_check) : null;
            if (layout.getEditText() instanceof MaterialAutoCompleteTextView) {
                if (!valid) layout.setStartIconDrawable((Drawable) null);
                else if (newlyValid) layout.setStartIconDrawable(check);
                layout.setStartIconTintList(ColorStateList.valueOf(getColor(R.color.valid)));
                layout.setStartIconContentDescription(R.string.valid_field);
            } else {
                if (newlyValid) layout.setEndIconDrawable(check);
                layout.setEndIconVisible(valid);
            }
            if (newlyValid && check instanceof Animatable && Motion.enabled(layout))
                ((Animatable) check).start();
            if (valid) previousValid.add(field); else previousValid.remove(field);
        }
        int validCount = StudentValidator.TOTAL_FIELDS - errors.size();
        binding.progress.setProgressCompat(validCount, Motion.enabled(binding.progress));
        binding.progressText.setText(getString(R.string.progress_format, validCount, StudentValidator.TOTAL_FIELDS));
        binding.generateButton.setEnabled(errors.isEmpty());
        return errors;
    }
    private void animateAccent(int next) {
        if (accentAnimator != null) accentAnimator.cancel();
        if (!Motion.enabled(binding.progress)) { accent = next; binding.progress.setIndicatorColor(next); return; }
        accentAnimator = ValueAnimator.ofArgb(accent, next);
        accentAnimator.setDuration(360);
        accentAnimator.addUpdateListener(animation -> {
            accent = (int) animation.getAnimatedValue();
            binding.progress.setIndicatorColor(accent);
        });
        accentAnimator.start();
    }
    private void loadPhoto(String nextPath, boolean animate) {
        int request = ++photoRequest;
        String oldPath = photoPath;
        photoPath = nextPath == null ? "" : nextPath;
        photoReady = false;
        validate();
        photoExecutor.execute(() -> {
            try {
                Bitmap bitmap = PhotoStore.load(getApplicationContext(), nextPath, 1000);
                runOnUiThread(() -> {
                    if (isDestroyed() || request != photoRequest) { bitmap.recycle(); return; }
                    photoPath = nextPath; photoReady = true;
                    binding.photoPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    binding.photoPreview.setImageBitmap(bitmap);
                    binding.photoError.setText(R.string.photo_valid);
                    binding.photoError.setTextColor(getColor(R.color.valid));
                    binding.photoButton.setText(R.string.retake_photo);
                    if (animate) Motion.enter(binding.photoPreview, 0);
                    validate();
                    if (!oldPath.equals(nextPath)) PhotoStore.delete(this, oldPath);
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    if (isDestroyed() || request != photoRequest) return;
                    photoReady = false;
                    binding.photoError.setText(R.string.photo_load_error);
                    binding.photoError.setTextColor(getColor(R.color.error));
                    validate();
                });
            }
        });
    }
    private void generate() {
        touched.addAll(EnumSet.allOf(Field.class));
        if (!validate().isEmpty()) return;
        Intent intent = new Intent(this, CredentialActivity.class);
        intent.putExtra(StudentData.EXTRA, snapshot());
        openScreen(intent);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putSerializable("draft", snapshot());
        state.putStringArray("touched", touched.stream().map(Enum::name).toArray(String[]::new));
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        photoRequest++;
        photoExecutor.shutdownNow();
        if (accentAnimator != null) accentAnimator.cancel();
        super.onDestroy();
    }
}
