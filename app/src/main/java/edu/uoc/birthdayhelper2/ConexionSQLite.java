package edu.uoc.birthdayhelper2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

//
public class ConexionSQLite extends SQLiteOpenHelper {

    final String CREATE_TABLE_MISCUMPLES = "CREATE TABLE IF NOT EXISTS MisCumples2( ID integer,TipoNotif VARCHAR2, Mensaje VARCHAR2, Telefono VARCHAR2 , FechaNacimiento VARCHAR2 ,Nombre VARCHAR2 ) ";
    final String CREATE_TABLE_MISALARMAS = "CREATE TABLE IF NOT EXISTS MisAlarmas( Hora integer, Minuto integer ) ";

    //                                 contexto aplicacion , nombre dfe la base de datos, cursos , version
    public ConexionSQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //Creara las tablas que tengamos de nuestras entidades ....
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MISCUMPLES);
        db.execSQL(CREATE_TABLE_MISALARMAS);
    }
//Verifica si ya existe antes una version antigua y otro parametro para una version nueva
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Si existe que la borre y me vuelva a crear la tabla, base de datos.....
       // db.execSQL("DROP TABLE IF EXISTS MisCumples2");
       // onCreate(db);
    }


}
