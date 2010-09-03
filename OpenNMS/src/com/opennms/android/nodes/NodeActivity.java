package com.opennms.android.nodes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NodeActivity extends Activity {
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Node Search Here");
        setContentView(textview);
    }

}
