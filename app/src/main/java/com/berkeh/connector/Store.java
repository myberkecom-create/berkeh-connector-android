package com.berkeh.connector;
import android.content.*;import android.database.*;import android.database.sqlite.*;import org.json.*;
public final class Store extends SQLiteOpenHelper {
 Store(Context c){super(c,"queue.db",null,1);}
 public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE messages(id TEXT PRIMARY KEY,payload TEXT NOT NULL,sender TEXT,created INTEGER,status TEXT,detail TEXT,attempts INTEGER DEFAULT 0)");}
 public void onUpgrade(SQLiteDatabase d,int o,int n){}
 void add(String id,String payload,String sender,long time)throws Exception{ContentValues v=new ContentValues();v.put("id",id);v.put("payload",Crypto.encrypt(payload));v.put("sender",sender);v.put("created",time);v.put("status","pending");v.put("detail","در صف ارسال");getWritableDatabase().insertWithOnConflict("messages",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
 Cursor pending(){return getReadableDatabase().rawQuery("SELECT id,payload FROM messages WHERE status='pending' ORDER BY created LIMIT 20",null);}
 void result(String id,String status,String detail){ContentValues v=new ContentValues();v.put("status",status);v.put("detail",detail);if(status.equals("sent"))v.put("payload","");getWritableDatabase().update("messages",v,"id=?",new String[]{id});getWritableDatabase().execSQL("UPDATE messages SET attempts=attempts+1 WHERE id=?",new Object[]{id});}
 int count(String state){try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM messages WHERE status=?",new String[]{state})){c.moveToFirst();return c.getInt(0);}}
 void retry(){getWritableDatabase().execSQL("UPDATE messages SET status='pending',detail='در صف تلاش مجدد' WHERE status='failed'");}
 void clear(){getWritableDatabase().delete("messages",null,null);}
 void prune(){getWritableDatabase().delete("messages","status='sent' AND created<?",new String[]{Long.toString(System.currentTimeMillis()-30L*86400000)});}
 String report(){StringBuilder b=new StringBuilder();try(Cursor c=getReadableDatabase().rawQuery("SELECT sender,created,status,detail,attempts FROM messages ORDER BY created DESC LIMIT 30",null)){while(c.moveToNext()){b.append(c.getString(0)).append(" • ").append(android.text.format.DateFormat.format("yyyy/MM/dd HH:mm",c.getLong(1))).append("\n").append(c.getString(3)).append(" | تلاش: ").append(c.getInt(4)).append("\n\n");}}return b.length()==0?"هنوز پیامکی دریافت نشده است.":b.toString();}
}
