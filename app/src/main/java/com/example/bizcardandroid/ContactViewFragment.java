package com.example.bizcardandroid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ContactViewFragment extends Fragment {

    private static final String TAG = "ContactViewFragment";
    private NetworkImageView imageView;
    private TextView textName;
    private TextView textPosition;
    private TextView textCompany;
    private TextView textPhone;
    private TextView textEmail;
    private TextView textWebsite;
    private TextView textViewCodeVal;
    private ImageLoader imageLoader;


    public ContactViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_contact_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: getArgs");
        assert getArguments() != null;
        int id = getArguments().getInt("id");
        Log.d(TAG, "onCreateView: " + id);
        imageView = view.findViewById(R.id.imageview_profile);
        textName = view.findViewById(R.id.textView_NameVal);
        textPosition = view.findViewById(R.id.textView_PositionVal);
        textCompany = view.findViewById(R.id.textView_CompanyVal);
        textPhone = view.findViewById(R.id.textView_PhoneVal);
        textEmail = view.findViewById(R.id.textView_EmailVal);
        textWebsite = view.findViewById(R.id.textView_WebsiteVal);
        textViewCodeVal = view.findViewById(R.id.textView_codeVal);
        imageLoader = MySingleton.getInstance(this.getActivity()).getImageLoader();
        getPerson(String.valueOf(id));
    }


    private void getPerson(String id) {
        Log.d(TAG, "getPerson: Called");
        final String URL = "http://34.73.24.69/";
        String getURL = URL + "api/user/" + id + "/";
        StringRequest getRequest = new StringRequest(Request.Method.GET, getURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: Response received");
                        JsonObject jsonObject =
                                new JsonParser().parse(response).getAsJsonObject();
                        JsonObject jsonData = jsonObject.getAsJsonObject("data");
                        textName.setText(jsonData.get("name").getAsString());
                        textCompany.setText(jsonData.get("company").getAsString());
                        textPosition.setText(jsonData.get("position").getAsString());
                        textEmail.setText(jsonData.get("email").getAsString());
                        textPhone.setText(jsonData.get("phone").getAsString());
                        textWebsite.setText(jsonData.get("website").getAsString());
                        textViewCodeVal.setText(jsonData.get("code").getAsString());
                        imageView.setImageUrl(jsonData.get("imgURL").getAsString(),imageLoader);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ERROR" );
                    }
                });
        MySingleton.getInstance(this.getActivity()).addToRequestQueue(getRequest);
    }
}
