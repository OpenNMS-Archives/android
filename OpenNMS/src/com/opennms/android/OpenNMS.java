package com.opennms.android;

import com.opennms.android.about.AboutActivity;
import com.opennms.android.alarms.AlarmActivity;
import com.opennms.android.nodes.NodeActivity;
import com.opennms.android.outages.OutageActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class OpenNMS extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, OutageActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("outages")
        	.setIndicator("Outages", res.getDrawable(R.drawable.ic_tab_outages))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, AlarmActivity.class);
        spec = tabHost.newTabSpec("alarms")
        	.setIndicator("Alarms", res.getDrawable(R.drawable.ic_tab_alarms))
        	.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, NodeActivity.class);
        spec = tabHost.newTabSpec("nodes")
        	.setIndicator("Nodes", res.getDrawable(R.drawable.ic_tab_nodes))
        	.setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, AboutActivity.class);
        spec = tabHost.newTabSpec("about")
        	.setIndicator("About", res.getDrawable(R.drawable.ic_tab_about))
        	.setContent(intent);
        tabHost.addTab(spec);
    }
}