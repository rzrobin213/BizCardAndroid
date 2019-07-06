package com.example.bizcardandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class AddUserActivity extends AppCompatActivity {

    private final String url = "http://34.73.24.69/";
    private String idNumber;
    private EditText editTextCode;
    private RequestQueue requestQueue;


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
        setContentView(R.layout.activity_add_user);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_add);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Intent passedIntent = getIntent();
        idNumber = passedIntent.getStringExtra("id");
        editTextCode = findViewById(R.id.editText_inputCode);
        Button buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactCode = editTextCode.getText().toString();
                addContact(contactCode);
            }
        });

    }

    private void addContact (String code)
    {
        String postURL = url + "api/user/" + idNumber + "/" + code +"/";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
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
                        builder.setTitle("Server Error");
                        builder.setMessage("Something Went Wrong! Please check the code to make" +
                                "sure it is not a  duplicate.");
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
        requestQueue.add(postRequest);
    }


}
