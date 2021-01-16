package com.vishalperipherals.maps_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.vishalperipherals.maps_demo.Internet.NetworkConnection;
import com.vishalperipherals.maps_demo.Network.ApplicationRequest;
import com.vishalperipherals.maps_demo.Services.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BroadcastLoc extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        sendhttprequest(context);

    }


    private void sendhttprequest(Context context) {


        String url = "https://vishalperipherals.in/delivery/api/customer_update.php?";


        StringRequest jsonRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray jsonArray = jsonObject.getJSONArray("customer");

                    int length = jsonArray.length();

                    for (int index = 0; index < length; index++) {


                        JSONObject object = jsonArray.getJSONObject(index);

                        //  User item = new User();

                      /*  item.setCustomer_id(object.getString("customer_id"));
                        item.setCustomer_name(object.getString("customer_name"));
                        item.setCustomer_email(object.getString("customer_email"));
                        item.setCustomer_telephone(object.getString("customer_telephone"));

*/
                        Integer success = object.getInt("success");

                        if (success == 1) {

                        } else {

                            Toast.makeText(context, "Server return invalid response ", Toast.LENGTH_SHORT).show();


                        }

                        // iconSubCategoriesList.add(item);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    ;
                    Toast.makeText(context, "Invalid response from the server ", Toast.LENGTH_SHORT).show();


                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                Toast.makeText(context, "Something went wrong ", Toast.LENGTH_SHORT).show();

            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request
                params.put("action", "trackupdate");

                return params;
            }

        };

        int socketTimeout = 30000; //30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


        jsonRequest.setRetryPolicy(policy);

        ApplicationRequest.getInstance(context).addToRequestQueue(jsonRequest);

    }
}
