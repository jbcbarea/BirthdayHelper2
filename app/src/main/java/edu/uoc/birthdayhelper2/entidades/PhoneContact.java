package edu.uoc.birthdayhelper2.entidades;

import android.graphics.Bitmap;

public class PhoneContact {

    private int id;
    private String TypeNotif;
    private String Message;
    private String Telephone;
    private String Birthdate;
    private String ContactName;
    private Bitmap bitmap;

    //Constructor con todas sus atributos
    public PhoneContact(int id, String typeNotif, String message, String telephone, String birthdate, String contactName,Bitmap bitmap) {
        this.id = id;
        TypeNotif = typeNotif;
        Message = message;
        Telephone = telephone;
        Birthdate = birthdate;
        ContactName = contactName;
        this.bitmap = bitmap;
    }
    public PhoneContact (){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeNotif() {
        return TypeNotif;
    }

    public void setTypeNotif(String typeNotif) {
        TypeNotif = typeNotif;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String telephone) {
        Telephone = telephone;
    }

    public String getBirthdate() {
        return Birthdate;
    }

    public void setBirthdate(String birthdate) {
        Birthdate = birthdate;
    }

    public String getContactName() {
        return ContactName;
    }

    public void setContactName(String contactName) {
        ContactName = contactName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
