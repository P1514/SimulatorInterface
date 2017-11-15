package facebook;

import java.awt.print.Pageable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.corba.se.impl.resolver.ORBDefaultInitRefResolverImpl;

import facebook4j.*;
import facebook4j.FacebookResponse.Metadata.Fields.Field;
import general.Server;

public class FacebookAgent {
	Facebook facebook;
	private String appId = "appId";
	private String appSecret = "appSecret";
	private String permissions = "";
	private String accessToken = appId + "|" + appSecret;

	public FacebookAgent() {
		facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(appId, appSecret);
		facebook.setOAuthAccessToken(new facebook4j.auth.AccessToken(accessToken, null));

	}

	public boolean add_account(String account) {

		try {
			facebook.getPage(account, new Reading().limit(1));
		} catch (FacebookException e) {
			if (e.getErrorCode() == 803) {
				System.err.println("Error 803:Attemped to Add an account that doesn't exist (" + account + ")");
				return false;
			}
			System.err.println("Error Code: " + e.getErrorCode() + " Message: " + e.getErrorMessage());
			return false;
		}

		try (Connection cnlocal = Server.connlocal();
				PreparedStatement stmt = cnlocal.prepareStatement(
						"Insert into accounts values ('Facebook',?,?) ON DUPLICATE KEY UPDATE source='Facebook'")) {
			stmt.setString(1, account);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			// System.err.println("Error Code: " + e.getErrorCode() + " Message: " +
			// e.getErrorMessage());
			return false;
		}
		return true;
	}

	public void fetch() {
		ArrayList<String> accounts = new ArrayList<String>();
		ArrayList<String> dates = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		Calendar starttime = Calendar.getInstance();

		try (Connection cnlocal = Server.connlocal();
				Statement stmt = cnlocal.createStatement();
				ResultSet rs = stmt.executeQuery("Select * from accounts where source like 'facebook'")) {
			while (rs.next()) {
				dates.add(rs.getString("last_update"));
				accounts.add(rs.getString("account"));
			}

		} catch (SQLException | ClassNotFoundException e) {
			System.err.println("Errors fetching accounts");
		}
		System.out.println(accounts.size() + " accounts found");
		for (int i = 0; i < accounts.size(); i++) {
			System.out.println("Started account: + " + accounts.get(i) + " since date: " + dates.get(i));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				cal.setTime(sdf.parse(dates.get(i)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			getData(cal.getTime(), accounts.get(i));
			to_DB(sdf.format(starttime.getTime()), accounts.get(i));
		}

		System.out.println("Finished Getting Posts");

	}

	public void to_DB(String date, String account) {
		try (Connection cnlocal = Server.connlocal();
				PreparedStatement ps = cnlocal.prepareStatement(
						"Insert into user values(?,?,0,'No Info','No Info') ON DUPLICATE KEY UPDATE name=?")) {

			for (MyAuthor author : FacebookUtil.authordb.values()) {
				ps.setLong(1, author.getId());
				ps.setString(2, author.getName());
				ps.setString(3, author.getName());
				try {
					ps.execute();
				}catch(SQLException e) {
					//e.printStackTrace();
					String trunc_name=author.getName().substring(0, 70);
					ps.setString(2, trunc_name);
					ps.setString(3, trunc_name);
					ps.execute();
				}

			}

		} catch (SQLException | ClassNotFoundException e) {
			//e.printStackTrace();
			System.err.println("Error Connection to DB");
		}

		try (Connection cnlocal = Server.connlocal();
				PreparedStatement ps = cnlocal.prepareStatement(
						"Insert into post(id,timestamp,message,likes,views,user_id,product,post_id) values(?,?,?,?,?,?,?,?)"
								+ " ON DUPLICATE KEY UPDATE message=?")) {

			for (MyPost post : FacebookUtil.postdb.values()) {
				ps.setLong(1, post.getId());
				ps.setString(2, post.getDate());
				ps.setString(3, post.getMessage());
				ps.setLong(4, post.getLikes());
				ps.setLong(5, post.getShares());
				ps.setLong(6, post.getAuthor());
				ps.setString(7, post.getAccount());
				ps.setNull(8, java.sql.Types.BIGINT);
				ps.setString(9, post.getMessage());
				//System.out.println(ps.toString());
				ps.execute();
				for (MyPost comm : post.getComments().values()) {
					ps.setLong(1, comm.getId());
					ps.setString(2, post.getDate());
					ps.setString(3, comm.getMessage());
					ps.setLong(4, comm.getLikes());
					ps.setLong(5, comm.getShares());
					ps.setLong(6, comm.getAuthor());
					ps.setString(7, comm.getAccount());
					ps.setLong(8, post.getId());
					ps.setString(9, comm.getMessage());
					//System.out.println(ps.toString());
					ps.execute();
				}

			}

		} catch (SQLException | ClassNotFoundException e) {
			System.err.println("Error Connection to DB");
			e.printStackTrace();
		}
		
		try(Connection cnlocal = Server.connlocal();
			PreparedStatement stmt = cnlocal.prepareStatement("Update accounts set last_update = ? where (source like 'Facebook' and account like ?)")){
			stmt.setString(1, date);
			stmt.setString(2, account);
			stmt.execute();
		}catch ( SQLException | ClassNotFoundException e) {
			System.err.print("Error on DB connection");
			e.printStackTrace();
		}

		FacebookUtil.clearMaps();
	}

	private void getData(Date sincedate, String account) {

		String fields = "comments.limit(900){like_count,id,message,created_time,from},";
		fields += "message,";
		fields += "shares,";
		fields += "likes.summary(true).limit(1),";
		fields += "id,";
		fields += "created_time,";
		fields += "from";
		ResponseList<Post> posts;
		while (true) {
			try {
				posts = facebook.getPosts(account, new Reading().fields(fields).since(sincedate).limit(1));
				break;
			} catch (FacebookException e) {
				System.err.println("ERROR Ocurred Retrying");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		Paging<Comment> comm_page;
		Paging<Post> post_page;
		int i = 1;
		do {
			if (posts.size() == 0)
				break;
			System.out.println("Post Nº " + (i++ - 1));
			Post post = posts.get(0);
			post_page = posts.getPaging();
			MyPost localpost = new MyPost(post, account);
			int ii = 1;
			PagableList<Comment> comments = post.getComments();
			do {
				if (comments.size() == 0)
					break;
				for (Comment comm : comments) {
					localpost.add_comment(comm, account);
					ii++;
					// System.out.println("Post Nº" + (i - 1) + "Comment Nº" + ii++);
				}

				comm_page = comments.getPaging();
				System.out.println("Comment Nº " + ii + "Date: " + localpost.getDate());
				if (comm_page.getNext() == null)
					break;
				while (true) {
					try {
						comments = facebook.fetchNext(comm_page);
						break;
					} catch (FacebookException e) {
						try {
							System.err.println("Error on next comment page " + e.getErrorCode() + " Retrying");
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}

			} while (true);
			if (post_page.getNext() == null)
				break;
			try {
				posts = facebook.fetchNext(post_page);
			} catch (FacebookException e) {
				try {
					System.err.println("Error on next posts page " + e.getErrorCode() + " Retrying");
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		} while (true);

	}

}
