package com.example.harshatrial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Xpot_v4.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_STATUS = "status";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_VEHICLE = "vehicle_details";

    public static final String TABLE_PARKING = "parking_locations";
    public static final String COL_PARKING_ID = "id";
    public static final String COL_OWNER_ID = "owner_id";
    public static final String COL_PARKING_NAME = "name";
    public static final String COL_PARKING_ADDRESS = "address";
    public static final String COL_TOTAL_SLOTS = "total_slots";
    public static final String COL_AVAILABLE_SLOTS = "available_slots";
    public static final String COL_PARKING_STATUS = "status";
    public static final String COL_PARKING_FEATURES = "features";
    public static final String COL_PARKING_PRICE_PER_HOUR = "price_per_hour";

    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOKING_ID = "id";
    public static final String COL_BOOKING_USER_ID = "user_id";
    public static final String COL_BOOKING_LOCATION_ID = "location_id";
    public static final String COL_BOOKING_TIME = "booking_time";
    public static final String COL_BOOKING_STATUS = "status";
    public static final String COL_BOOKING_NAME = "booker_name";
    public static final String COL_BOOKING_PHONE = "booker_phone";
    public static final String COL_BOOKING_VEHICLE = "vehicle_details";
    public static final String COL_BOOKING_HOURS = "hours";
    public static final String COL_BOOKING_TOTAL_PRICE = "total_price";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_ROLE + " TEXT, " +
                COL_USER_STATUS + " TEXT, " +
                COL_USER_PHONE + " TEXT DEFAULT '', " +
                COL_USER_VEHICLE + " TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE " + TABLE_PARKING + " (" +
                COL_PARKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OWNER_ID + " INTEGER, " +
                COL_PARKING_NAME + " TEXT, " +
                COL_PARKING_ADDRESS + " TEXT, " +
                COL_TOTAL_SLOTS + " INTEGER, " +
                COL_AVAILABLE_SLOTS + " INTEGER, " +
                COL_PARKING_STATUS + " TEXT, " +
                COL_PARKING_FEATURES + " TEXT DEFAULT '', " +
                COL_PARKING_PRICE_PER_HOUR + " REAL DEFAULT 5)");

        db.execSQL("CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOKING_USER_ID + " INTEGER, " +
                COL_BOOKING_LOCATION_ID + " INTEGER, " +
                COL_BOOKING_TIME + " TEXT, " +
                COL_BOOKING_STATUS + " TEXT, " +
                COL_BOOKING_NAME + " TEXT DEFAULT '', " +
                COL_BOOKING_PHONE + " TEXT DEFAULT '', " +
                COL_BOOKING_VEHICLE + " TEXT DEFAULT '', " +
                COL_BOOKING_HOURS + " INTEGER DEFAULT 1, " +
                COL_BOOKING_TOTAL_PRICE + " REAL DEFAULT 5)");

        // Initial Super Admin
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, "Super Admin");
        values.put(COL_USER_EMAIL, "admin@xpot.com");
        values.put(COL_USER_PASSWORD, "admin123");
        values.put(COL_USER_ROLE, "Super Admin");
        values.put(COL_USER_STATUS, "Approved");
        db.insert(TABLE_USERS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            addColumnIfMissing(db, TABLE_USERS, COL_USER_PHONE, "TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_USERS, COL_USER_VEHICLE, "TEXT DEFAULT ''");

            addColumnIfMissing(db, TABLE_PARKING, COL_PARKING_FEATURES, "TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_PARKING, COL_PARKING_PRICE_PER_HOUR, "REAL DEFAULT 5");

            addColumnIfMissing(db, TABLE_BOOKINGS, COL_BOOKING_NAME, "TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_BOOKINGS, COL_BOOKING_PHONE, "TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_BOOKINGS, COL_BOOKING_VEHICLE, "TEXT DEFAULT ''");
            addColumnIfMissing(db, TABLE_BOOKINGS, COL_BOOKING_HOURS, "INTEGER DEFAULT 1");
            addColumnIfMissing(db, TABLE_BOOKINGS, COL_BOOKING_TOTAL_PRICE, "REAL DEFAULT 5");
        }
    }

    private void addColumnIfMissing(SQLiteDatabase db, String tableName, String columnName, String typeSql) {
        if (!columnExists(db, tableName, columnName)) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + typeSql);
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String current = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equals(current)) {
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    public long registerUser(String name, String email, String password, String role, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_ROLE, role);
        values.put(COL_USER_STATUS, status);
        values.put(COL_USER_PHONE, "");
        values.put(COL_USER_VEHICLE, "");
        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public int updateUserProfile(int userId, String name, String phone, String vehicle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_PHONE, phone);
        values.put(COL_USER_VEHICLE, vehicle);
        return db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public Cursor loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?", new String[]{email, password});
    }
    
    public Cursor getPendingAdmins() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + "='Admin' AND " + COL_USER_STATUS + "='Pending'", null);
    }

    public void approveAdmin(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_STATUS, "Approved");
        db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }
    
    public long addParking(int ownerId, String name, String address, int totalSlots) {
        return addParking(ownerId, name, address, totalSlots, "", 5.0);
    }

    public long addParking(int ownerId, String name, String address, int totalSlots, String features, double pricePerHour) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_OWNER_ID, ownerId);
        values.put(COL_PARKING_NAME, name);
        values.put(COL_PARKING_ADDRESS, address);
        values.put(COL_TOTAL_SLOTS, totalSlots);
        values.put(COL_AVAILABLE_SLOTS, totalSlots);
        values.put(COL_PARKING_STATUS, "Enabled");
        values.put(COL_PARKING_FEATURES, features);
        values.put(COL_PARKING_PRICE_PER_HOUR, pricePerHour);
        return db.insert(TABLE_PARKING, null, values);
    }

    public int updateParking(int parkingId, String name, String address, int totalSlots, int availableSlots, String features, double pricePerHour) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PARKING_NAME, name);
        values.put(COL_PARKING_ADDRESS, address);
        values.put(COL_TOTAL_SLOTS, totalSlots);
        values.put(COL_AVAILABLE_SLOTS, Math.min(availableSlots, totalSlots));
        values.put(COL_PARKING_FEATURES, features);
        values.put(COL_PARKING_PRICE_PER_HOUR, pricePerHour);
        return db.update(TABLE_PARKING, values, COL_PARKING_ID + "=?", new String[]{String.valueOf(parkingId)});
    }

    public Cursor getParkingById(int parkingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PARKING + " WHERE " + COL_PARKING_ID + "=?", new String[]{String.valueOf(parkingId)});
    }

    public Cursor getParkingByOwner(int ownerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PARKING + " WHERE " + COL_OWNER_ID + "=?", new String[]{String.valueOf(ownerId)});
    }

    public Cursor getAllParking() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PARKING + " WHERE " + COL_PARKING_STATUS + "='Enabled'", null);
    }

    public void updateSlots(int parkingId, int availableSlots) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AVAILABLE_SLOTS, availableSlots);
        db.update(TABLE_PARKING, values, COL_PARKING_ID + "=?", new String[]{String.valueOf(parkingId)});
    }

    public long addBooking(int userId, int locationId) {
        Cursor user = getUserById(userId);
        String userName = "";
        String phone = "";
        String vehicle = "";
        if (user != null && user.moveToFirst()) {
            userName = user.getString(user.getColumnIndexOrThrow(COL_USER_NAME));
            phone = user.getString(user.getColumnIndexOrThrow(COL_USER_PHONE));
            vehicle = user.getString(user.getColumnIndexOrThrow(COL_USER_VEHICLE));
            user.close();
        }
        return addBookingDetailed(userId, locationId, userName, phone, vehicle, 1);
    }

    public long addBookingDetailed(int userId, int locationId, String bookerName, String bookerPhone, String vehicleDetails, int hours) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_AVAILABLE_SLOTS + ", " + COL_PARKING_PRICE_PER_HOUR + " FROM " + TABLE_PARKING + " WHERE " + COL_PARKING_ID + "=?", new String[]{String.valueOf(locationId)});
        if (cursor != null && cursor.moveToFirst()) {
            int available = cursor.getInt(0);
            double pricePerHour = cursor.getDouble(1);
            if (available > 0) {
                updateSlots(locationId, available - 1);
                
                ContentValues values = new ContentValues();
                values.put(COL_BOOKING_USER_ID, userId);
                values.put(COL_BOOKING_LOCATION_ID, locationId);
                values.put(COL_BOOKING_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                values.put(COL_BOOKING_STATUS, "Confirmed");
                values.put(COL_BOOKING_NAME, bookerName);
                values.put(COL_BOOKING_PHONE, bookerPhone);
                values.put(COL_BOOKING_VEHICLE, vehicleDetails);
                values.put(COL_BOOKING_HOURS, Math.max(1, hours));
                values.put(COL_BOOKING_TOTAL_PRICE, Math.max(1, hours) * pricePerHour);
                long id = db.insert(TABLE_BOOKINGS, null, values);
                cursor.close();
                return id;
            }
            cursor.close();
        }
        return -1;
    }

    public Cursor getBookingsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT b." + COL_BOOKING_ID + ", b." + COL_BOOKING_TIME + ", b." + COL_BOOKING_STATUS + ", " +
                        "b." + COL_BOOKING_HOURS + ", b." + COL_BOOKING_TOTAL_PRICE + ", b." + COL_BOOKING_PHONE + ", " +
                        "b." + COL_BOOKING_VEHICLE + ", p." + COL_PARKING_NAME + " AS parking_name, " +
                        "p." + COL_PARKING_ADDRESS + " AS parking_address, u." + COL_USER_NAME + " AS owner_name, " +
                        "u." + COL_USER_PHONE + " AS owner_phone " +
                        "FROM " + TABLE_BOOKINGS + " b " +
                        "JOIN " + TABLE_PARKING + " p ON b." + COL_BOOKING_LOCATION_ID + " = p." + COL_PARKING_ID + " " +
                        "LEFT JOIN " + TABLE_USERS + " u ON p." + COL_OWNER_ID + " = u." + COL_USER_ID + " " +
                        "WHERE b." + COL_BOOKING_USER_ID + "=? ORDER BY b." + COL_BOOKING_ID + " DESC",
                new String[]{String.valueOf(userId)});
    }

    public Cursor getBookingsByOwner(int ownerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT b." + COL_BOOKING_ID + ", b." + COL_BOOKING_TIME + ", b." + COL_BOOKING_STATUS + ", " +
                        "b." + COL_BOOKING_NAME + ", b." + COL_BOOKING_PHONE + ", b." + COL_BOOKING_HOURS + ", " +
                        "b." + COL_BOOKING_TOTAL_PRICE + ", p." + COL_PARKING_NAME + " AS parking_name " +
                        "FROM " + TABLE_BOOKINGS + " b " +
                        "JOIN " + TABLE_PARKING + " p ON b." + COL_BOOKING_LOCATION_ID + " = p." + COL_PARKING_ID + " " +
                        "WHERE p." + COL_OWNER_ID + "=? ORDER BY b." + COL_BOOKING_ID + " DESC",
                new String[]{String.valueOf(ownerId)});
    }

    public Cursor getOwnerContactForParking(int parkingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT u." + COL_USER_NAME + " AS owner_name, u." + COL_USER_PHONE + " AS owner_phone FROM " + TABLE_PARKING + " p " +
                        "LEFT JOIN " + TABLE_USERS + " u ON p." + COL_OWNER_ID + "=u." + COL_USER_ID + " WHERE p." + COL_PARKING_ID + "=?",
                new String[]{String.valueOf(parkingId)});
    }

    public Cursor getOwnerAnalytics(int ownerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " +
                        "(SELECT COUNT(*) FROM " + TABLE_PARKING + " WHERE " + COL_OWNER_ID + "=?) AS total_locations, " +
                        "(SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " b JOIN " + TABLE_PARKING + " p ON b." + COL_BOOKING_LOCATION_ID + "=p." + COL_PARKING_ID + " WHERE p." + COL_OWNER_ID + "=?) AS total_bookings, " +
                        "(SELECT IFNULL(SUM(b." + COL_BOOKING_TOTAL_PRICE + "),0) FROM " + TABLE_BOOKINGS + " b JOIN " + TABLE_PARKING + " p ON b." + COL_BOOKING_LOCATION_ID + "=p." + COL_PARKING_ID + " WHERE p." + COL_OWNER_ID + "=?) AS revenue",
                new String[]{String.valueOf(ownerId), String.valueOf(ownerId), String.valueOf(ownerId)});
    }

    public Cursor getSuperAnalytics() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " +
                "(SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + "='Admin') AS owners, " +
                "(SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + "='Admin' AND " + COL_USER_STATUS + "='Pending') AS pending_owners, " +
                "(SELECT COUNT(*) FROM " + TABLE_PARKING + ") AS total_locations, " +
                "(SELECT COUNT(*) FROM " + TABLE_BOOKINGS + ") AS total_bookings", null);
    }
}
