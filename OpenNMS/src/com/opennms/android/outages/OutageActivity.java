package com.opennms.android.outages;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.opennms.android.R;

public class OutageActivity extends ListActivity {
	private static final String TAG = "OutageActivity";
	private ProgressDialog m_progressDialog = null;
	private List<Outage> m_outages = new ArrayList<Outage>();
	private OutageAdapter m_outageAdapter = null;
	private Runnable m_viewOutages = null;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.outage);
        m_outageAdapter = new OutageAdapter(this, R.layout.severity_item, m_outages);
        setListAdapter(m_outageAdapter);
        m_viewOutages = new Runnable() {
        	public void run() {
        		try {
					getData();
				} catch (final Exception e) {
					Log.w(TAG, "An error occurred getting outage data.", e);
				}
        	}
        };
        Thread thread = new Thread(null, m_viewOutages, "MagentoBackground");
        thread.start();
        m_progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
    }

    public void getData() throws IOException, ParserConfigurationException, SAXException {
    	Log.d(TAG, "getData()");
    	List<Outage> outages = new ArrayList<Outage>();
    	
    	ClientResource resource = new ClientResource("http://localhost:8980/opennms/rest/outages?limit=50&orderBy=ifLostService&order=desc&ifRegainedService=null");
        resource.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin"));

        Log.d(TAG, "getting resource");
        resource.get();
        if (resource.getStatus().isSuccess() && resource.getResponseEntity().isAvailable()) { 
        	Log.d(TAG, "resource is available, parsing");
        	SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            OutageParseHandler handler = new OutageParseHandler(false);
            xr.setContentHandler(handler);
            Reader reader = null;
            try {
            	reader = resource.getResponseEntity().getReader();
				xr.parse(new InputSource(reader));
				outages = handler.getOutages();
            } catch (final Exception e) {
            	Log.w(TAG, "failed to parse", e);
            } finally {
            	if (reader != null) {
            		try {
            			reader.close();
            		} catch (final Exception e) {
            			Log.d(TAG, "Unable to close reader.", e);
            		}
            	}
            }
        } else {
        	Log.d(TAG, "failed to get response entity: " + resource.getStatus());
        }

        synchronized(m_outages) {
        	m_outages.clear();
        	m_outages.addAll(outages);
        }
        Log.d(TAG, "returning " + m_outages);
        runOnUiThread(m_returnRes);
    }

    private Runnable m_returnRes = new Runnable() {
    	public void run() {
			m_progressDialog.dismiss();
			m_outageAdapter.notifyDataSetChanged();
    	}
    };
}
