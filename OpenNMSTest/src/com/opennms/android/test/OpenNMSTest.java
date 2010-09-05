package com.opennms.android.test;

import android.test.ActivityInstrumentationTestCase2;

import com.opennms.android.OpenNMS;

public class OpenNMSTest extends ActivityInstrumentationTestCase2<OpenNMS> {

	public OpenNMSTest() {
		super("com.opennms.android", OpenNMS.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
}
