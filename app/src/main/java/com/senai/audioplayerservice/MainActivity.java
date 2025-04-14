package com.senai.audioplayerservice;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitulo;
    private String currentAudioPath;


    private final ActivityResultLauncher<String> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentAudioPath = uri.toString();
                    updateSongTitle(uri);

                    // Mostrar botão Play após seleção
                    findViewById(R.id.btnPlay).setVisibility(Button.VISIBLE);
                    Toast.makeText(this, "Música selecionada", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tvTitulo);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnSelect = findViewById(R.id.btnSelect);

        btnPlay.setVisibility(Button.GONE);

        btnSelect.setOnClickListener(v -> openAudioPicker());

        btnPlay.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "btnPlay");

            if (currentAudioPath != null) {
                startAudioService("PLAY");
            }
        });

        btnPause.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "btnPause");
            startAudioService("PAUSE");
        });

        btnStop.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "btnStop");
            startAudioService("STOP");
        });
    }

    private void openAudioPicker() {
        audioPickerLauncher.launch("audio/*");
    }

    private void startAudioService(String action) {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(action);
        if (action.equals("PLAY") && currentAudioPath != null) {
            intent.putExtra("path", currentAudioPath);
        }
        startService(intent);
    }

    private void updateSongTitle(Uri uri) {
        String fileName = "Música selecionada";

        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                    fileName = fileName.replace(".mp3", "");
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("updateSongTitle", "Erro ao obter nome da música: " + e.getMessage());
        }

        tvTitulo.setText(fileName);
    }

}