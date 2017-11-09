package facebook;

import java.awt.print.Pageable;
import java.util.Date;

import facebook4j.*;
import facebook4j.FacebookResponse.Metadata.Fields.Field;

public class FacebookAgent {
	Facebook facebook;
	private String appId = "appID";
	private String appSecret = "Secret";
	private String permissions = "";
	private String accessToken = appId+"|"+appSecret;
	
	public FacebookAgent() {
		facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(appId, appSecret);
		//facebook.setOAuthPermissions(permissions);
		//facebook.getOAuthAccessToken();
		facebook.setOAuthAccessToken(new facebook4j.auth.AccessToken(appId+"|"+appSecret, null));
		System.out.println(facebook.getOAuthAccessToken());
		
	}
	
	public void fetch(Date sincedate,String account) {
		
		try {
			
			String fields = "comments.limit(15000){like_count,id,message,created_time,from},";
			fields += "message,";
			fields += "shares,";
			fields += "likes.summary(true).limit(1),";
			fields += "id,";
			fields += "created_time,";
			fields += "from";
			
			ResponseList<Post> posts = facebook.getPosts(account, new Reading().fields(fields).since(sincedate).limit(1));
			for (Post post : posts) {
				MyPost localpost = new MyPost(post);
				for(Comment comm : post.getComments()) {
					localpost.add_comment(comm);
				}
				
				
				
			}
		} catch (FacebookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
