package com.example.electricitybillcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ElectricityDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "electricity_bills";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "consumer_name";
    public static final String COL_NUMBER = "consumer_number";
    public static final String COL_UNITS = "units";
    public static final String COL_AMOUNT = "bill_amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_NUMBER + " TEXT NOT NULL, " +
                COL_UNITS + " INTEGER NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // CREATE: Insert a new electricity bill
    public long insertBill(String name, String number,
                           int units, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_NUMBER, number);
        values.put(COL_UNITS, units);
        values.put(COL_AMOUNT, amount);

        return db.insert(TABLE_NAME, null, values);
    }

    // READ: Get all saved bills
    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );
    }

    // UPDATE: Edit an existing bill
    @SuppressWarnings("unused")
    public int updateBill(int id, String name, String number,
                          int units, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_NUMBER, number);
        values.put(COL_UNITS, units);
        values.put(COL_AMOUNT, amount);

        return db.update(
                TABLE_NAME,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // DELETE: Remove an existing bill
    @SuppressWarnings("unused")
    public int deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_NAME,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }
}
