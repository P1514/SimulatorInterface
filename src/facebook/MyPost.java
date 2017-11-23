package facebook;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import facebook4j.Comment;
import facebook4j.Post;

public class MyPost {
	
	private long id;
	private String message;
	private long author_id;
	private int likes;
	private int shares;
	private String create_date;
	private HashMap<Long, MyPost> comments = new HashMap<>();
	private String account;
	
	public MyPost(Post post,String _account) {
		create_Post(post.getId(), post.getMessage(), post.getFrom().getId(), post.getFrom().getName(), 
				    post.getLikes().getSummary().getTotalCount(), post.getSharesCount(), post.getCreatedTime().getTime(),_account);
		FacebookUtil.add_post(id, this);
	}
	
	public MyPost(Comment comm, String _account) {
		create_Post(comm.getId(), comm.getMessage(), comm.getFrom().getId(), comm.getFrom().getName(),
			    comm.getLikeCount(), 0, comm.getCreatedTime().getTime(), _account);
	}
	
	private void create_Post(String _id, String _message, String _author_id, String _author_name, Integer _likes, Integer _shares, Long _create_date, String _account) {
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
		if(!FacebookUtil.authordb_containsKey(author_id))
			FacebookUtil.add_author(author_id, new MyAuthor(author_id, _author_name));
		}catch (NumberFormatException e) {
			System.err.println("ERROR parsing author ID =>" + _author_id);
			return;
		}
		
		
		likes = _likes != null ? _likes : 0;
		shares = _shares != null ? _shares : 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		create_date = sdf.format(_create_date);
		this.account=_account;
	}
	
	public void add_comment(Comment comm,String _account) {
		MyPost to_add = new MyPost(comm, _account);
		comments.put(to_add.getId(), to_add);
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getMessage() {
		return message;
	}
	
	public long getAuthor() {
		return author_id;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public int getShares() {
		return shares;
	}
	
	public String getDate() {
		return create_date;
	}

	public HashMap<Long, MyPost> getComments() {
		return comments;
	}
	
	public String getAccount() {
		return account;
	}

}
