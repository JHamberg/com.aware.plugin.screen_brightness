package com.aware.plugin.screen_brightness;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.aware.Aware;

/**
 * Created by Jonatan Hamberg on 6.2.2017.
 */
public class BrightnessAnalyser extends IntentService{
    public static String brightness;
    public static String autoBrightness;

    public BrightnessAnalyser() {
        super(Plugin.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentValues data = new ContentValues();
        brightness = getScreenBrightness();
        autoBrightness = isAutoBrightness();
        data.put(Provider.Brightness_Data.BRIGHTNESS, brightness);
        data.put(Provider.Brightness_Data.AUTO_BRIGHTNESS, autoBrightness);
        getContentResolver().insert(Provider.Brightness_Data.CONTENT_URI, data);
        if(Aware.DEBUG){
            Log.d(Plugin.TAG, data.toString());
        }
    }

    private String isAutoBrightness() {
        try {
            int mode = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            return mode == 1 ? "true" : "false";
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    private String getScreenBrightness(){
        try {
            Integer brightness = android.provider.Settings.System.getInt(getApplicationContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            return brightness < 0 ? "unknown" : brightness.toString();
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }
}
