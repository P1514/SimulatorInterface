package facebook;

import java.util.HashMap;

public class FacebookUtil {

	
	public static HashMap<Long, MyPost> postdb = new HashMap<>();
	public static HashMap<Long, MyAuthor> authordb = new HashMap<>();
	
	public static synchronized void add_post(Long key, MyPost post) {
		postdb.put(key, post);
	}
	
	public static synchronized void add_author(Long key, MyAuthor author) {
		authordb.put(key, author);
	}
	
	
	
}
