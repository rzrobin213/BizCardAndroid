package com.example.bizcardandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int PICK_IMAGE = 1;
    private final String URL = "http://34.73.24.69/";
    private NetworkImageView imageView;
    private EditText editTextName;
    private EditText editTextPosition;
    private EditText editTextCompany;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private EditText editTextWebsite;
    private TextView textViewCodeVal;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;
    private long lastClickTime = 0;

    private int savedUserNumber;

    public static String dataKey = "com.example.bizcardandroid.data_key";
    public static String firstTimeKey = "com.example.bizcardandroid.first_time_key";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            String personID = Integer.toString(savedUserNumber);
            switch (item.getItemId()) {
                case R.id.navigation_add:
                    Intent intent1 =
                            new Intent(MainActivity.this, AddUserActivity.class);
                    intent1.putExtra("id",personID);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_profile:
                    return true;
                case R.id.navigation_contacts:
                    Intent intent2 =
                            new Intent(MainActivity.this, ContactView.class);
                    intent2.putExtra("id",personID);
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
        imageView = findViewById(R.id.imageview_profile);
        requestQueue = Volley.newRequestQueue(this);

        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        editTextName = findViewById(R.id.editText_name);
        editTextCompany = findViewById(R.id.editText_Company);
        editTextPosition = findViewById(R.id.editText_Position);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPhone = findViewById(R.id.editText_phone);
        editTextWebsite = findViewById(R.id.editText_website);
        textViewCodeVal = findViewById(R.id.textView_codeVals);
        Button buttonImage = findViewById(R.id.button_new_image);
        Button buttonUpdate = findViewById(R.id.buttonUpdateVal);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
            navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            buttonImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: button image");
                    if(SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult
                            (Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            });
            buttonUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Update Button pressed");
                    if(SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                    JsonObject jsonData = new JsonObject();
                    jsonData.addProperty("name", (editTextName.getText().toString()));
                    jsonData.addProperty("email", (editTextEmail.getText().toString()));
                    jsonData.addProperty("phone", (editTextPhone.getText().toString()));
                    jsonData.addProperty("company", (editTextCompany.getText().toString()));
                    jsonData.addProperty("position",
                            (editTextPosition.getText().toString()));
                    jsonData.addProperty("website", (editTextWebsite.getText().toString()));

                    String data = jsonData.toString();

                    String ext = "api/user/" + savedUserNumber + "/";
                    updateValues(data, ext);
                }
            });
        }

        SharedPreferences sharedPref = getSharedPreferences("shared_pref", MODE_PRIVATE);
        boolean firstTime = sharedPref.getBoolean(firstTimeKey, true);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (firstTime) {
            Log.d(TAG, "onCreate: First Time");
            createNewPerson();
            editor.putBoolean(firstTimeKey, false);
            editor.apply();
        }
        else {
            String savedID = sharedPref.getString(dataKey, "-1");
            Log.d(TAG, "onCreate: got saved ID: " + savedID);
            if (strToInt(savedID) <= 0) {
                Log.d(TAG, "onCreate: SAVE ERROR");
                displayErrorMsg("Save error.", "Something went wrong with the saving " +
                        "your data. Please recreate your data.");
                editor.clear();
                editor.apply();
                createNewPerson();
            } else {
                getPerson(savedID);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: chosen image");
        if (requestCode == PICK_IMAGE) {
            Log.d(TAG, "onActivityResult: request code picked img");
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: result == result_ok");
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap =
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    String chosenImage = encodeToBase64(bitmap);
                    setImage(String.valueOf(savedUserNumber), chosenImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onActivityResult: Error",e);
                }
            }
        }
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
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: Called");
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }

    /**
     * Helper method that sends a volley POST request to the server and creates a new person with
     * default values. Then, it updates the layout to match the newly created person.
     */
    private void createNewPerson ()
    {
        Log.d(TAG, "createNewPerson: Called");
        final String data= "{}";
        String postURL = URL + "api/users/";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "onResponse: newPerson received");
                            JsonObject jsonObject =
                                    new JsonParser().parse(response).getAsJsonObject();
                            JsonObject jsonData = jsonObject.getAsJsonObject("data");
                            textViewCodeVal.setText(jsonData.get("code").getAsString());
                            savedUserNumber = jsonData.get("id").getAsInt();
                            imageView.setImageUrl
                                    (jsonData.get("imgURL").getAsString(),imageLoader);
                        }
                        catch (JsonParseException e) {
                            Log.d(TAG, "onResponse: JSON parse error when creating person");
                            displayErrorMsg(e.toString(), "JSON parse failure. Please" +
                                    "try again");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Create New Person Failed");
                        if(error.networkResponse != null) {
                            displayErrorMsg(new String(error.networkResponse.data),
                                    "A server error has occurred. Please try again later.");
                        }
                        else {
                            displayErrorMsg("An unknown error has occurred.",
                                    "Please recreate your data.");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return data.getBytes(StandardCharsets.UTF_8);
            }

        };
        requestQueue.add(postRequest);
    }

    /**
     * [getPerson] sends a volley GET request to the server to get the appropriate
     * data of a particular person. This person is identified with a code. If the request is
     * successful, it will set the layout according to the data received from the server. If the
     * request fails, it will display an error dialog to the user.
     * @param code the code of the person that is being fetched.
     */
    private void getPerson(String code) {
        Log.d(TAG, "getPerson: Called");
        String getURL = URL + "api/user/" + code + "/";
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest getRequest = new StringRequest(Request.Method.GET, getURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: Response received");
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
                        imageView.setImageUrl(jsonData.get("imgURL").getAsString(),imageLoader);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: getPerson Volley Error");
                        if(error.networkResponse != null) {
                            displayErrorMsg(new String(error.networkResponse.data),
                                    "A server error has occurred. Please try again later.");
                        }
                        else {
                            displayErrorMsg("An unknown error has occurred.",
                                    "Please recreate your data.");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                });
        requestQueue.add(getRequest);
    }

    /**
     * [updateValues] sends a volley POST request to the server with a body containing fields for
     * the server to update with new data. [data] represents the string equivalent of a JSON that
     * is to be sent to the server. The [ext] is the code representing the person it should be
     * updated.
     * @param data
     * @param ext
     */
    private void updateValues (String data, String ext)
    {
        Log.d(TAG, "updateValues: Called");
        final String saveData = data;
        String postURL = URL + ext;

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "onResponse: updatedValue received");
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
                            imageView.setImageUrl
                                    (jsonData.get("imgURL").getAsString(),imageLoader);
                            Toast.makeText(MainActivity.this,
                                    "Your profile has been updated.",
                                    Toast.LENGTH_SHORT).show();
                        } catch (JsonParseException e) {
                            Log.d(TAG, "onResponse: UpdatedValue JSON error ");
                            displayErrorMsg("Server Error","JSON parse error please try" +
                                    "again");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Volley error");
                        if(error.networkResponse != null) {
                            displayErrorMsg(new String(error.networkResponse.data),
                                    "A server error has occurred. Please try again later.");
                        }
                        else {
                            displayErrorMsg("An unknown error has occurred.",
                                    "Please recreate your data.");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return saveData == null ? null : saveData.getBytes(StandardCharsets.UTF_8);
            }

        };
        requestQueue.add(postRequest);
    }

    /**
     * [SetImage] sends a volley POST request to the server with a body that contains a base64
     * encoding of an image. Then, this sets the image of a person (whose id is [code]) to the newly
     * updated image.
     * @param code the code of the person that is having their image changed.
     * @param image a base64 encoding of an image to be sent to the server.
     */
    private void setImage(String code, String image) {
        Log.d(TAG, "setImage: Called");
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("img",image);
        final String data= jsonData.toString();
        final String postURL = URL + "api/images/" + code + "/";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, postURL + "string/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "onResponse: image received");
                            Toast.makeText
                                    (MainActivity.this, "Image successfully uploaded",
                                            Toast.LENGTH_SHORT).show();
                            imageView.setImageUrl(postURL,imageLoader);
                        }
                        catch (JsonParseException e) {
                            Log.d(TAG, "onResponse: JSON parse error when setting image");
                            displayErrorMsg(e.toString(), "JSON parse failure. Please" +
                                    "try again");
                            SharedPreferences sharedPref =
                                    getSharedPreferences("shared_pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: image posting Failed");
                        displayErrorMsg("An error occurred while uploading the image.",
                                "Post request failed please try again.");
                    }
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return data.getBytes(StandardCharsets.UTF_8);
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
    public int strToInt (String numStr) {
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

    /**
     * [displayErrorMsg] is a helper method to display an error message on a pop-up alert dialog
     * with [title] set as the title and [msg] set as the message.
     * @param title a string that becomes the title of the dialog.
     * @param msg a string that becomes the body of the dialog.
     */
    private void displayErrorMsg(String title, String msg) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(msg);
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

    /**
     * [encodeToBase64] is a helper method that encodes a Bitmap [image] to its base 64 String
     * representation
     * @param image the bitmap image that is converted.
     * @return the String representation of the bitmap image.
     */
    public static String encodeToBase64(Bitmap image)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
