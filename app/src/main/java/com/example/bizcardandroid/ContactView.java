package com.example.bizcardandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContactView extends AppCompatActivity {

    private String idNumber;
    private RequestQueue requestQueue;
    private ListView listView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_add:
                    Intent intent =
                            new Intent(ContactView.this, AddUserActivity.class);
                    intent.putExtra("id", idNumber);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.navigation_profile:
                    finish();
                    return true;
                case R.id.navigation_contacts:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        Intent passedIntent = getIntent();
        idNumber = passedIntent.getStringExtra("id");

        navView.setSelectedItemId(R.id.navigation_contacts);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listView = findViewById(R.id.contactsListView);
        getPerson(idNumber);
    }



    private void getPerson(String code) {
        final String url = "http://34.73.24.69/";
        String getURL = url + "api/user/" + code + "/";
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest getRequest = new StringRequest(Request.Method.GET, getURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<String> names = new ArrayList<>();
                        String[] nameArray = new String[0];
                        JsonObject jsonObject =
                                new JsonParser().parse(response).getAsJsonObject();
                        JsonObject jsonData = jsonObject.getAsJsonObject("data");
                        JsonElement jsonContacts = jsonData.get("contacts");
                        if (jsonContacts.isJsonArray()) {
                            JsonArray contacts = jsonContacts.getAsJsonArray();
                            for(JsonElement elt : contacts) {
                                if (elt.isJsonObject()) {
                                    names.add(elt.getAsJsonObject().get("name").getAsString());
                                }
                            }
                        }
                        if(!names.isEmpty()) {
                            nameArray = names.toArray(new String[0]);
                        }
//                        if(nameArray == null) {
//                            nameArray = new String[10];
//                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ContactView.this,
                                android.R.layout.simple_list_item_1,nameArray);
                        listView.setAdapter(adapter);
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
}
