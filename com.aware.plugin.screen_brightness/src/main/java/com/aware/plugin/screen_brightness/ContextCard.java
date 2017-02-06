package com.aware.plugin.screen_brightness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {
    private BrightnessListener brightnessListener = new BrightnessListener();
    private TextView text;
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

    private void updateBrightness(Context context){
        Cursor latest = context.getContentResolver().query(Provider.Brightness_Data.CONTENT_URI, null, null, null, Provider.Brightness_Data.TIMESTAMP + " DESC LIMIT 1");
        if(latest != null && latest.moveToFirst()){
            String brightness = latest.getString(latest.getColumnIndex(Provider.Brightness_Data.BRIGHTNESS));
            String autoBrightness = latest.getString(latest.getColumnIndex(Provider.Brightness_Data.AUTO_BRIGHTNESS));
            text.setText("Brightness: " + brightness + " Auto-brightness: " + autoBrightness);
        }
        if(latest != null && !latest.isClosed()){
            latest.close();
        }
    }

    @Override
    public View getContextCard(Context context) {
        View card = LayoutInflater.from(context).inflate(R.layout.card, null);
        text = (TextView) card.findViewById(R.id.hello);
        text.setText("Loading..");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Plugin.ACTION_PLUGIN_SCREEN_BRIGHTNESS);
        context.registerReceiver(brightnessListener, filter);
        return card;
    }
}
