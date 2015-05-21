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
import jp.kyuuki.reward.android.activities.ShowableProgressDialog;
import jp.kyuuki.reward.android.commons.Logger;
import jp.kyuuki.reward.android.commons.VolleyUtils;
import jp.kyuuki.reward.android.components.api.GetMediaUsers;
import jp.kyuuki.reward.android.components.api.GetPointHistories;
import jp.kyuuki.reward.android.models.MediaUser;
import jp.kyuuki.reward.android.models.PointHistory;

/**
 * ポイント履歴一覧フラグメント。
 */
public class PointHistoryListFragment extends BaseFragment {

    private static final String TAG = PointHistoryListFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    long mPoint;
    List<PointHistory> mPointHistories;

    // View
    private TextView mCurrentPointText;
    private AbsListView mListView;

    private ListAdapter mAdapter;

    // 進捗ダイアログを表示してくれる人
    private ShowableProgressDialog mShowableProgressDialog;

    // 通信
    public RequestQueue mRequestQueue;

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

        // このフラグメントは回転しても作り直さない。
        setRetainInstance(true);

        mShowableProgressDialog = (ShowableProgressDialog) getActivity();
        mRequestQueue = VolleyUtils.getRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // バックスタックから戻ったときに状態は遷移したままで onCreateView のみが呼ばれる。
        // ポイント履歴画面から他の Activity 呼び出さないので、onCreateView のみ呼ばれるパターンが存在する？！
        // とりあえず、オファー一覧フラグメント共通にしておく。
        if (state == State.INITIAL) {
            state.start(this);
        }

        View view = inflater.inflate(R.layout.fragment_point_history_list_list, container, false);

        mCurrentPointText = (TextView) view.findViewById(R.id.current_point_text);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // View の再設定は毎回せなあかんものだろうか？
        if (state == State.READY) {
            mCurrentPointText.setText(String.valueOf(mPoint));
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
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
    }

    // TODO: ポイント履歴が一件もないときの表示も考える (優先度低め)
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
     * 状態管理
     *
     * - 参考: http://idios.hatenablog.com/entry/2012/07/07/235137
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(PointHistoryListFragment fragment) {
                fragment.getMediaUser();

                fragment.transit(GETTING_USER);
            }
        },

        // ユーザー情報取得中
        GETTING_USER {
            @Override
            public void successGetMediaUser(PointHistoryListFragment fragment, MediaUser mediaUser) {
                // ポイント表示を更新
                fragment.mPoint = mediaUser.point;
                fragment.mCurrentPointText.setText(String.valueOf(fragment.mPoint));
                fragment.getPointHistories();

                fragment.transit(GETTING_POINT_HISTORIES);
            }

            @Override
            public void failureGetMediaUser(PointHistoryListFragment fragment) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                fragment.mShowableProgressDialog.dismissProgressDialog();
                Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();

                fragment.transit(ERROR);
            }
        },

        // ポイント履歴取得中
        GETTING_POINT_HISTORIES {
            @Override
            public void successGetPointHistories(PointHistoryListFragment fragment, List<PointHistory> pointHistories) {
                fragment.showPointHistories();
                fragment.mShowableProgressDialog.dismissProgressDialog();

                fragment.transit(READY);
            }

            @Override
            public void failureGetPointHistories(PointHistoryListFragment fragment) {
                // TODO: 端末の通信状態を確認
                // TODO: サーバーの状態を確認
                // TODO: エラーダイアログを表示
                fragment.mShowableProgressDialog.dismissProgressDialog();
                Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_communication), Toast.LENGTH_LONG).show();

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
        public void start(PointHistoryListFragment fragment) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得成功
        public void successGetMediaUser(PointHistoryListFragment fragment, MediaUser mediaUser) {
            throw new IllegalStateException();
        }

        // ユーザー情報取得失敗
        public void failureGetMediaUser(PointHistoryListFragment fragment) {
            throw new IllegalStateException();
        }

        // ポイント履歴取得成功
        public void successGetPointHistories(PointHistoryListFragment fragment, List<PointHistory> pointHistories) {
            throw new IllegalStateException();
        }

        // ポイント履歴取得失敗
        public void failureGetPointHistories(PointHistoryListFragment fragment) {
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
                    state.successGetMediaUser(PointHistoryListFragment.this, mediaUser);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureGetMediaUser(PointHistoryListFragment.this);
                }
            }
        );

        mShowableProgressDialog.showProgressDialog(null, getString(R.string.dialog_message_communicating));
        mRequestQueue.add(request);
    }

    // キャンペーン情報 (案件情報) 取得
    private void getPointHistories() {
        MediaUser mediaUser = MediaUser.getMediaUser(getActivity());

        // TODO: 状態遷移でユーザー登録が確実にすんでいるようにする。
        long mediaUserId = -1;
        if (mediaUser != null) {
            mediaUserId = mediaUser.mediaUserId;
        }

        // TODO: メディア ID 取得
        final GetPointHistories api = GetPointHistories.create(1, mediaUserId);

        JsonArrayRequest request = new JsonArrayRequest(api.getUrl(),

            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Logger.i(TAG, "HTTP: body is " + response.toString());

                    mPointHistories = api.parseJsonResponse(response);
                    state.successGetPointHistories(PointHistoryListFragment.this, mPointHistories);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureGetPointHistories(PointHistoryListFragment.this);
                }
            }
        );

        mRequestQueue.add(request);
    }

    // ポイント履歴を表示
    private void showPointHistories() {
        // この時点で Activity が存在しないパターンがある
        if (getActivity() == null) {
            Logger.e(TAG, "showPointHistories() getActivity is null.");
            return;
        }

        mAdapter = new PointHistoryArrayAdapter(getActivity(), R.layout.list_item_point_history, mPointHistories);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }
}
