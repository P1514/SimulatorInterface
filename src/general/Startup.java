package general;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.json.JSONArray;
import org.json.JSONException;

import com.mysql.jdbc.Connection;

/**
 * The Class Startup runs every time the server boots up.
 */
@WebListener
public class Startup implements ServletContextListener {

	// private static final Logger LOGGER = new
	// Logging().create(Startup.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		new Oversight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// LOGGER.log(Level.INFO,"Shutting down!");
	}

}