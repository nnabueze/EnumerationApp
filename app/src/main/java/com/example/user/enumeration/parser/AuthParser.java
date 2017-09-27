package com.example.user.enumeration.parser;

import com.example.user.enumeration.model.AuthModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by USER on 9/27/2017.
 */

public class AuthParser {
    public static AuthModel parseFeed(String content){
        AuthModel auth = new AuthModel();
        try {
            JSONObject jsonObj = new JSONObject(content);

            //Log.d("OutPut",jsonObj.getString("token"));
            auth.setToken(jsonObj.getString("access_token"));

            //return auth;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return auth;
    }
}
