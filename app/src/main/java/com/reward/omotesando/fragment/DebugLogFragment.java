package com.reward.omotesando.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;

public class DebugLogFragment extends Fragment {
    // View
    EditText mLogEditText;

    public static DebugLogFragment newInstance() {
        DebugLogFragment fragment = new DebugLogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DebugLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_debug_log, container, false);

        mLogEditText = (EditText) v.findViewById(R.id.log_edit_text);

        mLogEditText.append(Logger.getMessages());

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
