package com.berkeh.connector;
import android.app.job.*;import android.content.*;import android.database.Cursor;import org.json.*;
public final class SyncJob extends JobService {
 private volatile boolean stopped;
 static volatile boolean running;
 static void schedule(Context c){if(!Config.enabled(c))return;JobScheduler j=c.getSystemService(JobScheduler.class);ComponentName name=new ComponentName(c,SyncJob.class);j.schedule(new JobInfo.Builder(711,name).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setBackoffCriteria(30000,JobInfo.BACKOFF_POLICY_EXPONENTIAL).build());if(j.getPendingJob(712)==null)j.schedule(new JobInfo.Builder(712,name).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000).build());}
 static void cancel(Context c){JobScheduler j=c.getSystemService(JobScheduler.class);j.cancel(711);j.cancel(712);}
 public boolean onStartJob(JobParameters p){stopped=false;new Thread(()->{boolean retry=drain(this);if(!stopped)jobFinished(p,retry&&p.getJobId()==711);},"berkeh-sync").start();return true;}
 public boolean onStopJob(JobParameters p){stopped=true;return Config.enabled(this);}
 static synchronized boolean drain(Context c){if(!Config.enabled(c))return false;running=true;try(Store s=new Store(c)){
  try(Cursor cur=s.pending()){while(cur.moveToNext()){if(!Config.enabled(c))return false;String id=cur.getString(0);try{JSONObject r=Api.post(c,"transactions",new JSONObject(Crypto.decrypt(cur.getString(1))));String detail="ارسال شد";if(r.optBoolean("duplicate"))detail="قبلاً در سایت ثبت شده";else if(r.optBoolean("ignored"))detail="سایت نپذیرفت: فیلتر فرستنده";else{JSONObject tx=r.optJSONObject("transaction");if(tx!=null){String match=tx.optString("match_status");detail="ثبت در سایت • "+tx.optString("parser_status")+" • "+match;if(tx.optInt("matched_order_id")>0)detail+=" • سفارش "+tx.optInt("matched_order_id");}}s.result(id,"sent",detail);
   }catch(Exception e){boolean permanent=e instanceof Api.HttpError&&(((Api.HttpError)e).code==400||((Api.HttpError)e).code==422||((Api.HttpError)e).code==413);s.result(id,permanent?"failed":"pending",Api.explain(e));Config.prefs(c).edit().putString("last",Api.explain(e)).apply();if(!permanent)return true;}
  }}
  if(Config.enabled(c)){try{Api.post(c,"heartbeat",Api.identity(c));Config.prefs(c).edit().putString("last","آخرین ارتباط موفق: "+android.text.format.DateFormat.format("MM/dd HH:mm",System.currentTimeMillis())).apply();}catch(Exception e){Config.prefs(c).edit().putString("last",Api.explain(e)).apply();}}
  s.prune();return s.count("pending")>0;
 }catch(Exception e){Config.prefs(c).edit().putString("last","خطای صف یا رمزگشایی؛ تنظیمات و فضای گوشی بررسی شود.").apply();return true;}finally{running=false;}}
}
