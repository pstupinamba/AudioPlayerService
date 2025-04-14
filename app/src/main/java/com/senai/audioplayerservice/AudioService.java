package com.senai.audioplayerservice;

import static androidx.fragment.app.FragmentManager.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.support.v4.media.session.MediaSessionCompat;


/**
 * Serviço em foreground responsável pela reprodução de áudio.
 * Implementa:
 * - Controle do MediaPlayer
 * - Notificação persistente
 * - Tratamento dos comandos básicos (play/pause/stop)
 */
public class AudioService extends Service {

    // Componente principal para reprodução de áudio
    private MediaPlayer mediaPlayer;

    // Gerenciador de áudio do sistema
    private AudioManager audioManager;

    // Sessão de mídia para integração com controles externos
    private MediaSessionCompat mediaSession;

    // Notificação do foreground service
    private Notification notification;

    // ID do canal de notificação (Android 8+)
    private static final String CHANNEL_ID = "AudioServiceChannel";

    // Armazena o caminho da música atualmente em reprodução
    private String currentPlayingPath;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicialização dos componentes
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        /**
         * MediaSession para compatibilidade com:
         * - Controles de hardware
         * - Notificações de mídia
         * - Wearables/Android Auto
         */
        mediaSession = new MediaSessionCompat(this, "AudioService");

        createNotificationChannel();
        notification = createNotification("Pronto para reproduzir");

        // Inicia como foreground service (obrigatório para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, notification);
        }
    }

    /**
     * Recebe e processa os comandos da Activity
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PLAY":
                    String path = intent.getStringExtra("path");
                    if (path != null) {
                        playAudio(path);
                    }
                    break;
                case "PAUSE":
                    pauseAudio();
                    break;
                case "STOP":
                    stopAudio();
                    break;
            }
        }

        // Mantém o serviço ativo mesmo se o sistema tentar encerrá-lo
        return START_STICKY;
    }

    /**
     * Lógica de reprodução de áudio com tratamento de estados:
     * - Se já está tocando a mesma música: ignora
     * - Se está pausado: continua de onde parou
     * - Nova música: prepara e inicia a reprodução
     */
    private void playAudio(String path) {
        try {

            Log.e("AudioService", "Iniciando música! ");

            // Se já está tocando a mesma música, não faz nada
            if (mediaPlayer.isPlaying() && currentPlayingPath.equals(path)) {
                return;
            }

            // Se está pausado na mesma música, apenas continua
            if (!mediaPlayer.isPlaying() && currentPlayingPath != null && currentPlayingPath.equals(path)) {
                mediaPlayer.start();


                // Supondo que 'path' seja uma String contendo o caminho/URI do arquivo
                Uri uri = Uri.parse(path);  // Converte String para Uri corretamente
                String songName = AudioUtil.extractSongName(this, uri);
                updateNotification("Tocando: " + songName);

                return;
            }

            // Nova música - prepara do início
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPlayingPath = path;

            // Atualiza notificação com nome da música
            Uri uri = Uri.parse(path);
            String songName = AudioUtil.extractSongName(this, uri);
            updateNotification("Tocando: " + songName);

        } catch (Exception e) {
            Log.e("AudioService", "Erro ao reproduzir: " + e.getMessage());
            updateNotification("Erro ao reproduzir música");
        }
    }

    /**
     * Pausa a reprodução atual (se estiver tocando)
     */
    private void pauseAudio() {
        try{
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updateNotification("Reprodução pausada");
            }
        } catch (Exception e) {
            Log.e("AudioService", "Erro: " + e.getMessage());
        }
    }

    /**
     * Para a reprodução e libera recursos
     */
    private void stopAudio() {
        mediaPlayer.stop();
        currentPlayingPath = null;
        updateNotification("Reprodução parada");
        stopForeground(true);
        stopSelf();
    }

    /**
     * Atualiza a notificação do foreground service
     */
    private void updateNotification(String text) {
        try{
            Notification updatedNotification = createNotification(text);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.notify(1, updatedNotification); // Corrigido aqui
            }

       } catch (Exception e) {
           Log.e("updateNotification", "Erro ao atualizar notificação: " + e.getMessage());
       }
    }

    /**
     * Cria o canal de notificação (requerido para Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Canal para notificações do player de áudio");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Cria a notificação com estilo de mídia
     */
    private Notification createNotification(String text) {

        Log.e("AudioService", "Criação de Notificação" );

        // Intent para reabrir o app ao clicar na notificação
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Configurar MediaSession
        mediaSession.setActive(true);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Player")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
    }

    /**
     *  Exigência obrigatória quando você estende a classe Service
     *  Não há necessidade de comunicação direta com outros componentes. (Started Service apenas)
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}