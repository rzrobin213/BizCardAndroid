package com.example.bizcardandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;



import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private final String url = "http://34.73.24.69/";
    private EditText editTextName;
    private EditText editTextPosition;
    private EditText editTextCompany;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private EditText editTextWebsite;
    private TextView textViewCodeVal;
    private RequestQueue requestQueue;
    private int savedUserNumber;
    public static String dataKey = "com.example.bizcardandroid.data_key";
    public static String firstTimeKey = "com.example.bizcardandroid.first_time_key";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            TextView codeVal = findViewById(R.id.textView_codeVals);
            String personCode = codeVal.getText().toString();
            switch (item.getItemId()) {
                case R.id.navigation_add:
                    Intent intent1 =
                            new Intent(MainActivity.this, AddUserActivity.class);
                    intent1.putExtra("code",personCode);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_profile:
                    return true;
                case R.id.navigation_contacts:
                    Intent intent2 =
                            new Intent(MainActivity.this, ContactView.class);
                    intent2.putExtra("code",personCode);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        editTextName = findViewById(R.id.editText_name);
        editTextCompany = findViewById(R.id.editText_Company);
        editTextPosition = findViewById(R.id.editText_Position);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPhone = findViewById(R.id.editText_phone);
        editTextWebsite = findViewById(R.id.editText_website);
        textViewCodeVal = findViewById(R.id.textView_codeVals);
        Button buttonUpdate = findViewById(R.id.buttonUpdateVal);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObject jsonData = new JsonObject();
                jsonData.addProperty("name", (editTextName.getText().toString()));
                jsonData.addProperty("email", (editTextEmail.getText().toString()));
                jsonData.addProperty("phone", (editTextPhone.getText().toString()));
                jsonData.addProperty("company", (editTextCompany.getText().toString()));
                jsonData.addProperty("position", (editTextPosition.getText().toString()));
                jsonData.addProperty("website", (editTextWebsite.getText().toString()));

                String data = jsonData.toString();

                String ext = "api/user/" + savedUserNumber + "/";
                UpdateValues(data, ext);
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("shared_pref",MODE_PRIVATE);
        boolean firstTime = sharedPref.getBoolean(firstTimeKey,true);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (firstTime) {
            createNewPerson();
            editor.putBoolean(firstTimeKey,false);
            editor.apply();
        }
        else {
            String savedID = sharedPref.getString(dataKey,"-1");
            if(strToInt(savedID) <= 0){
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Save Error");
                builder.setMessage("Please reenter data. Sorry");
                createNewPerson();
            }
            else {
                getPerson(savedID);
            }
        }

        navView.setSelectedItemId(R.id.navigation_profile);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPref = getSharedPreferences("shared_pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(dataKey, String.valueOf(savedUserNumber));
        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_profile);
    }

    private void getPerson(String code) {
        String getURL = url + "api/user/" + code + "/";
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest getRequest = new StringRequest(Request.Method.GET, getURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonObject jsonObject =
                                new JsonParser().parse(response).getAsJsonObject();
                        JsonObject jsonData = jsonObject.getAsJsonObject("data");
                        editTextName.setText(jsonData.get("name").getAsString());
                        editTextCompany.setText(jsonData.get("company").getAsString());
                        editTextPosition.setText(jsonData.get("position").getAsString());
                        editTextEmail.setText(jsonData.get("email").getAsString());
                        editTextPhone.setText(jsonData.get("phone").getAsString());
                        editTextWebsite.setText(jsonData.get("website").getAsString());
                        textViewCodeVal.setText(jsonData.get("code").getAsString());
                        savedUserNumber = jsonData.get("id").getAsInt();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(getRequest);


    }

    private void UpdateValues (String data, String ext)
    {
        final String saveData= data;
        String postURL = url + ext;

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject jsonObject =
                                    new JsonParser().parse(response).getAsJsonObject();
                            JsonObject jsonData = jsonObject.getAsJsonObject("data");
                            editTextName.setText(jsonData.get("name").getAsString());
                            editTextCompany.setText(jsonData.get("company").getAsString());
                            editTextPosition.setText(jsonData.get("position").getAsString());
                            editTextEmail.setText(jsonData.get("email").getAsString());
                            editTextPhone.setText(jsonData.get("phone").getAsString());
                            editTextWebsite.setText(jsonData.get("website").getAsString());
                            savedUserNumber = jsonData.get("id").getAsInt();
                        } catch (JsonParseException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Server Error",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.v("VOLLEY", error.toString());
                    }
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return saveData == null ? null : saveData.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding", saveData);
                    return null;
                }
            }

        };
        requestQueue.add(postRequest);
    }

    private void createNewPerson ()
    {
        final String data= "{}";
        String postURL = url + "api/users/";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject jsonObject =
                                    new JsonParser().parse(response).getAsJsonObject();
                            JsonObject jsonData = jsonObject.getAsJsonObject("data");
                            textViewCodeVal.setText(jsonData.get("code").getAsString());
                            savedUserNumber = jsonData.get("id").getAsInt();
                        }
                        catch (JsonParseException e) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle("Server Error");
                            builder.setMessage("JSON failure");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("Server Error");
                        builder.setMessage("Post Request Failed");
                    }
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return data == null ? null : data.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };
        requestQueue.add(postRequest);
    }

    /**
     * This method converts a string containing positive (not including zero) numeric values to an
     * int value. It returns the numeric value for strings containing positive numbers and -1 for
     * every other string.
     * @param numStr the string to be converted.
     * @return an int containing the numeric representation of the string or -1 for other cases.
     */
    public static int strToInt (String numStr) {
        if(numStr != null)
        {
            try {
                int result = Integer.parseInt(numStr);
                if (result > 0)
                    return result;
                else
                    return -1;

            }
            catch (NumberFormatException e) {
                return -1;
            }

        }
        return -1;

    }
}
