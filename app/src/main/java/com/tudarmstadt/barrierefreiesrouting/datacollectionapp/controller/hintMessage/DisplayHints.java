package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.hintMessage;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;

import org.osmdroid.views.MapView;

/**
 * Created by deniz on 27.09.17.
 */

public class DisplayHints {

    private Context context;

    public DisplayHints(Context context){
        this.context = context;
    }

    public void simpleHint(String title, String s){
        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(context);
        builder1.setTitle(title);
        builder1.setMessage(s);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        android.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void displaySmallHint(String message, MapView mv){
        Toast.makeText(mv.getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
