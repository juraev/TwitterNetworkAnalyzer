import models.TwitterRepository;
import models.TwitterRepositoryImpl;
import models.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    private static final TwitterRepository repo = new TwitterRepositoryImpl();

    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("twitter_api.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String bearer = properties.getProperty("twitter_bearer");
        String username = properties.getProperty("main_user_username");

//        User mainUser = getUserById(MAIN_USER_ID, bearer);
        User mainUser = TwitterRepo.getUserByUsername(username, bearer);

        List<User> users = TwitterRepo.getFollowers(mainUser.getId(), bearer);;

        repo.addFollowers(mainUser, users);
        repo.close();
    }

}
