package amazon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	
	public JSONArray getReviews(long start, long finish) {
		int pageNumber = 1;
		
		while(true) { 
			url = DEFAULT_URL.replace(ASIN, this.asin);
			url = url.replace(PAGE_NUMBER, "" + pageNumber);
			System.out.println("URL: " + url);
			try {
				Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36").get();
				Elements reviewElements = doc.select(".review");
//				if no posts are found, uncomment the next line to check if we're getting a captcha
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
					String text = textElement.text();
					
					Element dateElement = reviewElement.select(".review-date").first();
					String date = dateElement.text().replace("on ", "");
					
					Review r = new Review(author, text, date, productText);
					
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
		
		JSONArray json = new JSONArray();
		for (Review r : reviews.values()) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("Author", r.getAuthor());
				obj.put("Message", r.getText());
				obj.put("Date", r.getDate());
				json.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Amazon Reviews: " + reviews.size());
		
		System.out.println(json.toString());
		return json;
	}

	public void store() {
		String sqlPost = "INSERT INTO sentimentposts.post VALUES (?,?,?,?,?,?,?);";
		String sqlUser = "INSERT INTO sentimentposts.user VALUES (?,?,?,?);";
		for (Review r : reviews.values()) {
			try (Connection cnlocal = Server.connlocal(); PreparedStatement insert = cnlocal.prepareStatement(sqlPost)){
				System.out.println("Executing: " + insert.toString());
				insert.setTimestamp(1, new Timestamp(r.getDateInEpoch()));
				insert.setString(2, r.getText());
				insert.setInt(3, 0);
				insert.setString(4, "0");
				insert.setInt(5, 1); //TODO: get user id 
				insert.setString(6, r.getProduct()); //TODO: fix product name?
				insert.setNull(7, java.sql.Types.BIGINT);
				insert.executeUpdate();
	
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			
			try (Connection cnlocal = Server.connlocal(); PreparedStatement insert = cnlocal.prepareStatement(sqlUser)){
				System.out.println("Executing: " + insert.toString());
				insert.setString(1, r.getAuthor());
				insert.setNull(2, java.sql.Types.INTEGER);
				insert.setNull(3, java.sql.Types.VARCHAR);
				insert.setNull(4, java.sql.Types.VARCHAR);
				insert.executeUpdate();
	
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
}
