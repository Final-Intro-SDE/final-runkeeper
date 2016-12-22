/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runkip.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author benhur
 */
@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/runkip")
public class Runkip {
        @Context
    UriInfo uriInfo;
    @Context
    Request request;
	    
  ObjectMapper mapper = new ObjectMapper();
  JSONObject obj;
	    String arrayToJson = "";
	    static String data = "";
	private final static String CLIENT_ID = "d2e8c62e82a24439868579320bcb06a9";
        private final static String CLIENT_SECRET = "dccb69d503c44772886cabcc075e740a";
        private final static String CALLBACK_URL = "com.example.runkeeperapi://RunKeeperIsCallingBack";
	

@GET
@Path("/authc/{authCode}")
@Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
  public void getAccessToken2 (@PathParam("authCode") String authCode){
	System.out.println("in getAccessToken "+authCode+ " hj");
       convertToken(authCode);
//accessToken eyJkYXRhIjp7InIiOiJjb20uZXhhbXBsZS5ydW5rZWVwZXJhcGk6Ly9SdW5LZWVwZXJJc0NhbGxpbmdCYWNrIiwiYyI6ImQyZThjNjJlODJhMjQ0Mzk4Njg1NzkzMjBiY2IwNmE5IiwidCI6MTQ4MTc1ODQzMjEwMCwidSI6NDg1Njk3NzJ9LCJtYWMiOiI5NysxazRFaXgzcGI2MmFKZXA2bnlqaGVCNGtaN3h6SWxwbFkxUUVzd0hRPSJ9
//accessToken 978c2ed2267e4313983b4950db494a0f
	}
void convertToken(String authCode) {
        HttpClient client = new DefaultHttpClient();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nameValuePairs.add(new BasicNameValuePair("code", authCode));
        nameValuePairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
        nameValuePairs.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        try {
            HttpPost post = new HttpPost("https://runkeeper.com/apps/token");
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            //HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(response
                            .getEntity());
                    final JSONObject json = new JSONObject(jsonString);

                    String accessToken = json.getString("access_token");
                    getTotalDistance(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
	
    }

    private void getAccessToken(String authCode) {
        String accessTokenUrl = "https://runkeeper.com/apps/token";
        final String finalUrl = String.format(accessTokenUrl, authCode,CLIENT_ID, CLIENT_SECRET);
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
		    sendPost(finalUrl);

//                    HttpClient client = new DefaultHttpClient();
//                    HttpPost post = new HttpPost(finalUrl);
//
//                    HttpResponse response = client.execute(post);
//
//                    String jsonString = EntityUtils.toString(response
//                            .getEntity());
//                    final JSONObject json = new JSONObject(jsonString);
//
//                    String accessToken = json.getString("access_token");
//                    getTotalDistance(accessToken);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        networkThread.start();
    } 

    //Runkeeper's total distance, returns a json array
    public static void getTotalDistance(String accessToken) { 
	System.out.println("in getTotalDistance "+accessToken);
        try {
	  
     URL objs = new URL("http://api.runkeeper.com/records");
   HttpURLConnection con = (HttpURLConnection) objs.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("Authorization", "Bearer " + accessToken);
                con.setRequestProperty ("Accept", "*/*");

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
//String jsonString = EntityUtils.toString(response.getEntity());
	   // System.out.println("jsnnn "+jsonString);
            JSONArray jsonArray = new JSONArray(response.toString().trim());
            findTotalWalkingDistance(jsonArray);
	    
	    
	    

        } catch (Exception e) {
            
            e.printStackTrace();
                }
    }

    //calculating the distance run, in kilometers
    public static double findTotalWalkingDistance(JSONArray arrayOfRecords) {
        double wk=0.0;
	try {
            //Each record has activity_type and array of statistics. Traverse to  activity_type = Walking
            for (int ii = 0; ii < arrayOfRecords.length(); ii++) {
                JSONObject statObject = (JSONObject) arrayOfRecords.get(ii);
                if ("Running".equalsIgnoreCase(statObject.getString("activity_type"))) {
                    //Each activity_type has array of stats, navigate to "Overall" statistic to find the total distance walked.
                    JSONArray walkingStats = statObject.getJSONArray("stats");
                    for (int jj = 0; jj < walkingStats.length(); jj++) {
                        JSONObject iWalkingStat = (JSONObject) walkingStats.get(jj);
                        if ("Overall".equalsIgnoreCase(iWalkingStat.getString("stat_type"))) {
                            long totalWalkingDistanceMeters = iWalkingStat.getLong("value");
                            double totalWalkingDistanceKm = totalWalkingDistanceMeters * 0.001;
                           try {
			       wk = totalWalkingDistanceKm;
//	
  

	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
			    
                        }
                    }
                }
            }
     
        } catch (Exception e) {
                    
            e.printStackTrace();
        }
	System.out.println("walked "+wk);
                            return wk;
    }
    
    static String sendurl(String target, String mtd, String input){
	try {
		    

			URL targetUrl = new URL(target);

			HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod(mtd);
			
			if(mtd.equals("POST")||mtd.equals("PUT")){
			httpConnection.setRequestProperty("Content-Type", "application/json");
			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(input.getBytes());
			outputStream.flush();
			}
			if (httpConnection.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ httpConnection.getResponseCode());
			}

			BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
					(httpConnection.getInputStream())));

			String output="";
			System.out.println("Output from Server:\n");
			
			while ((output = responseBuffer.readLine()) != null) {
				data = output;
			}
	    System.out.println("datas "+data);
			httpConnection.disconnect();
			

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		 }
	return data;
	}
    private void sendPost(String urls) throws Exception {
URL url = new URL(urls);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
  httpCon.setDoOutput(true);
  httpCon.setRequestMethod("POST");
  OutputStreamWriter out = new OutputStreamWriter(
      httpCon.getOutputStream());
//  System.out.println(httpCon.getResponseCode());
//  System.out.println(httpCon.getResponseMessage());
  out.close();
   BufferedReader in = new BufferedReader( new InputStreamReader(httpCon.getInputStream())); 
   String output; StringBuffer response = new StringBuffer(); 
   while ((output = in.readLine()) != null) { response.append(output); } 
   in.close(); //printing result from response 
   System.out.println(response.toString());

	}
}
