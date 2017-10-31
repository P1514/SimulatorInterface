
package general;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Future;

import javax.websocket.server.ServerEndpoint;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

// TODO: Auto-generated Javadoc
/**
 * The Class Server.
 */
@ServerEndpoint("/server")
public class Server {
	private static DataSource condata = null;
	private static DataSource conlocal = null;
	public static boolean runPolarity = true;

	/**
	 * Connlocal.
	 *
	 * @return the connection
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public static Connection connlocal() throws ClassNotFoundException, SQLException {
		try {

			while(conlocal==null) Thread.sleep(1000);

			Future<Connection> future = conlocal.getConnectionAsync();
			while (!future.isDone()) {
				try {
					Thread.sleep(100); // simulate work
				} catch (InterruptedException x) {
					Thread.currentThread().interrupt();
				}
			}

			return future.get(); // should return instantly
		} catch (Exception e) {
			System.out.println("Error connection to Database");
			return null;
		}
	}
	
	public static Connection conndata() throws ClassNotFoundException, SQLException {
		try {

			if (condata == null)
				startconnections();

			Future<Connection> future = condata.getConnectionAsync();
			while (!future.isDone()) {
				try {
					Thread.sleep(100); // simulate work
				} catch (InterruptedException x) {
					Thread.currentThread().interrupt();
				}
			}

			return future.get(); // should return instantly
		} catch (Exception e) {
			System.out.println("Error connection to Database");
			return null;
		}
	}

	public static void startconnections() {
		PoolProperties p = new PoolProperties();
		p.setUrl("jdbc:mysql://127.0.0.1:3306/sentimentposts?autoReconnect=true&useSSL=false");
		p.setDriverClassName("com.mysql.jdbc.Driver");
		p.setUsername("diversity");
		p.setPassword("!diversity!");
		p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setFairQueue(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(1);
		p.setMaxIdle(1);
		p.setInitialSize(1);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(1);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
		conlocal = new DataSource();
		conlocal.setPoolProperties(p);
		
		p = new PoolProperties();
		p.setUrl("jdbc:mysql://127.0.0.1:3306/sentimentanalysis?autoReconnect=true&useSSL=false");
		p.setDriverClassName("com.mysql.jdbc.Driver");
		p.setUsername("diversity");
		p.setPassword("!diversity!");
		p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setFairQueue(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(1);
		p.setMaxIdle(1);
		p.setInitialSize(1);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(1);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
		condata = new DataSource();
		condata.setPoolProperties(p);
	}

}
