package com.simple0926.huazaina.nativeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class Db extends SQLiteOpenHelper {
    private static final String NAME = "huazaina-native.db";
    private static final int VERSION = 1;
    private final Context appContext;

    Db(Context context) { super(context, NAME, null, VERSION); this.appContext = context.getApplicationContext(); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE records (id TEXT PRIMARY KEY, amt INTEGER NOT NULL, cat TEXT NOT NULL, kind TEXT NOT NULL, date TEXT NOT NULL, pay TEXT, note TEXT, ts INTEGER)");
        db.execSQL("CREATE INDEX idx_records_date ON records(date)");
        db.execSQL("CREATE TABLE cats (id TEXT PRIMARY KEY, name TEXT NOT NULL, color TEXT NOT NULL, icon TEXT, custom INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE meta (k TEXT PRIMARY KEY, v TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据保护规则：升级前先写一份旧数据快照；迁移只允许加法（ALTER/建表/回填），
        // 绝不 DROP records / cats，绝不 DELETE 用户记录。
        backupBeforeUpgrade(db, oldVersion);
        // 当前 schema 版本 1；未来按 oldVersion 分支迁移，禁止破坏性语句。
    }

    private void backupBeforeUpgrade(SQLiteDatabase db, int oldVersion) {
        try {
            JSONObject root = new JSONObject();
            root.put("app", "花在哪");
            root.put("version", Core.VERSION);
            root.put("exportedAt", Instant.now().toString());
            root.put("reason", "pre-upgrade-v" + oldVersion);
            JSONArray recs = new JSONArray();
            Cursor c = db.query("records", null, null, null, null, null, null);
            while (c.moveToNext()) {
                JSONObject o = new JSONObject();
                o.put("id", c.getString(c.getColumnIndexOrThrow("id")));
                o.put("amt", c.getInt(c.getColumnIndexOrThrow("amt")));
                o.put("cat", c.getString(c.getColumnIndexOrThrow("cat")));
                o.put("kind", c.getString(c.getColumnIndexOrThrow("kind")));
                o.put("date", c.getString(c.getColumnIndexOrThrow("date")));
                o.put("pay", c.getString(c.getColumnIndexOrThrow("pay")));
                o.put("note", c.getString(c.getColumnIndexOrThrow("note")));
                o.put("ts", c.getLong(c.getColumnIndexOrThrow("ts")));
                recs.put(o);
            }
            c.close();
            root.put("records", recs);
            File f = new File(appContext.getFilesDir(), "huazaina-pre-upgrade-v" + oldVersion + ".json");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(root.toString(2).getBytes("UTF-8"));
            fos.close();
        } catch (Exception ignored) {}
    }

    File autoFile() { return new File(appContext.getFilesDir(), "huazaina-autosave.json"); }

    void writeAutoBackup(List<Rec> records, List<Cat> custom) {
        try {
            String text = Core.backupJson(records, custom, Instant.now().toString());
            File tmp = new File(appContext.getFilesDir(), "huazaina-autosave.tmp");
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(text.getBytes("UTF-8"));
            fos.getFD().sync();
            fos.close();
            File dst = autoFile();
            if (dst.exists()) dst.delete();
            if (!tmp.renameTo(dst)) {
                FileOutputStream out = new FileOutputStream(dst);
                out.write(text.getBytes("UTF-8"));
                out.close();
                tmp.delete();
            }
        } catch (Exception ignored) {}
    }

    ImportData readAutoBackup() {
        try {
            File f = autoFile();
            if (!f.exists() || f.length() == 0) return null;
            FileInputStream in = new FileInputStream(f);
            byte[] b = new byte[(int) f.length()];
            int off = 0, n;
            while (off < b.length && (n = in.read(b, off, b.length - off)) > 0) off += n;
            in.close();
            return Core.parseBackup(new String(b, "UTF-8"));
        } catch (Exception e) { return null; }
    }

    int autoBackupCount() {
        ImportData d = readAutoBackup();
        return d == null ? 0 : d.records.size();
    }

    String autoBackupTime() {
        try {
            File f = autoFile();
            if (!f.exists()) return "";
            return new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date(f.lastModified()));
        } catch (Exception e) { return ""; }
    }

    void deleteAutoBackup() {
        File f = autoFile();
        if (f.exists()) f.delete();
        File tmp = new File(appContext.getFilesDir(), "huazaina-autosave.tmp");
        if (tmp.exists()) tmp.delete();
    }


    List<Rec> records() {
        List<Rec> out = new ArrayList<>();
        Cursor c = getReadableDatabase().query("records", null, null, null, null, null, "date ASC, ts ASC");
        while (c.moveToNext()) {
            Rec r = new Rec();
            r.id = c.getString(c.getColumnIndexOrThrow("id"));
            r.amt = c.getInt(c.getColumnIndexOrThrow("amt"));
            r.cat = c.getString(c.getColumnIndexOrThrow("cat"));
            r.kind = c.getString(c.getColumnIndexOrThrow("kind"));
            r.date = c.getString(c.getColumnIndexOrThrow("date"));
            r.pay = c.getString(c.getColumnIndexOrThrow("pay"));
            r.note = c.getString(c.getColumnIndexOrThrow("note"));
            r.ts = c.getLong(c.getColumnIndexOrThrow("ts"));
            out.add(r);
        }
        c.close();
        return out;
    }

    List<Cat> customCats() {
        List<Cat> out = new ArrayList<>();
        Cursor c = getReadableDatabase().query("cats", null, "custom=1", null, null, null, "name COLLATE LOCALIZED ASC");
        while (c.moveToNext()) {
            out.add(new Cat(c.getString(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("color")),
                    c.getString(c.getColumnIndexOrThrow("icon")), true));
        }
        c.close();
        return out;
    }

    void addRecord(Rec r) {
        ContentValues v = new ContentValues();
        v.put("id", r.id); v.put("amt", r.amt); v.put("cat", r.cat); v.put("kind", Core.kindOf(r));
        v.put("date", r.date); v.put("pay", r.pay); v.put("note", r.note); v.put("ts", r.ts);
        getWritableDatabase().insertWithOnConflict("records", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    void updateRecord(Rec r) { addRecord(r); }

    void deleteRecord(String id) {
        getWritableDatabase().delete("records", "id=?", new String[]{id});
    }

    void addCustomCat(Cat cat) {
        ContentValues v = new ContentValues();
        v.put("id", cat.id); v.put("name", cat.name); v.put("color", cat.color);
        v.put("icon", cat.icon); v.put("custom", 1);
        getWritableDatabase().insertWithOnConflict("cats", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    void deleteCustomCat(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("cat", "other");
            db.update("records", v, "cat=?", new String[]{id});
            db.delete("cats", "id=?", new String[]{id});
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    void replaceAll(List<Rec> records, List<Cat> custom) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("records", null, null);
            db.delete("cats", null, null);
            for (Rec r : records) {
                ContentValues v = new ContentValues();
                v.put("id", r.id); v.put("amt", r.amt); v.put("cat", r.cat); v.put("kind", Core.kindOf(r));
                v.put("date", r.date); v.put("pay", r.pay); v.put("note", r.note); v.put("ts", r.ts);
                db.insertWithOnConflict("records", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            }
            for (Cat c : custom) {
                ContentValues v = new ContentValues();
                v.put("id", c.id); v.put("name", c.name); v.put("color", c.color);
                v.put("icon", c.icon); v.put("custom", 1);
                db.insertWithOnConflict("cats", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("records", null, null);
            db.delete("cats", null, null);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    String meta(String key, String def) {
        Cursor c = getReadableDatabase().query("meta", new String[]{"v"}, "k=?", new String[]{key}, null, null, null);
        String v = c.moveToFirst() ? c.getString(0) : def;
        c.close();
        return v;
    }

    void setMeta(String key, String value) {
        ContentValues v = new ContentValues();
        v.put("k", key); v.put("v", value);
        getWritableDatabase().insertWithOnConflict("meta", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    boolean hasWelcomed() { return "1".equals(meta("welcomed", "0")); }
    void setWelcomed() { setMeta("welcomed", "1"); }
}
