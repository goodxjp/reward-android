package com.reward.omotesando.fragment;

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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;

import com.reward.omotesando.activities.ShowableProgressDialog;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.*;
import com.reward.omotesando.components.api.PostPurchases;
import com.reward.omotesando.models.Item;

import org.json.JSONObject;

import java.util.List;

/**
 * 商品一覧 (ポイント交換) フラグメント。
 */
public class ItemFragment extends BaseFragment
        implements AbsListView.OnItemClickListener,
                   ItemManager.ItemManagerCallbacks,
                   View.OnClickListener,  // わかりにくい…リスト内のボタンのリスナー
                   ExchangeDialogFragment.OnExchangeDialogListener {

    private static final String TAG = ItemFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


    /*
     * 初期処理
     */

    public static ItemFragment newInstance() {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }


    /*
     * ライフサイクル
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);  // ログのためだけ

        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // 状態がそのままで onCreateView が呼ばれる場合がある。
        if (state == State.INITIAL || state == State.ERROR) {
            state.start(this);
        }

        // View の再設定
        if (state == State.READY) {
            // mAdapter とかは初期化されないけど mListView の Adapter は初期化されてしまうもの？
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /*
     * AbsListView.OnItemClickListener
     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }


    /*
     * ItemManager.ItemManagerCallbacks
     */

    @Override
    public void onSuccessGetItems(List<Item> items) {
        state.successGetItems(this, items);
    }

    @Override
    public void onErrorGetItems(String message) {
        state.failureGetItems(this, message);
    }


    /*
     * View.OnClickListener
     */

    // ポイント交換ボタン限定
    @Override
    public void onClick(View v) {
        Item item = (Item) v.getTag();
        // TODO: リソース化、文字列リソース整理
        ExchangeDialogFragment dialog = ExchangeDialogFragment.newInstance(item.name, "交換してもよいですか？", item);
        dialog.setTargetFragment(this, 0);  // コールバック用のイベントリスナー登録のため。これが定型パターンらしい。
        dialog.show(getActivity().getSupportFragmentManager(), "exchange_dialog");
    }


    /*
     * ExchangeDialogFragment.OnExchangeDialogListener
     */

    // ポイント交換確認ダイアログのコールバック
    // TODO: Item はパラメータがいい？Fragment で保持する？
    @Override
    public void onExchangeDialogClick(int which, Item item) {
        Logger.e(TAG, "onExchangeDialogClick " + which);
        exchange(item);
    }

    private void exchange(Item item) {
        Context applicationContext = getActivity().getApplicationContext();

        // 購入 API
        final PostPurchases api = new PostPurchases(applicationContext, item);

        JsonObjectRequest request = new JsonObjectRequest(VolleyApi.getVolleyMethod(api), api.getUrl(applicationContext), api.getJsonRequest(),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApi.Log(TAG, api, response);

                        ShowableProgressDialog showableDialog = (ShowableProgressDialog) getActivity();
                        if (showableDialog != null) {
                            showableDialog.dismissProgressDialog();
                        }

                        // TODO: 交換後の画面遷移をどうするか？
                        // TODO: リソース化、文字列リソース整理
                        Toast.makeText(getActivity(), "交換完了！ (交換後の画面遷移要検討)", Toast.LENGTH_LONG).show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);

                        ShowableProgressDialog showableDialog = (ShowableProgressDialog) getActivity();
                        if (showableDialog != null) {
                            showableDialog.dismissProgressDialog();
                        }

                        Activity activity = getActivity();

                        // TODO: 通信エラー内容によって、処理を変える
                        if (error.networkResponse == null) {
                            // 通信状態が悪いか、サーバー停止
                            // サーバーの停止は他の監視方法によって検知するので、通信状態が悪い方のケアを行う。
                            Toast.makeText(activity, activity.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                        } else {
                            // TODO: 在庫切れなどエラーによりメッセージ出し分け
                            // 購入 API
                            // - ポイント不足
                            // - 在庫切れ
                            // - ポイント不一致 (ポイント変更)
                            // - 1 日 1 回の制限
                            // - 何からの制限がかかって購入不可

                            // - アカウント停止
                            // - メンテナンス中
                            // - サーバーエラー (DB おかしいとか、予期せぬ例外発生)
                            Toast.makeText(activity, activity.getString(R.string.message_critical_server_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // TODO: 全ての API でタイムアウト時間変更
        DefaultRetryPolicy policy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        ((ShowableProgressDialog) getActivity()).showProgressDialog(null, getString(R.string.dialog_message_communicating));

        RequestQueue requestQueue = VolleyUtils.getRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(request);
    }


    /*
     * 状態管理
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(ItemFragment fragment) {
                if (fragment.getItems()) {
                    fragment.transit(READY);
                } else {
                    fragment.transit(GETTING_ITEMS);
                }
            }
        },

        // 商品一覧取得中
        GETTING_ITEMS {
            @Override
            public void successGetItems(ItemFragment fragment, List<Item> items) {
                fragment.showItems(items);

                fragment.transit(READY);
            }

            @Override
            public void failureGetItems(ItemFragment fragment, String message) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                // TODO: ダイアログでエラーメッセージ表示。
                Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();

                fragment.transit(ERROR);
            }
        },

        // 操作可能状態
        READY,

        // エラー状態
        ERROR {
            @Override
            public void start(ItemFragment fragment) {
                if (fragment.getItems()) {
                    fragment.transit(READY);
                } else {
                    fragment.transit(GETTING_ITEMS);
                }
            }
        };

        /*
         * イベント
         */
        // 初期処理開始
        public void start(ItemFragment fragment) {
            throw new IllegalStateException();
        }

        // 商品一覧取得成功
        public void successGetItems(ItemFragment fragment, List<Item> items) {
            throw new IllegalStateException();
        }

        // 商品一覧取得失敗
        public void failureGetItems(ItemFragment fragment, String message) {
            throw new IllegalStateException();
        }
    }

    // 状態遷移 (enum State 内でのみ使用すること)
    private void transit(State nextState) {
        Logger.d(TAG, "STATE: " + state + " -> " + nextState);
        state = nextState;
    }

    /**
     * 商品一覧を取得。
     *
     * @return true: 取得成功 / false: 取得待ち
     */
    private boolean getItems() {
        List<Item> items;

        items = ItemManager.getItems(getActivity().getApplicationContext(), this);

        if (items != null) {
            showItems(items);
            return true;
        } else {
            return false;
        }
    }

    // 商品一覧を表示
    private void showItems(List<Item> items) {
        mAdapter = new ItemArrayAdapter(getActivity(), R.layout.list_item_item, items);
        ((ItemArrayAdapter) mAdapter).setOnExchangeButtonClickListener(this);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
