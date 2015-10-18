package com.reward.omotesando.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.UserManager;
import com.reward.omotesando.components.VolleyApi;
import com.reward.omotesando.components.api.GetGifts;
import com.reward.omotesando.models.Gift;
import com.reward.omotesando.models.User;

import org.json.JSONArray;

import java.util.List;

/**
 * ギフト券一覧フラグメント。
 */
public class GiftListFragment extends BaseFragment
        implements UserManager.Callback {

    private static final String TAG = GiftListFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    List<Gift> mGifts;

    // View
    private AbsListView mListView;

    private ListAdapter mAdapter;

    /*
     * 初期処理
     */
    public static GiftListFragment newInstance() {
        GiftListFragment fragment = new GiftListFragment();
        return fragment;
    }

    public GiftListFragment() {
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

        View view = inflater.inflate(R.layout.fragment_gift_list_list, container, false);

        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // View の再設定は毎回せなあかんものだろうか？
        if (state == State.READY) {
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

    // TODO: ギフト券が一件もないときの表示
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
            public void start(GiftListFragment fragment) {
                if (fragment.getUser()) {
                    fragment.getPointHistories();
                    transit(fragment, GETTING_GIFTS);
                } else {
                    transit(fragment, GETTING_USER);
                }
            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetUser(GiftListFragment fragment, User user) {
                // ポイント表示を更新
                fragment.updateUser(user);

                fragment.getPointHistories();

                transit(fragment, GETTING_GIFTS);
            }

            @Override
            public void failureGetUser(GiftListFragment fragment, String message) {
                // TODO: エラーの表示方法をちゃんと考えた方がよさげ
                if (message != null) {
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                transit(fragment, ERROR);
            }

            @Override
            public void detach(GiftListFragment fragment) {
                UserManager.cancelGetUser(fragment);
                transit(fragment, INITIAL);
            }
        },

        // ポイント履歴取得中
        GETTING_GIFTS {
            @Override
            public void successGetGifts(GiftListFragment fragment, List<Gift> pointHistories) {
                fragment.showPointHistories();

                transit(fragment, READY);
            }

            @Override
            public void failureGetGifts(GiftListFragment fragment) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();

                transit(fragment, ERROR);
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
        public void start(GiftListFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetUser(GiftListFragment fragment, User user) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetUser(GiftListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // ギフト券取得成功
        public void successGetGifts(GiftListFragment fragment, List<Gift> gifts) {
            throw new IllegalStateException();
        }

        // ギフト券取得失敗
        public void failureGetGifts(GiftListFragment fragment) {
            throw new IllegalStateException();
        }

        // Detach
        public void detach(GiftListFragment fragment) {
            // どの状態でも Detach イベントが発生する可能性あり
            // 通信中でなければ何もしない。
        }

        /*
         * 状態遷移 (enum State 内でのみ使用すること)
        */
        private static void transit(GiftListFragment fragment, State nextState) {
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
//        mPoint = user.point;
//        mCurrentPointText.setText(String.valueOf(mPoint));
    }

    // TODO: キャンセルに対応
    /**
     * ギフト券取得。
     */
    private void getPointHistories() {
        final GetGifts api = new GetGifts(getActivity());

        JsonArrayRequest request = new JsonArrayRequest(api.getUrl(getActivity()),

            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    VolleyApi.Log(TAG, api, response);

                    mGifts = api.parseJsonResponse(response);
                    state.successGetGifts(GiftListFragment.this, mGifts);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyApi.Log(TAG, api, error);

                    state.failureGetGifts(GiftListFragment.this);
                }
            }
        );

        VolleyUtils.getRequestQueue(getActivity().getApplicationContext()).add(request);
    }

    /*
     * ギフト券表示。
     */
    private void showPointHistories() {
        // この時点で Activity が存在しないパターンがある
        if (getActivity() == null) {
            Logger.e(TAG, "showPointHistories() getActivity is null.");
            return;
        }

        mAdapter = new GiftArrayAdapter(getActivity(), R.layout.list_item_gift, mGifts);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }
}
