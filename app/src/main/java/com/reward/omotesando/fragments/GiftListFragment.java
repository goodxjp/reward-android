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
import com.reward.omotesando.components.VolleyApi;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetGifts;
import com.reward.omotesando.models.Gift;

import org.json.JSONArray;

import java.util.List;

/**
 * ポイント交換履歴 (ギフト券一覧) フラグメント。
 */
public class GiftListFragment extends BaseFragment {

    private static final String TAG = GiftListFragment.class.getName();

    @Override
    protected String getLogTag() {
        return TAG;
    }

    // Model
    List<Gift> mGifts;

    // View
    private AbsListView mListView;

    private ListAdapter mAdapter;

    // ギフト券一覧取得リクエストを送信中かどうか
    private Request mRequestGetGifts = null;


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


    /*
     * 状態管理
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(GiftListFragment fragment) {
                fragment.getPointHistories();
                transit(fragment, GETTING_GIFTS);
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
            public void failureGetGifts(GiftListFragment fragment, String message) {
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
                if (fragment.mRequestGetGifts != null) {
                    fragment.mRequestGetGifts.cancel();
                    fragment.mRequestGetGifts = null;
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
        public void start(GiftListFragment fragment) {
            throw new IllegalStateException();
        }

        // ギフト券一覧取得成功
        public void successGetGifts(GiftListFragment fragment, List<Gift> gifts) {
            throw new IllegalStateException();
        }

        // ギフト券一覧取得失敗
        public void failureGetGifts(GiftListFragment fragment, String message) {
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
     * ギフト券取得。
     */
    private void getPointHistories() {
        final Context applicationContext = getActivity().getApplicationContext();
        final GetGifts api = new GetGifts(getActivity());

        JsonArrayRequest request = new JsonArrayRequest(api.getUrl(getActivity()),

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        VolleyApi.Log(TAG, api, response);
                        mRequestGetGifts = null;

                        // TODO: 同じの何回も書いてる。
                        mGifts = api.parseJsonResponse(response);
                        if (mGifts == null) {
                            state.failureGetGifts(GiftListFragment.this,
                                    com.reward.omotesando.components.Error.getMessageCriticalSeverError(applicationContext, Error.GET_GIFTS_RESPONSE_WRONG));
                        } else {
                            state.successGetGifts(GiftListFragment.this, mGifts);
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);
                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);
                        mRequestGetGifts = null;

                        // TODO: 同じの何回も書いてる。
                        if (error.networkResponse == null) {
                            // レスポンスなし
                            state.failureGetGifts(GiftListFragment.this,
                                    Error.getMessageCommunicationError(applicationContext));
                        } else if (apiError == null) {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            state.failureGetGifts(GiftListFragment.this,
                                    Error.getMessageCriticalSeverError(applicationContext, Error.GET_GIFTS_ERROR_RESPONSE_WRONG));
                        } else {
                            // API からの正常なエラーレスポンス
                            state.failureGetGifts(GiftListFragment.this,
                                    ErrorCode.getMessage(applicationContext, apiError.code, apiError.message));
                        }
                    }
                }
        );

        mRequestGetGifts = VolleyApi.send(getActivity().getApplicationContext(), request);
    }

    /*
     * ギフト券表示。
     */
    private void showPointHistories() {
        mAdapter = new GiftArrayAdapter(getActivity(), R.layout.list_item_gift, mGifts);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
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
}
