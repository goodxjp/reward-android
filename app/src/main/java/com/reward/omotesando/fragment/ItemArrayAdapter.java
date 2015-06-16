package com.reward.omotesando.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Item;

import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<Item> {
    LayoutInflater mInflater;

    public ItemArrayAdapter(Context context, int textViewResourceId, List<Item> list) {
        super(context, textViewResourceId, list);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_item, null);

            holder = new ViewHolder();
            holder.itemNameText = (TextView) view.findViewById(R.id.item_name_text);
            holder.itemPointText = (TextView) view.findViewById(R.id.item_point_text);
            holder.exchangeButton = (Button) view.findViewById(R.id.exchange_button);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Item item = getItem(position);

        // View

        // 各種表示データ更新
        holder.itemNameText.setText(item.name);
        holder.itemPointText.setText(String.valueOf(item.point));

        holder.exchangeButton.setTag(item);
        holder.exchangeButton.setOnClickListener(this.mOnExchangeButtonClickListener);

        return view;
    }

    // イベントリスナー
    View.OnClickListener mOnExchangeButtonClickListener;

    public void setOnExchangeButtonClickListener(View.OnClickListener l) {
        this.mOnExchangeButtonClickListener = l;
    }

    // ViewHolder パターン
    private static class ViewHolder {
        TextView itemNameText;
        TextView itemPointText;
        Button exchangeButton;
    }
}
