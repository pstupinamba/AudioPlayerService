package com.senai.audioplayerservice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel para gerenciar o estado da reprodução
 * Mantém os dados mesmo durante mudanças de configuração
 */
public class PlayerView extends ViewModel {
    private final MutableLiveData<String> currentSongTitle = new MutableLiveData<>();

    /**
     * Atualiza o título da música atual
     */
    public void updateSongTitle(String title) {
        currentSongTitle.postValue(title);
    }

    /**
     * Retorna o LiveData com o título atual
     */
    public MutableLiveData<String> getCurrentSongTitle() {
        return currentSongTitle;
    }
}