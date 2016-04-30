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
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.activities.PointHistoryActivity;
import com.reward.omotesando.activities.ShowableProgressDialog;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.Error;
import com.reward.omotesando.components.ItemListManager;
import com.reward.omotesando.components.VolleyApi;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.PostPurchases;
import com.reward.omotesando.models.Item;

import org.json.JSONObject;

import java.util.List;

import static com.reward.omotesando.components.api.ErrorCode.ERROR_CODE_2005;
import static com.reward.omotesando.components.api.ErrorCode.ERROR_CODE_2007;
import static com.reward.omotesando.components.api.ErrorCode.ERROR_CODE_9999;

/**
 * 商品一覧 (ポイント交換) フラグメント。
 */
public class ItemListFragment extends BaseFragment
        implements AbsListView.OnItemClickListener,
                   ItemListManager.Callback,
                   View.OnClickListener,  // わかりにくい…リスト内のボタンのリスナー
                   ExchangeDialogFragment.OnExchangeDialogListener {

    private static final String TAG = ItemListFragment.class.getName();
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
    public static ItemListFragment newInstance() {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }


    /*
     * ライフサイクル
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 回転しても作り直さない。
        // 通信直後に回転すると Fragment が作り直されちゃって、ダイアログで落ちる。
        setRetainInstance(true);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);  // ログのためだけ

        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

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
        state.detach(this);

        // 回転の時はリクエストを続けたいので、ここでキャンセルはしないほうがよさげ
//        // 購入リクエストキャンセル
//        // サーバーに到達してしまっていたらしょうがないが、送信で手間取っているときはキャンセルできるはず。
//        if (purchaseRequest != null) {
//            purchaseRequest.cancel();
//            purchaseRequest = null;
//        }

        // やっぱり、回転と終了の区別がつかないのが問題。
        // 回転の時は通信続けたい。
        // 終了の時は通信続けると、ダイアログで落ちる。
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
    public void onSuccessGetItemList(List<Item> items) {
        state.successGetItems(this, items);
    }

    @Override
    public void onErrorGetItemList(String message) {
        state.failureGetItems(this, message);
    }


    /*
     * View.OnClickListener
     */

    // ポイント交換ボタン限定
    @Override
    public void onClick(View v) {
        Item item = (Item) v.getTag();
        ExchangeDialogFragment dialog = ExchangeDialogFragment.newInstance(item.name, getActivity().getString(R.string.message_confirm_exchange), item);
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
        Logger.d(TAG, "onExchangeDialogClick " + which);
        exchange(item);
    }

    // 購入リクエストを送信中かどうか
    private static Request purchaseRequest = null;

    /**
     * ギフト券交換処理。
     *
     * @param item  交換対象の商品
     */
    private void exchange(Item item) {
        // TODO: 全部名前変える。
        final Context appContext = getActivity().getApplicationContext();

        // すでに購入リクエスト送信中なら何もしない。
        if (purchaseRequest != null) {
            Logger.e(TAG, "exchange() Already sending purchaseRequest.");
            return;
        }

        // 購入 API
        final PostPurchases api = new PostPurchases(appContext, item);

        JsonObjectRequest request = new JsonObjectRequest(VolleyApi.getVolleyMethod(api), api.getUrl(appContext), api.getJsonRequest(),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        purchaseRequest = null;

                        VolleyApi.Log(TAG, api, response);

                        // Detach 時に通信キャンセルしていないのでレスポンス受信時に Activity がない場合がある。
                        // 本当は、購入処理中はくるくる表示するなどして Fragment を Detach させないようにするほうがよさげ。
                        if (getActivity() != null) {
                            //PointHistoryActivity.start(getActivity(), PointHistoryActivity.TAB_EXCHANGE_HISTORY, true);  // うーん、これだけだと戻るボタンで戻っちゃう。FLAG_ACTIVITY_NO_HISTORY フラグ意味ある？
                            PointHistoryActivity.start(getActivity(), PointHistoryActivity.PointHistoryTab.EXCHANGE_HISTORY, false);
                            getActivity().finish();

                            Toast.makeText(appContext, getString(R.string.message_complete_exchange), Toast.LENGTH_LONG).show();
                        } else {
                            // こっちは getString もダメ。
                            Toast.makeText(appContext, appContext.getString(R.string.message_complete_exchange_no_transit), Toast.LENGTH_LONG).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        purchaseRequest = null;

                        VolleyApi.Log(TAG, api, error);
                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);

                        Activity activity = getActivity();

                        if (error.networkResponse == null) {
                            // レスポンスなし
                            Toast.makeText(appContext, Error.getMessageCommunicationError(appContext), Toast.LENGTH_LONG).show();
                        } else if (apiError == null) {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            Toast.makeText(appContext, Error.getMessageCriticalSeverError(appContext, Error.POST_PURCHASES_ERROR_RESPONSE_WRONG), Toast.LENGTH_LONG).show();
                        } else {
                            // API からの正常なエラーレスポンス

                            if (apiError.code == ERROR_CODE_9999.code) {
                                // 強制終了
                                // 本当はダメ。上位にこの種類のエラーを渡して、上位で処理すべき。
                                System.exit(0);
                                // TODO: 全 API で共通で起こりうるエラーの処理を共通化したい。
                            } else if (apiError.code == ERROR_CODE_2005.code) {
                                // 1 日 1 回までしか交換できない

                                // Detach 時に通信キャンセルしていないのでレスポンス受信時に Activity がない場合がある。
                                if (activity == null) {
                                    Toast.makeText(appContext, ErrorCode.getMessage(appContext, apiError.code, apiError.message), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // TODO: とりあえず簡易的にダイアログ表示。ちゃんと汎用化したい。
                                AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                        activity.getString(R.string.dialog_title_error_exchange),
                                        activity.getString(R.string.message_restricted_one_day));
                                dialog.show(getActivity().getSupportFragmentManager(), "alert_dialog");
                            } else if (apiError.code == ERROR_CODE_2007.code) {
                                // 在庫切れ

                                // Detach 時に通信キャンセルしていないのでレスポンス受信時に Activity がない場合がある。
                                if (activity == null) {
                                    Toast.makeText(appContext, ErrorCode.getMessage(appContext, apiError.code, apiError.message), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // TODO: とりあえず簡易的にダイアログ表示。ちゃんと汎用化したい。
                                AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                        activity.getString(R.string.dialog_title_error_exchange),
                                        activity.getString(R.string.message_out_of_stock));
                                dialog.show(getActivity().getSupportFragmentManager(), "alert_dialog");
                            } else {
                                // message をそのまま表示するエラーコード
                                // 未知 (多分、新設) のエラーコードは message をそのまま表示する。
                                Toast.makeText(appContext, ErrorCode.getMessage(appContext, apiError.code, apiError.message), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        // 送信処理
        purchaseRequest = VolleyApi.send(appContext, request);

        ((ShowableProgressDialog) getActivity()).showProgressDialog(null, getString(R.string.dialog_message_communicating));
    }


    /*
     * 状態管理
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(ItemListFragment fragment) {
                if (fragment.getItems()) {
                    transit(fragment, READY);
                } else {
                    transit(fragment, GETTING_ITEMS);
                }
            }
        },

        // 商品一覧取得中
        GETTING_ITEMS {
            @Override
            public void successGetItems(ItemListFragment fragment, List<Item> items) {
                fragment.showItems(items);

                transit(fragment, READY);
            }

            @Override
            public void failureGetItems(ItemListFragment fragment, String message) {
                Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();

                transit(fragment, ERROR);
            }

            @Override
            public void detach(ItemListFragment fragment) {
                ItemListManager.cancelGetItems(fragment);

                transit(fragment, INITIAL);
            }
        },

        // 操作可能状態
        READY,

        // エラー状態
        ERROR {
            @Override
            public void start(ItemListFragment fragment) {
                if (fragment.getItems()) {
                    transit(fragment, READY);
                } else {
                    transit(fragment, GETTING_ITEMS);
                }
            }
        };

        /*
         * イベント
         */
        // 初期処理開始
        public void start(ItemListFragment fragment) {
            throw new IllegalStateException();
        }

        // 商品一覧取得成功
        public void successGetItems(ItemListFragment fragment, List<Item> items) {
            throw new IllegalStateException();
        }

        // 商品一覧取得失敗
        public void failureGetItems(ItemListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // Detach
        public void detach(ItemListFragment fragment) {
            // どの状態でも Detach イベントが発生する可能性あり
            // 通信中でなければ何もしない。
        }

        /*
         * 状態遷移 (enum State 内でのみ使用すること)
        */
        private static void transit(ItemListFragment fragment, State nextState) {
            Logger.d(TAG, "STATE: " + fragment.state + " -> " + nextState);
            fragment.state = nextState;
        }
    }

    /**
     * 商品一覧を取得。
     *
     * @return true: 取得成功 / false: 取得待ち
     */
    private boolean getItems() {
        List<Item> items;

        items = ItemListManager.getItemList(getActivity().getApplicationContext(), this);

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
