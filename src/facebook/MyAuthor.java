package facebook;

public class MyAuthor {
	private Long id;
	private String name;
	
	public MyAuthor (Long _id, String _name) {
		id=_id;
		name=_name;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
