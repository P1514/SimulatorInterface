package deprecated;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Settings;

/**
 * The Class GetLastPost.
 *
 * @author Uninova - IControl
 */
@Deprecated
public class GetLastPost {
	// No longer used, it was used for influence graphs to get last post
	private Connection cnlocal;

	/**
	 * Get Last Post from a specific author.
	 */
	@Deprecated
	public GetLastPost() {
	}

	/**
	 * Search database for an opinion from a specific author and returns it as a
	 * JSONArray.
	 *
	 * @param name - Author's name to search for
	 * @return JSONArray - with all the information regarding that specific post
	 * @throws JSONException in case and error occured while building the JSON
	 */
	@Deprecated
	public JSONArray get(String name) throws JSONException {
		JSONArray result = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("Op", "posttable");
		result.put(obj);
		String insert = new String();
		PreparedStatement query1 = null;
		insert = "Select id,reach,timestamp FROM opinions where authors_id in (Select id from authors where name=?) ORDER BY timestamp DESC LIMIT 1";
		ResultSet rs = null;

		try {
			dbconnect();
			query1 = cnlocal.prepareStatement(insert);
			query1.setString(1, name);
			rs = query1.executeQuery();
			rs.next();
			obj = new JSONObject();
			obj.put("Id", rs.getInt("id"));
			int id = rs.getInt("id");
			obj.put("Reach", trunc(String.valueOf(rs.getDouble("reach"))));
			obj.put("Date", rs.getDate("timestamp"));
			rs.close();
			query1.close();
			cnlocal.close();

			dbconnect();
			insert = "SELECT Sum(likes), Sum(views), count(*), message FROM posts where opinions_id=?";
			query1 = cnlocal.prepareStatement(insert);
			query1.setInt(1, id);
			rs = query1.executeQuery();
			rs.next();
			obj.put("Ncomms", rs.getInt("count(*)"));
			obj.put("Nlikes", rs.getInt("Sum(likes)"));
			obj.put("Nviews", rs.getInt("Sum(views)"));
			obj.put("Message", rs.getString("message"));
			result.put(obj);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			;
			try {
				if (query1 != null)
					query1.close();
			} catch (Exception e) {
			}
			;
			try {
				if (cnlocal != null)
					cnlocal.close();
			} catch (Exception e) {
			}
			;
		}

		return result;

	}
	@Deprecated
	private String trunc(String number) {
		double result = 0;
		try {

			result = Double.valueOf(number);
			number = String.format("%.2f", result);
			result = Double.parseDouble(number);

		} catch (Exception e) {
			number = number.replaceAll(",", ".");
			result = Double.parseDouble(number);

		}
		return Double.toString(result);

	}
	@Deprecated
	private void dbconnect() {
		try {
			cnlocal = Settings.connlocal();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
