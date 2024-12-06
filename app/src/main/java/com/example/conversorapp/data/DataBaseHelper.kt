package com.example.conversorapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object{
        private const val DATABASE_NAME = "CarteiraVirtual.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_RECURSOS = "recursos"
        const val COLUMN_ID = "_id"
        const val COLUMN_MOEDA = "moeda"
        const val COLUMN_VALOR = "valor"

        private const val CREATE_TABLE_RECURSOS = """
            CREATE TABLE $TABLE_RECURSOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MOEDA TEXT NOT NULL,
                $COLUMN_VALOR REAL NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_RECURSOS)
        db.execSQL("INSERT INTO recursos (moeda, valor) VALUES ('BRL', 0), ('USD', 0), ('EUR', 0), ('ETH', 0), ('BTC', 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECURSOS")
        onCreate(db)
    }
}