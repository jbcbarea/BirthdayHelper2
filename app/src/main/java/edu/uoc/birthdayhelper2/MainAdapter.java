package edu.uoc.birthdayhelper2;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

import edu.uoc.birthdayhelper2.entidades.PhoneContact;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    //Inicializamos variables
    Activity activity;
    ArrayList<PhoneContact> arrayList;
    Context context;

    //Creamos el constructor

    public MainAdapter(Activity activity, ArrayList<PhoneContact> arrayList, Context context){
        this.activity = activity;
        this.arrayList = arrayList;
        this.context = context;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inicializamos Vistas
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
        //ViewHolder holder = new ViewHolder(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Inicializamos los objetos de contactos
       //ContactObject model = arrayList.get(position);
        PhoneContact cump = arrayList.get(position);
        holder.txtName.setText(cump.getContactName());
        holder.txtNumber.setText(cump.getTelephone());
        holder.txtNotification.setText(cump.getTypeNotif());
        //holder.txtDate.setText(cump.getBirthdate());
        //int ident = model.getContact_Photo();
        holder.imgView.setImageBitmap(cump.getBitmap());
        //Aqui por medio de holder referenciamos el layout donde tenemos cada fila del RecyclerView para que con un método setOnclickListener al pulksar nos llecve a la nueva Activity
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //System.out.println(cump.getContactName());
              // System.out.println(cump.getTelephone());
               Intent intent = new Intent(context,Personalize_Contacts.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               intent.putExtra("Id", cump.getId());
               intent.putExtra("Name", cump.getContactName());
               intent.putExtra("Telephones",cump.getTelephone());
               intent.putExtra("Birthday",cump.getBirthdate());
               context.getApplicationContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        //Return array size
        return   arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //Referenciamos las variables, it´s holding the view somehow

        TextView txtName,txtNumber,txtNotification,txtDate;
        ImageView imgView;
        ConstraintLayout parentLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Asignamos las variables

            txtName = itemView.findViewById(R.id.txtName);
            txtNumber = itemView.findViewById(R.id.txtNumber);
            txtNotification = itemView.findViewById(R.id.txtNotificatio);
            imgView = itemView.findViewById(R.id.imgPhoto);
            parentLayout = itemView.findViewById(R.id.parentLayout);
           // txtDate = itemView.findViewById(R.id.txtDate);
        }
    }



}

