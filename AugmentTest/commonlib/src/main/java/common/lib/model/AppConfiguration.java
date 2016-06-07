package common.lib.model;

import java.io.Serializable;
import java.util.Locale;

public class AppConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1359480939951191822L;
	
	int versionCode;
	String language;
	String lastKnownMCC;
	String lastKnownMNC;
	Locale locale;
	
	public AppConfiguration(){
		
	}
	
	public int getAppVersion() {
		return versionCode;
	}
	public void setAppVersion(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLastKnownMCC() {
		return lastKnownMCC;
	}
	public void setLastKnownMCC(String lastKnownMCC) {
		this.lastKnownMCC = lastKnownMCC;
	}
	public String getLastKnownMNC() {
		return lastKnownMNC;
	}
	public void setLastKnownMNC(String lastKnownMNC) {
		this.lastKnownMNC = lastKnownMNC;
	}


	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
}
