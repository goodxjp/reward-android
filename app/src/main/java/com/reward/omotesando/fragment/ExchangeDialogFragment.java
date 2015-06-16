package com.reward.omotesando.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.api.PostPurchases;
import com.reward.omotesando.models.Item;

import org.json.JSONObject;

/**
 * ポイント交換確認ダイアログ。
 */
public class ExchangeDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ITEM = "item";

    private static ProgressDialog progressDialog = null;

    private OnExchangeDialogListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param message Parameter 2.
     * @param item Parameter 2.
     * @return A new instance of fragment ProgressDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExchangeDialogFragment newInstance(String title, String message, Item item) {
        ExchangeDialogFragment fragment = new ExchangeDialogFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putString(ARG_TITLE, title);
        }
        args.putString(ARG_MESSAGE, message);
        args.putSerializable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    public ExchangeDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment targetFragment = this.getTargetFragment();
        try {
            mListener = (OnExchangeDialogListener) targetFragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Don't implement OnExchangeDialogListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (progressDialog != null) {
            return progressDialog;
        }

        String title = getArguments().getString(ARG_TITLE);
        final Item item = (Item) getArguments().getSerializable(ARG_ITEM);
        String message = getArguments().getString(ARG_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onExchangeDialogClick(which, item);
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
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

    public static interface OnExchangeDialogListener {
        public void onExchangeDialogClick(int which, Item item);
    }

}
