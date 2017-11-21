package general;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import amazon.AmazonAgent;
import facebook.FacebookAgent;

import java.sql.Statement;

import java.sql.Connection;

/**
 * The Class Oversight.
 */
public class Oversight extends TimerTask {

	// private static final Logger LOGGER = new
	// Logging().create(Oversight.class.getName());

	/**
	 * Instantiates a new oversight.
	 */
	public Oversight() {
		Timer timer = new Timer();
		timer.schedule(this, 1000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		
		while (Server.runPolarity) {
			if (newPosts()) {
				calculatePolarity();
			}
			AmazonAgent.fetch();
			FacebookAgent.fetch();
			try {
				Thread.sleep(1000 * 60 * 5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void calculatePolarity() {
		Polarity_Calculation pc = new Polarity_Calculation();
		try (Connection cnlocal = Server.conndata();
				Statement query = cnlocal.createStatement();
				PreparedStatement query2 = cnlocal.prepareStatement("update posts set polarity = 50.00 where id = ?");
				ResultSet rs = query.executeQuery("Select id,message from posts where polarity is null limit 1200")) {
			while (rs.next()) {
				String message = rs.getString("message");
				long post_id = rs.getLong("id");
				query2.setLong(1, post_id);
				query2.execute();
				pc.calc_pol(post_id, message);
			}
			Thread.sleep(1000*60);
		} catch (ClassNotFoundException | SQLException | InterruptedException e) {
			System.out.println("Error on fetching new posts");
		}

	}

	private boolean newPosts() {
		try (Connection cnlocal = Server.conndata();
				Statement stmt = cnlocal.createStatement();
				ResultSet rs = stmt.executeQuery("Select id from posts where polarity is null order by id limit 1")) {

			if (rs.next())
				return true;

		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Error on fetching new posts");
		}
		return false;
	}

}
