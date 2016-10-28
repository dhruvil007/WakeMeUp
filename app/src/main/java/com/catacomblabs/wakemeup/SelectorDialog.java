package com.catacomblabs.wakemeup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SelectorDialog extends DialogFragment {

    public interface SelectorDialogListener {
        void onDialogPositive(DialogFragment dialog);

        void onDialogNegative(DialogFragment dialog);
    }

    public SelectorDialogListener dialogListener;
    private String name;
    public static Boolean cancelAlarm = false;

    public void getName(String name) {
        this.name = name;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (SelectorDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement SelectorDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!cancelAlarm) {
            builder.setMessage("Wake up at " + name + "?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.onDialogPositive(SelectorDialog.this);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.onDialogNegative(SelectorDialog.this);
                }
            });
        } else {
            builder.setMessage("Cancel alarm?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.onDialogPositive(SelectorDialog.this);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.onDialogNegative(SelectorDialog.this);
                }
            });
        }
        return builder.create();
    }
}
