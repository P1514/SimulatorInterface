package twitter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

import com.vdurmont.emoji.EmojiParser;

import general.Server;
import twitter4j.Query;

public class TwitterHandler {

	private static HashMap<String, TwitterAccount> accounts = new HashMap<>();
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static HashMap<Long, TwitterPost> posts = new HashMap<>();

	public TwitterHandler() {
		fetchAccounts();
	}

	public HashMap<String, TwitterAccount> getAccounts() {
		return accounts;
	}

	public void fetchAccounts() {
		try (Connection cnlocal = Server.connlocal();
				Statement stmt = cnlocal.createStatement();
				ResultSet rs = stmt.executeQuery("Select * from accounts where source like 'Twitter'");) {
			while (rs.next()) {
				addAccount(rs.getString("account"), rs.getString("last_update"));
			}
		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
		}
	}

	public void addAccount(String account, String time) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(time));
		} catch (ParseException e) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
		}
		TwitterAccount tAccount = new TwitterAccount(account, cal);
		accounts.put(account, tAccount);
	}

	public void addPost(Long postId, TwitterPost post) {
		posts.putIfAbsent(postId, post);
	}
	
	public void clearPosts() {
		posts.clear();
	}

	public HashMap<Long, TwitterPost> getPosts() {
		return posts;
	}

	public void savePost(TwitterPost post) {
		try (Connection cnlocal = Server.connlocal();
				PreparedStatement ps = cnlocal.prepareStatement(
						"INSERT INTO `sentimentposts`.`user` (`id`, `name`, `location`) VALUES (?, ?, ?) on duplicate key update name=?, location=?")) {
			ps.setLong(1, post.getUserId());
			ps.setString(2, post.getUserName());
			if (post.getUserLocation().length() > 90)
				ps.setString(3, post.getUserLocation().substring(0, 89));
			else
				ps.setString(3, post.getUserLocation());
			ps.setString(4, post.getUserName());
			if (post.getUserLocation().length() > 90)
				ps.setString(5, post.getUserLocation().substring(0, 89));
			else
				ps.setString(5, post.getUserLocation());
			ps.execute();

		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
		}
		if (post.getParentPostId() != -1) {
			String str1 = "INSERT INTO post (id, timestamp, message, likes, views, user_id, product, post_id, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) on duplicate key update message=?, likes=?, views=?";
			try (Connection cnlocal = Server.connlocal();
					PreparedStatement ps1 = cnlocal.prepareStatement(str1)){
				ps1.setLong(1, post.getPostId());
				ps1.setString(2, post.getTimestamp());
				ps1.setString(3, EmojiParser.parseToAliases(post.getMessage()));
				ps1.setLong(4, post.getLikes());
				ps1.setInt(5, post.getRetweets());
				ps1.setLong(6, post.getUserId());
				ps1.setString(7, post.getProduct());
				ps1.setLong(8, post.getParentPostId());
				ps1.setString(9, "Twitter");
				ps1.setString(10, EmojiParser.parseToAliases(post.getMessage()));
				ps1.setLong(11, post.getLikes());
				ps1.setLong(12, post.getRetweets());
				ps1.execute();

			} catch (SQLException | ClassNotFoundException e) {
				Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
			}
		} else {
			String str2 = "INSERT INTO post (id, timestamp, message, likes, views, user_id, product, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?) on duplicate key update message=?, likes=?, views=?";
			try (Connection cnlocal = Server.connlocal(); PreparedStatement ps1 = cnlocal.prepareStatement(str2)) {
				ps1.setLong(1, post.getPostId());
				ps1.setString(2, post.getTimestamp());
				ps1.setString(3, EmojiParser.parseToAliases(post.getMessage()));
				ps1.setLong(4, post.getLikes());
				ps1.setInt(5, post.getRetweets());
				ps1.setLong(6, post.getUserId());
				ps1.setString(7, post.getProduct());
				ps1.setString(8, "Twitter");
				ps1.setString(9, EmojiParser.parseToAliases(post.getMessage()));
				ps1.setLong(10, post.getLikes());
				ps1.setLong(11, post.getRetweets());
				ps1.execute();
			} catch (SQLException | ClassNotFoundException e) {
				Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
			}
		}
	}

	public ArrayList<Query> generateQueries(TwitterAccount account) {
		Query query = new Query();
		String since = (new SimpleDateFormat("yyyy-MM-dd").format(account.getLastUpdated().getTime()));
		long sinceId = getSinceId(account);
		ArrayList<Query> queries = new ArrayList<>();
		query.setQuery("@" + account.getName());
		query.setCount(100);
		// query.setSince(since);
		query.setSinceId(sinceId);
		query.setResultType(Query.ResultType.recent);
		queries.add(query);
		query = new Query();
		query.setQuery(account.getName());
		query.setCount(100);
		query.setSinceId(sinceId);
		// query.setSince(since);
		query.setResultType(Query.ResultType.recent);
		queries.add(query);
		query = new Query();
		query.setQuery(account.getName());
		query.setCount(100);
		query.setSinceId(sinceId);
		// query.setSince(since);
		query.setResultType(Query.ResultType.popular);
		queries.add(query);
		query = new Query();
		query.setQuery("@" + account.getName());
		query.setCount(100);
		query.setSinceId(sinceId);
		// query.setSince(since);
		query.setResultType(Query.ResultType.popular);
		queries.add(query);

		return queries;

	}

	private long getSinceId(TwitterAccount account) {
		try (Connection cnlocal = Server.connlocal();
				Statement stmt = cnlocal.createStatement();
				ResultSet rs = stmt.executeQuery(
						"Select max(id) from post where product like '" + account.getName() + "' and source='Twitter'")) {
			while (rs.next()) {
				return rs.getLong(1);
			}

		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
		}
		return -1;
	}

	public void updateLastUpdated(TwitterAccount account) {
		try (Connection cnlocal = Server.connlocal();
				PreparedStatement ps1 = cnlocal.prepareStatement(
						"UPDATE `sentimentposts`.`accounts` SET `last_update`=? WHERE `source`='Twitter' and`account`=?")) {
			ps1.setString(1, df.format(account.getLastUpdated().getTime()));
			ps1.setString(2, account.getName());
			ps1.execute();
			accounts.put(account.getName(), account);
		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(TwitterAgent.class.getName()).severe(e.getMessage());
		}

	}

}
