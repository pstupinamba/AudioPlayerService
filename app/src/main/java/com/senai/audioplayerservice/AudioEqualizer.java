package com.senai.audioplayerservice;

public class AudioEqualizer {

    static {
        System.loadLibrary("equalizer");
    }

    public native int applyEqualization(short[] audioData, int[] gain);

}
