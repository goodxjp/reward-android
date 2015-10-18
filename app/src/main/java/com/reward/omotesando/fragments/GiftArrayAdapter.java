package com.reward.omotesando.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Gift;
import com.reward.omotesando.models.PointHistory;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.List;

public class GiftArrayAdapter extends ArrayAdapter<Gift> {
    LayoutInflater mInflater;

    public GiftArrayAdapter(Context context, int textViewResourceId, List<Gift> list) {
        super(context, textViewResourceId, list);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_gift, null);

            holder = new ViewHolder();
            holder.nameText = (TextView) view.findViewById(R.id.name_text);
            holder.codeText = (TextView) view.findViewById(R.id.code_text);
            holder.expirationAtText = (TextView) view.findViewById(R.id.expiration_at_text);
            holder.occurredAtText = (TextView) view.findViewById(R.id.occurred_at_text);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Gift gift = getItem(position);

        // View

        // 各種表示データ更新
        holder.nameText.setText(gift.name);
        holder.codeText.setText(gift.code);
        if (gift.expirationAt == null) {
            holder.expirationAtText.setText("");
        } else {
            holder.expirationAtText.setText(DateFormatUtils.ISO_DATE_FORMAT.format(gift.expirationAt) + "\n" + DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(gift.expirationAt));  // TODO: 日付と時刻でちゃんとレイアウトをわける
        }
        holder.occurredAtText.setText(DateFormatUtils.ISO_DATE_FORMAT.format(gift.occurredAt) + "\n" + DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(gift.occurredAt));  // TODO: 日付と時刻でちゃんとレイアウトをわける

        return view;
    }


    // ViewHolder パターン
    private static class ViewHolder {
        TextView nameText;
        TextView codeText;
        TextView expirationAtText;
        TextView occurredAtText;
    }
}
