package com.reward.omotesando.models;

import android.content.Context;

import com.reward.omotesando.R;

/**
 * メディア。
 *
 * - 本当は単数形の Medium が正しいかもしれないが、日本語的にわかりづらいので Media という名前で作成。
 */
public class Media {
    static Media media;

    public int mediaId;
    public String mediaKey;

    public static Media getMedia(Context context) {
        if (media != null) {
            return media;
        }

        int mediaId = context.getResources().getInteger(R.integer.media_id);
        String mediaKey = context.getResources().getString(R.string.media_key);

        media = new Media();
        media.mediaId = mediaId;
        media.mediaKey = mediaKey;

        return media;
    }
}
