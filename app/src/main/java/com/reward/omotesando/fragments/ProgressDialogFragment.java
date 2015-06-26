package com.reward.omotesando.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * 通信中ダイアログ。
 *
 * - http://furudate.hatenablog.com/entry/2014/01/09/162421
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private static ProgressDialog progressDialog = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param message Parameter 2.
     * @return A new instance of fragment ProgressDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressDialogFragment newInstance(String title, String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putString(ARG_TITLE, title);
        }
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public ProgressDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (progressDialog != null) {
            return progressDialog;
        }

        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);

        progressDialog = new ProgressDialog(getActivity());
        if (title != null) {
            progressDialog.setTitle(title);
        }
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);

        return progressDialog;
    }

    @Override
    public Dialog getDialog(){
        return progressDialog;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        progressDialog = null;
    }
}
