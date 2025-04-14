package com.senai.audioplayerservice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

/**
 * Classe utilitária para operações com arquivos de áudio
 * Padrão: Utility Class (métodos estáticos)
 */
public class AudioUtil {

    private static final String TAG = "AudioUtil";

    /**
     * Extrai o nome legível de um arquivo de áudio via ContentResolver
     * @param uri URI do arquivo (content:// ou file://)
     * @return Nome do arquivo sem extensão ou fallback padrão
     */
    public static String extractSongName(Context context, Uri uri) {
        String fileName = "Música desconhecida";

        // Primeiro tenta obter via ContentResolver (mais confiável)
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                    // Remove extensão se existir
                    fileName = fileName.replace(".mp3", "").replace(".MP3", "");
                }
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Sem permissão para acessar o arquivo: " + uri);
            return "Arquivo bloqueado";
        } catch (Exception e) {
            Log.e(TAG, "Falha inesperada ao ler metadata", e);
            return "Música desconhecida";
        }

        return fileName;
    }

}
