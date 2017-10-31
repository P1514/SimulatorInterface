package general;

import java.sql.Connection;

import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;

import jersey.repackaged.jsr166e.CompletableFuture;

public class Polarity_Calculation {

	private static String url = "https://gateway.watsonplatform.net/natural-language-understanding/api";
	private static String username = "username";
	private static String password = "password";
	private NaturalLanguageUnderstanding service;

	public Polarity_Calculation() {
		service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, username,
				password);
	}

	public void calc_pol(Long id, String message) {
		
		SentimentOptions sentiment;
		Features features;
		AnalyzeOptions parameters;
		AnalysisResults response = null;

		sentiment = new SentimentOptions.Builder().build();

		features = new Features.Builder().sentiment(sentiment).build();

		parameters = new AnalyzeOptions.Builder().text(message).features(features).build();

		ServiceCall<AnalysisResults> call = service.analyze(parameters);
		call.enqueue(new myServiceCallback(id));

		return;
	}
}
