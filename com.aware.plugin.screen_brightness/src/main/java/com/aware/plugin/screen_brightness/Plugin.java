package com.aware.plugin.screen_brightness;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;
import com.aware.utils.Scheduler;

public class Plugin extends Aware_Plugin {
    public static final String ACTION_PLUGIN_SCREEN_BRIGHTNESS = "ACTION_PLUGIN_SCREEN_BRIGHTNESS";
    public static final String SCHEDULER_PLUGIN_SCREEN_BRIGHTNESS = "SCHEDULER_PLUGIN_SCREEN_BRIGHTNESS";
    private static ContextProducer contextProducer;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        /**
         * Plugins share their current status, i.e., context using this method.
         * This method is called automatically when triggering
         * {@link Aware#ACTION_AWARE_CURRENT_CONTEXT}
         **/
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                if(Aware.DEBUG) Log.d(Plugin.TAG, "Context changed, sending broadcast to UI!");
                Intent updateUiIntent = new Intent(ACTION_PLUGIN_SCREEN_BRIGHTNESS);
                sendBroadcast(updateUiIntent);
            }
        };

        contextProducer = CONTEXT_PRODUCER;

        //Add permissions you need (Android M+).
        //By default, AWARE asks access to the #Manifest.permission.WRITE_EXTERNAL_STORAGE

        //REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.Brightness_Data.CONTENT_URI };

        //Activate plugin -- do this ALWAYS as the last thing (this will restart your own plugin and apply the settings)
        Aware.startPlugin(this, "com.aware.plugin.screen_brightness");
    }

    public static ContextProducer getContextProducer() {
        return contextProducer;
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean permissions_ok = true;
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {
            //Check if the user has toggled the debug messages
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            //Initialize our plugin's settings
            Aware.setSetting(this, Settings.INTERVAL_PLUGIN_SCREEN_BRIGHTNESS, Settings.DEFAULT_INTERVAL_PLUGIN_SCREEN_BRIGHTNESS);

            try{
                Scheduler.Schedule brightnessSampler = Scheduler.getSchedule(this, SCHEDULER_PLUGIN_SCREEN_BRIGHTNESS);
                if(brightnessSampler == null || brightnessSampler.getInterval() != Long.parseLong(Aware.getSetting(this, Settings.INTERVAL_PLUGIN_SCREEN_BRIGHTNESS))){
                    brightnessSampler = new Scheduler.Schedule(SCHEDULER_PLUGIN_SCREEN_BRIGHTNESS)
                            .setInterval(Long.parseLong(Aware.getSetting(this, Settings.INTERVAL_PLUGIN_SCREEN_BRIGHTNESS)))
                            .setActionType(Scheduler.ACTION_TYPE_SERVICE)
                            .setActionClass(getPackageName() + "/" + BrightnessService.class.getName());
                    Scheduler.saveSchedule(this, brightnessSampler);
                    // Run once immediately
                    Intent initialValues = new Intent(this, BrightnessService.class);
                    startService(initialValues);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Scheduler.removeSchedule(this, SCHEDULER_PLUGIN_SCREEN_BRIGHTNESS);

        //Stop AWARE's instance running inside the plugin package
        Aware.stopAWARE();
    }
}
