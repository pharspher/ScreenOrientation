package com.example.screenorientation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class LeaveDialogActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DialogFragment newFragment = LeaveDialogFragment.newInstance(R.string.leave_dialog_title, R.string.leave_dialog_message);
        newFragment.show(getFragmentManager(), "dialog");
    }
    
    public static class LeaveDialogFragment extends DialogFragment {

        public static LeaveDialogFragment newInstance(int title, int message) {
            LeaveDialogFragment frag = new LeaveDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putInt("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            int message = getArguments().getInt("message");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.leave_dialog_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((LeaveDialogActivity)getActivity()).doPositiveClick();
                            }
                        }
                    )
                    .setNegativeButton(R.string.leave_dialog_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((LeaveDialogActivity)getActivity()).doNegativeClick();
                            }
                        }
                    )
                    .create();
        }
    }
    
    private void doPositiveClick() {
        Intent serviceIntent = new Intent(TopViewService.ACTION_FINISH_SERVICE);
        //serviceIntent.setClassName("com.example.screenorientation", "com.example.screenorientation.TopViewService");
        sendBroadcast(serviceIntent);
        finish();
    }

    private void doNegativeClick() {
        finish();
    }
}
