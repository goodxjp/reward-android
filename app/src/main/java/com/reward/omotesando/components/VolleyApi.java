package com.reward.omotesando.components;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.api.RewardApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Volley ライブラリと API のブリッジ。
 */
public class VolleyApi {
    static Map<String, Integer> methodMap;

    static {
        methodMap = new HashMap<>();
        methodMap.put("GET", Request.Method.GET);
        methodMap.put("POST", Request.Method.POST);
        methodMap.put("PUT", Request.Method.PUT);
        methodMap.put("DELETE", Request.Method.DELETE);
        methodMap.put("HEAD", Request.Method.HEAD);
        methodMap.put("OPTIONS", Request.Method.OPTIONS);
        methodMap.put("TRACE", Request.Method.TRACE);
        methodMap.put("PATCH", Request.Method.PATCH);
    }

    public static int getVolleyMethod(RewardApi api) {
        return method2VolleyMethod(api.method);
    }

    public static int method2VolleyMethod(String method) {
        String m = method.toUpperCase();
        return methodMap.get(m);
    }

    public static void Log(String tag, RewardApi api, JSONObject response) {
        Logger.i(tag, "HTTP: [" + api.getClass().getSimpleName() + "] response = " + response.toString());
    }

    public static void Log(String tag, RewardApi api, JSONArray response) {
        Logger.i(tag, "HTTP: [" + api.getClass().getSimpleName() + "] response = " + response.toString());
    }

    public static void Log(String tag, RewardApi api, VolleyError error) {
        Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] error = " + error.getMessage());
        if (error.networkResponse != null) {
            Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] statusCode = " + error.networkResponse.statusCode);
            if (error.networkResponse.data != null) {
                String data = null;
                try {
                    data = new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(tag, "Data is not String.");
                }
                Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] data = " + data);
            }
        }
    }

    // API のエラーコードとメッセージ
    // TODO: Volley に依存しない部分は他の所に移動したい。
    public static class ApiError {
        public int code;
        public String message;

//        ApiError(int code, String message) {
//            this.code = code;
//            this.message = message;
//        }
    }

    public static ApiError parseVolleyError(VolleyError error) {
        if (error.networkResponse == null) {
            return null;
        }

        if (error.networkResponse.data == null) {
            return null;
        }

        ApiError apiError = new ApiError();
        try {
            String data = new String(error.networkResponse.data, "UTF-8");
            JSONObject o = new JSONObject(data);
            apiError.code = o.getInt("code");
            apiError.message = o.getString("message");
        } catch (UnsupportedEncodingException e) {
            // TODO 致命的エラー検出
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            // TODO 致命的エラー検出
            e.printStackTrace();
            return null;
        }

        return apiError;
    }
}
