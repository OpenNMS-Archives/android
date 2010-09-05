package com.opennms.android.outages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class OutageParseHandler extends DefaultHandler {
	private static final String TAG = "OutageParseHandler";
	private List<Outage> m_outages = new ArrayList<Outage>();
	private Set<Integer> m_nodeIds = new HashSet<Integer>();
	private Outage m_currentOutage = null;
	private StringBuffer m_currentText = null;
	private Pattern m_pattern = Pattern.compile("^(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d[\\+\\-\\s]*\\d\\d):(\\d\\d)$");
	private SimpleDateFormat m_dateFormat;
	private boolean m_allowDuplicateNodes = true;

	public OutageParseHandler() {
		super();
		m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	}

	public OutageParseHandler(final boolean allowDuplicateNodes) {
		this();
		m_allowDuplicateNodes = allowDuplicateNodes;
	}

	public List<Outage> getOutages() {
		return m_outages;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
		if (localName.equals("outage")) {
			m_currentOutage = new Outage();
			final String outageId = atts.getValue("id");
			try {
				m_currentOutage.setId(Integer.valueOf(outageId));
			} catch (final NumberFormatException e) {
				Log.w(TAG, "Unable to parse outage id: " + outageId, e);
			}
		} else if (localName.equals("serviceLostEvent")) {
			m_currentOutage.setSeverity(atts.getValue("severity"));
		}
		m_currentText = new StringBuffer();
	}

	@Override
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
		String currentText = null;
		if (m_currentText != null) {
			currentText = m_currentText.toString();
		}
		if (localName.equals("outage")) {
			final Integer nodeId = m_currentOutage.getNodeId();

			if (m_allowDuplicateNodes || nodeId == null) {
				m_outages.add(m_currentOutage);
			} else {
				if (!m_nodeIds.contains(nodeId)) {
					m_outages.add(m_currentOutage);
				}
			}
			m_nodeIds.add(m_currentOutage.getNodeId());
			m_currentOutage = null;
		} else if (localName.equals("ipAddress")) {
			m_currentOutage.setIpAddress(currentText);
		} else if (localName.equals("name")) {
			m_currentOutage.setServiceName(currentText);
		} else if (localName.equals("ifLostService")) {
			m_currentOutage.setIfLostService(getDateFromString(currentText));
		} else if (localName.equals("ifRegainedService")) {
			m_currentOutage.setIfRegainedService(getDateFromString(currentText));
		} else if (localName.equals("description")) {
			m_currentOutage.setDescription(currentText);
		} else if (localName.equals("host")) {
			m_currentOutage.setHost(currentText);
		} else if (localName.equals("logMessage")) {
			m_currentOutage.setLogMessage(currentText);
		} else if (localName.equals("uei")) {
			m_currentOutage.setUei(currentText);
		} else if (localName.equals("nodeId")) {
			final String nodeId = currentText;
			try {
				m_currentOutage.setNodeId(Integer.valueOf(nodeId));
			} catch (final NumberFormatException e) {
				Log.w(TAG, "Unable to parse node id: " + nodeId, e);
			}
		}
		m_currentText = null;
	}

	@Override
    public void characters(final char ch[], final int start, final int length) {
    	m_currentText.append(ch, start, length);
    }

	protected Date getDateFromString(final String dateString) {
		Date date = null;
		final Matcher matcher = m_pattern.matcher(dateString);
		try {
			if (matcher.matches()) {
				return m_dateFormat.parse(matcher.group(1) + matcher.group(2));
			} else {
				return m_dateFormat.parse(dateString);
			}
		} catch (final ParseException e) {
			Log.w(TAG, String.format("Unable to parse date '%s'", dateString), e);
		}
		return date;
	}
}
