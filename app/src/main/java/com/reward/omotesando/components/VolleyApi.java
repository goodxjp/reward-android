package com.reward.omotesando.components;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.api.RewardApi;

import org.json.JSONArray;
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
}
