package com.opennms.android.outages;

import java.util.Date;

public class Outage {
	private Integer m_id;
	private String m_ipAddress;
	private String m_serviceName;
	private Date m_ifLostService;
	private Date m_ifRegainedService;
	private String m_description;
	private String m_host;
	private String m_logMessage;
	private String m_uei;
	private String m_severity;
	private Integer m_nodeId;

	public Integer getId() {
		return m_id;
	}
	public void setId(final Integer id) {
		m_id = id;
	}
	public String getIpAddress() {
		return m_ipAddress;
	}
	public void setIpAddress(final String ip) {
		m_ipAddress = ip;
	}
	public String getServiceName() {
		return m_serviceName;
	}
	public void setServiceName(final String serviceName) {
		m_serviceName = serviceName;
	}
	public Date getIfLostService() {
		return m_ifLostService;
	}
	public void setIfLostService(final Date ifLostService) {
		m_ifLostService = ifLostService;
	}
	public Date getIfRegainedService() {
		return m_ifRegainedService;
	}
	public void setIfRegainedService(final Date ifRegainedService) {
		m_ifRegainedService = ifRegainedService;
	}
	public String getDescription() {
		return m_description;
	}
	public void setDescription(final String description) {
		m_description = description;
	}
	public String getHost() {
		return m_host;
	}
	public void setHost(final String host) {
		m_host = host;
	}
	public String getLogMessage() {
		return m_logMessage;
	}
	public void setLogMessage(final String logMessage) {
		m_logMessage = logMessage;
	}
	public String getUei() {
		return m_uei;
	}
	public void setUei(final String uei) {
		m_uei = uei;
	}
	public String getSeverity() {
		return m_severity;
	}
	public void setSeverity(final String severity) {
		m_severity = severity;
	}
	public Integer getNodeId() {
		return m_nodeId;
	}
	public void setNodeId(final Integer nodeId) {
		m_nodeId = nodeId;
	}

	public String toString() {
		return String.format("Outage[id=%d,ipAddress=%s,host=%s,description=%s]", m_id, m_ipAddress, m_host, m_description);
	}
}
