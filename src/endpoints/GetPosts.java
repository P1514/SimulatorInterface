package endpoints;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Server;
import general.TwitterAgent;

@Path("/getPosts")
public class GetPosts {

	@Context
	UriInfo ui;
	public static final String err_unknown = "ERROR ";
	public static final String err_dbconnect = "Cannot connect to database Please Try Again Later.";
	public static final String err_cr = "Cannot connect to common repository";
	private List<String> epochsTo;
	private List<String> epochsFrom;
	private List<String> accounts;
	private long pssId = -1;
	private String pssName = "";
	

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
		TwitterAgent twitter = new TwitterAgent();
		return Response.status(Response.Status.OK).entity(twitter.getPageFeed(params.get("accounts[]").get(0)).toString()).build();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}

	

	private boolean verifyparams(MultivaluedMap<String, String> params) {
		if (params.get("accounts[]") == null)
			return false;
		if (params.get("epochsTo[]") == null)
			return false;
		if (params.get("epochsFrom[]") == null)
			return false;
		if (params.get("pssId") != null)
			pssId = Long.parseLong(params.getFirst("pssId"));
		if (params.get("pssName") != null)
			pssName = params.getFirst("pssName");

		accounts = params.get("accounts[]");
		epochsTo = params.get("epochsTo[]");
		epochsFrom = params.get("epochsFrom[]");

		if (epochsTo.size() != epochsFrom.size())
			return false;
		if (accounts.size() != epochsTo.size())
			return false;

		return true;

	}


}
