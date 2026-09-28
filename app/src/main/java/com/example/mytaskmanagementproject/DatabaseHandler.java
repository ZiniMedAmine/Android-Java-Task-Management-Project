package com.example.mytaskmanagementproject;

import static com.google.android.ads.mediationtestsuite.utils.DataStore.getContext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3; // Increment version
    private static final String DATABASE_NAME = "taskManager";
    private static final String TABLE_TASKS = "tasks";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESC = "description";
    private static final String KEY_DDL = "deadline";
    private static final String KEY_STATUS = "status";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IMAGE_PATH = "image_path"; // New column

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_TITLE + " TEXT, "
                + KEY_DESC + " TEXT, "
                + KEY_DDL + " INTEGER, "
                + KEY_STATUS + " INTEGER, "
                + KEY_USER_ID + " TEXT, "
                + KEY_IMAGE_PATH + " TEXT" // Add image path column
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + KEY_USER_ID + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + KEY_IMAGE_PATH + " TEXT");
        }
    }

    void addTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.get_title());
        values.put(KEY_DESC, task.get_description());

        if (task.get_deadline() != null) {
            values.put(KEY_DDL, task.get_deadline().getTime());
        } else {
            values.putNull(KEY_DDL);
        }

        values.put(KEY_STATUS, (task.get_status() != null && task.get_status()) ? 1 : 0);
        values.put(KEY_USER_ID, task.get_userId());

        // Add image path
        if (task.get_imagePath() != null) {
            values.put(KEY_IMAGE_PATH, task.get_imagePath());
        } else {
            values.putNull(KEY_IMAGE_PATH);
        }

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    Task getTask(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[] {KEY_ID, KEY_TITLE, KEY_DESC, KEY_DDL, KEY_STATUS, KEY_USER_ID, KEY_IMAGE_PATH},
                KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor == null) return null;

        try {
            if (!cursor.moveToFirst()) return null;

            int tid = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESC));

            Date deadline = null;
            int ddlIndex = cursor.getColumnIndex(KEY_DDL);
            if (!cursor.isNull(ddlIndex)) {
                long ddlMillis = cursor.getLong(ddlIndex);
                deadline = new Date(ddlMillis);
            }

            boolean status = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)) == 1;
            String userId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_PATH));

            return new Task(tid, title, desc, deadline, status, userId, imagePath);
        } finally {
            cursor.close();
        }
    }

    public List<Task> getUserTasks(String userId){
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_USER_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});

        try {
            if (cursor.moveToFirst()){
                do {
                    Task task = new Task();
                    task.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                    task.set_title(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
                    task.set_description(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESC)));

                    int ddlIndex = cursor.getColumnIndex(KEY_DDL);
                    if (!cursor.isNull(ddlIndex)) {
                        long ddlMillis = cursor.getLong(ddlIndex);
                        task.set_deadline(new Date(ddlMillis));
                    } else {
                        task.set_deadline(null);
                    }

                    task.set_status(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)) == 1);
                    task.set_userId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
                    task.set_imagePath(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_PATH)));

                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return taskList;
    }

    public List<Task> getAllTasks(){
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()){
                do {
                    Task task = new Task();
                    task.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                    task.set_title(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
                    task.set_description(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESC)));

                    int ddlIndex = cursor.getColumnIndex(KEY_DDL);
                    if (!cursor.isNull(ddlIndex)) {
                        long ddlMillis = cursor.getLong(ddlIndex);
                        task.set_deadline(new Date(ddlMillis));
                    } else {
                        task.set_deadline(null);
                    }

                    task.set_status(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)) == 1);
                    task.set_userId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
                    task.set_imagePath(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_PATH)));

                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return taskList;
    }

    public int updateTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.get_title());
        values.put(KEY_DESC, task.get_description());

        if (task.get_deadline() != null) {
            values.put(KEY_DDL, task.get_deadline().getTime());
        } else {
            values.putNull(KEY_DDL);
        }

        values.put(KEY_STATUS, (task.get_status() != null && task.get_status()) ? 1 : 0);
        values.put(KEY_USER_ID, task.get_userId());

        // Update image path
        if (task.get_imagePath() != null) {
            values.put(KEY_IMAGE_PATH, task.get_imagePath());
        } else {
            values.putNull(KEY_IMAGE_PATH);
        }

        return db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[] {String.valueOf(task.get_id())});
    }

    public void deleteTask(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        Task task = getTask(id);
        if (task != null && task.get_imagePath() != null) {
            ImageUtils.deleteImageFile(getContext(), task.get_imagePath());
        }

        db.delete(TABLE_TASKS, KEY_ID + " =?", new String[] {String.valueOf(id)});
        db.close();
    }

    public void deleteUserTasks(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        List<Task> userTasks = getUserTasks(userId);
        for (Task task : userTasks) {
            if (task.get_imagePath() != null) {
                ImageUtils.deleteImageFile(getContext(), task.get_imagePath());
            }
        }

        db.delete(TABLE_TASKS, KEY_USER_ID + " =?", new String[] {userId});
        db.close();
    }

    public void markTaskComplete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, 1);
        db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[] {String.valueOf(id)});
        db.close();
    }

    public void close() {
        // This method is already available in SQLiteOpenHelper
        // We're just providing an explicit call
        getReadableDatabase().close();
    }
}