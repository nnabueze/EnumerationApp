package com.example.user.enumeration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.enumeration.parser.AuthParser;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 9/27/2017.
 */

public class Utility {
    public static final String LOGIN_MOBILE ="http://hub.ercasng.com:8011/api/pos/MobileLogin";
    public static final String TOKEN ="http://hub.ercasng.com:8011/token";
    public static final String ENUMERATION ="http://hub.ercasng.com:8011/api/pos/Enumeration";
    public static final String IMAGE_UPLOAD ="http://hub.ercasng.com:8011/api/pos/UserImage";
}
