package general;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.sql.Connection;

import facebook.*;

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
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		try {
			cal.setTime(sdf.parse("2012-01-01"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// all done
		
		new FacebookAgent().fetch(cal.getTime(),"AirForce1");
		System.out.println(FacebookUtil.authordb.toString());
		System.out.println("----------");
		System.out.println(FacebookUtil.postdb.toString());
		
		try(Connection cnlocal = Server.connlocal();
				PreparedStatement ps = cnlocal.prepareStatement("Insert into user values(?,?,102,'CIS','ALIEN')")){
			
			for(MyAuthor author : FacebookUtil.authordb.values()) {
				ps.setLong(1, author.getId().intValue());
				ps.setString(2, author.getName());
				ps.execute();
			}
			
		}catch(SQLException | ClassNotFoundException e) {
			System.err.println("Error Connection to DB");
		}
		
		try(Connection cnlocal = Server.connlocal();
				PreparedStatement ps = cnlocal.prepareStatement("Insert into post(id,timestamp,message,likes,views,product,post_id) values(?,?,?,?,?,'testing',?)")){
			
			for(MyPost post : FacebookUtil.postdb.values()) {
				ps.setLong(1, post.getId().intValue());
				Calendar cal2 = Calendar.getInstance();
				cal2.setTimeInMillis(post.getDate());
				ps.setString(2, cal2.getTime().toString());
				ps.setString(3, post.getMessage());
				ps.setLong(4, post.getLikes());
				ps.setLong(5, post.getShares());
				//ps.setLong(6, post.getAuthor().intValue());
				ps.setNull(6, java.sql.Types.BIGINT);
				ps.execute();
				for(MyPost comm : post.getComments().values()) {
					ps.setLong(1, comm.getId().intValue());
					cal2 = Calendar.getInstance();
					cal2.setTimeInMillis(comm.getDate());
					ps.setString(2, cal2.getTime().toString());
					ps.setString(3, comm.getMessage());
					ps.setLong(4, comm.getLikes());
					ps.setLong(5, comm.getShares());
					//ps.setLong(6, comm.getAuthor());
					ps.setLong(6,post.getId().intValue());
					ps.execute();
				}
				
			}
			
		}catch(SQLException | ClassNotFoundException e) {
			System.err.println("Error Connection to DB");
			e.printStackTrace();
		}
		//new Oversight();
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