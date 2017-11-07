package amazon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AmazonAgent {

	private static final String DEFAULT_URL = "https://www.amazon.com/product-reviews/[ASIN]/ref=cm_cr_arp_d_paging_btm_[PAGE_NUMBER]?ie=UTF8&reviewerType=all_reviews&pageNumber=[PAGE_NUMBER]";
	private static final String ASIN = "[ASIN]";
	private static final String PAGE_NUMBER = "[PAGE_NUMBER]";
	
	private List<Review> reviews;
	private String url;
	private String asin;

	public AmazonAgent(String asin) {
		reviews = new ArrayList<Review>();
		this.asin = asin;
		
		url = DEFAULT_URL.replace(ASIN, this.asin);
		url = url.replace(PAGE_NUMBER, "1");
	}
	
	public String getReviews() {
		String json = "";
		int pageNumber = 1;
		
		while(true) { 
			System.out.println("URL: " + url);
			url = DEFAULT_URL.replace(ASIN, this.asin);
			url = url.replace(PAGE_NUMBER, "" + pageNumber);
			try {
				Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36").get();
				System.out.println(doc.html().toString());
				Elements reviewElements = doc.select(".review");
				if (reviewElements == null || reviewElements.isEmpty()) {
					break;
				} 
				
				for (Element reviewElement : reviewElements) {
					Element authorElement = reviewElement.select(".author").first();
					String author = authorElement.text();
					
					Element textElement = reviewElement.select(".review-text").first();
					String text = textElement.text();
					
					Element dateElement = reviewElement.select(".review-date").first();
					String date = dateElement.text().replace("on ", "");
					
					reviews.add(new Review(author, text, date));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			pageNumber++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		BufferedWriter output = null;
        try {
            File file = new File("example.txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write("AUTHOR\t MESSAGE\t DATE");
            for (Review r : reviews) {
            	output.write(r.getAuthor() + "\t" + r.getText() + "\t" + r.getDate());
            }
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        } 
		return json;
	}

}
