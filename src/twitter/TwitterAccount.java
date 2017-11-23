package twitter;

import java.util.Calendar;

public class TwitterAccount {
	String name;
	Calendar lastUpdated;
	
	public TwitterAccount(String _account, Calendar _lastUpdated) {
		name=_account;
		lastUpdated=_lastUpdated;
	}
	
	public String getName() {
		return name;
	}
	
	public Calendar getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(Calendar lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
}
