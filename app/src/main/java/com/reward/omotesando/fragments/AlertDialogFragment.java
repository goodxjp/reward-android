package com.reward.omotesando.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.reward.omotesando.R;

/**
 * 警告ダイアログ。
 *
 * - 標準的なガイドラインに沿って作って見よう。
 *   - http://developer.android.com/intl/ja/guide/topics/ui/dialogs.html
 *   - Activity から呼び出される前提だね。
 *   - コールバックのパラメータも微妙。
 */
public class AlertDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private OnAlertDialogListener mListener;

    public static AlertDialogFragment newInstance(String title, String message) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putString(ARG_TITLE, title);
        }
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public AlertDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnAlertDialogListener) activity;
        } catch (ClassCastException e) {
            mListener = null;
            //throw new ClassCastException(activity.toString() + "must implement OnAlertDialogListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
               .setMessage(message)
               .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onAlertDialogPositiveClick(AlertDialogFragment.this);
                        }
                    }
               });

        return builder.create();
    }

    public static interface OnAlertDialogListener {
        public void onAlertDialogPositiveClick(AlertDialogFragment dialog);
    }
}
