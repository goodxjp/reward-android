package com.reward.omotesando.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.UserManager;
import com.reward.omotesando.models.User;

/**
 * 現在のポイントを表示するフラグメント。
 */
public class CurrentPointFragment extends BaseFragment implements UserManager.Callback {

    private static final String TAG = CurrentPointFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    long mPoint = -1;

    // View
    private TextView mCurrentPointText;

    /*
     * 初期処理
     */
    public static CurrentPointFragment newInstance() {
        CurrentPointFragment fragment = new CurrentPointFragment();
        return fragment;
    }

    public CurrentPointFragment() {
    }


    /*
     * ライフサイクル
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 回転しても作り直さない。
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_current_point, container, false);

        mCurrentPointText = (TextView) view.findViewById(R.id.current_point_text);

        // 状態がそのままで onCreateView が呼ばれる場合がある。
        // - バックスタックから戻ったときに状態は遷移したままで onCreateView のみが呼ばれる。
        if (state == State.INITIAL || state == State.ERROR) {
            state.start(this);
        } else if (state == State.READY) {
            // View の再設定
            updateView();
        } else {
            // 通信中のことはないはず
            throw new IllegalStateException();
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        state.detach(this);
    }


    /*
     * UserManager.UserManagerCallbacks
     */
    @Override
    public void onSuccessGetUser(User user) {
        state.successGetUser(this, user);
    }

    @Override
    public void onErrorGetUser(String message) {
        state.failureGetUser(this, message);
    }


    /*
     * 状態管理
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(CurrentPointFragment fragment) {
                if (fragment.getUser()) {
                    transit(fragment, READY);
                } else {
                    transit(fragment, GETTING_USER);
                }
            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetUser(CurrentPointFragment fragment, User user) {
                fragment.updateUser(user);
                transit(fragment, READY);
            }

            @Override
            public void failureGetUser(CurrentPointFragment fragment, String message) {
                // TODO: 他の部品でも同時にエラーが起こる可能性が高いから、エラー表示は Activity に委譲した方がいいかも。
                Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                transit(fragment, ERROR);
            }

            @Override
            public void detach(CurrentPointFragment fragment) {
                UserManager.cancelGetUser(fragment);
                transit(fragment, INITIAL);
            }
        },

        // 操作可能状態
        READY,

        // エラー状態
        ERROR;

        /*
         * イベント
         */
        // 初期処理開始
        public void start(CurrentPointFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetUser(CurrentPointFragment fragment, User user) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetUser(CurrentPointFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // Detach
        public void detach(CurrentPointFragment fragment) {
            // どの状態でも Detach イベントが発生する可能性あり
            // 通信中でなければ何もしない。
        }

        /*
         * 状態遷移 (State 内でのみ使用すること)
         */
        private static void transit(CurrentPointFragment fragment, State nextState) {
            Logger.d(TAG, "STATE: " + fragment.state + " -> " + nextState);
            fragment.state = nextState;
        }
    }

    /**
     * ユーザー情報取得。
     *
     * - 取得に成功した場合はユーザー情報の更新まで完了。うーん、微妙な仕様。
     *
     * @return true: 取得成功 / false: 取得待ち
     */
    private boolean getUser() {
        User user = UserManager.getUser(getActivity().getApplicationContext(), this);

        if (user != null) {
            updateUser(user);
            return true;
        } else {
            return false;
        }
    }

    /**
     * ユーザー情報更新。
     */
    private void updateUser(User user) {
        mPoint = user.point;
        mCurrentPointText.setText(String.valueOf(mPoint));
    }

    /**
     * 画面更新。
     *
     * - Model に保存された値を View に反映。
     * - Model そのままで View のみ再作成されるパターンがある。
     */
    private void updateView() {
        if (mPoint >= 0) {
            mCurrentPointText.setText(String.valueOf(mPoint));
        }
    }
}
