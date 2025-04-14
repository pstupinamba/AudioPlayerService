package com.senai.audioplayerservice;

/**
 * Classe auxiliar para compartilhar o ViewModel entre Activity e Service.
 * Usa o padrão Singleton para manter uma única instância acessível globalmente.
 *
 * Nota: Em uma arquitetura mais complexa, seria preferível usar injeção de dependências.
 */
public class ViewModel{
    private static PlayerView viewModelInstance;

    /**
     * Define a instância do ViewModel a ser compartilhada.
     */
    public static void setViewModel(PlayerView viewModel) {
        viewModelInstance = viewModel;
    }

    /**
     * Obtém a instância compartilhada do ViewModel.
     */
    public static PlayerView getViewModel() {
        return viewModelInstance;
    }
}