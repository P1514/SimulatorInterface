package amazon;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Review {

	private String author;
	private Date date;
	private long dateInEpoch;
	private String text;
	private List<Review> comments;
	
	public Review() {
		this.author = "Unknown";
		this.text = "";
		this.comments = new ArrayList<Review>();
		this.dateInEpoch = this.date.getTime();
	}
	

	public Review(String author, String text, String date) {
		this.author = author;
		this.text = text;
		this.comments = new ArrayList<Review>();
		handleDate(date);
		this.dateInEpoch = this.date.toInstant().getEpochSecond();
	}
	
	private void handleDate(String value) {
		if (value.contains("ago")) {
			Calendar cal = Calendar.getInstance();
			if (value.contains("days")) {
				cal.add(Calendar.DATE, -Integer.parseInt(value.split(" ")[0]));
			} else if (value.contains("month")) {
				cal.add(Calendar.MONTH, -Integer.parseInt(value.split(" ")[0]));
			} else if (value.contains("year")) {
				cal.add(Calendar.YEAR, -Integer.parseInt(value.split(" ")[0]));
			}
			date = cal.getTime();
		} else {
			try {
				DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
				date = format.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public long getDateInEpoch() {
		return dateInEpoch;
	}
	
	public void setDateInEpoch(long dateInEpoch) {
		this.dateInEpoch = dateInEpoch;
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
	
	public void addComment(Review comment) {
		comments.add(comment);
	}
	
	public List<Review> getComments() {
		return comments;
	}
}
