import models.TwitterRepository;
import models.TwitterRepositoryImpl;
import models.User;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String BEARER = "YOUR_BEARER_HERE";
    private static final String PAGINATION_TOKEN = "pagination_token";
    private static final String PREVIOUS_TOKEN = "previous_token";
    private static final String NEXT_TOKEN = "next_token";
    private static final String DONE = "Done!";
    private static final String META = "meta";
    private static final String DATA = "data";
    private static final String MAIN_USER_ID = "THE_MAIN_USER_ID_HERE"; //Asadullo
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String USERNAME = "username";

    private static final TwitterRepository repo = new TwitterRepositoryImpl();

    // To set your environment variables in your terminal run the following line:
    // export 'BEARER_TOKEN'='<your_bearer_token>'

    public static void main(String args[]) throws IOException, URISyntaxException {

        String token = null;

        List<User> users = new ArrayList<>();

        User mainUser = getUserById(MAIN_USER_ID);

        while (true) {
            String res = getFollowers(MAIN_USER_ID, BEARER, token);

            JSONObject obj;
            try {
                obj = new JSONObject(res);
            } catch (JSONException jse) {
                System.out.println(res);
                break;
            }
            JSONArray followers = obj.getJSONArray(DATA);

            int size = followers.length();

            for (int i = 0; i < size; i++) {
                JSONObject follower = followers.getJSONObject(i);

                users.add(new User(follower.getString(ID),
                        follower.getString(NAME),
                        follower.getString(USERNAME)));
            }

            if (obj.getJSONObject(META).has(NEXT_TOKEN))
                token = obj.getJSONObject(META).getString(NEXT_TOKEN);
            else break;
        }

        repo.addFollowers(mainUser, users);
        repo.close();
    }

    private static User getUserById(final String userId) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(String.format("https://api.twitter.com/2/users/%s", userId));

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", BEARER));
        httpGet.setHeader("Content-Type", "application/json");


        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        User user = null;
        if (null != entity) {
            String userResponse = EntityUtils.toString(entity, "UTF-8");
            JSONObject jOb = new JSONObject(userResponse);

            jOb = jOb.getJSONObject(DATA);

            System.out.println(userResponse);
            user = new User(jOb.getString(ID),
                    jOb.getString(NAME),
                    jOb.getString(USERNAME));

        }

        return user;
    }

    /*
     * This method calls the v2 followers lookup endpoint by user ID
     * */
    private static String getFollowers(String userId, String bearerToken, String paginationToken) throws IOException, URISyntaxException {
        String tweetResponse = null;

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(String.format("https://api.twitter.com/2/users/%s/followers", userId));
//        ArrayList<NameValuePair> queryParameters;
//        queryParameters = new ArrayList<>();
//        queryParameters.add(new BasicNameValuePair("user.fields", "created_at"));
//        uriBuilder.addParameters(queryParameters);

        if (paginationToken != null) {
            uriBuilder.addParameter(PAGINATION_TOKEN, paginationToken);
        }

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");


        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            tweetResponse = EntityUtils.toString(entity, "UTF-8");
        }
        return tweetResponse;
    }
}
