package twi_classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.MediaEntity;
import twitter4j.Status;

//@Path("/twiservice")
@Path("")
public class RestService {
	static ConfigureFile configureFile=null;
	static ConfigNRetrieve twitterRetrieval=null;
	static Thread t1;
	  
	  @GET
	  @Produces("application/json")
	  @Path("/begin")
	  public Response begin() throws JSONException {
		  if(twitterRetrieval!=null) return Response.status(500).entity("[Get-method begin] has already been called.").build();
		  
		  t1 = new Thread(new ConfigNRetrieve ());
		  t1.start();  
		JSONObject jsonObject = new JSONObject();
		twitterRetrieval= new ConfigNRetrieve();
		jsonObject.put("Status:", "The Social-Media Adapter begun to retrieve tweets"); 
		String result = "@Produces(\"application/json\") \n\n" + jsonObject;	
		//System.out.println("begin tweets:"+ConfigNRetrieve.tweets);
		System.out.println("End of begin method");
		return Response.status(200).entity(result).build();
	  }
	   
	  @POST
	  @Path("/retrieve")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response retrieveTweets(String input) throws IOException {
		  if(twitterRetrieval==null) return Response.status(500).entity("Call [Get-method begin] or just wait a few seconds to initialize.").build();
		  JSONObject jo = new JSONObject(input);
		  String[] keywordsList = (jo.getString("keywords")).split(",");
		  String placeString = jo.getString("place");
		
		  //System.out.println("The place is: "+placeString);
		  List<JSONObject> listJSONobj = new ArrayList<JSONObject>();
		  System.out.println("The number of tweets is: "+ConfigNRetrieve.tweets.size());
		  
			for (Status tweet : ConfigNRetrieve.tweets) { 
				Boolean TweetContainAllKeywords =true;
				for(String keywordString: keywordsList) {
					if(!tweet.getText().toLowerCase().contains(keywordString.toLowerCase())) TweetContainAllKeywords=false;
					//System.out.println("One keyeord is:" + keywordString);
				}
				if(placeString.length()>1)
					if(!(tweet.getUser().getLocation()).toLowerCase().contains(placeString.toLowerCase())) TweetContainAllKeywords=false;
				
				if(TweetContainAllKeywords) {
					MediaEntity[] media = tweet.getMediaEntities(); //get the media entities from the status
					String images="";
					for(MediaEntity m : media){  //search trough your entities
						images+=(m.getMediaURL())+" "; } //get your url!
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("source", "Twitter"); 
					jsonObject.put("sourceImg", tweet.getUser().getBiggerProfileImageURL());
					jsonObject.put("poster", tweet.getUser().getName());
					jsonObject.put("postDate", tweet.getCreatedAt());
					jsonObject.put("postText", tweet.getText());
					jsonObject.put("postPhoto", images);
					listJSONobj.add(jsonObject);
				}
			}			
			String result = "@Produces(\"application/json\") Output: \n\nF to C-Results Converter Output: \n\n" + listJSONobj;
			return Response.status(200).entity(result).build();
	  }//============================== End of retrieveTweets() ===============================	  

	  @POST
	  @Path("/configure")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response configure(String input) throws IOException {
		  if(twitterRetrieval!=null) return Response.status(500).entity("You can configure the SMA adapter before the Call [Get-method begin].").build();
		  
		  JSONObject jo = new JSONObject(input);
		  
		  configureFile = new ConfigureFile();
		  ConfigureFile.setAuthConsumerKey(jo.getString("AuthConsumerKey")) ;
		  ConfigureFile.setAuthConsumerSecret(jo.getString("AuthConsumerSecret")) ;
		  ConfigureFile.setAuthAccessToken(jo.getString("AuthAccessToken")) ;
		  ConfigureFile.setAccessTokenSecret(jo.getString("AccessTokenSecret")) ;
		  ConfigureFile.setTimedelay(Long.parseLong(jo.getString("TimeDelay")));
		  ConfigureFile.setMax_tweets(Long.parseLong(jo.getString("Max_Tweets")));
		  ConfigureFile.setArrayKeyWords((jo.getString("KeyWords").split(",")));

	      JSONObject jsonObject = new JSONObject();
		  jsonObject.put("Configuration", "Success"); 
		  System.out.println("The data are: "+ConfigureFile.getAccessTokenSecret()+" "+ConfigureFile.getAuthAccessToken()+" "+ConfigureFile.getAuthConsumerKey()+" "+ConfigureFile.getAuthConsumerSecret());
		  System.out.println(ConfigureFile.getMax_tweets()+" "+ConfigureFile.getTimedelay()+" "+ConfigureFile.getArrayKeyWords()[1]);
		  String result = "@Produces(\"application/json\") Output: \n\nF to C-Results Converter Output: \n\n" + jsonObject;
		  return Response.status(200).entity(result).build();
	  }	  
}
