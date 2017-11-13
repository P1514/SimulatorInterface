package general;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterAgentdraft {

    static private final String TWITTER_BASE_URL = "https://twitter.com/";
    static private final String USER_STATUS_POSTFIX = "/statuses/";
    static private final String CONSUMER_KEY = "MBiaWIuV9f8ZAuUGfMRhlLdND";
    static private final String CONSUMER_SECRET = "KC4GoGf6EZxJgruMOmYyhTFLJD07qKrRqW3yEx4w0Vvtf3ZXTm";
    static private final String ACCESS_TOKEN = "2237544295-L37YEFJyyGOPVw0CZ3KDwn7NZaf79L9Lf09kaD9";
    static private final String ACCESS_TOKEN_SECRET = "gnBBNh3xlnUQalanxLAqxXEZ0Nx4Ss5TcUXc1rMJmijSw";

    private final long pauseTime = 1000L; //Delay of the next query to the Twitter Serach API (1s)

    private final String defaultPage = "google";
    //private final String defaultPage = "myfcmb";

    private final Date defaultDate = new Date(1388534400000L);
    // The factory instance is re-useable and thread safe.
    private Twitter twitter;
    
    
  
    public TwitterAgent(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        TwitterFactory tfactory = new TwitterFactory(this.getDefaultOptions(consumerKey, consumerSecret, accessToken, accessTokenSecret));
        this.twitter = tfactory.getInstance();

    }

    public TwitterAgent() {
        //Create an instance of TwitterAgent using default keys
        this(CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
    }

    public Twitter getTwitter() {
        return this.twitter;
    }

    public RequestToken requestAccessToken() {
        try {
            return twitter.getOAuthRequestToken();
        } catch (TwitterException ex) {
            Logger.getLogger(TwitterAgent.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public void setAccessToken(String accessToken, String accessTokenSecret) {

        twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

    }

    private Configuration getDefaultOptions(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setIncludeEntitiesEnabled(true)
                .setIncludeMyRetweetEnabled(true);

        return cb.build();
    }



    public boolean isValidPage(String accountName) {
        String[] TUsers = {accountName};
        try {
            twitter.lookupUsers(TUsers);
        } catch (TwitterException ex) {
            Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Twitter account does not exist!");
            return false;
        }
        Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Twitter account does exist!");
        return true;
    }

    //Return the number of retweets of specific page

    public int getPageLikesRetweets(String pageName) {
        int likes_retweets = 0;
        try {
            likes_retweets = this.getTwitter().showUser(pageName).getStatusesCount();
        } catch (TwitterException ex) {
            Logger.getLogger(TwitterAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return likes_retweets;
    }

    //Return the number  of followers of the page
    public int getPageFriendsFollowers(String pageName) {
        int friends_followers = 0;
        try {
            friends_followers = this.getTwitter().showUser(pageName).getFollowersCount();
        } catch (TwitterException ex) {
            Logger.getLogger(TwitterAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return friends_followers;
    }

    //Construct the url of specific status for a user who make the post
    public String getUserStatusURL(String screenName, long statusID) {
        return TWITTER_BASE_URL + screenName + USER_STATUS_POSTFIX + String.valueOf(statusID);
    }

    /**
     *
     * @param group The group name to fetch the accounts
     * @return A Set of Post Objects
     * @throws JSONException 
     */
    public JSONArray getPageFeed(String account, ) throws JSONException {

        //Set<Post> posts = new HashSet(0);
        //Map<Long, Individual> twitterEntities = new HashMap();
        //Individual individual;
        Query query = new Query();
        QueryResult queryres;
        //Post tmpPost;
        User twitterUser = null;
        long firstPostEpoch = -1;
        long currentPostEpoch = 0;
        //long sincePostEpoch = (group.getSince() <= 0 ? this.defaultDate.getTime() : group.getSince());
        int postsPERquery = 10;
        long min = 0L;
        query.setMaxId(min);
        boolean stopFetching = false;
        boolean isPageOwner = false;
        List<Status> results;
        JSONArray output= new JSONArray();
        try {
            //Set Twitter Search Query
            //query.setQuery("@" + group.getProfile().getName());
            query.setQuery("@" + account);
            query.setCount(postsPERquery);
            do {
                isPageOwner = false;
                queryres = this.getTwitter().search(query);
                results = queryres.getTweets();
                if (results == null || results.isEmpty() || stopFetching) {
                    break;
                }

                for (Status status : results) {

                    /*twitterUser = status.getUser();
                    //Created time (Java Date) of the current tweet formatted in Linux Epoch Time (long)
                    currentPostEpoch = status.getCreatedAt().getTime();
                    stopFetching = (group.getSince() >= 0 && currentPostEpoch <= sincePostEpoch);
                    //Set the time of the first fetched post
                    if (firstPostEpoch < 0) {
                        Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Setting first post epoch@" + currentPostEpoch + " epoch date: {0}", new Date(currentPostEpoch).toString());
                        firstPostEpoch = currentPostEpoch;
                    }
                    //Break loop if interval time match current fetched tweet created time
                    if (stopFetching) {
                        break;
                    }

                    //Retrieve personal info for all users which post to the group except for the owner of the account (current page)
                    if (twitterUser != null && twitterUser.getScreenName() != null && !(isPageOwner = twitterUser.getScreenName().equalsIgnoreCase(group.getProfile().getName()))) //Check if entity already exists
                    {
                        if (twitterEntities.containsKey(status.getUser().getId())) {
                            Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Entity with @ID:" + status.getUser().getId() + " already exists skipping personal info retrieval.. ");
                        } else {
                            Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Retrieving personal info for Username: " + (status.getUser().getScreenName() != null ? status.getUser().getScreenName() : "<NULL>") + " with UserID: " + status.getUser().getId());
                            individual = new Individual();
                            /*individual.setProfile(group.getProfile());
                            individual.setGroup(group);
                            individual.setProfileType(group.getProfile().getProfileType());
                            individual.setUserId(status.getUser().getId());
                            individual.setFullname(status.getUser().getName());
                            individual.setLocation(CharMatcher.ASCII.retainFrom(status.getUser().getLocation()));
                            individual.setScreenName(status.getUser().getScreenName());
                            individual.setProfileImageUrl(twitterUser.getBiggerProfileImageURL());

                            //Save current users' demographic data
                            twitterEntities.put(status.getUser().getId(), individual);
                        }
                        twitterEntities.get(status.getUser().getId()).increasePostNum();
                    } else {
                        Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, (twitterUser != null ? "Skipping personal info retrieval for user: " + twitterUser.getScreenName() + " because is the owner of the account: @" + group.getProfile().getName() : "Skipping entity for user: " + twitterUser.getScreenName() + " could not retrieve personal info (User object is null)"));
                    }
*/
                    //Increase the number of fetched posts
                    //group.increasePostNum();
                    //tmpPost = new Post();
                    //tmpPost.setGroup(group);
                    //tmpPost.setContentId(String.valueOf(status.getId()));
                    //tmpPost.setUserId(status.getUser().getId());
                    //tmpPost.setCreatedDate(status.getCreatedAt());
                    //tmpPost.setPostUrl(this.getUserStatusURL(twitterUser.getScreenName(), status.getId()));
                    //tmpPost.setContent(CharMatcher.ASCII.retainFrom(status.getText()));
                    //tmpPost.setEpoch(status.getCreatedAt().getTime());
                    //posts.add(tmpPost);
                		JSONObject out = new JSONObject();
                		out.put("User", status.getUser().getName());
                		out.put("Tweet", status.getText());
                		output.put(out);
                		

                    //sbuilder.append("[Count of Retweets: ".concat(String.valueOf(status.getRetweetCount()).concat("]\n")));
                    //sbuilder.append("[Reply: ".concat(String.valueOf(status.getInReplyToStatusId()).concat("]\n\n")));
                }
                //Sleep a while in order to avoid hitting Twitter API maxRateLimit
                Thread.sleep(pauseTime);
                //Get next query result page
            } while ((query = queryres.nextQuery()) != null);

            Logger.getLogger(TwitterAgent.class.getName()).log(Level.INFO, "Posts were retrieved! Total: " + "group.getPostsNum()" + " Last Epoch Time: " + currentPostEpoch + " Epoch Date:  {0}", new Date(currentPostEpoch).toString());
        } catch (InterruptedException | TwitterException ex) {
            Logger.getLogger(TwitterAgent.class.getName()).severe(ex.getMessage());
        }

        //Set Group Meta Info
        //group.setSince(firstPostEpoch);
        //Set Group Posts
        //group.setPosts(posts);
        //Set Group Individuals Info
        //Set<Individual> individuals = new HashSet(0);
        //individuals.addAll(twitterEntities.values());
        //group.setIndividuals(matcher.getBestMatchLocation(individuals));

        return output;

    }

}
