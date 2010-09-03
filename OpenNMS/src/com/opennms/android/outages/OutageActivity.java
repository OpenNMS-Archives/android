package com.opennms.android.outages;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OutageActivity extends Activity {
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Outage List Here");
        setContentView(textview);
    }

}
