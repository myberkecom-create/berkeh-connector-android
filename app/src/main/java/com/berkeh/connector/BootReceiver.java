package com.berkeh.connector;
import android.content.*;
public final class BootReceiver extends BroadcastReceiver {public void onReceive(Context c,Intent i){String a=i.getAction();if(Intent.ACTION_BOOT_COMPLETED.equals(a)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(a))SyncJob.schedule(c);}}
