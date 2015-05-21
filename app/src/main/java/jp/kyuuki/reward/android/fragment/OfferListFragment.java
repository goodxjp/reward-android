package jp.kyuuki.reward.android.fragment;

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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import jp.kyuuki.reward.android.R;

import jp.kyuuki.reward.android.activities.MainActivity;
import jp.kyuuki.reward.android.commons.Logger;
import jp.kyuuki.reward.android.commons.VolleyUtils;
import jp.kyuuki.reward.android.components.api.GetMediaUsers;
import jp.kyuuki.reward.android.components.api.GetOffers;
import jp.kyuuki.reward.android.models.MediaUser;
import jp.kyuuki.reward.android.models.Offer;

/**
 * オファー一覧フラグメント。
 *
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class OfferListFragment extends BaseFragment implements AbsListView.OnItemClickListener {

    private static final String TAG = OfferListFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    private OnFragmentInteractionListener mListener;

    // Model
    long mPoint;
    List<Offer> offers;

    // View
    private TextView mCurrentPointText;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // 通信
    public RequestQueue mRequestQueue;

    /*
     * 初期処理
     */
    private static final String ARG_CREATE_TIME = "create_time";

    public static OfferListFragment newInstance() {
        OfferListFragment fragment = new OfferListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CREATE_TIME, System.currentTimeMillis());
        fragment.setArguments(args);
        return fragment;
    }

    // 空のコンストラクタが必要。
    // http://y-anz-m.blogspot.jp/2012/04/androidfragment-setarguments.html
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OfferListFragment() {
    }


    /*
     * ライフサイクル
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // このフラグメントは回転しても作り直さない。
        // Activity で強制的に作り直しちゃっている場合があるので要注意！
        setRetainInstance(true);

        if (getArguments() != null) {
            Logger.e(TAG, "Create time  = " + getArguments().getLong(ARG_CREATE_TIME));
            Logger.e(TAG, "Current time = " + System.currentTimeMillis());
        }

        mRequestQueue = VolleyUtils.getRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: ログだけのために無理やり感が強い。もっときれいな方法ないか？
        super.onCreateView(inflater, container, savedInstanceState);

        // バックスタックから戻ったときに状態は遷移したままで onCreateView のみが呼ばれる。
        if (state == State.INITIAL) {
            state.start(this);
        }

        View view = inflater.inflate(R.layout.fragment_offerlist, container, false);

        mCurrentPointText = (TextView) view.findViewById(R.id.current_point_text);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // View の再設定は毎回せなあかんものだろうか？
        if (state == State.READY) {
            // View に ID 付けておけば復旧してくれるもの？
            mCurrentPointText.setText(String.valueOf(mPoint));
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
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
        Offer offer = (Offer) parent.getItemAtPosition(position);

        if (mListener != null) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
            mListener.onFragmentInteraction(offer);
        }

        // TODO: オファー詳細表示

        //Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(offer.getExecuteUrl()));
        //startActivity(i);
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
        void onFragmentInteraction(Offer offer);
    }


    // このフラグメントにデータ取得の責任も持たせてしまう。
    /*
     * 状態管理
     *
     * - 参考: http://idios.hatenablog.com/entry/2012/07/07/235137
     */
    private State state = State.INITIAL;

    // 全部のメソッドに状態管理対象のオブジェクトを引数と渡すのがキモいけど、State パターンも同じような感じかも。
    // 状態をオブジェクトにしちゃうと重くなりそうだし、しかたがないかな。
    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(OfferListFragment fragment) {
                fragment.getMediaUser();

                fragment.transit(GETTING_USER);
            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetMediaUser(OfferListFragment fragment, MediaUser mediaUser) {
                // ポイント表示を更新
                fragment.mPoint = mediaUser.point;
                fragment.mCurrentPointText.setText(String.valueOf(fragment.mPoint));
                fragment.getCampaignData();

                fragment.transit(GETTING_OFFERS);
            }

            @Override
            public void failureGetMediaUser(OfferListFragment fragment) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                MainActivity activity = (MainActivity) fragment.getActivity();
                if (activity != null) {
                    activity.dismissProgressDialog();
                    Toast.makeText(activity, activity.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                fragment.transit(ERROR);
            }
        },

        // オファー情報取得中
        GETTING_OFFERS {
            @Override
            public void successGetCampaginData(OfferListFragment fragment, List<Offer> offers) {
                fragment.showCampaignData();

                // TODO: MainActivity 依存を解消する。
                MainActivity activity = (MainActivity) fragment.getActivity();
                if (activity != null) {
                    activity.dismissProgressDialog();
                }

                fragment.transit(READY);
            }

            @Override
            public void failureGetCampaginData(OfferListFragment fragment) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                MainActivity activity = (MainActivity) fragment.getActivity();
                if (activity != null) {
                    activity.dismissProgressDialog();
                    Toast.makeText(activity, activity.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                fragment.transit(ERROR);
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
        public void start(OfferListFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetMediaUser(OfferListFragment fragment, MediaUser mediaUser) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetMediaUser(OfferListFragment fragment) {
            throw new IllegalStateException();
        }

        // キャンペーン情報取得成功
        public void successGetCampaginData(OfferListFragment fragment, List<Offer> offers) {
            throw new IllegalStateException();
        }

        // キャンペーン情報取得失敗
        public void failureGetCampaginData(OfferListFragment fragment) {
            throw new IllegalStateException();
        }
    }

    // 状態遷移 (enum State 内でのみ使用すること)
    private void transit(State nextState) {
        Logger.d(TAG, "STATE: " + state + " -> " + nextState);
        state = nextState;
    }

    // ユーザー情報取得
    private void getMediaUser() {
        MediaUser mediaUser = MediaUser.getMediaUser(getActivity());

        // TODO: 状態遷移でユーザー登録が確実にすんでいるようにする。
        long mediaUserId = -1;
        if (mediaUser != null) {
            mediaUserId = mediaUser.mediaUserId;
        }

        final GetMediaUsers api = GetMediaUsers.create(mediaUserId);

        JsonObjectRequest request = new JsonObjectRequest(api.getUrl(),

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Logger.i(TAG, "HTTP: body is " + response.toString());

                    MediaUser mediaUser = api.parseJsonResponse(response);
                    state.successGetMediaUser(OfferListFragment.this, mediaUser);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureGetMediaUser(OfferListFragment.this);
                }
            }
        );

        ((MainActivity) getActivity()).showProgressDialog(null, getString(R.string.dialog_message_communicating));
        mRequestQueue.add(request);
    }

    // キャンペーン情報 (案件情報) 取得
    private void getCampaignData() {
        MediaUser mediaUser = MediaUser.getMediaUser(getActivity());

        // TODO: 状態遷移でユーザー登録が確実にすんでいるようにする。
        long mediaUserId = -1;
        if (mediaUser != null) {
            mediaUserId = mediaUser.mediaUserId;
        }

        // TODO: メディア ID 取得
        final GetOffers api = GetOffers.create(1, mediaUserId);

        JsonArrayRequest request = new JsonArrayRequest(api.getUrl(),

            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Logger.i(TAG, "HTTP: body is " + response.toString());

                    offers = api.parseJsonResponse(response);
                    state.successGetCampaginData(OfferListFragment.this, offers);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureGetCampaginData(OfferListFragment.this);
                }
            }
        );

        mRequestQueue.add(request);
    }

    private void showCampaignData() {
        // この時点で Activity が存在しないパターンがある
        if (getActivity() == null) {
            Logger.e(TAG, "showCampaignData() getActivity is null.");
            return;
        }

        mAdapter = new OfferArrayAdapter(getActivity(), R.layout.list_item_offer, offers);
        // http://skyarts.com/blog/jp/skyarts/?p=3964
//        // API 9 で動かすための苦肉の策。
//        if (mListView instanceof ListView) {
//            ((ListView) mListView).setAdapter(adapter);
//        } else {
//            ((GridView) mListView).setAdapter(adapter);
//        }
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }
}
