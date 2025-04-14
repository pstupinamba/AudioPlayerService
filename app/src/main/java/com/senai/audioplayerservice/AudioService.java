package com.senai.audioplayerservice;

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

public class AudioService extends Service {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private Notification notification;
    private static final String CHANNEL_ID = "AudioServiceChannel";

    private String currentPlayingPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "AudioService");

        createNotificationChannel();
        notification = createNotification("Pronto para reproduzir");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, notification);
        }
    }

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
        return START_STICKY;
    }

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
                String songName = extractSongName(path);
                updateNotification("Tocando: " + songName);
                return;
            }

            // Nova música - prepara do início
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPlayingPath = path;

            Uri uri = Uri.parse(path);
            String songName = AudioUtil.extractSongName(this, uri);
            updateNotification("Tocando: " + songName);

        } catch (Exception e) {
            Log.e("AudioService", "Erro ao reproduzir: " + e.getMessage());
            updateNotification("Erro ao reproduzir música");
        }
    }

    private String extractSongName(String path) {
        Uri uri = Uri.parse(path);
        String fileName = uri.getLastPathSegment();
        return fileName != null ? fileName.replace(".mp3", "") : "Música desconhecida";
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification("Reprodução pausada");
        }
    }

    private void stopAudio() {
        mediaPlayer.stop();
        currentPlayingPath = null;
        updateNotification("Reprodução parada");
        stopForeground(true);
        stopSelf();
    }

    private void updateNotification(String text) {
        Notification updatedNotification = createNotification(text);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, updatedNotification); // Corrigido aqui
        }
    }

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

    private Notification createNotification(String text) {

        Log.e("AudioService", "Criação de Notificação" );

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}