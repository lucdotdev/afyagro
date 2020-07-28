package com.example.afyagro.helpers;

import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringOperation {


    public StringOperation(){}

    public boolean isEmpty(EditText s){
        return s.getText().toString().trim().length() <= 0;
    }

    public boolean isValidEmail(EditText s){
        return s.getText().toString().contains("@");
    }
    public boolean isValidPhone(EditText s){
        return s.getText().toString().trim().length() == 9;
    }

    public boolean isValidpassword(EditText s){return s.getText().toString().trim().length()>=6;}
}
