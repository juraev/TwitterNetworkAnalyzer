package models;

import java.util.List;

public interface TwitterRepository {

    public int addFollower(User followee, User follower);

    public int addFollowers(User followee, List<User> followers);

    public void close();
}
