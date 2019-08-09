package com.example.bizcardandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = "AddUserActivity";
    private final String URL = "http://34.73.24.69/";
    private String idNumber;
    private EditText editTextCode;
    BottomNavigationView navView;
    Button buttonAdd;
    private long lastClickTime = 0;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_add:
                    return true;
                case R.id.navigation_profile:
                    finish();
                    return true;
                case R.id.navigation_contacts:
                    Intent intent =
                            new Intent(AddUserActivity.this, ContactView.class);
                    intent.putExtra("id",idNumber);
                    startActivity(intent);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Created");
        setContentView(R.layout.activity_add_user);
        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_add);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Intent passedIntent = getIntent();
        idNumber = passedIntent.getStringExtra("id");
        editTextCode = findViewById(R.id.editText_inputCode);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ButtonClicked");
                if(SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                String contactCode = editTextCode.getText().toString();
                addContact(contactCode.toUpperCase());
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navView.setOnNavigationItemSelectedListener(null);
        buttonAdd.setOnClickListener(null);
    }

    /**
     * Sends a volley POST request to add an another user to the main user's contacts.
     * @param code
     */
    private void addContact (String code)
    {
        Log.d(TAG, "addContact: Contact added");
        String postURL = URL + "api/user/" + idNumber + "/" + code +"/";

        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject jsonObject =
                                    new JsonParser().parse(response).getAsJsonObject();
                            if(jsonObject.get("success").getAsBoolean()) {
                                JsonObject jsonData = jsonObject.getAsJsonObject("data");
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(AddUserActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("Contact was added successfully");
                                builder.setMessage(jsonData.get("name").getAsString() +
                                        " is now in your contacts.");
                                builder.setNegativeButton("close",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            else {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(AddUserActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("Adding Contact Failed");
                                builder.setMessage("Something went wrong. Double check your code.");
                                builder.setNegativeButton("close",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                        catch (JsonParseException e) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(AddUserActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle("Server Error");
                            builder.setMessage("JSON failure");
                            builder.setNegativeButton("close",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(AddUserActivity.this);
                        builder.setCancelable(true);
                        String errorString, errorTitle, errorMsg = null;
                        if(error.networkResponse !=null) {
                            errorString = new String(error.networkResponse.data);
                            JsonObject errorJson =
                                    new JsonParser().parse(errorString).getAsJsonObject();
                            errorTitle = errorJson.get("data").getAsString();
                            if (errorTitle.equals("Duplicate User"))
                                errorMsg = "This user already in your contacts.";
                            if (errorTitle.equals("User or Contact not found"))
                                errorMsg = "This user does not exist, " +
                                        "please enter a different code.";
                        }
                        else {
                            errorTitle = "Unknown network error";
                            errorMsg = "An unknown error has occurred, please check your " +
                                    "connection and try again later.";
                        }
                        builder.setTitle(errorTitle);
                        builder.setMessage(errorMsg);
                        builder.setNegativeButton("close",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
        );
        MySingleton.getInstance(this).addToRequestQueue(postRequest);
    }
}
