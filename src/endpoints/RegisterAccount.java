package endpoints;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;

import amazon.AmazonAgent;
import facebook.FacebookAgent;
import general.TwitterAgent;

@Path("/registerSFE")
public class RegisterAccount {

	@Context
	UriInfo ui;
	public static final String err_unknown = "ERROR ";
	public static final String err_dbconnect = "Cannot connect to database Please Try Again Later.";
	public static final String err_cr = "Cannot connect to common repository";
	MultivaluedMap<String, String> params;

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
		params = ui.getQueryParameters();
		if (!checkparams())
			return Response.status(Response.Status.BAD_REQUEST).entity("No Accounts Provided").build();
		List<String> types = params.get("type[]");
		List<String> accounts = params.get("accounts[]");
		int p_size = accounts.size();
		String response = "";
		for (int i = 0; i < p_size; i++) {
			switch (types.get(i).toLowerCase()) {
			case "facebook":
				if (FacebookAgent.add_account(accounts.get(i))) {
					response += "Facebook + " + accounts.get(i) + " Success\n\r";
				} else {
					response += "Facebook + " + accounts.get(i) + " Doesn't Exist \n\r";
				}
				break;
			case "twitter":
				TwitterAgent.registerAccount(accounts.get(i));
				response += "Twitter + " + accounts.get(i) + " Success\n\r";
				break;
			case "amazon":
				AmazonAgent.registerAccount(accounts.get(i));
				response += "Amazon + " + accounts.get(i) + " Success\n\r";
				break;
			}
		}

		return Response.status(Response.Status.OK).entity(response).build();
	}

	private boolean checkparams() {
		if (!params.containsKey("accounts[]"))
			return false;
		if (!params.containsKey("type[]"))
			return false;
		if (params.get("accounts[]").size() != params.get("type[]").size())
			return false;
		return true;
	}
}