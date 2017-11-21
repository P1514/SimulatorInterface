package twitter;

import sun.util.logging.resources.logging_de;
import sun.util.logging.resources.logging_fr;

public class TwitterPost {
	long userId;
	String userName;
	String userLocation;
	long postId;
	String timestamp;
	String message;
	int likes;
	int retweets;
	String product;
	long parentPostId;

	public TwitterPost(long _userId, String _userName, String _userLocation, long _postId, String _timestamp,
			String _message, int _likes, int _retweets, String _product, long _parentPostId) {
		userId = _userId;
		userName = _userName;
		userLocation = _userLocation;
		postId = _postId;
		timestamp = _timestamp;
		message = _message;
		likes = _likes;
		retweets = _retweets;
		product = _product;
		parentPostId = _parentPostId;
	}
	
	public long getLikes() {
		return likes;
	}
	
	public String getMessage() {
		return message;
	}
	
	public long getParentPostId() {
		return parentPostId;
	}
	
	public long getPostId() {
		return postId;
	}
	
	public String getProduct() {
		return product;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public String getUserLocation() {
		return userLocation;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public int getRetweets() {
		return retweets;
	}
}
