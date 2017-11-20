package twitter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import general.Server;
import twitter4j.*;
import twitter4j.conf.*;

public final class TwitterAgent {

	private static Twitter twitter;
	private static TwitterHandler tHandler = new TwitterHandler();
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private TwitterAgent() {
		TwitterFactory tfactory = new TwitterFactory(getConfig());
		twitter = tfactory.getInstance();
	}

	private static Twitter getTwitter() {
		return twitter;
	}

	private static Configuration getConfig() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("RIXWmnbXBYOxxtCrUTDHvaTpr")
				.setOAuthConsumerSecret("dkZ7Qo6sqSLwHMCTleyIArVPhLq2sBtVLglpghciDzvlCMUX4r")
				.setOAuthAccessToken("927925616539832320-2EXPiCYOjBc3YgwNnBCuTEDlq5vkL7f")
				.setOAuthAccessTokenSecret("mUi35i9FUa3aBC5KgPC4K4jlhy4fgx0aWMZ4VJsNswcYS")
				.setIncludeEntitiesEnabled(true).setIncludeMyRetweetEnabled(true);

		return cb.build();
	}

	public static void fetch() {
		User twitterUser = null;
		TwitterPost currentPost;
		TwitterFactory tfactory = new TwitterFactory(getConfig());
		twitter = tfactory.getInstance();
		Paging p = new Paging();
		p.setCount(1000);
		QueryResult queryresult;
		Calendar cal = Calendar.getInstance();
		long currentPostEpoch;
		for (TwitterAccount account : tHandler.getAccounts().values()) {
			ArrayList<Query> queries = tHandler.generateQueries(account);
			currentPostEpoch = 0;
			List<Status> results;
			int page = 1;
			for (Query currentQuery : queries)
				try {
					Logger.getLogger(TwitterAgent.class.getName()).info("Getting posts from twitter, please wait...");
					do {
	
						Logger.getLogger(TwitterAgent.class.getName())
								.info("Getting " + currentQuery.getResultType().toString() + " posts from "
										+ currentQuery.getQuery() + " currentpage: " + page);
						queryresult = getTwitter().search(currentQuery);
						results = queryresult.getTweets();
						for (Status status : results) {

							twitterUser = status.getUser();
							currentPostEpoch = status.getCreatedAt().getTime();

							currentPost = new TwitterPost(twitterUser.getId(), twitterUser.getName(),
									twitterUser.getLocation(), status.getId(), df.format(currentPostEpoch),
									status.getText(), status.getFavoriteCount(), status.getRetweetCount(), account.getName(),
									status.getInReplyToStatusId());
							tHandler.addPost(status.getId(), currentPost);
						}

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
						}
						page++;
					} while ((currentQuery = queryresult.nextQuery()) != null);
				} catch (TwitterException ex) {
					Logger.getLogger(TwitterAgent.class.getName()).severe(ex.getMessage());
				}
			account.setLastUpdated(cal);
			tHandler.updateLastUpdated(account);
		}
		
		for(TwitterPost post: tHandler.getPosts().values())
			tHandler.savePost(post);
			Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO,
					"Posts were retrieved! Total: " + tHandler.getPosts().size());
			tHandler.clearPosts();

	}

	public static boolean registerAccount(String account) {
		int count = 0;
		String sql = "SELECT COUNT(*) FROM sentimentposts.accounts WHERE source LIKE 'Twitter' AND account LIKE ?;";
		try (Connection cnlocal = Server.connlocal(); PreparedStatement select = cnlocal.prepareStatement(sql)) {
			select.setString(1, account);
			try (ResultSet rs = select.executeQuery()) {
				while (rs.next()) {
					count = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		if (count == 0) {
			String sql2 = "INSERT INTO sentimentposts.accounts VALUES (?, ?, ?);";
			try (Connection cnlocal2 = Server.connlocal(); PreparedStatement insert = cnlocal2.prepareStatement(sql2)) {
				insert.setString(1, "Twitter");
				insert.setString(2, account);
				insert.setTimestamp(3, new Timestamp(1000));
				insert.executeUpdate();
				tHandler.addAccount(account, (new Timestamp(1000)).toString());
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			return true;
		}

		return false;
	}

}
