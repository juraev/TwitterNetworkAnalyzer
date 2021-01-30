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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public interface TwitterApiEndpoint {
    String PAGINATION_TOKEN = "pagination_token";
    String PREVIOUS_TOKEN = "previous_token";
    String NEXT_TOKEN = "next_token";
    String DONE = "Done!";
    String META = "meta";
    String DATA = "data";
    String ID = "id";
    String NAME = "name";
    String USERNAME = "username";
    String RATE_LIMIT_MSG = "Rate limit exceeded\n";

    static User getUserById(final String userId, String bearer) {

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
                if (!jOb.has(DATA)) {
                    return null;
                }
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

    static User getUserByUsername(final String username, String bearer) {
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
                if (!jOb.has(DATA)) {
                    return null;
                }
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

    static List<User> getFollowers(String userId, String bearer) {
        List<User> followersAll = new ArrayList<>();
        String token = "P7SLO3786LHHEZZZ";

        while (true) {
            String response;

            try {
                response = request("https://api.twitter.com/2/users/%s/followers",
                        bearer, token, userId);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                break;
            }

            if (response != null) {
                if (RATE_LIMIT_MSG.equals(response)) {
                    System.out.println(RATE_LIMIT_MSG + " Going to sleep...");
                    try {
                        Thread.sleep(Duration.ofMinutes(15).toMillis());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(" Waking up...");
                    continue;
                }
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                } catch (JSONException exception) {
                    break;
                }
                System.out.println(response);
                if (!obj.has(DATA)) {
                    break;
                }
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

    static String request(String requestBody, String bearer, String paginationToken, String... params)
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
