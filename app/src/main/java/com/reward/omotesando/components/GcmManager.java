package com.reward.omotesando.components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;

/**
 * GCM マネージャー。
 *
 * - GCM サンプルの Activity でやってしまっていることをコンポーネント化。
 * - https://github.com/google/gcm/blob/master/gcm-client/GcmClient/src/main/java/com/google/android/gcm/demo/app/DemoActivity.java
 */
public class GcmManager {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    //String senderId;
    // Google Developers Console の「プロジェクト番号」
    // String Resources で管理する。

    /**
     * Tag used on log messages.
     */
    static final String TAG = GcmManager.class.getName();

    private static GcmManager gcmManager;

    private Context mApplicationContext;
    private GoogleCloudMessaging mGcm;

    private GcmManager(Context applicationContext) {
        mApplicationContext = applicationContext;
        mGcm = GoogleCloudMessaging.getInstance(applicationContext);
    }

    public static synchronized GcmManager getInstance(Context applicationContext) {
        if (gcmManager == null) {
            gcmManager = new GcmManager(applicationContext);
        }

        return gcmManager;
    }

    // とりあえず、登録されているかどうかにかかわらず、登録しようとしてみる。
    // TODO: 不整合が起きたときに強制的に registration ID を取得するしくみを追加。
    public boolean tryToRegister(Activity activity, GcmManagerCallbacks callbacks) {
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices(activity) == false) {
            Logger.d(TAG, "gcm = " + mGcm);  // Google 開発者サービスをインストールしていなくても gcm インスタンスはできるようだ。
            Logger.i(TAG, "No valid Google Play Services APK found.");
            return false;
        }

        String regId = getRegistrationId(mApplicationContext);
        //regId = ""; // TODO: 強制的に送信

        if (regId.isEmpty()) {
            // GCM 未登録
            registerInBackground(callbacks);
            return true;
        }

        return false;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity activity) {
    //private boolean checkPlayServices() {
        //int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplicationContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // TODO: ほとんどないはずだが、もうちょっと親切にダイアログを表示する。
                Logger.e(TAG, "This device is not supported.");
                //finish();
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Logger.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Logger.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final GcmManagerCallbacks callbacks) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String regId = "";
                try {
                    // gcm はコンストラクタで作成済み。
//                    if (gcm == null) {
//                        gcm = GoogleCloudMessaging.getInstance(applicationContext);
//                    }
                    String senderId = mApplicationContext.getString(R.string.gcm_sender_id);
                    Logger.d(TAG, "sender ID = " + senderId);
                    regId = mGcm.register(senderId);
                    // ↑のメソッド完了が遅い場合あり。
                    Logger.d(TAG, "Device registered, registration ID=" + regId);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    //sendRegistrationIdToBackend();
                    // onPostExecute 内のコールバック関数で実施。

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mApplicationContext, regId);
                } catch (IOException ex) {
                    Logger.e(TAG, "Error :" + ex.getMessage());
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.

                    // TODO: エラー内容により、処理を行う。
                    // http://growthhack.sirok.co.jp/growthpush/android-sdk-3/
                    // リトライするべきエラー内容もあるが、現状は再起動時のタイミングでリトライしよう。
                }

                return regId;
            }

            @Override
            protected void onPostExecute(String regid) {
                Logger.d(TAG, "onPostExecute regid = " + regid);

                if (callbacks != null) {
                    callbacks.onRegistered(regid);
                }
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(GcmManager.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    // コールバック
    public static interface GcmManagerCallbacks {
        void onRegistered(String regId);
    }
}
