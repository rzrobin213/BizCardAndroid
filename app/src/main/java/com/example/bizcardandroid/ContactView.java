package com.example.bizcardandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ContactView extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ContactView";
    private String idNumber;
    private BottomNavigationView navView;
    private RecyclerView recyclerView;
    private Spinner spinner;
    private ArrayList<Contact> recentlyAddedContactList;

    RecyclerViewAdapter.OnClickItemListener listener =
            new RecyclerViewAdapter.OnClickItemListener() {
        @Override
        public void onClickItem(Contact item) {
            Bundle bundle = new Bundle();
            bundle.putInt("id", item.getId());
            Log.d(TAG, "onClickItem: ID placed in Bundle" + item.getId());
            ContactViewFragment frag = new ContactViewFragment();
            Log.d(TAG, "onClickItem: created frag");
            frag.setArguments(bundle);
            Log.d(TAG, "onClickItem: setargs");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Log.d(TAG, "onClickItem: transaction set");
            transaction.replace(R.id.placeholder, frag);
            Log.d(TAG, "onClickItem: replaced");
            transaction.commit();
            Log.d(TAG, "onClickItem: committed");
            Toast.makeText(ContactView.this, item.getName(), Toast.LENGTH_SHORT).show();
        }
    };


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
        Log.d(TAG, "onCreate: created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);
        navView = findViewById(R.id.nav_view);
        Intent passedIntent = getIntent();
        idNumber = passedIntent.getStringExtra("id");

        navView.setSelectedItemId(R.id.navigation_contacts);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        recyclerView = findViewById(R.id.contactsRecyclerView);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_array, R.layout.spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        getPerson(idNumber);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        navView.setOnNavigationItemSelectedListener(null);
        spinner.setOnItemSelectedListener(null);
    }

    private void getPerson(String code) {
        String getURL = MySingleton.URL + "api/user/" + code + "/";
        StringRequest getRequest = new StringRequest(Request.Method.GET, getURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: Request Sent");
                        ArrayList<Contact> people = new ArrayList<>();
                        JsonObject jsonObject =
                                new JsonParser().parse(response).getAsJsonObject();
                        JsonObject jsonData = jsonObject.getAsJsonObject("data");
                        JsonElement jsonContacts = jsonData.get("contacts");
                        if (jsonContacts.isJsonArray()) {
                            JsonArray contacts = jsonContacts.getAsJsonArray();
                            int counter = 0;
                            for(JsonElement elt : contacts) {
                                if (elt.isJsonObject()) {
                                    Log.d(TAG, "onResponse: is JsonObject");
                                    String name = elt.getAsJsonObject().get("name").getAsString();
                                    String company =
                                            elt.getAsJsonObject().get("company").getAsString();
                                    String imgURL =
                                            elt.getAsJsonObject().get("imgURL").getAsString();
                                    int id =
                                            elt.getAsJsonObject().get("id").getAsInt();
                                    Contact c = new Contact(name,company,imgURL,counter,id);
                                    counter++;
                                    people.add(c);
                                }
                            }
                        }
                        Log.d(TAG, "onResponse: " + people.toString());
                        recentlyAddedContactList = people;
                        Collections.sort(people, new SortName());
                        RecyclerViewAdapter adapter =
                                new RecyclerViewAdapter
                                        (ContactView.this, people,listener);
                        recyclerView.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(ContactView.this);
                            builder.setCancelable(true);
                            builder.setTitle(new String(error.networkResponse.data));
                            builder.setMessage("A server error has occurred. " +
                                    "Please try again later.");
                            builder.setNegativeButton("close",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(ContactView.this);
                            builder.setCancelable(true);
                            builder.setTitle("An unknown error has occurred");
                            builder.setMessage("Please try again later.");
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
        });
        MySingleton.getInstance(this).addToRequestQueue(getRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextSize(14);
        ArrayList<Contact> temp = recentlyAddedContactList;
        RecyclerViewAdapter adapter;
        if (temp != null) {
            switch (position) {
                case 0 :
                    Log.d(TAG, "onItemSelected: Name in order selected" + position);
                    Collections.sort(temp,new SortName());
                    adapter = new RecyclerViewAdapter(ContactView.this, temp,listener );
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onItemSelected: Adapter Set");
                    break;
                case 1:
                    Log.d(TAG, "onItemSelected: Name reverse selected" + position);
                    Collections.sort(temp,new SortName());
                    Collections.reverse(temp);
                    adapter = new RecyclerViewAdapter(ContactView.this, temp,listener);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onItemSelected: Adapter Set");
                    break;
                case 2:
                    Log.d(TAG, "onItemSelected: Company in order selected" + position);
                    Collections.sort(temp,new SortCompany());
                    adapter = new RecyclerViewAdapter(ContactView.this, temp,listener);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onItemSelected: Adapter Set");
                    break;
                case 3:
                    Log.d(TAG, "onItemSelected: Company reverse selected" + position);
                    Collections.sort(temp,new SortCompany());
                    Collections.reverse(temp);
                    adapter = new RecyclerViewAdapter(ContactView.this, temp,listener);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onItemSelected: Adapter Set");
                    break;
                case 4:
                    Log.d(TAG, "onItemSelected: Recently added selected" + position);
                    Collections.sort(temp, new SortRecent());
                    adapter = new RecyclerViewAdapter(ContactView.this, temp,listener);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "onItemSelected: Adapter Set");
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

class SortName implements Comparator<Contact> {

    @Override
    public int compare(Contact o1, Contact o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

class SortCompany implements Comparator<Contact> {

    @Override
    public int compare(Contact o1, Contact o2) {
        return o1.getCompany().compareTo(o2.getCompany());
    }
}

class SortRecent implements Comparator<Contact> {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int compare(Contact o1, Contact o2) {
        return Integer.compare(o2.getOrder(), o1.getOrder());
    }
}
