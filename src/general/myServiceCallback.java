package general;

import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class myServiceCallback implements ServiceCallback<AnalysisResults> {
	private long id;

	public myServiceCallback(long _id) {
		super();
		this.id = _id;
	}

	@Override
	public void onFailure(Exception arg0) {
		try(Connection cnlocal = Server.conndata();
				PreparedStatement ps = cnlocal.prepareStatement("update posts set polarity = 50.00 where id = ?")){
			ps.setLong(1, id);
			ps.execute();
			
		}catch (SQLException | ClassNotFoundException e) {
			System.out.println("ERROR logging to server");
		}
		System.out.println("Error on Response"+arg0);
		System.out.println("TIME: "+System.currentTimeMillis());
	}

	@Override
	public void onResponse(AnalysisResults arg0) {
		double polarity=50;
		try(Connection cnlocal = Server.conndata();
				PreparedStatement ps = cnlocal.prepareStatement("update posts set polarity = ? where id = ?")){
			polarity = (arg0.getSentiment().getDocument().getScore()+1)*50;
			ps.setDouble(1, polarity);
			ps.setLong(2, id);
			ps.execute();
			
		}catch (SQLException | ClassNotFoundException e) {
			System.out.println("ERROR logging to server");
		}
		System.out.println("Post ID: "+id +" Polarity value: "+polarity);
		System.out.println("TIME: "+System.currentTimeMillis());
		
	}
}