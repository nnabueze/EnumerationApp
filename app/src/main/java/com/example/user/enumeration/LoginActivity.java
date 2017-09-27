package com.example.user.enumeration;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.enumeration.model.AuthModel;
import com.example.user.enumeration.parser.AuthParser;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private AuthModel auth;
    @Bind(R.id.input_email) EditText emailText;
    @Bind(R.id.input_password) EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        if (!validate()){
            onLoginFail();
        }else{
            if (!isOnLine()){
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_SHORT).show();
            }else{
                makeCall();
            }
        }
    }

    private void makeCall() {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Utility.TOKEN,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        auth = AuthParser.parseFeed(response);
                        responseResult();

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        onLoginFailDialog();
                    }
                }){
            //adding header param

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", emailText.getText().toString());
                params.put("password", passwordText.getText().toString());
                params.put("grant_type", "password");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void responseResult() {
        if (auth == null){
            Toast.makeText(this, "Parser Error occurred", Toast.LENGTH_SHORT).show();
        }else {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("token",auth.getToken());
            editor.commit();

            StringRequest request = new StringRequest(Request.Method.POST, Utility.LOGIN_MOBILE,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            activityStart();
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            onLoginFailDialog();
                        }
                    }){
                //adding header param

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Email", emailText.getText().toString());
                    params.put("Password", passwordText.getText().toString());

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + auth.getToken());
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        }
    }

    private void activityStart() {
        Intent i = new Intent(this, EnumerationActivity.class);
        startActivity(i);
    }

    private void onLoginFailDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Error Message");
        builder.setMessage("Invalid Email and Password");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    //validating login details
    private boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Enter a valid email address");
            valid=false;
        }else{
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 3){
            passwordText.setError("Password can not be less than 3");
            valid=false;
        }else{
            passwordText.setError(null);
        }
        return valid;
    }

    private void onLoginFail() {
        Toast.makeText(this, "Invalid email and password", Toast.LENGTH_SHORT).show();
    }

    private boolean isOnLine() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }else {
            return false;
        }
    }
}
