package models;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User {
    String mId;
    String mName;
    String mUserName;

    public User(String mId, String mName, String mUserName) {
        this.mId = mId;
        this.mName = mName;
        this.mUserName = mUserName;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUserName() {
        return mUserName;
    }
}
