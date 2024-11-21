package com.example.forkid;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.File;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SMS_UTILS {
    private static final String TAG = "SMS_UTILS";
    public void deleteSmsUsingSql(Context context, SmsMessage smsMessage) {
        try {
            String id = getSmsId(context, smsMessage);
            if (id != null) {
                String cmd = "sqlite3 /data/data/com.android.providers.telephony/databases/mmssms.db \"DELETE FROM sms WHERE _id=" + id + ";\"";

                // Выполняем команду с root-доступом
                try {
                    // Открыть процесс для команды `su` (для получения root-доступа)
                    Process process = Runtime.getRuntime().exec(new String[]{"su", cmd});
                    // Открыть поток для передачи команды
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("exit\n");
                    // Считать вывод команды
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    // Закрыть все потоки
                    os.close();
                    reader.close();
                    // Ожидать завершения процесса и получить код завершения
                    int resultCode = process.waitFor();
                    if (resultCode == 0) {
                        Log.d(TAG, "Команда выполнена успешно: \n" + output.toString());
                    } else {
                        Log.d(TAG, "Ошибка выполнения команды: код " + resultCode);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при выполнении команды: " + e.getMessage(), e);
                }
                Log.d("SMSReceiver", "Сообщение удалено из базы данных с помощью root-доступа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSmsId(Context context, SmsMessage smsMessage) {
        String smsUri = "content://sms/inbox";
        Uri uri = Uri.parse(smsUri);
        String id = null;

        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String address = c.getString(c.getColumnIndexOrThrow("address"));
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String messageId = c.getString(c.getColumnIndexOrThrow("_id"));

                if (address.equals(smsMessage.getOriginatingAddress()) && body.equals(smsMessage.getMessageBody())) {
                    id = messageId;
                    break;
                }
            }
            c.close();
        }
        return id;
    }

}
