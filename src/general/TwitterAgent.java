package general;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.Query.ResultType;
import twitter4j.conf.*;
import org.apache.commons.lang3.StringEscapeUtils;

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
		Query query = new Query();
		Query query1 = new Query();
		Query query2 = new Query();
		Query query3 = new Query();
		Query queryArr[] = { query, query1, query2, query3 };
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		QueryResult queryresult;
		long currentPostEpoch = 0;
		List<Status> results;
		JSONArray output = new JSONArray();
		account = account.substring(1, account.length() - 1);
		int page = 1;
		query.setQuery("@" + account);
		query.setCount(100);
		query.setResultType(Query.ResultType.recent);
		query1.setQuery(account);
		query1.setCount(100);
		query1.setResultType(Query.ResultType.recent);
		query2.setQuery(account);
		query2.setCount(100);
		query2.setResultType(Query.ResultType.popular);
		query3.setQuery("@" + account);
		query3.setCount(100);
		query3.setResultType(Query.ResultType.popular);
		for (Query currentQuery : queryArr)
			try {
				Logger.getLogger(TwitterAgent.class.getName()).info("Getting posts from twitter, please wait...");
				do {
					currentQuery.setMaxId(-1);
					Logger.getLogger(TwitterAgent.class.getName())
							.info("Getting " + currentQuery.getResultType().toString() + " posts from "
									+ currentQuery.getQuery() + " currentpage: " + page);
					queryresult = this.getTwitter().search(currentQuery);
					results = queryresult.getTweets();

					for (Status status : results) {

						twitterUser = status.getUser();
						currentPostEpoch = status.getCreatedAt().getTime();

						JSONObject out = new JSONObject();
						out.put("source", "twitter");
						out.put("user_id", twitterUser.getId());
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

						try (Connection cnlocal = Server.connlocal();
								PreparedStatement ps = cnlocal.prepareStatement(
										"INSERT IGNORE INTO `sentimentposts`.`user` (`id`, `name`, `location`) VALUES (?, ?, ?)")) {
							ps.setLong(1, twitterUser.getId());
							ps.setString(2, twitterUser.getName());
							ps.setString(3, twitterUser.getLocation());
							ps.execute();

						} catch (SQLException | ClassNotFoundException e) {
							Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
						}
						if (status.getInReplyToStatusId() != -1) {

							try (Connection cnlocal = Server.connlocal();
									PreparedStatement ps1 = cnlocal.prepareStatement(
											"INSERT INTO `sentimentposts`.`post` (`id`, `timestamp`, `message`, `likes`, `views`, `user_id`, `product`, `post_id`) VALUES (?, ?, ?, ?, ?, ?, ?,?) on duplicate key update\n"
													+ "message=?, likes=?, views=?;")) {
								ps1.setLong(1, status.getId());
								ps1.setString(2, df.format(currentPostEpoch));
								ps1.setString(3, status.getText());
								ps1.setLong(4, status.getFavoriteCount());
								ps1.setLong(5, status.getRetweetCount());
								ps1.setLong(6, twitterUser.getId());
								ps1.setString(7, account);
								ps1.setLong(8, status.getInReplyToStatusId());
								ps1.setString(9, status.getText());
								ps1.setLong(10, status.getFavoriteCount());
								ps1.setLong(11, status.getRetweetCount());
								ps1.execute();

							} catch (SQLException | ClassNotFoundException e) {
								Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
							}
						} else {
							try (Connection cnlocal = Server.connlocal();
									PreparedStatement ps1 = cnlocal.prepareStatement(
											"INSERT INTO `sentimentposts`.`post` (`id`, `timestamp`, `message`, `likes`, `views`, `user_id`, `product`) VALUES (?, ?, ?, ?, ?, ?, ?) on duplicate key update\n"
													+ "message=?, likes=?, views=?;")) {
								ps1.setLong(1, status.getId());
								ps1.setString(2, df.format(currentPostEpoch));
								ps1.setString(3, status.getText());
								ps1.setLong(4, status.getFavoriteCount());
								ps1.setLong(5, status.getRetweetCount());
								ps1.setLong(6, twitterUser.getId());
								ps1.setString(7, account);
								ps1.setString(8, status.getText());
								ps1.setLong(9, status.getFavoriteCount());
								ps1.setLong(10, status.getRetweetCount());
								ps1.execute();

							} catch (SQLException | ClassNotFoundException e) {
								Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
							}
						}
						output.put(out);

					}
					Thread.sleep(1000);
					page++;
				} while ((currentQuery = queryresult.nextQuery()) != null);
			} catch (TwitterException ex) {
				Logger.getLogger(TwitterAgent.class.getName()).severe(ex.getMessage());
			}
		Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Posts were retrieved! Total: " + output.length()
				+  " Last Date:  {0}", new Date(currentPostEpoch).toString());
		return output;

	}

}
