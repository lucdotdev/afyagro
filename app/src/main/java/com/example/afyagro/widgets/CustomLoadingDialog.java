package com.example.afyagro.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.afyagro.R;

public class CustomLoadingDialog {
    private Activity activity;
    private AlertDialog alertDialog;

    @SuppressLint("InflateParams")
    public CustomLoadingDialog(Activity myActivity) {
        this.activity = myActivity;

    }

    public void startLoading() {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View w = layoutInflater.inflate(R.layout.custom_loading, null);
        w.findViewById(R.id.effectuer).setVisibility(View.GONE);
        alert.setView(w);
        alert.setCancelable(false);
        alertDialog = alert.create();
        alertDialog.show();
    }

    public void ok() {

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams") View w = layoutInflater.inflate(R.layout.custom_loading, null);
        w.findViewById(R.id.textView).setVisibility(View.GONE);
        w.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
        w.findViewById(R.id.effectuer).setVisibility(View.VISIBLE);

        alertDialog.dismiss();
        alert.setView(w);
        alert.setCancelable(false);
        alertDialog = alert.create();
        alertDialog.show();

    }

    public void dismissLoading() {
        alertDialog.dismiss();
    }
}
