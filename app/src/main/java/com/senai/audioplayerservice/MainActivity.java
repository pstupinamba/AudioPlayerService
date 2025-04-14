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



/**
 * Activity principal que gerencia a interface do usuário do player de áudio.
 * Responsabilidades:
 * - Seleção de arquivos de música via seletor de conteúdo
 * - Controle de reprodução (play/pause/stop)
 * - Atualização da interface conforme o estado do player
 */
public class MainActivity extends AppCompatActivity {

    // Componente para exibir o título da música atual
    private TextView tvTitulo;

    // Armazena o caminho URI do áudio selecionado
    private String currentAudioPath;


    /**
     * ActivityResultLauncher para seleção de arquivos de áudio.
     * Usa o contrato GetContent() padrão do Android para seleção de arquivos.
     */
    private final ActivityResultLauncher<String> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Atualiza o caminho atual do áudio
                    currentAudioPath = uri.toString();

                    // Extrai e exibe o nome do arquivo (sem extensão)
                    String fileName = AudioUtil.extractSongName(this, uri);
                    tvTitulo.setText(fileName);

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

        // Inicialização dos componentes de UI
        tvTitulo = findViewById(R.id.tvTitulo);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnSelect = findViewById(R.id.btnSelect);


        // Estado inicial - botão Play oculto
        btnPlay.setVisibility(Button.GONE);

        // Configura listeners para os botões
        btnSelect.setOnClickListener(v -> openAudioPicker());

        btnPlay.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "Click no btnPlay");

            if (currentAudioPath != null) {
                startAudioService("PLAY");
            }
        });

        btnPause.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "Click no btnPause");
            startAudioService("PAUSE");
        });

        btnStop.setOnClickListener(v -> {
            Log.i("AudioPlayMainActivity", "Click btnStop");
            startAudioService("STOP");

            // Esconder o botão Play
            findViewById(R.id.btnPlay).setVisibility(Button.GONE);

            // Resetar o título para o padrão
            tvTitulo.setText("Nenhuma música selecionada");

            // Limpar o caminho atual do áudio
            currentAudioPath = null;
        });
    }

    /**
     * Abre o seletor de arquivos de áudio
     * Filtra por tipo MIME "audio/*" para mostrar apenas arquivos de áudio
     */
    private void openAudioPicker() {
        audioPickerLauncher.launch("audio/*");
    }

    /**
     * Comunica-se com o AudioService via Intent
     * @param action Ação a ser executada (PLAY/PAUSE/STOP)
     */
    private void startAudioService(String action) {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(action);
        if (action.equals("PLAY") && currentAudioPath != null) {
            intent.putExtra("path", currentAudioPath);
        }
        startService(intent);
    }

    //OLD: FOI USADO PARA ATUALIZAR TÍTULO DA MÚSICA QUE ESTÁ SELECIONADA/TOCANDO
    //FOI SUBSTITUIDO PELA CLASSE AudioUtil
//    private void updateSongTitle(Uri uri) {
//        String fileName = "Música selecionada";
//
//        try {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            if (cursor != null) {
//                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                if (cursor.moveToFirst() && nameIndex != -1) {
//                    fileName = cursor.getString(nameIndex);
//                    fileName = fileName.replace(".mp3", "");
//                }
//                cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e("updateSongTitle", "Erro ao obter nome da música: " + e.getMessage());
//        }
//
//        tvTitulo.setText(fileName);
//    }

}