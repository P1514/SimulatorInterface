package facebook;

import java.util.HashMap;

import facebook4j.Comment;
import facebook4j.Post;

public class MyPost {
	
	private long id;
	private String message;
	private long author_id;
	private int likes;
	private int shares;
	private long create_date;
	private HashMap<Long, MyPost> comments = new HashMap<>();
	
	public MyPost(Post post) {
		create_Post(post.getId(), post.getMessage(), post.getFrom().getId(), post.getFrom().getName(), 
				    post.getLikes().getSummary().getTotalCount(), post.getSharesCount(), post.getCreatedTime().getTime());
		FacebookUtil.add_post(id, this);
	}
	
	public MyPost(Comment comm) {
		create_Post(comm.getId(), comm.getMessage(), comm.getFrom().getId(), comm.getFrom().getName(),
			    comm.getLikeCount(), 0, comm.getCreatedTime().getTime());
	}
	
	private void create_Post(String _id, String _message, String _author_id, String _author_name, Integer _likes, Integer _shares, Long _create_date) {
		String strip_id = _id.split("_")[1]; 
		try {
			id = Long.parseLong(strip_id);
		}catch (NumberFormatException e) {
			System.err.println("ERROR parsing post ID =>" + _id + "after strip =>"+strip_id);
			return;
		}
		
		message = _message;
		try {
		author_id = Long.parseLong(_author_id);
		if(!FacebookUtil.authordb.containsKey(author_id))
			FacebookUtil.add_author(author_id, new MyAuthor(author_id, _author_name));
		}catch (NumberFormatException e) {
			System.err.println("ERROR parsing author ID =>" + _author_id);
			return;
		}
		
		
		likes = _likes;
		shares = _shares;
		create_date = _create_date;
	}
	
	public void add_comment(Comment comm) {
		MyPost to_add = new MyPost(comm);
		comments.put(to_add.getId(), to_add);
	}
	
	public Long getId() {
		return this.id;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Long getAuthor() {
		return author_id;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public int getShares() {
		return shares;
	}
	
	public long getDate() {
		return create_date;
	}

	public HashMap<Long, MyPost> getComments() {
		return comments;
	}

}
