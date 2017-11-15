package facebook;

public class MyAuthor {
	private long id;
	private String name;
	
	public MyAuthor (long _id, String _name) {
		id=_id;
		name=_name.trim();
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
