package com.berkeh.connector;

import android.content.*;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public final class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent i) {

        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(i.getAction())) {
            return;
        }

        SmsMessage[] parts =
                Telephony.Sms.Intents.getMessagesFromIntent(i);

        if (parts == null || parts.length == 0) {
            return;
        }

        String sender = parts[0].getOriginatingAddress();

        StringBuilder b = new StringBuilder();

        for (SmsMessage p : parts) {
            b.append(p.getMessageBody());
        }

        String body = b.toString();

        long time = parts[0].getTimestampMillis();

        PendingResult pending = goAsync();

        new Thread(() -> {

            try {

                JSONObject data = new JSONObject();

                data.put("sender", sender);
                data.put("body", body);
                data.put("received_at", time);
                data.put("source", "debug-sms");

                String id =
                        "sms-debug-" + time;

                try (Store s = new Store(c)) {

                    s.add(
                            id,
                            data.toString(),
                            sender,
                            time
                    );
                }

                Config.prefs(c)
                        .edit()
                        .putString(
                                "last",
                                "SMS دریافت شد از: " + sender
                        )
                        .apply();

            } catch (Exception e) {

                Config.prefs(c)
                        .edit()
                        .putString(
                                "last",
                                "خطا در ذخیره SMS: " + e.getMessage()
                        )
                        .apply();

            } finally {

                pending.finish();

            }

        }, "berkeh-sms-debug").start();
    }
}
