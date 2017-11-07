package amazon;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Review {

	private String author;
	private Date date;
	private String text;
	
	public Review() {
		this.author = "Unknown";
		this.text = "";
	}
	
	public Review(String author, String text, String date) {
		this.author = author;
		this.text = text;
		DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
		try {
			this.date = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
