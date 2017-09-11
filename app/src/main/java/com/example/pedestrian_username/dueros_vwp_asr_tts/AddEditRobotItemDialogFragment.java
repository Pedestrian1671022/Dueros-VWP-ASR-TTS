package com.example.pedestrian_username.dueros_vwp_asr_tts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddEditRobotItemDialogFragment extends DialogFragment {

    public static final String POSITION_KEY = "POSITION_KEY";
    private EditText robot_name_edit_text;
    private EditText master_uri_edit_text;
    private RobotItem robotItem = new RobotItem();
    private DialogListener mListener;
    private int position = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString()  + " must implement DialogListener");
        }
    }

    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            position = args.getInt(POSITION_KEY, -1);
            robotItem.load(args);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_add_robot_item, null);
        robot_name_edit_text = (EditText) v.findViewById(R.id.robot_name_edit_text);
        master_uri_edit_text = (EditText) v.findViewById(R.id.master_uri_edit_text);

        robot_name_edit_text.setText(robotItem.getRobot_name());
        master_uri_edit_text.setText(robotItem.getMaster_uri());

        builder.setTitle("Edit/Add RobotItem")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String name = robot_name_edit_text.getText().toString().trim();
                        String masterUri = master_uri_edit_text.getText().toString().trim();

                        if (masterUri.equals("")) {
                            Toast.makeText(getActivity(), "Master URI required", Toast.LENGTH_SHORT).show();
                        } else if (name.equals("")) {
                            Toast.makeText(getActivity(), "Robot name required", Toast.LENGTH_SHORT).show();
                        } else {
                            mListener.onAddEditDialogPositiveClick(new RobotItem(name, masterUri), position);
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onAddEditDialogNegativeClick(AddEditRobotItemDialogFragment.this);
                dialog.cancel();
            }
        });
        return builder.create();
    }

    public interface DialogListener {
        void onAddEditDialogPositiveClick(RobotItem robotItem, int position);

        void onAddEditDialogNegativeClick(DialogFragment dialog);
    }
}
