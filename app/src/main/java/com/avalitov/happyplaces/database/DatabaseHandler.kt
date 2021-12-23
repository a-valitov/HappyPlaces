package com.avalitov.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.avalitov.happyplaces.models.HappyPlaceModel

class DatabaseHandler(context: Context) :
SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1    // this DB version
        private val DATABASE_NAME = "HappyPlacesDatabase"
        private val TABLE_PLACES = "HappyPlacesTable"

        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IMAGE_PATH = "image_path"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // a String value containing SQL instruction
        val CREATE_PLACES_TABLE = ("CREATE TABLE $TABLE_PLACES(" +
                "$COLUMN_ID INTEGER PRIMARY KEY," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_IMAGE_PATH TEXT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_DATE TEXT," +
                "$COLUMN_LOCATION TEXT," +
                "$COLUMN_LATITUDE DOUBLE," +
                "$COLUMN_LONGITUDE DOUBLE)")

        db?.execSQL(CREATE_PLACES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLACES")
        onCreate(db)
    }

    fun addPlace(place: HappyPlaceModel) : Long{
        val values = ContentValues()
        values.put(COLUMN_TITLE, place.title)
        values.put(COLUMN_IMAGE_PATH, place.imagePath)
        values.put(COLUMN_DESCRIPTION, place.description)
        values.put(COLUMN_DATE, place.date)
        values.put(COLUMN_LOCATION, place.location)
        values.put(COLUMN_LATITUDE, place.latitude)
        values.put(COLUMN_LONGITUDE, place.longitude)

        val db = this.writableDatabase
        val result = db.insert(TABLE_PLACES, null, values)

        db.close()

        return result
    }

    fun getAllPlacesList() : ArrayList<HappyPlaceModel> {

        val happyPlacesList = ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_PLACES"
        val db = this.readableDatabase

        try {
            val cursor : Cursor = db.rawQuery(selectQuery, null)

            if(cursor.moveToFirst()){
                do {
                    val place = HappyPlaceModel(
                        cursor.getInt((cursor.getColumnIndex(COLUMN_ID))),
                        cursor.getString((cursor.getColumnIndex(COLUMN_TITLE))),
                        cursor.getString((cursor.getColumnIndex(COLUMN_IMAGE_PATH))),
                        cursor.getString((cursor.getColumnIndex(COLUMN_DESCRIPTION))),
                        cursor.getString((cursor.getColumnIndex(COLUMN_DATE))),
                        cursor.getString((cursor.getColumnIndex(COLUMN_LOCATION))),
                        cursor.getDouble((cursor.getColumnIndex(COLUMN_LATITUDE))),
                        cursor.getDouble((cursor.getColumnIndex(COLUMN_LONGITUDE)))
                    )

                    happyPlacesList.add(place)
                } while (cursor.moveToNext())
            }

            cursor.close()

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

//        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLACES", null)
//
//        while (cursor.moveToNext()) {
//
//
//            val titleValue = (cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)))
//
//            val place = HappyPlaceModel()
//
//            happyPlacesList.add(place)
//        }
//
//        cursor.close()

        return happyPlacesList
    }

}