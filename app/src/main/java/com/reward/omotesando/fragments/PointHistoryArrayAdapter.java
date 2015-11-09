package com.reward.omotesando.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.List;

import com.reward.omotesando.R;
import com.reward.omotesando.models.PointHistory;

public class PointHistoryArrayAdapter extends ArrayAdapter<PointHistory> {
    LayoutInflater mInflater;

    public PointHistoryArrayAdapter(Context context, int textViewResourceId, List<PointHistory> list) {
        super(context, textViewResourceId, list);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_point_history, null);

            holder = new ViewHolder();
            holder.occurredAtText = (TextView) view.findViewById(R.id.occurred_at);
            holder.nameText = (TextView) view.findViewById(R.id.name_text);
            holder.pointText = (TextView) view.findViewById(R.id.point_text);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        PointHistory pointHistory = getItem(position);

        // View

        // 各種表示データ更新
        holder.occurredAtText.setText(DateFormatUtils.ISO_DATE_FORMAT.format(pointHistory.occurredAt) + "\n" + DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(pointHistory.occurredAt));  // TODO: 日付と時刻でちゃんとレイアウトをわける
        holder.nameText.setText(pointHistory.detail);
        if (pointHistory.pointChange > 0) {
            holder.pointText.setText("+" + pointHistory.pointChange);
        } else {
            holder.pointText.setText(String.valueOf(pointHistory.pointChange));
        }

        return view;
    }


    // ViewHolder パターン
    private static class ViewHolder {
        TextView occurredAtText;
        TextView nameText;
        TextView pointText;
    }
}
