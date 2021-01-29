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

public class TwitterRepo {
    private static final String PAGINATION_TOKEN = "pagination_token";
    private static final String PREVIOUS_TOKEN = "previous_token";
    private static final String NEXT_TOKEN = "next_token";
    private static final String DONE = "Done!";
    private static final String META = "meta";
    private static final String DATA = "data";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String USERNAME = "username";

    public static User getUserById(final String userId, String bearer) {

        User user = null;
        String response = null;

        try {
            response = request("https://api.twitter.com/2/users/%s", bearer, null, userId);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        if (response != null) {
            JSONObject jOb;
            try {
                jOb = new JSONObject(response);
                jOb = jOb.getJSONObject(DATA);
            } catch (JSONException exception) {
                exception.printStackTrace();
                return null;
            }

            user = new User(jOb.getString(ID),
                    jOb.getString(NAME),
                    jOb.getString(USERNAME));
        }

        return user;
    }

    public static User getUserByUsername(final String username, String bearer) {
        User user = null;
        String response = null;

        try {
            response = request("https://api.twitter.com/2/users/by/username/%s", bearer, null, username);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        if (response != null) {
            JSONObject jOb;
            try {
                jOb = new JSONObject(response);
                jOb = jOb.getJSONObject(DATA);
            } catch (JSONException exception) {
                exception.printStackTrace();
                return null;
            }
            user = new User(jOb.getString(ID),
                    jOb.getString(NAME),
                    jOb.getString(USERNAME));
        }

        return user;
    }

    public static List<User> getFollowers(String userId, String bearer) {
        List<User> followersAll = new ArrayList<>();
        String token = null;

        while (true) {
            String response;

            try {
                response = request("https://api.twitter.com/2/users/%s/followers",
                        bearer, token, userId);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                return null;
            }

            if (response != null) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                } catch (JSONException exception) {
                    return null;
                }
                System.out.println(response);
                JSONArray followersJSON = obj.getJSONArray(DATA);

                int size = followersJSON.length();

                for (int i = 0; i < size; i++) {
                    JSONObject follower = followersJSON.getJSONObject(i);

                    followersAll.add(new User(follower.getString(ID),
                            follower.getString(NAME),
                            follower.getString(USERNAME)));
                }

                if (obj.getJSONObject(META).has(NEXT_TOKEN))
                    token = obj.getJSONObject(META).getString(NEXT_TOKEN);
                else
                    break;
            }
        }

        return followersAll;
    }

    private static String request(String requestBody, String bearer, String paginationToken, String... params)
            throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(String.format(requestBody, params));

        if (paginationToken != null) {
            uriBuilder.addParameter(PAGINATION_TOKEN, paginationToken);
        }

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearer));
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            return EntityUtils.toString(entity, "UTF-8");
        }
        return null;
    }


}
