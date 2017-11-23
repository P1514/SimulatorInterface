package endpoints;


import java.sql.SQLException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;

import twitter.TwitterAgent;
import amazon.AmazonAgent;
import facebook.FacebookAgent;

/**
 * sample call
 * http://localhost:8080/SimInterface/endpoints/loadPosts?amazonAccounts[]=a1,a2,a3&twitterAccounts[]=t1,t2&facebookAccounts[]=f1,f2,f3,f4
 *
 */
@Path("/loadPosts")
public class LoadPosts {

	@Context
	UriInfo ui;
	public static final String err_unknown = "ERROR ";
	public static final String err_dbconnect = "Cannot connect to database Please Try Again Later.";
	public static final String err_cr = "Cannot connect to common repository";
	

	@GET
//	@Produces(MediaType.TEXT_HTML)
	public Response welcome() throws JSONException, SQLException, InterruptedException {
		MultivaluedMap<String, String> params = ui.getQueryParameters();
		
		if(params.get("amazonAccounts[]")!=null){
			String[] amazonAccounts = params.get("amazonAccounts[]").get(0).split(",");
			for (String s : amazonAccounts) {
				AmazonAgent.registerAccount(s);
			}
			AmazonAgent.fetch();
		}
		
		if(params.get("facebookAccounts[]")!=null){
			String[] facebookAccounts = params.get("facebookAccounts[]").get(0).split(",");;
			for (String s : facebookAccounts) {
				FacebookAgent.add_account(s);
			}
			FacebookAgent.fetch();
		}	
			
		if(params.get("twitterAccounts[]")!=null){
			String[] twitterAccounts = params.get("twitterAccounts[]").get(0).split(",");;
			for (String s : twitterAccounts) {
				TwitterAgent.registerAccount(s);
			}
			TwitterAgent.fetch();
		}
		
		return Response.status(Response.Status.OK).entity("").build();
	}
}
