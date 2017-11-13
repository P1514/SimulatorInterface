package general;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.conf.*;

public class TwitterAgent {

	private Twitter twitter;

	public TwitterAgent() {
		TwitterFactory tfactory = new TwitterFactory(this.getConfig());
		this.twitter = tfactory.getInstance();
	}

	public Twitter getTwitter() {
		return this.twitter;
	}

	private Configuration getConfig() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("RIXWmnbXBYOxxtCrUTDHvaTpr")
				.setOAuthConsumerSecret("dkZ7Qo6sqSLwHMCTleyIArVPhLq2sBtVLglpghciDzvlCMUX4r")
				.setOAuthAccessToken("927925616539832320-2EXPiCYOjBc3YgwNnBCuTEDlq5vkL7f")
				.setOAuthAccessTokenSecret("mUi35i9FUa3aBC5KgPC4K4jlhy4fgx0aWMZ4VJsNswcYS")
				.setIncludeEntitiesEnabled(true).setIncludeMyRetweetEnabled(true);

		return cb.build();
	}

	public int getPageLikesRetweets(String pageName) {
		int likes_retweets = 0;
		try {
			likes_retweets = this.getTwitter().showUser(pageName).getStatusesCount();
		} catch (TwitterException ex) {
			Logger.getLogger(TwitterAgent.class.getName()).log(Level.SEVERE, null, ex);
		}
		return likes_retweets;
	}

	public JSONArray getPageFeed(String account) throws JSONException, InterruptedException {
		User twitterUser = null;
		Paging p = new Paging();
		p.setCount(1000);
		Query query = new Query();;
		QueryResult queryresult;
		long currentPostEpoch = 0;
		List<Status> results;
		JSONArray output = new JSONArray();
		query.setQuery("from:"+account);
		//query.setQuery("source:"+ account);
        query.setCount(1000);
        
		try {

			queryresult = this.getTwitter().search(query);
			//results = twitter.getUserTimeline("@"+account,p);
			do {
				
				results = queryresult.getTweets();
				
				if (results == null || results.isEmpty()) {
					//TODO
				}

				for (Status status : results) {

					twitterUser = status.getUser();
					currentPostEpoch = status.getCreatedAt().getTime();

					JSONObject out = new JSONObject();
					out.put("source", "twitter");
					out.put("Fname", twitterUser.getName());
					out.put("age", "");
					out.put("gender", "");
					out.put("postId", status.getId());
					out.put("location", twitterUser.getLocation());
					out.put("retweet", status.getRetweetCount());
					out.put("account", account);
					out.put("url", "");
					out.put("mediaSpecificInfo", "true");
					out.put("imgUrl", "");// take care
					out.put("postEpoch", currentPostEpoch);
					out.put("post", status.getText());
					output.put(out);
					}
				Thread.sleep(10000);
		} while ((query = queryresult.nextQuery()) != null);
			Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Posts were retrieved! Total: "
					+ output.length() + " Last Epoch Time: " + currentPostEpoch + " Epoch Date:  {0}",
					new Date(currentPostEpoch).toString());
		} catch (TwitterException ex) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(ex.getMessage());
		}

		return output;

	}

}
