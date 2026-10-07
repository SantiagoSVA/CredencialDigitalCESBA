package mx.edu.cesba.credencial.ui.home;

import android.content.Intent;
import android.os.Bundle;
import mx.edu.cesba.credencial.databinding.ActivityHomeBinding;
import mx.edu.cesba.credencial.ui.common.BaseActivity;
import mx.edu.cesba.credencial.ui.common.Motion;
import mx.edu.cesba.credencial.ui.form.StudentFormActivity;

public final class HomeActivity extends BaseActivity {
    private ActivityHomeBinding binding;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fitSystemBars(binding.getRoot(), false);
        Motion.press(binding.createButton);
        binding.createButton.setOnClickListener(view -> openScreen(new Intent(this, StudentFormActivity.class)));
        if (state == null) {
            Motion.enter(binding.logo, 40); Motion.enter(binding.title, 150);
            Motion.enter(binding.motto, 240); Motion.enter(binding.createButton, 340);
        }
    }
}
