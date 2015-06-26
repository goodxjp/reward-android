package com.reward.omotesando.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reward.omotesando.commons.Logger;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {

    private String TAG = BaseFragment.class.getName();
    protected String getLogTag() {
        return TAG;
    }

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(getLogTag(), "[" + this.hashCode() +"] onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v(getLogTag(), "[" +  this.hashCode() +"] onCreateView()");
        Logger.v(getLogTag(), "[" +  this.hashCode() +"]   savedInstanceState = " + savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        Logger.v(getLogTag(),  "[" +  this.hashCode() +"] onAttach()");
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        Logger.v(getLogTag(),  "[" +  this.hashCode() +"] onDetach()");
        super.onDetach();
    }

}
