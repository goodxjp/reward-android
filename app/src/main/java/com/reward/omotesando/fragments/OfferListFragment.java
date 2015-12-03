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

import java.util.ArrayList;
import java.util.List;

import com.reward.omotesando.R;

import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.OfferListManager;
import com.reward.omotesando.components.UserManager;
import com.reward.omotesando.models.Offer;
import com.reward.omotesando.models.OfferListTab;
import com.reward.omotesando.models.User;

/**
 * 案件一覧フラグメント。
 *
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class OfferListFragment extends BaseFragment
        implements AbsListView.OnItemClickListener,
        UserManager.Callback,
        OfferListManager.Callback {

    private static final String TAG = OfferListFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    //long mPoint;  // TODO: ポイントを表示を一時停止中
    int mCategoryId;  // 表示しているカテゴリ
    List<Offer> mOffers;

    // View
    //private TextView mCurrentPointText;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    private OnFragmentInteractionListener mListener;


    /*
     * 初期処理
     */
    private static final String ARG_CREATE_TIME = "create_time";
    private static final String ARG_CATEGORY_ID = "category_id";

    public static OfferListFragment newInstance(int categoryId) {
        OfferListFragment fragment = new OfferListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CREATE_TIME, System.currentTimeMillis());
        args.putInt(ARG_CATEGORY_ID, categoryId);
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
            Logger.v(TAG, "Create time  = " + getArguments().getLong(ARG_CREATE_TIME));
            Logger.v(TAG, "Current time = " + System.currentTimeMillis());

            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);  // ログだけのため

        // やっぱり、onDetach -> onAttach -> onCreate で変数が初期化されないのが腑に落ちない。
        Logger.v(TAG, "state = " + state);

        // バックスタックから戻ったときに状態は遷移したままで onCreateView のみが呼ばれる。
        if (state == State.INITIAL) {
            Logger.v(TAG, "Activity = " + getActivity());
            state.start(this);
        }

        View view = inflater.inflate(R.layout.fragment_offer_list, container, false);

//        mCurrentPointText = (TextView) view.findViewById(R.id.current_point_text);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // View の再設定は毎回せなあかんものだろうか？
        if (state == State.READY) {
            // View に ID 付けておけば復旧してくれるもの？
//            mCurrentPointText.setText(String.valueOf(mPoint));
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

        // ここで通信を止めていいか微妙だけど、ここが Fragment 終了時の最後の砦なのでしょうがない。
        // ただし、回転したときなどは onDetach -> onAttach -> onCreateView となり、終了されないこともあるので注意。

        // 取得中の通信止めちゃうので、onAttach で再取得するために初期状態に戻す。
        state.detach(this);
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

            // アクティビティにオファー詳細表示依頼
            mListener.onFragmentInteraction(offer);
        }
    }


    /*
     * UserManager.UserManagerCallback
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
     * OfferListManager.OfferListManagerCallback
     */
    @Override
    public void onSuccessGetOfferList(List<Offer> offers) {
        state.successGetOfferList(this, offers);
    }

    @Override
    public void onErrorGetOfferList(String message) {
        state.failureGetOfferList(this, message);
    }


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

                if (!fragment.getUser()) {
                    transit(fragment, GETTING_USER);
                } else if (!fragment.getOfferList()) {
                    transit(fragment, GETTING_OFFERS);
                } else {
                    transit(fragment, READY);
                }

            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetUser(OfferListFragment fragment, User user) {
                // ポイント表示を更新
//                fragment.mPoint = user.point;
//                fragment.mCurrentPointText.setText(String.valueOf(fragment.mPoint));
                if (!fragment.getOfferList()) {
                    transit(fragment, GETTING_OFFERS);
                } else {
                    transit(fragment, READY);
                }
            }

            @Override
            public void failureGetUser(OfferListFragment fragment, String message) {
                // TODO: エラーの表示方法をちゃんと考えた方がよさげ
                if (message != null) {
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                transit(fragment, ERROR);
            }

            @Override
            public void detach(OfferListFragment fragment) {
                UserManager.cancelGetUser(fragment);

                transit(fragment, INITIAL);
            }
        },

        // オファー情報取得中
        GETTING_OFFERS {
            @Override
            public void successGetOfferList(OfferListFragment fragment, List<Offer> offers) {
                fragment.showOfferList(fragment.filterOfferList(offers, fragment.mCategoryId));

                transit(fragment, READY);
            }

            @Override
            public void failureGetOfferList(OfferListFragment fragment, String message) {
                // TODO: エラーの表示方法をちゃんと考えた方がよさげ
                if (message != null) {
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                }

                transit(fragment, ERROR);
            }

            @Override
            public void detach(OfferListFragment fragment) {
                OfferListManager.cancelGetOfferList(fragment);

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
        public void start(OfferListFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetUser(OfferListFragment fragment, User user) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetUser(OfferListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // キャンペーン情報取得成功
        public void successGetOfferList(OfferListFragment fragment, List<Offer> offers) {
            throw new IllegalStateException();
        }

        // キャンペーン情報取得失敗
        public void failureGetOfferList(OfferListFragment fragment, String message) {
            throw new IllegalStateException();
        }

        // Detach
        public void detach(OfferListFragment fragment) {
            // どの状態でも Detach イベントが発生する可能性あり
            // 通信中でなければ何もしない。
        }

        /*
         * 状態遷移 (enum State 内でのみ使用すること)
         */
        private static void transit(OfferListFragment fragment, State nextState) {
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
//        if (user != null) {
//            //updateUser(user);
//            return true;
//        } else {
//            return false;
//        }
        return user != null;
    }

    /**
     * オファー情報取得。
     *
     * @return true: 取得成功 / false: 取得待ち
     */
    private boolean getOfferList() {
        List<Offer> offerList = OfferListManager.getOfferList(getActivity().getApplicationContext(), this);

        if (offerList != null) {
            // TODO: 将来的には OfferListManager にカテゴリ分けの責任を持たせる
            showOfferList(filterOfferList(offerList, mCategoryId));
            return true;
        } else {
            return false;
        }
    }

    private List<Offer> filterOfferList(List<Offer> offers, int categoryId) {
        List<Offer> result = new ArrayList<>();

        for (Offer offer : offers) {
            if (offer.campaignCategoryId == categoryId) {
                result.add(offer);
            }
        }

        return result;
    }

    private void showOfferList(List<Offer> offerList) {
        // 表示しているモデルを更新
        mOffers = offerList;

        // この時点で Activity が存在しないパターンがある？
        if (getActivity() == null) {
            Logger.e(TAG, "showOfferList() getActivity is null.");
            throw new NullPointerException();  // TODO: あとで消す
            //return;
        }

        // getView 上書きしているから textViewResourceId は実はなんでもいい。
        if (getResources().getBoolean(R.bool.is_classic)) {
            mAdapter = new OfferArrayAdapter(getActivity(), R.layout.list_item_offer_classic, mOffers);
        } else {
            mAdapter = new OfferArrayAdapter(getActivity(), R.layout.list_item_offer, mOffers);
        }
        // http://skyarts.com/blog/jp/skyarts/?p=3964
//        // API 9 で動かすための苦肉の策。
//        if (mListView instanceof ListView) {
//            ((ListView) mListView).setAdapter(adapter);
//        } else {
//            ((GridView) mListView).setAdapter(adapter);
//        }
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
        void onFragmentInteraction(Offer offer);
    }
}
