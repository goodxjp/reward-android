package com.reward.omotesando.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.reward.omotesando.R;
import com.reward.omotesando.models.MediaUser;

public class DebugMainFragment extends Fragment {
    // View
    TextView mMediaUserIdText;
    TextView mTerminalIdText;
    Button mInitializeButton;

    public static DebugMainFragment newInstance(String param1, String param2) {
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
        mTerminalIdText = (TextView) v.findViewById(R.id.terminal_id_text);

        mInitializeButton = (Button) v.findViewById(R.id.initialize_button);

        MediaUser mediaUser = MediaUser.getMediaUser(getActivity());
        mMediaUserIdText.setText(String.valueOf(mediaUser.mediaUserId));
        mTerminalIdText.setText(mediaUser.terminalId);

        mInitializeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaUser.clearMediaUser(getActivity());
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
