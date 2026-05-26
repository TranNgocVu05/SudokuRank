package ntu.tranngocvu.sudokurank.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import ntu.tranngocvu.sudokurank.R;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        ImageButton btnBack = findViewById(R.id.btnBackGuide);

        btnBack.setOnClickListener(v -> finish());
    }
}