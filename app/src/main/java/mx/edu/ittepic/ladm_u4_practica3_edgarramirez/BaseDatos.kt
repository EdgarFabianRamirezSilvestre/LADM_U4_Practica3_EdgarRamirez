package mx.edu.ittepic.ladm_u4_practica3_edgarramirez

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context: Context?, name : String, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE CELULAR(ID INTEGER PRIMARY KEY AUTOINCREMENT, MARCA VARCHAR(100), MODELO VARCHAR(100), PRECIO DOUBLE)")
        db.execSQL("CREATE TABLE ENTRANTES(NUMERO VARCHAR(200), MENSAJE VARCHAR(2000), CONTESTADO CHAR(1))")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}