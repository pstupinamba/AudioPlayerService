package com.senai.audioplayerservice;

public class AudioEqualizer {

    //applyEqualization

    static {
        System.loadLibrary("equalizer");
    }

    public native int applyEqualization(short[] audioData, int[] gain);

}
