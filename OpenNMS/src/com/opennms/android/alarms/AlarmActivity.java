package com.opennms.android.alarms;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AlarmActivity extends Activity {
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Alarm List Here");
        setContentView(textview);
    }

}
