package com.example.user.enumeration;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.enumeration.parser.AuthParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class EnumerationActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Menu menu;
    private EditText surname, firstname, middlename, phone, email, region, csp, meterNo, meterType;
    private Button submit;

    private Button buttonChoose;

    private ImageView imageView;

    private Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enumeration);

        setUpCollapsToolBar();
        getEditText();

        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        submit = (Button) findViewById(R.id.btn_login);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utility.IMAGE_UPLOAD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(EnumerationActivity.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                            onLoginFailDialog("Communication Error!");

                        } else if (error instanceof AuthFailureError) {
                            Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                            onLoginFailDialog("Authentication Error!");
                        } else if (error instanceof ServerError) {
                            Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                            onLoginFailDialog("Server Side Error!");
                        } else if (error instanceof NetworkError) {
                            Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                            onLoginFailDialog("Network Error!");
                        } else if (error instanceof ParseError) {
                            Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                            onLoginFailDialog("Parse Error!");
                        }
                        Log.d("Maps:", " Error: " + new String(error.networkResponse.data));
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Creating parameters
                Map<String,String> params = new HashMap<>();
                Log.e("Image One", image);
                //Adding parameters
                params.put("File", image);

                //returning parameters
                return params;
            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("content-type", "multipart/form-data");
//                return headers;
//            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void login() {
        if (!validate()){
            onLoginFail();
        }else{
            if (!isOnLine()){
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_SHORT).show();
            }else{
                //makeCall();
                uploadImage();
            }
        }
    }

    private void makeCall() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final ProgressDialog progressDialog = new ProgressDialog(EnumerationActivity.this, R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Submiting...");
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Utility.ENUMERATION,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        responseResult();

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //Showing toast
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            onLoginFailDialog("Communication Error!");

                        } else if (error instanceof AuthFailureError) {
                            onLoginFailDialog("Authentication Error!");
                        } else if (error instanceof ServerError) {
                            onLoginFailDialog("Server Side Error!");
                        } else if (error instanceof NetworkError) {
                            onLoginFailDialog("Network Error!");
                        } else if (error instanceof ParseError) {
                            onLoginFailDialog("Parse Error!");
                        }
                    }
                }){
            //adding header param

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Surname", surname.getText().toString());
                params.put("Firstname", firstname.getText().toString());
                params.put("Middlename", middlename.getText().toString());
                params.put("Phone", phone.getText().toString());
                params.put("Email", email.getText().toString());
                params.put("Region", region.getText().toString());
                params.put("CashServicePoint", csp.getText().toString());
                params.put("MeterNo", meterNo.getText().toString());
                params.put("MeterType", meterType.getText().toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + preferences.getString("token", null).toString());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void responseResult() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);*/
            builder = new AlertDialog.Builder(this);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Success Message");
        builder.setMessage("Processed successfully");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent s = new Intent(EnumerationActivity.this, EnumerationActivity.class);
                startActivity(s);
            }
        }).show();
    }

    private void onLoginFailDialog(String msg) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);*/
            builder = new AlertDialog.Builder(this);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Error Message");
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
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

    private void onLoginFail() {
        Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
    }

    private boolean validate() {
        boolean valid = true;

        String sur = surname.getText().toString();
        String first = firstname.getText().toString();
        String middle = middlename.getText().toString();
        String phoneNumber = phone.getText().toString();
        String reg = region.getText().toString();
        String csps = csp.getText().toString();
        String meterNumber = meterNo.getText().toString();
        String meter = meterType.getText().toString();

        if (sur.isEmpty() || sur.length()<3){
            surname.setError("Enter a valid surname");
            valid=false;
        }else{
            surname.setError(null);
        }

        if (first.isEmpty() || first.length() < 3){
            firstname.setError("Enter a valid first name");
            valid=false;
        }else{
            firstname.setError(null);
        }

        if (middle.isEmpty() || middle.length() < 3){
            middlename.setError("Enter a valid middle name");
            valid=false;
        }else{
            middlename.setError(null);
        }

        if (phoneNumber.isEmpty() || phoneNumber.length() < 3){
            phone.setError("Enter a valid phone number");
            valid=false;
        }else{
            phone.setError(null);
        }

        if (reg.isEmpty() || reg.length() < 3){
            region.setError("Enter a valid region");
            valid=false;
        }else{
            region.setError(null);
        }

        if (csps.isEmpty()){
            csp.setError("Enter a valid cash service point");
            valid=false;
        }else{
            csp.setError(null);
        }

        if (meterNumber.isEmpty()){
            meterNo.setError("Meter Number can not be empty");
            valid=false;
        }else{
            meterNo.setError(null);
        }

        if (meter.isEmpty()){
            meterType.setError("Meter type can not be empty");
            valid=false;
        }else{
            meterType.setError(null);
        }

        return valid;
    }

    private void getEditText() {
        surname = (EditText) findViewById(R.id.input_surname);
        firstname = (EditText) findViewById(R.id.input_firstname);
        middlename = (EditText) findViewById(R.id.input_middlename);
        phone = (EditText) findViewById(R.id.input_phone);
        email = (EditText) findViewById(R.id.input_email1);
        region = (EditText) findViewById(R.id.input_region);
        csp = (EditText) findViewById(R.id.input_CSP);
        meterNo = (EditText) findViewById(R.id.input_meterNo);
        meterType = (EditText) findViewById(R.id.input_meterType);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        imageView  = (ImageView) findViewById(R.id.imageView);
    }

    private void setUpCollapsToolBar() {
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Added for feature use", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showOption(R.id.action_info);
                } else if (isShow) {
                    isShow = false;
                    hideOption(R.id.action_info);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        hideOption(R.id.action_info);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_info) {
            return true;
        }else if(id == R.id.action_logout){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().clear().commit();

            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }
}
