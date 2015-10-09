package com.reward.omotesando.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.reward.omotesando.BuildConfig;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.models.User;

public class DebugMainFragment extends Fragment {
    private static final String TAG = DebugMainFragment.class.getName();

    // View
    TextView mMediaUserIdText;
    TextView mUserKeyText;
    TextView mTerminalIdText;
    Button mInitializeButton;

    public static DebugMainFragment newInstance() {
        DebugMainFragment fragment = new DebugMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DebugMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_debug_main, container, false);

        mMediaUserIdText = (TextView) v.findViewById(R.id.media_user_id_text);
        mUserKeyText = (TextView) v.findViewById(R.id.user_key_text);
        mTerminalIdText = (TextView) v.findViewById(R.id.terminal_id_text);

        mInitializeButton = (Button) v.findViewById(R.id.initialize_button);

        User user = User.getUser(getActivity());
        if (user != null) {
            mMediaUserIdText.setText(String.valueOf(user.userId));
            mUserKeyText.setText(user.userKey);
            mTerminalIdText.setText(null);
        }

        mInitializeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.clearUser(getActivity());
                Toast.makeText(getActivity(), "Initialize", Toast.LENGTH_LONG).show();
            }
        });

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
