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

@Path("/getPosts")
public class GetPosts {

	@Context
	UriInfo ui;
	public static final String err_unknown = "ERROR ";
	public static final String err_dbconnect = "Cannot connect to database Please Try Again Later.";
	public static final String err_cr = "Cannot connect to common repository";
	

	/**
	 * Builds a matrix where the rows are all the rules in the selected design
	 * project and the columns are the design projects with those rules. Still need
	 * to include the polarity value of each rule.
	 * 
	 * @return - a JSON string with the lean rule matrix
	 * @throws JSONException
	 * @throws SQLException
	 * @throws InterruptedException 
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response welcome() throws JSONException, SQLException, InterruptedException {
		MultivaluedMap<String, String> params = ui.getQueryParameters();
		if(params.get("accounts[]")!=null){
			TwitterAgent.registerAccount(params.get("accounts[]").get(0));
			TwitterAgent.fetch();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
}
