package com.senai.audioplayerservice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

public class AudioUtil {

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
        } catch (Exception e) {
            Log.e("AudioUtils", "Erro ao consultar ContentResolver", e);
            // Fallback: extrai do último segmento do URI
            String lastSegment = uri.getLastPathSegment();
            if (lastSegment != null) {
                fileName = lastSegment.replace(".mp3", "").replace(".MP3", "");
            }
        }

        return fileName;
    }

}
