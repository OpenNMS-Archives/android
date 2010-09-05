package com.opennms.android;

public class ServerSettings {
	private static ServerSettings m_instance;
	private boolean m_https = false;
	private String m_host = "10.0.2.2";
	private int m_port = 8980;
	private String m_path = "/opennms/rest";
	private String m_username = "admin";
	private String m_password = "admin";
	private String m_base;
	
	protected ServerSettings() {
	}

	public static ServerSettings getInstance() {
		if (m_instance == null) {
			m_instance = new ServerSettings();
		}
		return m_instance;
	}

	public boolean getHttps() {
		return m_https;
	}
	public void setHttps(final boolean https) {
		m_https = https;
		m_base = null;
	}
	public String getHost() {
		return m_host;
	}
	public void setHost(final String host) {
		m_host = host;
		m_base = null;
	}
	public int getPort() {
		return m_port;
	}
	public void setPort(final int port) {
		m_port = port;
		m_base = null;
	}
	public String getPath() {
		return m_path;
	}
	public void setPath(final String path) {
		m_path = path;
		m_base = null;
	}
	public String getUsername() {
		return m_username;
	}
	public void setUsername(final String username) {
		m_username = username;
	}
	public String getPassword() {
		return m_password;
	}
	public void setPassword(final String password) {
		m_password = password;
	}
	
	public String getBase() {
		if (m_base == null) {
			m_base = String.format("http%s://%s:%d%s", (m_https? "s":""), m_host, m_port, m_path);
		}
		return m_base;
	}
}
