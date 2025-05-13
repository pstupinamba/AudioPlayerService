package com.senai.audioplayerservice;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AudioServiceTest {


    @Mock
    private MediaPlayer mockMediaPlayer;

    @Mock
    private AudioManager mockAudioManager;

    @Mock
    private NotificationManager mockNotificationManager;

    @Mock
    private Notification mockNotification;

    @Mock
    private MediaSessionCompat mockMediaSession;

    @Mock
    private IBinder mockBinder;

    private AudioService audioService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        audioService = new AudioService(){
            @Override
            public Object getSystemService(String name){
                switch (name){
                    case Context.AUDIO_SERVICE:
                        return mockAudioManager;
                    case Context.NOTIFICATION_SERVICE:
                        return mockNotificationManager;
                    default:
                        return getSystemService(name);
                }
            }

        };

        audioService.setMediaPlayer(mockMediaPlayer);
        audioService.setAudioManager(mockAudioManager);
        audioService.setMediaSession(mockMediaSession);

    }

    @Test
    public void testPlayAudio_neMusic() throws Exception {
        String path = "/sdcard/Music/02 - Jaded.mp3";
        Uri uri = Uri.parse(path);

        when(mockMediaPlayer.isPlaying()).thenReturn(false);
        when(AudioUtil.extractSongName(any(), any())).thenReturn("FakeSong");

        //audioService.currentPlayingPath = null; // simulando que nenhuma música está tocando
        audioService.playAudio(path);

        verify(mockMediaPlayer).reset();
        verify(mockMediaPlayer).setDataSource(any(Context.class), eq(uri));
        verify(mockMediaPlayer).prepare();
        verify(mockMediaPlayer).start();
    }

    @Test
    public void testPlayAudio_isPlayingSame() throws Exception {
        String path = "content://audio/media/1";

        when(mockMediaPlayer.isPlaying()).thenReturn(true);
        //audioService.currentPlayingPath = path;

        audioService.playAudio(path);

        verify(mockMediaPlayer, never()).reset();
        verify(mockMediaPlayer, never()).setDataSource(any(), any());
        verify(mockMediaPlayer, never()).prepare();
    }

    @Test
    public void testPlayAudio_retomaMusicaPausada() throws Exception {
        String path = "content://audio/media/1";
        //audioService.currentPlayingPath = path;

        when(mockMediaPlayer.isPlaying()).thenReturn(false);
        when(AudioUtil.extractSongName(any(), any())).thenReturn("FakeSong");

        audioService.playAudio(path);

        verify(mockMediaPlayer).start();
    }

    @Test
    public void testPauseAudio() {
        when(mockMediaPlayer.isPlaying()).thenReturn(true);

        audioService.pauseAudio();

        verify(mockMediaPlayer).pause();
    }

    @Test
    public void testStopAudio() {
        audioService.stopAudio();

        verify(mockMediaPlayer).stop();
        verify(mockNotificationManager, atLeastOnce()).notify(eq(1), any());
    }

    @Test
    public void testCreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioService.createNotificationChannel();

            verify(mockNotificationManager).createNotificationChannel(any(NotificationChannel.class));
        }
    }

    @Test
    public void testCreateNotification() {
        Notification notification = audioService.createNotification("Reproduzindo música");
        assertNotNull(notification);
    }

}
