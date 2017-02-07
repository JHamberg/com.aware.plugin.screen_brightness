package com.aware.plugin.screen_brightness;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.utils.IContextCard;

import org.w3c.dom.Text;

import java.util.Date;

public class ContextCard implements IContextCard {
    private BrightnessListener brightnessListener = new BrightnessListener();
    private TextView brightness, autoBrightness, lastUpdated;
    public class BrightnessListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBrightness(context);
        }
    }

    //Constructor used to instantiate this card
    public ContextCard() {
        // Not implemented
    }

    @SuppressLint("SetTextI18n")
    private void updateBrightness(Context context){
        Cursor latest = context.getContentResolver().query(Provider.Brightness_Data.CONTENT_URI, null, null, null, Provider.Brightness_Data.TIMESTAMP + " DESC LIMIT 1");
        if(latest != null && latest.moveToFirst()){
            String brightness = latest.getString(latest.getColumnIndex(Provider.Brightness_Data.BRIGHTNESS));
            String autoBrightness = latest.getString(latest.getColumnIndex(Provider.Brightness_Data.AUTO_BRIGHTNESS));
            this.brightness.setText("Brightness: " + brightness);
            this.autoBrightness.setText("Auto-brightness: " + autoBrightness);
        }
        if(latest != null && !latest.isClosed()){
            latest.close();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getContextCard(Context context) {
        View card = LayoutInflater.from(context).inflate(R.layout.card, null);
        brightness = (TextView) card.findViewById(R.id.brightness);
        autoBrightness = (TextView) card.findViewById(R.id.autoBrightness);
        brightness.setText("Loading..");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Plugin.ACTION_PLUGIN_SCREEN_BRIGHTNESS);
        context.registerReceiver(brightnessListener, filter);

        updateBrightness(context);
        return card;
    }
}
