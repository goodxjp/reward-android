package com.reward.omotesando.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.models.Offer;

/**
 * 案件詳細フラグメント。
 *
 * A fragment with a Google +1 button.
 * Use the {@link OfferDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OfferDetailFragment extends BaseFragment {

    private static final String TAG = OfferDetailFragment.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    private Offer mOffer;

    // View
    private ImageView mIconImage;

    private TextView mName;
    private TextView mDetail;
    private TextView mPoint;
    private TextView mPeriod;
    private TextView mRequirement;
    private TextView mRequirementDetail;

    private Button mExecuteButton;


    /*
     * 初期処理
     */
    private static final String ARG_OFFER = "offer";

    public static OfferDetailFragment newInstance(Offer offer) {
        OfferDetailFragment fragment = new OfferDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_OFFER, offer);
        fragment.setArguments(args);
        return fragment;
    }

    public OfferDetailFragment() {
        // Required empty public constructor
    }


    /*
     * ライフサイクル
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {
            mOffer = (Offer) getArguments().getSerializable(ARG_OFFER);
        } else {
            // バグ
            throw new IllegalArgumentException("OfferDetailFragment getArguments is null.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if (getResources().getBoolean(R.bool.is_classic)) {
            view = inflater.inflate(R.layout.fragment_offer_detail_classic, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_offer_detail, container, false);
        }

        mIconImage =  (ImageView) view.findViewById(R.id.icon_image);

        // http://qiita.com/gari_jp/items/829a54bfa937f4733e29
        ImageContainer imageContainer = (ImageContainer) mIconImage.getTag();
        if (imageContainer != null) {
            imageContainer.cancelRequest();
        }

        ImageLoader imageLoader = VolleyUtils.getImageLoader(getActivity());
        // TODO: 画像をちゃんとしたものに変更
        ImageListener listener = ImageLoader.getImageListener(mIconImage, android.R.drawable.ic_menu_rotate, android.R.drawable.ic_delete);
        mIconImage.setTag(imageLoader.get(mOffer.iconUrl, listener));

        mName = (TextView) view.findViewById(R.id.name_text);
        mName.setText(mOffer.name);

        mPoint = (TextView) view.findViewById(R.id.point_text);
        mPoint.setText("" + mOffer.point);

        mDetail = (TextView) view.findViewById(R.id.detail_text);
        mDetail.setText(mOffer.detail);

        mRequirement = (TextView) view.findViewById(R.id.requirement_text);
        mRequirement.setText(mOffer.requirement);

        mRequirementDetail = (TextView) view.findViewById(R.id.requirement_detail_text);
        mRequirementDetail.setText(mOffer.requirementDetail);

        mExecuteButton = (Button) view.findViewById(R.id.execute_button);
        mExecuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    // 案件実行
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mOffer.getExecuteUrl()));
                    startActivity(i);
                }
            }
        );

        return view;
    }
}
