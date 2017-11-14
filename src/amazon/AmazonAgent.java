package amazon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.media.jfxmedia.logging.Logger;
import com.vdurmont.emoji.EmojiParser;

import general.Server;

public class AmazonAgent {

	protected static final String DEFAULT_URL = "https://www.amazon.com/product-reviews/[ASIN]/ref=cm_cr_arp_d_paging_btm_[PAGE_NUMBER]?ie=UTF8&reviewerType=all_reviews&pageNumber=[PAGE_NUMBER]";
	protected static final String ASIN = "[ASIN]";
	protected static final String PAGE_NUMBER = "[PAGE_NUMBER]";
	
	protected Map<Integer, Review> reviews;
	private String url;
	private String asin;
	private int id;
	public AmazonAgent(String asin) {
		reviews = new ConcurrentHashMap<Integer, Review>();
		this.asin = asin;
		id = 0;
		url = DEFAULT_URL.replace(ASIN, this.asin);
		url = url.replace(PAGE_NUMBER, "1");
	}
	
	public void getReviews(long start, long finish) {
		int pageNumber = 1;

		System.out.println("Getting reviews...");
		while(true) { 
			url = DEFAULT_URL.replace(ASIN, this.asin);
			url = url.replace(PAGE_NUMBER, "" + pageNumber);
//			System.out.println("URL: " + url);
			try {
				Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36").get();
				Elements reviewElements = doc.select(".review");
//				if no posts are found, uncomment the next line and inspect the result to check if we're getting a captcha
//				System.out.println(doc.html());
				if (reviewElements == null || reviewElements.isEmpty()) {
					break;
				} 
				
				Element productElement = doc.select(".product-title").first();
				String productText = productElement.text();
				for (Element reviewElement : reviewElements) {
					Element authorElement = reviewElement.select(".author").first();
					String author = authorElement.text();
					
					Element textElement = reviewElement.select(".review-text").first();
					String text = EmojiParser.parseToAliases(textElement.text());
					
					Element dateElement = reviewElement.select(".review-date").first();
					String date = dateElement.text().replace("on ", "");
					
					String reviewId = reviewElement.attr("id");
					Long convertedId = generateId(reviewId);
					Review r = new Review(convertedId, author, convertedId, text, date, productText);
					
					if ((start == -1 || r.getDateInEpoch() >= start) && (finish == -1 || r.getDateInEpoch() <= finish)) {
						reviews.put(id++, r);
					} 
					
					
//					this will not work because we need to click "comments" for the page to load the replies, which isnt possible with Jsoup :(
//					Elements commentElements = doc.select(".review-comment");
//					for (Element commentElement : commentElements) {
//						Element commentAuthorElement = commentElement.select(".author").first();
//						String commentAuthor = commentAuthorElement.text();
//						
//						Element commentTextElement = commentElement.select(".review-comment-text").first();
//						String commentText = commentTextElement.text();
//						
//						Element commentDateElement = commentElement.select(".comment-time-stamp").first();
//						String commentDate = commentDateElement.text().replace("on ", "");
//						Review comment = new Review(commentAuthor, commentText, commentDate);
//						r.addComment(comment);
//					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			pageNumber++;
		}
		
//		JSONArray json = new JSONArray();
//		for (Review r : reviews.values()) {
//			JSONObject obj = new JSONObject();
//			try {
//				obj.put("Author", r.getAuthor());
//				obj.put("Message", r.getText());
//				obj.put("Date", r.getDate());
//				json.put(obj);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
		System.out.println("Finished retrieving reviews. Reviews found: " + reviews.size());
		
//		System.out.println(json.toString());
//		return json;
	}

	public void store() {
		System.out.println("Storing data into database...");
		String sqlPost = "INSERT INTO sentimentposts.post VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE message = ?;";
		String sqlUser = "REPLACE sentimentposts.user (id, name, age, gender, location) VALUES (?,?,?,?,?);";
		
		for (Review r : reviews.values()) {
			try (Connection cnlocal2 = Server.connlocal(); PreparedStatement insert2 = cnlocal2.prepareStatement(sqlUser)){
				insert2.setLong(1, r.getAuthorId());
				insert2.setString(2, r.getAuthor());
				insert2.setNull(3, java.sql.Types.INTEGER);
				insert2.setNull(4, java.sql.Types.VARCHAR);
				insert2.setNull(5, java.sql.Types.VARCHAR);
				insert2.executeUpdate();
				
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			try (Connection cnlocal = Server.connlocal(); PreparedStatement insert = cnlocal.prepareStatement(sqlPost)){
				insert.setLong(1, r.getId().longValue());
				insert.setTimestamp(2, new Timestamp(r.getDateInEpoch()));
				insert.setString(3, r.getText());
				insert.setInt(4, 0);
				insert.setInt(5, 0);
				insert.setLong(6, r.getAuthorId()); 
				insert.setString(7, asin); 
				insert.setNull(8, java.sql.Types.BIGINT);
				insert.setString(9, r.getText());
				insert.executeUpdate();
	
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			
		}
		System.out.println("Finished storing data.");
	}
	
	@Deprecated
	private BigInteger convertToNumber(String text) {
		StringBuilder sb = new StringBuilder();
		
	    for (char c : text.toCharArray()) {
	    	if (Character.isDigit(c)) {
	    		sb.append(c);
	    	} else {
	    		sb.append((int)c);
	    	}
	    }
	    
	    BigInteger b = new BigInteger(sb.toString());
	    
	    return b;
	}
	
	private Long generateId(String text) {
		int[] pos = {0, 3, 7, 9};
		StringBuilder sb = new StringBuilder();
		char[] arr = text.toCharArray();
		for (int i : pos) {
			sb.append((int) arr[i]);
		}
		
		int checksum = 0;
		for (char c : arr) {
			checksum += (int) c;
		}
		sb.append(checksum);
		return Long.parseLong(sb.toString());
	}
	
	public static boolean registerAccount(String account) {
		int count = 0;
		String sql = "SELECT COUNT(*) FROM sentimentposts.accounts WHERE source LIKE 'Amazon' AND account LIKE ?;";
		try (Connection cnlocal = Server.connlocal(); PreparedStatement select = cnlocal.prepareStatement(sql)){
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
			String sql2 = "INSERT INTO sentimentposts.accounts VALUES (?, ?);";
			try (Connection cnlocal2 = Server.connlocal(); PreparedStatement insert = cnlocal2.prepareStatement(sql2)){
				insert.setString(1, "Amazon");
				insert.setString(2, account);
				insert.executeUpdate();
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
