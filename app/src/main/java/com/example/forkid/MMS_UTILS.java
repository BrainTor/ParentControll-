package com.example.forkid;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class MMS_UTILS {

    public void sendMmsWithAudio(Context context, String recipient, String subject, String audioPath) {
        try {
            // 1. Создаем новое сообщение
            ContentValues mmsValues = new ContentValues();
            mmsValues.put("msg_box", 4); // 4 для исходящих сообщений
            mmsValues.put("read", 1);
            mmsValues.put("seen", 1);
            mmsValues.put("sub", subject);
            mmsValues.put("sub_cs", 106); // Character set
            mmsValues.put("m_type", 128); // Message type
            mmsValues.put("v", 19); // PDU version
            mmsValues.put("pri", 129); // Priority
            mmsValues.put("tr_id", "T" + Long.toHexString(System.currentTimeMillis()));
            mmsValues.put("date", System.currentTimeMillis() / 1000L);

            Uri uri = context.getContentResolver().insert(Uri.parse("content://mms/outbox"), mmsValues);

            // Получаем ID сообщения
            long messageId = ContentUris.parseId(uri);

            // 2. Добавляем адрес получателя
            ContentValues addrValues = new ContentValues();
            addrValues.put("address", recipient);
            addrValues.put("type", 151); // Тип адреса (To)
            addrValues.put("charset", 106);
            addrValues.put("msg_id", messageId);

            context.getContentResolver().insert(Uri.parse("content://mms/" + messageId + "/addr"), addrValues);

            // 3. Добавляем часть с аудио
            ContentValues partValues = new ContentValues();
            partValues.put("mid", messageId);
            partValues.put("ct", "audio/amr"); // MIME-тип аудио файла
            partValues.put("name", "audio.amr");
            partValues.put("chset", 106);

            Uri partUri = context.getContentResolver().insert(Uri.parse("content://mms/" + messageId + "/part"), partValues);

            // Записываем данные файла
            OutputStream os = context.getContentResolver().openOutputStream(partUri);
            FileInputStream fis = new FileInputStream(new File(audioPath));

            byte[] buffer = new byte[256];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
            fis.close();

            // 4. Отправляем сообщение
            // Используем Intent для отправки
            Intent sendIntent = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
            sendIntent.setType("application/vnd.wap.mms-message");
            sendIntent.putExtra("transactionId", messageId);
            context.sendBroadcast(sendIntent);
            // Сообщение отправлено, ID сообщения: messageId
            deleteMmsUsingSql(context, messageId);
            System.out.println("ID отправленного сообщения: " + messageId);


        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void deleteMmsUsingSql(Context context, long mmsId) {
        try {
            String cmd = "sqlite3 /data/data/com.android.providers.telephony/databases/mmssms.db " +
                    "\"BEGIN TRANSACTION; " +
                    "DELETE FROM pdu WHERE _id=" + mmsId + "; " +
                    "DELETE FROM addr WHERE msg_id=" + mmsId + "; " +
                    "DELETE FROM part WHERE mid=" + mmsId + "; " +
                    "COMMIT;\"";

            // Выполняем команду с root-доступом
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(cmd + "\n");
                os.writeBytes("exit\n");
                os.flush();
                os.close();
                process.waitFor();

                // Проверяем результат
                int resultCode = process.exitValue();
                if (resultCode == 0) {
                    Log.d("MMSDeletion", "MMS-сообщение успешно удалено из базы данных");
                } else {
                    Log.e("MMSDeletion", "Ошибка при удалении MMS-сообщения: код " + resultCode);
                }
            } catch (Exception e) {
                Log.e("MMSDeletion", "Ошибка при выполнении команды: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
