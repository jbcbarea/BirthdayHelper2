package edu.uoc.birthdayhelper2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import edu.uoc.birthdayhelper2.entidades.PhoneContact;

public class Personalize_Contacts extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener  {

    private String name;
    private int Idimage;

    private String Telephones2;
    private boolean checkBoxState;
    private String Telephones;
    private String Date;
    int photoId;
    private boolean buttonPersonalizePressed = false;
    ImageView ContactImage;
    private ArrayList<PhoneContact> arrayListPersonalize = new ArrayList<>();
    private ArrayList<PhoneContact> arrayListPersonalize2 = new ArrayList<>();
    TextView Cname;
    TextView Telephon;
    TextView txtDate;
    TextView txtAlert;
    Spinner spinner;
    EditText editTextMessage;
    CheckBox checkBox;
    Button buttonPersonalize;
    Button buttonSave;
    PhoneContact mC = new PhoneContact();

    ArrayList<PhoneContact> misCumples = new ArrayList<>();
    ArrayList<PhoneContact> misCumples2 = new ArrayList<>();
    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalize__contacts);

        //Por medio de este método recojo los datos del Recycler view en función de que contacto he seleccionado y los paso conun intent
        getIncomingIntent();


        ContactImage = findViewById(R.id.imageView);
        Cname = findViewById(R.id.textViewNombre);
        txtDate  = findViewById(R.id.txtDate);
        checkBox = findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(this);
        buttonPersonalize = findViewById(R.id.buttonPersonalize);
        buttonSave = findViewById(R.id.buttonBack);
        txtAlert =findViewById(R.id.textViewAlert);
        spinner = findViewById(R.id.spinner);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextMessage.setEnabled(false);
        ArrayList<String> numbers = new ArrayList<>();
        //Botón de guardado al pulsar este botón volveremos a la Actividad anterior donde se carga el Recycler View también actualizamos la base de datos por si hemos hecho cambios
        //en la personalizacion o en enviar mensaje y el mensaje que queremos enviar.
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Personalize_Contacts.this, MainActivity.class);

                getContactsPersonalize();
                updateMisCumplesPersonalize();
                checkBoxSendSMS();
                consultFromSQLPersonalizeContacts();
                startActivity(intent);
            }
        });
        //Botón de personalización nos lleva a la personalización de contactos dedl teléfono filtrando por el contacto que tenemos en la Actividad
        buttonPersonalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String proyeccion[]={ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.PHOTO_ID};

                String filtro=ContactsContract.Contacts.DISPLAY_NAME + " like ?";

                String args_filtro[]={"%"+name+"%"};

                ArrayList<String> lista_contactos=new ArrayList<String>();
                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        proyeccion, filtro, args_filtro, null);

                if(cur.getCount()==1) {
                    cur.moveToNext();
                    @SuppressLint("Range") int id = cur.getInt(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    @SuppressLint("Range") String lookupKey=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                    Uri contactUri = ContactsContract.Contacts.getLookupUri(id, lookupKey);
                    startActivity(new Intent(Intent.ACTION_VIEW, contactUri));
                    txtAlert.setText("Pulse al botón de Guardar para actualizar...");
                    }

                }

        });

/*

       for (int i =0;i< mainActivity.misCumplesArrayList.size();i++){

           Telephones2 = mainActivity.misCumplesArrayList.get(i).getTelephone();

           System.out.println("A ver si salen los telefonos"+Telephones2);
           }
  */

        Cname.setText(name);
        txtDate.setText(Date);
        ContactImage.setImageBitmap((BitmapFactory.decodeStream(abrirFoto((Idimage)))));
        //Pasa,mos la lista de los numeros = numbers;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,numbers );
        numbers.add(Telephones);
        numbers.add("654123765");
        spinner.setAdapter(adapter);
        //Para que cuando seleccionamos alguno nos selecionaria ese y tendríamos ese número.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(getApplicationContext(),"Este es el telefono al que se le mandará el MSN"+parent.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                //Lo tendría que meter como nuevo número en la base de datos??...
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //Recibo los datos por medio de getIntent de la actividad anterior donde se muestra el Recyclerview..
    public void getIncomingIntent() {

        if (getIntent().hasExtra("Name") && getIntent().hasExtra("Id")) {

            //Sacar la fecha Telefono, nombre , id........
            name = getIntent().getStringExtra("Name");
            Idimage = getIntent().getIntExtra("Id",2);
            Telephones = getIntent().getStringExtra("Telephones");
            Date = getIntent().getStringExtra("Birthday");
            //System.out.println(name+Idimage);
        }
    }


    private void consultFromSQLPersonalizeContacts () {
        ConexionSQLite con = new ConexionSQLite( this,"MisCumples2", null, 1);
        SQLiteDatabase db = con.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID,Telefono,FechaNacimiento,Nombre FROM MisCumples2 WHERE ID = '"+Idimage+"'", null);

        while (cursor.moveToNext()) {
            //Importante me crea uno nuevo cada vez si no solo me coge el último ya que crea solo un objeto de tipo MisCumples.....
            PhoneContact misCumple2 = new PhoneContact();
            int id = cursor.getInt(0);
            String telephone = cursor.getString(1);
            String birthday = cursor.getString(2);
            String contactName = cursor.getString(3);

            misCumple2.setId(id);
            misCumple2.setTelephone(telephone);
            misCumple2.setBirthdate(birthday);
            misCumple2.setContactName(contactName);
            misCumple2.setBitmap((BitmapFactory.decodeStream(abrirFoto(id))));

            arrayListPersonalize2.add(misCumple2);
            System.out.println("******************************************************************************************************");

        }

        cursor.close();
        db.close();
    }


    public InputStream abrirFoto(int identificador){
        Uri contactUri= ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI,identificador);
        InputStream inputStream=ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),contactUri,true);
        return inputStream;
    }

    @SuppressLint("Range")
    //Al personalizar los contactos tenemos que recoger los datos por medio de cursor y getContentResolver
    public void getContactsPersonalize() {
        //Pedimos que nos deje compartir los contactos que tenmemos para que se actuaslizen el el Recycler View ya que se genera con los datos guardados en la tabla MisCumples2
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
        }

        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        Log.i("CONTACT", "TOTAL # OF CONTACTS" + Integer.toString(cursor.getCount()));

            while (cursor.moveToNext() ) {
                //Sacamos los datos
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

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

                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,

                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(id2)}, null);

                if (phoneCursor.moveToNext() && c.moveToNext()) {

                    @SuppressLint("Range") String contactNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    @SuppressLint("Range") String BirthDAy = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));

                    PhoneContact mCumple = new PhoneContact();
                    mCumple.setId(Integer.parseInt(id2));
                    mCumple.setTelephone(contactNumber);
                    mCumple.setBirthdate(BirthDAy);
                    mCumple.setContactName(contactName);
                    mCumple.setBitmap(BitmapFactory.decodeStream(abrirFoto(Integer.parseInt(id2))));
                    // Coger un array con los datos y llevarlos a la base dee datos tenemos que recorrer todo el array con un bucle y ir metiendo la sentecias sql
                    arrayListPersonalize.add(mCumple);
                }
                phoneCursor.close();
                c.close();
            }
            cursor.close();

    }

    //Actualizamos la base de datos por medio de este método
    private void  updateMisCumplesPersonalize () {

        ConexionSQLite con = new ConexionSQLite( this,"MisCumples2", null, 1);
        SQLiteDatabase db = con.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2", null);
        if (cursor.getCount() > 0) {

            for (int i = 0; i < arrayListPersonalize.size(); i++) {


                String insert = " UPDATE MisCumples2 SET  Telefono ='" +
                        arrayListPersonalize.get(i).getTelephone() + "', FechaNacimiento ='" + arrayListPersonalize.get(i).getBirthdate() + "', Nombre = '" + arrayListPersonalize.get(i).getContactName() + "' WHERE ID ='" + arrayListPersonalize.get(i).getId() + "'";
                db.execSQL(insert);
            }
            db.close();

        }
        cursor.close();
    }

    private void deleteSQLite() {
        ConexionSQLite con = new ConexionSQLite( this,"MisCumples2", null, 1);
        SQLiteDatabase db = con.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MisCumples2",null);
        if (cursor.getCount() > 0) {
            db.execSQL("UPDATE MisCumples2 SET FechaNacimiento = null, Nombre = null, Telefono = null");
        }
        cursor.close();
        db.close();
    }
//Por medio de este método si esta pulsado el checkBox para enviar mensaje, nos aparecerá disponible el editText para escribir nuestro mensaje que se mandará a los contactos que lo tengan seleccionado cuando se dispare la alarma

    private void checkBoxSendSMS () {

          if (checkBoxState) {

              mC.setTypeNotif("EnviarSMS");
            checkBox.setChecked(true);
            editTextMessage.setEnabled(true);

            ConexionSQLite con = new ConexionSQLite( this,"MisCumples2", null, 1);

            SQLiteDatabase db = con.getWritableDatabase();

            String message = editTextMessage.getText().toString();

            String query = " UPDATE MisCumples2 SET TipoNotif = 'EnviarSMS', Mensaje = '"+message+"' WHERE Nombre = '" + name + "' ";

            db.execSQL(query);
            db.close();

        }else if(!checkBoxState) {

              mC.setTypeNotif("Notificacion");
            ConexionSQLite con = new ConexionSQLite( this,"MisCumples2", null, 1);

            SQLiteDatabase db = con.getWritableDatabase();

            String query2 = " UPDATE MisCumples2 SET TipoNotif = 'Notificacion' WHERE ID = '" + Idimage + "' ";
            db.execSQL(query2);
            db.close();

        }
    }
    private void checkIfPersonalized () {

        if (buttonPersonalizePressed) {

            getContactsPersonalize();
            updateMisCumplesPersonalize();

        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            checkBoxState = true;
            editTextMessage.setEnabled(true);
        } else {
            checkBoxState = false;
            editTextMessage.setEnabled(false);
        }
    }

}
