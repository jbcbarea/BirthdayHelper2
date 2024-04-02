package edu.uoc.birthdayhelper2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.uoc.birthdayhelper2.entidades.PhoneContact;

import static android.content.ContentValues.TAG;
import static edu.uoc.birthdayhelper2.R.drawable.ic_baseline_notification_important_24;
import static edu.uoc.birthdayhelper2.R.drawable.ic_baseline_person_24;


//Implementamos el método onReceive
public class MyAlam extends BroadcastReceiver {


    ArrayList<PhoneContact> misCumplesArrayListFechaNotificacion = new ArrayList<>();
    ArrayList<String> birthdayNotification = new ArrayList<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        //Creo que aqui se hace lo que yo pong aaqui una vez la alarma se dispara
       // Toast.makeText(context.getApplicationContext(), "ewrfrsdfsd", Toast.LENGTH_SHORT).show();
        // MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        // mediaPlayer.start();

        consultFromSQLiteFechaNotification(context);
        //setNotification(context);
        System.out.println("La alarma funciona pero no hay cumpleaños hoy");
        sendMSM(context);
    }

    private void setNotification(Context context) {

        System.out.println("Notificacion");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "My Notification");
        builder.setContentTitle("Cumpleaños Contactos");

        System.out.println("Los CUMPLES!!!!!!!!!!!"+misCumplesArrayListFechaNotificacion.size());
        for (int i = 0; i < misCumplesArrayListFechaNotificacion.size(); i++) {
            PhoneContact mc = new PhoneContact();
            mc.setContactName(misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());
            birthdayNotification.add(misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());

            System.out.println("*****CUMPLEAÑOS CONTACTOS******"+misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());
        }

        // Convertir el array en un Set para eliminar duplicados
        Set<String> set = new LinkedHashSet<>(birthdayNotification);

        // Convertir el Set de vuelta a un ArrayList (si es necesario)
        List<String> listaSinDuplicados = new ArrayList<>(set);
        System.out.println("?¿"+birthdayNotification.toString());
        //System.out.println(mc.getContactName());
        builder.setContentText("Cumpleaños: "+listaSinDuplicados.toString());
        builder.setSmallIcon(ic_baseline_notification_important_24);
        builder.setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());
    }

    private void consultFromSQLiteFechaNotification(Context context) {

        this.misCumplesArrayListFechaNotificacion= new ArrayList<>();
        ConexionSQLite con = new ConexionSQLite(context, "MisCumples2", null, 1);
        //Meter aqui el calendar para comparar con la fecha actual....//solo mes y dia y ya.
        SQLiteDatabase db = con.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
        int monthnow = calendar.get(Calendar.MONTH);
        int montNow = (monthnow+1);
        String dayMonth;
        String monthNowOk;

        if (day_of_month< 10 && montNow<10 ) {
            monthNowOk ="0"+String.valueOf(montNow);
            dayMonth ="0"+String.valueOf(day_of_month);
        }else if (day_of_month < 10 && montNow >10 ) {
            monthNowOk = String.valueOf(montNow);
            dayMonth ="0"+String.valueOf(day_of_month);
        } else if (day_of_month > 10 && montNow <10 ) {
            monthNowOk = "0" + String.valueOf(montNow);
            dayMonth = String.valueOf(day_of_month);
        } else {
            dayMonth=String.valueOf(day_of_month);
            monthNowOk =String.valueOf(montNow);
        }

        String actualDate = "--" + monthNowOk+"-" + dayMonth;
        System.out.println("ACTUALLLLLDATEEEEEEEEEEEEEEEEE!!!!!!!!!!!"+actualDate);


        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2 where FechaNacimiento = '" + actualDate + "'", null);
        // Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2 where FechaNacimiento = '" + actualDate + "' and TipoNotif = 'Notificacion'", null);
        //Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2", null);
        System.out.println(actualDate);
        System.out.println(day_of_month);
        System.out.println(monthnow); //Sale cero asi que tengo que sumarle uno al mes...

        while (cursor.moveToNext()) {
            //Importante me crea uno nuevo cada vez si no solo me coge el último ya que crea solo un objeto de tipo MisCumples.....
            PhoneContact misCumple2 = new PhoneContact();

            int id = cursor.getInt(0);
            String typoNotif = cursor.getString(1);
            String message = cursor.getString(2);
            String telephone = cursor.getString(3);
            String birthday = cursor.getString(4);
            String contactName = cursor.getString(5);

            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+typoNotif);

            misCumple2.setId(id);
            misCumple2.setTypeNotif(typoNotif);
            misCumple2.setMessage(message);
            misCumple2.setTelephone(telephone);
            misCumple2.setBirthdate(birthday);
            misCumple2.setContactName(contactName);
            //misCumple2.setBitmap((BitmapFactory.decodeStream(abrirFoto(id))));



            misCumplesArrayListFechaNotificacion.add(misCumple2);
            System.out.println( id + contactName + birthday+"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

            //System.out.println("A ver los telefonos como salen ...."+telephone+"---------------"+id);
        }
        System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW"+misCumplesArrayListFechaNotificacion.toString());

        if (cursor.getCount() > 0) {
            System.out.println("werfffffff"+cursor.getCount());
            setNotification(context);
        }
        cursor.close();
        db.close();
    }

    private void sendMSM(Context context) {


        //Con la base de datpos mirar esto
        ConexionSQLite con = new ConexionSQLite(context, "MisCumples2", null, 1);
        //Meter aqui el calendar para comparar con la fecha actual....//solo mes y dia y ya.
        SQLiteDatabase db = con.getReadableDatabase();
        SmsManager smsManager = SmsManager.getDefault();
        Calendar calendar = Calendar.getInstance();

        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
        int monthnow = calendar.get(Calendar.MONTH);
        int montNow = (monthnow+1);
        String dayMonth;
        String monthNowOk;

        if (day_of_month< 10 && montNow<10 ) {
            monthNowOk ="0"+String.valueOf(montNow);
            dayMonth ="0"+String.valueOf(day_of_month);
        }else if (day_of_month < 10 && montNow >10 ) {
            monthNowOk = String.valueOf(montNow);
            dayMonth ="0"+String.valueOf(day_of_month);
        } else if (day_of_month > 10 && montNow <10 ) {
            monthNowOk = "0" + String.valueOf(montNow);
            dayMonth = String.valueOf(day_of_month);
        } else {
            dayMonth=String.valueOf(day_of_month);
            monthNowOk =String.valueOf(montNow);
        }

        String actualDate = "--" + monthNowOk+"-" + dayMonth;
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2 where FechaNacimiento = '" + actualDate + "' and TipoNotif = 'EnviarSMS'", null);
        //Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2",null )

        //Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2 WHERE FechaNacimiento = ? AND TipoNotif = ?", new String[]{actualDate, tipoNotif});

        while (cursor.moveToNext()) {
            //Importante me crea uno nuevo cada vez si no solo me coge el último ya que crea solo un objeto de tipo MisCumples.....
            PhoneContact misCumple2 = new PhoneContact();

            int Id = cursor.getInt(0);
            String typoNotif = cursor.getString(1);
            String message = cursor.getString(2);
            String telephone = cursor.getString(3);
            String birthday = cursor.getString(4);
            String contactName = cursor.getString(5);

            System.out.println(typoNotif+message+telephone+birthday+contactName);
            System.out.println("ACTUALDATE######################################···············"+actualDate);
            try {
                smsManager.sendTextMessage(telephone, null, message, null, null);
                Log.d(TAG, "SMS enviado.,"+telephone);
                System.out.println("Msn enviado");
            } catch (Exception e) {
                Log.d(TAG, "No se pudo enviar el SMS.");
                e.printStackTrace();
            }
        }
        cursor.close();

    }

    public void imprimeDatos () {

        for (int i=0; i<misCumplesArrayListFechaNotificacion.size();i++) {
            PhoneContact mc = new PhoneContact();
            mc.setContactName(misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());
            birthdayNotification.add(misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());
            System.out.println("*******CUMPLEAÑOS CONTACTOS*********"+misCumplesArrayListFechaNotificacion.get(i).getContactName().toString());
        }
    }

}


