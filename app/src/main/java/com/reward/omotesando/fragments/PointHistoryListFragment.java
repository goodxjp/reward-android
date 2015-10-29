package com.reward.omotesando.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.Error;
import com.reward.omotesando.components.UserManager;
import com.reward.omotesando.components.VolleyApi;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetPointHistories;
import com.reward.omotesando.models.PointHistory;
import com.reward.omotesando.models.User;

import org.json.JSONArray;

import java.util.List;

/**
 * ポイント取得履歴 (ポイント履歴一覧) フラグメント。
 */
public class PointHistoryListFragment extends BaseFragment
        implements UserManager.Callback {

    private static final String TAG = PointHistoryListFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    long mPoint;
    List<PointHistory> mPointHistories;

    // View
    //private TextView mCurrentPointText;
    private AbsListView mListView;

    private ListAdapter mAdapter;

    // ポイント履歴一覧取得リクエストを送信中かどうか
    private Request mRequestGetPointHistories = null;


    /*
     * 初期処理
     */
    public static PointHistoryListFragment newInstance() {
        PointHistoryListFragment fragment = new PointHistoryListFragment();
        return fragment;
    }

    // 空のコンストラクタが必要。
    // http://y-anz-m.blogspot.jp/2012/04/androidfragment-setarguments.html
    public PointHistoryListFragment() {
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

        View view = inflater.inflate(R.layout.fragment_point_history_list_list, container, false);

        //mCurrentPointText = (TextView) view.findViewById(R.id.current_point_text);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // View の再設定は毎回せなあかんものだろうか？
        if (state == State.READY) {
            //mCurrentPointText.setText(String.valueOf(mPoint));
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }

        // バックスタックから戻ったときに状態は遷移したままで onCreateView のみが呼ばれる。
        // ポイント履歴画面から他の Activity 呼び出さないので、onCreateView のみ呼ばれるパターンが存在する？！
        // とりあえず、オファー一覧フラグメント共通にしておく。
        if (state == State.INITIAL) {
            state.start(this);
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
            public void start(PointHistoryListFragment fragment) {
                if (fragment.getUser()) {
                    fragment.getPointHistories();
                    transit(fragment, GETTING_POINT_HISTORIES);
                } else {
                    transit(fragment, GETTING_USER);
                }
            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetUser(PointHistoryListFragment fragment, User user) {
                // ポイント表示を更新
                fragment.updateUser(user);

                fragment.getPointHistories();

                transit(fragment, GETTING_POINT_HISTORIES);
            }

            @Override
            public void failureGetUser(PointHistoryListFragment fragment, String message) {
                // TODO: エラーの表示方法をちゃんと考えた方がよさげ
                if (message != null) {
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                transit(fragment, ERROR);
            }

            @Override
            public void detach(PointHistoryListFragment fragment) {
                UserManager.cancelGetUser(fragment);
                transit(fragment, INITIAL);
            }
        },

        // ポイント履歴取得中
        GETTING_POINT_HISTORIES {
            @Override
            public void successGetPointHistories(PointHistoryListFragment fragment, List<PointHistory> pointHistories) {
                fragment.showPointHistories();

                transit(fragment, READY);
            }

            @Override
            public void failureGetPointHistories(PointHistoryListFragment fragment, String message) {
                // TODO: エラーの表示方法をちゃんと考えた方がよさげ
                if (message != null) {
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                transit(fragment, ERROR);
            }

            @Override
            public void detach(PointHistoryListFragment fragment) {
                if (fragment.mRequestGetPointHistories != null) {
                    fragment.mRequestGetPointHistories.cancel();
                    fragment.mRequestGetPointHistories = null;
                }

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
        public void start(PointHistoryListFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetUser(PointHistoryListFragment fragment, User user) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetUser(PointHistoryListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // ポイント履歴取得成功
        public void successGetPointHistories(PointHistoryListFragment fragment, List<PointHistory> pointHistories) {
            throw new IllegalStateException();
        }

        // ポイント履歴取得失敗
        public void failureGetPointHistories(PointHistoryListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // Detach
        public void detach(PointHistoryListFragment fragment) {
            // どの状態でも Detach イベントが発生する可能性あり
            // 通信中でなければ何もしない。
        }

        /*
         * 状態遷移 (State 内でのみ使用すること)
         */
        private static void transit(PointHistoryListFragment fragment, State nextState) {
            Logger.d(TAG, "STATE: " + fragment.state + " -> " + nextState);
            fragment.state = nextState;
        }
    }

    /**
     * ユーザー情報取得。
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
        //mCurrentPointText.setText(String.valueOf(mPoint));
    }

    /**
     * ポイント履歴取得。
     *
     * - ここでしか使わなそうなので共通化しない。
     */
    private void getPointHistories() {
        final Context applicationContext = getActivity().getApplicationContext();
        final GetPointHistories api = new GetPointHistories(getActivity());

        JsonArrayRequest request = new JsonArrayRequest(api.getUrl(getActivity()),

            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    VolleyApi.Log(TAG, api, response);
                    mRequestGetPointHistories = null;

                    mPointHistories = api.parseJsonResponse(response);
                    if (mPointHistories == null) {
                        state.failureGetPointHistories(PointHistoryListFragment.this,
                                Error.getMessageCriticalSeverError(applicationContext, Error.GET_POINT_HISORIES_RESPONSE_WRONG));
                    } else {
                        state.successGetPointHistories(PointHistoryListFragment.this, mPointHistories);
                    }
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyApi.Log(TAG, api, error);
                    VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);
                    mRequestGetPointHistories = null;

                    if (error.networkResponse == null) {
                        // レスポンスなし
                        state.failureGetPointHistories(PointHistoryListFragment.this,
                                Error.getMessageCommunicationError(applicationContext));
                    } else if (apiError == null) {
                        // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                        state.failureGetPointHistories(PointHistoryListFragment.this,
                                Error.getMessageCriticalSeverError(applicationContext, Error.GET_POINT_HISORIES_ERROR_RESPONSE_WRONG));
                    } else {
                        // API からの正常なエラーレスポンス
                        state.failureGetPointHistories(PointHistoryListFragment.this,
                                ErrorCode.getMessage(applicationContext, apiError.code, apiError.message));
                    }
                }
            }
        );

        // 送信処理
        mRequestGetPointHistories = VolleyApi.send(getActivity().getApplicationContext(), request);
    }

    /**
     * ポイント履歴表示。
     */
    private void showPointHistories() {
        // この時点で Activity が存在しないパターンがある？
        if (getActivity() == null) {
            Logger.e(TAG, "showPointHistories() getActivity is null.");
            throw new NullPointerException();  // TODO: あとで消す
            //return;
        }

        mAdapter = new PointHistoryArrayAdapter(getActivity(), R.layout.list_item_point_history, mPointHistories);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }


    // TODO: ポイント履歴が一件もないときの表示
    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }
}
