package edu.uoc.birthdayhelper2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.uoc.birthdayhelper2.entidades.PhoneContact;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.makeText;
import static edu.uoc.birthdayhelper2.R.*;
import static edu.uoc.birthdayhelper2.R.drawable.*;

public class MainActivity extends AppCompatActivity {

/*
Para enviar mensajes SMS (Short Message Service) hay que utilizar la clase SMSManager. Esta clase tiene un método
estático llamado getDefault para obtener una referencia a un objeto de este tipo. Con ese objeto, podemos invocar
al método sendTextMessage para enviar fácilmente un mensaje corto de texto. Para poder realizar esta operación,
necesitamos, además, los permisos necesarios en el fichero de manifiesto:
<uses-permission android:name="android.permission.SEND_SMS"/>
 */


    private int hour;
    private int minute;
    private int hora;
    private int minuto;
    private boolean tengo_permisos = false;
    private final int PETICION_PERMISOS = 1;
    RecyclerView recyclerView2;
    public static ArrayList<PhoneContact> misCumplesArrayList = new ArrayList<>();
    ArrayList<PhoneContact> misCumplesArrayList2 = new ArrayList<>();
    private ConexionSQLite con;
    private ConexionSQLite con2;
    private MainAdapter adapter;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        //Instancia  de la clase conexionSqlite que me hara conectarme a la base de datos
        con = new ConexionSQLite(getApplicationContext(), "MisCumples2", null, 1);
        con2 = new ConexionSQLite(getApplicationContext(),"MisAlarmas",null,1);
        recyclerView2 = findViewById(R.id.recycler_view);
        misCumplesArrayList.removeAll(misCumplesArrayList);
        recyclerView2.setAdapter(null);

        if (checkSelfPermission("android.permission.READ_CONTACTS")
                != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.SEND_SMS")
                        != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{
                            "android.permission.READ_CONTACTS",
                            "android.permission.SEND_SMS"},
                    PETICION_PERMISOS);
        //deleteSQLite();
        //deleteSQLiteMisAlarmas();
        consultSQliteAlarm();
        getContacts();
        insertContacts();
        initializeRecyclerView();
        //Si la version es igual o superior a oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationChannel channel = new NotificationChannel("My Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    //Creo el menu lo inflo del recurso menu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itConfiguration:
                //Dialogo instrucciones

                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int selectedminute) {

                        deleteSQLiteMisAlarmas();
                        hour = hourOfDay;
                        minute = selectedminute;
                        //Metodo para cargar la alrma me imagino....comprueba la hora y la fecha de manera que si es el cumple zasca.... y si manda msn o notificacion.
                        setAlarm(hour,minute);
                        insertAlarm();
                        consultSQliteAlarm();
                        System.out.println("Hora aqui "+hourOfDay+":"+selectedminute);
                        Toast.makeText(getApplicationContext(), "La alarma ha quedado establecida a la siguiente hora: " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
                    }
                };

                int style = AlertDialog.THEME_HOLO_LIGHT;
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour, minute, true);
                timePickerDialog.setTitle("Selecciona Hora");
                System.out.println(hour+":"+minute);
                timePickerDialog.show();
                //insertAlarm();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAlarm (int hora, int minutos) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        /*Crear alarma*/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hora);
        calendar.set(Calendar.MINUTE, minutos);
        Intent intent = new Intent(getApplicationContext(), MyAlam.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    //Inserto los contactos extraidos de la aplicación de Contactos del telefono que los metí en un Array de objetos misCumples (misCumplesArrayList2) en la base de datos en la tabla misCumples2
    //Cuando no hay nada en la tabla para que solo inserte los datos de los conbtactos la primera vez y despues haya persistencia de los datos.

    private void insertContacts() {

        SQLiteDatabase db = con.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2", null);
        if (cursor.getCount() == 0) {

            for (int i = 0; i < misCumplesArrayList2.size(); i++) {
                Log.i("CONTACT", "TOTAL # OF CONTACTS" + Integer.toString(misCumplesArrayList2.size()));
                Log.i("CONTACT", "INSERT INTO DATABASE" + misCumplesArrayList2.get(i).getId() + misCumplesArrayList2.get(i).getTypeNotif() + misCumplesArrayList2.get(i).getMessage() +
                        misCumplesArrayList2.get(i).getTelephone() + misCumplesArrayList2.get(i).getBirthdate() + misCumplesArrayList2.get(i).getContactName());

                String insert = " INSERT INTO MisCumples2 (ID,TipoNotif,Mensaje,Telefono,FechaNacimiento,Nombre) VALUES ('" + misCumplesArrayList2.get(i).getId() + "','" + misCumplesArrayList2.get(i).getTypeNotif() + "','" + misCumplesArrayList2.get(i).getMessage() + "','" +
                        misCumplesArrayList2.get(i).getTelephone() + "','" + misCumplesArrayList2.get(i).getBirthdate() + "','" + misCumplesArrayList2.get(i).getContactName() + "')";
                db.execSQL(insert);
            }

            db.close();
        }
        cursor.close();
    }

    private void deleteSQLite() {

        SQLiteDatabase db = con.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2",null);
        if (cursor.getCount() > 0) {
            db.execSQL("DELETE FROM MisCumples2");
        }
        db.close();
        cursor.close();
    }

    private void consultFromSQLite() {


        SQLiteDatabase db = con.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2", null);

        while (cursor.moveToNext()) {
            //Importante me crea uno nuevo cada vez si no solo me coge el último ya que crea solo un objeto de tipo MisCumples.....
            PhoneContact contact = new PhoneContact();
            int id = cursor.getInt(0);
            String typoNotif = cursor.getString(1);
            String message = cursor.getString(2);
            String telephone = cursor.getString(3);
            String birthday = cursor.getString(4);
            String contactName = cursor.getString(5);

            contact.setId(id);
            contact.setTypeNotif(typoNotif);
            contact.setMessage(message);
            contact.setTelephone(telephone);
            contact.setBirthdate(birthday);
            contact.setContactName(contactName);
            contact.setBitmap((BitmapFactory.decodeStream(abrirFoto(id))));


            misCumplesArrayList.add(contact);
           // System.out.println("Prueba de que va y mne vuelve loco" + id + contactName + birthday);
           // System.out.println(misCumplesArrayList.toString());

          //  System.out.println("A ver los telefonos como salen ...." + telephone + "---------------" + id + "---------------------" + birthday);
        }

        Log.i("CONTACTos", "Array de la base de datos" + misCumplesArrayList2.toString());
        cursor.close();
        db.close();
    }

        //Saco los contactos de la aplicacion de contactos del teléfono y los meto en un Arraylist de objetos PhoneContact
    @SuppressLint("Range")
    public void getContacts() {
        //Pedimos que nos deje compartir los contactos que tenmemos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
        }

        //hememos enviado la direccion de lo que tenemos en el telefono contact apllication  y nos devuelve los datos a traves de un cursor
        String proyeccion[] = {ContactsContract.Contacts._ID};
        //ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Uri uriBirthday = ContactsContract.Data.CONTENT_URI;

        //Para las fechas tener en cuenta que terengo que poner ContactsContract.Data.CONTENT_URI

        String[] proyeccion2 = new String[]{
                ContactsContract.CommonDataKinds.Event.START_DATE};

        String filter =
                ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;

        //Probe con distintos filtros para sacr los diferentes telefonos pero solo sacaba de un tipo de cada contacto.

        String filterHOME =
                ContactsContract.CommonDataKinds.Phone.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

        String filterMOBILE =
                ContactsContract.CommonDataKinds.Phone.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

        String filterTYPE =
                ContactsContract.CommonDataKinds.Phone.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Phone.TYPE;


        String[] argsFiltro = new String[]{
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,

        };

        Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        Log.i("CONTACT", "TOTAL # OF CONTACTS" + Integer.toString(cursor.getCount()));

        //El cursor va a leer los datos hasta el final de los contactos

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext() ) {

                //Sacamos los datos
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));



                @SuppressLint("Range") int id3 = cursor.getInt(
                        cursor.getColumnIndex(ContactsContract.Data._ID));



                // @SuppressLint("Range") String  contactNumber4 = String.valueOf(Integer.parseInt(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));


                // @SuppressLint("Range") String contactNumber9 = phoneCursorWORK.getString(phoneCursorWORK.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

                @SuppressLint("Range") String id2 = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));

                Log.i("CONTACT", "****************************************" + contactName + " -----...... " + id2 + "-----------....  " + " -------- " );




                    Uri uriBirthday2 = ContactsContract.Data.CONTENT_URI;
                    String[] proyeccionBirthday = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Event.START_DATE
                    };

                    String filtro =
                            ContactsContract.Data.MIMETYPE + "= ? AND " +
                                    ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " AND " +
                                    ContactsContract.CommonDataKinds.Event.CONTACT_ID + " = ? ";

                    String[] argsFiltroB = new String[]{
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                            String.valueOf(id2)};

                     Cursor c = getContentResolver().query(uriBirthday2, null, filtro, argsFiltroB, null);


                //  Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //Cursor cursor2 = getContentResolver().query(uriBirthday, null, filter, null, null);
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,

                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(id2)}, null);

                if (phoneCursor.moveToNext() && c.moveToNext()) {

                    @SuppressLint("Range") String contactNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    @SuppressLint("Range") String BirthDAy = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));

                    //   @SuppressLint("Range") String BirthDAy = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));

                    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+BirthDAy);
                    int counter =1;
                    PhoneContact mCumple = new PhoneContact();
                    mCumple.setId(Integer.parseInt(id2));
                    //Dejo por defecto la opción de Notificacion para todos los contactos despues e podrá modificar
                    mCumple.setTypeNotif("Notificacion");
                    mCumple.setMessage("");
                    mCumple.setTelephone(contactNumber);
                    mCumple.setBirthdate(BirthDAy);
                    mCumple.setContactName(contactName);
                    mCumple.setBitmap(BitmapFactory.decodeStream(abrirFoto(Integer.parseInt(id2))));

                    // Coger un array con los datos y llevarlos a la base dee datos tenemos que recorrer todo el array con un bucle y ir metiendo la sentecias sql
                    misCumplesArrayList2.add(mCumple);

                }
                phoneCursor.close();
                c.close();
            }

            cursor.close();
        }
    }
        private void initializeRecyclerView() {

            consultFromSQLite();
            recyclerView2.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MainAdapter(this,misCumplesArrayList,MainActivity.this);
            recyclerView2.setAdapter(adapter);
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PETICION_PERMISOS)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                tengo_permisos = true;
            else
                tengo_permisos = false;
    }


    private void insertAlarm() {

        SQLiteDatabase db = con2.getWritableDatabase();

         String insert = " INSERT INTO MisAlarmas (Hora,Minuto) VALUES ('" +hour+ "','" + minute + "')";
            db.execSQL(insert);
            db.close();
        }
    private void deleteSQLiteMisAlarmas() {
        SQLiteDatabase db = con2.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisAlarmas",null);
        if (cursor.getCount() > 0) {
            db.execSQL("DELETE FROM MisAlarmas");
        }
        db.close();
        cursor.close();
    }
    private void consultSQliteAlarm() {

        SQLiteDatabase db = con2.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisAlarmas", null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //Importante me crea uno nuevo cada vez si no solo me coge el último ya que crea solo un objeto de tipo MisCumples.....
                hora = cursor.getInt(0);
                minuto = cursor.getInt(1);
            }
            System.out.println("Nos metemos en el metodo consultAlarmSQLLite");
            //setAlarm(hora, minuto);
            System.out.println(hora+":"+minuto);
            cursor.close();
            db.close();
        }
    }

    public InputStream abrirFoto(int identificador){
        Uri contactUri= ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI,identificador);
        InputStream inputStream=ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),contactUri,true);
        return inputStream;
    }
}



