package facebook;

import java.util.HashMap;

public class FacebookUtil {

	
	static HashMap<Long, MyPost> postdb = new HashMap<>();
	static HashMap<Long, MyAuthor> authordb = new HashMap<>();
	
	public static synchronized void add_post(long key, MyPost post) {
		postdb.put(key, post);
	}
	
	public static synchronized void add_author(long key, MyAuthor author) {
		authordb.put(key, author);
	}
	
	public static void clearMaps() {
		postdb.clear();
		authordb.clear();
	}

	public static synchronized boolean authordb_containsKey(long author_id) {
		return authordb.containsKey(author_id);
	}
	
	
	
}
