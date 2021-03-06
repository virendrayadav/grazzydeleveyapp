package delivery.grazzy.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by developer.nithin@gmail.com
 */
public class Track extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

 
    String  dis_trav = "";
    Boolean show_current_location=false;
    

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    TextView pickup_time, pickup_address, order_no, delivery_time, delivery_address, passcode, total_cost_current_order, back, picked_up, verify, details;
    ImageButton call_restaurant, call_customer, customer_photo,my_location;

    Location destination_location, source_location;

    DirectionsJSONParser parser;

    Boolean drawn = false;

    LayoutInflater layoutInflater;

    RelativeLayout delivery_address_bar, restaurant_address_bar;

    Marker marker;

//    SimpleDateFormat sdf;
    SimpleDateFormat sdf_changed;
    Date date = null;
    SimpleDateFormat newsdf;

    Dialog loading, photo_pop_up;

    int CALL_PHONE_permission;
    int currentapiVersion;

    StringRequest update_order_status, get_user_pic,get_delivery_boy_location,insert_location_into_db;

    LocationManager manager = null;

    NetworkImageView user_pic;
    Calendar calendar;
    Date d = null;

    Handler handler = null;
    Runnable t;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = true;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener locationListener;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 3000; // 3 sec
    private static int FATEST_INTERVAL = 3000; // 3 sec
    private static int DISPLACEMENT = 10; // 10 meters



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("track","v7");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
            window.setStatusBarColor(Color.parseColor(getString(R.string.my_statusbar_color)));
        }

        setContentView(R.layout.track);

        calendar = Calendar.getInstance();

        CALL_PHONE_permission = ContextCompat.checkSelfPermission(Track.this,
                "android.permission.CALL_PHONE_permission");

        currentapiVersion = android.os.Build.VERSION.SDK_INT;

        loading = new Dialog(Track.this);
        loading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading.setContentView(R.layout.loading);

        photo_pop_up = new Dialog(Track.this);
        photo_pop_up.requestWindowFeature(Window.FEATURE_NO_TITLE);
        photo_pop_up.setContentView(R.layout.list_popup);


//        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newsdf = new SimpleDateFormat("hh:mm a ");
        sdf_changed = new SimpleDateFormat("HH:mm:ss");

        sdf_changed.setTimeZone(TimeZone.getDefault());
//        sdf.setTimeZone(TimeZone.getDefault());
        newsdf.setTimeZone(TimeZone.getDefault());

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        user_pic = (NetworkImageView) photo_pop_up.findViewById(R.id.user_pic);
        user_pic.setDefaultImageResId(R.drawable.user);

        destination_location = new Location("destination_location");
        source_location = new Location("source_location");

        pickup_time = (TextView) findViewById(R.id.pickup_time);
        pickup_address = (TextView) findViewById(R.id.pickup_address);
        order_no = (TextView) findViewById(R.id.order_no);
        delivery_time = (TextView) findViewById(R.id.delivery_time);
        delivery_address = (TextView) findViewById(R.id.delivery_address);
        passcode = (TextView) findViewById(R.id.passcode);
        total_cost_current_order = (TextView) findViewById(R.id.total_cost_current_order);

        back = (TextView) findViewById(R.id.back);
        picked_up = (TextView) findViewById(R.id.picked_up);
        verify = (TextView) findViewById(R.id.verify);
        details = (TextView) findViewById(R.id.details);

        back.setOnClickListener(this);
        picked_up.setOnClickListener(this);
        verify.setOnClickListener(this);
        details.setOnClickListener(this);

        call_restaurant = (ImageButton) findViewById(R.id.call_restaurant);
        call_customer = (ImageButton) findViewById(R.id.call_customer);
        customer_photo = (ImageButton) findViewById(R.id.customer_photo);
        my_location = (ImageButton)findViewById(R.id.my_location);

        call_restaurant.setOnClickListener(this);
        call_customer.setOnClickListener(this);


        try {
            d = sdf_changed.parse(AppController.getInstance().delivered_on);
            calendar.setTime(d);


        } catch (Exception e) {
            Log.e("ParseException A", e.toString());
        }

        pickup_time.setText("Pick-up : " + ( newsdf.format(calendar.getTimeInMillis()-Integer.parseInt(AppController.getInstance().preparation_time)*60*1000)));
        pickup_address.setText(AppController.getInstance().restaurant_address);
        order_no.setText("# " + AppController.getInstance().order_number);
        try {
            delivery_time.setText("Deliver : " + newsdf.format((Date) sdf_changed.parse(AppController.getInstance().delivered_on)));
        } catch (Exception e) {
            Log.e("Exception B", e.toString());
        }
        delivery_address.setText(AppController.getInstance().delivery_location);
        passcode.setText("Passcode : " + AppController.getInstance().passcode);
        total_cost_current_order.setText("Rs. " + AppController.getInstance().total_cost);

        delivery_address_bar = (RelativeLayout) findViewById(R.id.delivery_address_bar);
        restaurant_address_bar = (RelativeLayout) findViewById(R.id.restaurant_address_bar);


        customer_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                get_user_pic();
            }
        });

        my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if(mLastLocation!=null)
                    {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Float.parseFloat(mLastLocation.getLatitude() + ""), Float.parseFloat(mLastLocation.getLongitude() + "")), ((mMap.getMaxZoomLevel() + mMap.getMinZoomLevel()) / 2) + ((mMap.getMaxZoomLevel() + mMap.getMinZoomLevel()) / 4)));

                        show_current_location=false;
                    }else{

                        Toast toast = Toast.makeText(Track.this," Fetching Location... ",Toast.LENGTH_LONG);
                        toast.getView().setBackgroundResource(R.color.colorPrimary);
                        toast.show();

                        show_current_location=true;
                    }

                } else {
                    buildAlertMessageNoGps();
                }

            }
        });

        call_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + AppController.getInstance().restaurant_phone));
                if (ActivityCompat.checkSelfPermission(Track.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);

            }
        });


        call_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + AppController.getInstance().phone));
                if (ActivityCompat.checkSelfPermission(Track.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);


            }
        });

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }

        if (checkPlayServices()) {
            // Building the GoogleApi client

            Log.e("checkPlayServices","was true");
            buildGoogleApiClient();

            createLocationRequest();
        }
        
        locationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


                Log.e("location", "changed");
                Log.e("location", location.toString());
                mLastLocation = location;

                if (mLastLocation != null) {

                    if(show_current_location)
                    {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Float.parseFloat(mLastLocation.getLatitude() + ""), Float.parseFloat(mLastLocation.getLongitude() + "")), ((mMap.getMaxZoomLevel() + mMap.getMinZoomLevel()) / 2) + ((mMap.getMaxZoomLevel() + mMap.getMinZoomLevel()) / 4)));

                        show_current_location=false;
                        insert_location_into_db();
                    }


                }

            }

        };

        Log.e("verify","enabled : "+AppController.getInstance().status.contains("Picked Up"));

        if(AppController.getInstance().status.contains("Picked Up"))
        {
            verify.setEnabled(true);
        }

    }


    private void insert_location_into_db() {

        insert_location_into_db = new StringRequest(Request.Method.POST,getString(R.string.base_url)+getString(R.string.addlocation),

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {



                        Log.e("location insert", "" +response);




                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {


            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                // TODO Auto-generated method stub

                Map<String, String> params = new HashMap<String, String>();

                params.put("deliveryboy_id",""+AppController.getInstance().sharedPreferences.getString("id", ""));
                params.put("latitude",""+mLastLocation.getLatitude());
                params.put("langitude",""+mLastLocation.getLongitude());

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        AppController.getInstance().getRequestQueue().add(insert_location_into_db);
    }
    

    private void get_user_pic() {

        loading.show();

        get_user_pic = new StringRequest(Request.Method.POST,getString(R.string.base_url)+getString(R.string.Profilepicture) ,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        loading.dismiss();

                        Log.e("response", "" +response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String url;

                            url = jsonObject.get("url").toString();


                        } catch (JSONException e) {
                            Log.e("JSONException", "" +e.toString());
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                String error_msg="";

                Log.e("error", "" + volleyError.toString());

                loading.dismiss();

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError)
                {
                    error_msg="No Internet Connection";

                } else if (volleyError instanceof AuthFailureError)
                {
                    error_msg="Error Occured, Please try later" ;

                } else if (volleyError instanceof ServerError)
                {
                    error_msg="Server Error, Please try later";

                } else if (volleyError instanceof NetworkError)
                {
                    error_msg="Network Error, Please try later";

                } else if (volleyError instanceof ParseError)
                {
                    error_msg="Error Occured, Please try later";
                }

                Toast.makeText(Track.this, error_msg, Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                // TODO Auto-generated method stub

                Map<String, String> params = new HashMap<String, String>();

                params.put("order_id ", AppController.getInstance().id+"");

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        AppController.getInstance().getRequestQueue().add(get_user_pic);
    }


    protected void makeRequest() {
        ActivityCompat.requestPermissions(Track.this,
                new String[]{"android.permission.CALL_PHONE_permission"},
                10);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
           }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e("onMapReady","onMapReady");

        mMap = googleMap;

        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }else
        {
            mMap.setMyLocationEnabled(true);
            locationButton.setVisibility(View.GONE);
        }


        destination_location.setLatitude(Double.parseDouble(AppController.getInstance().shipping_lat));
        destination_location.setLongitude(Double.parseDouble(AppController.getInstance().shipping_long));


        source_location.setLatitude(Double.parseDouble(AppController.getInstance().restaurant_latitude));
        source_location.setLongitude(Double.parseDouble(AppController.getInstance().restaurant_langitude));


        Log.e("destination_location",""+destination_location.toString());
        Log.e("source_location",""+source_location.toString());


        loading.show();

        String url = getDirectionsUrl(new LatLng(source_location.getLatitude(), source_location.getLongitude()), new LatLng(destination_location.getLatitude(), destination_location.getLongitude()));
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

        if(!ConnectivityReceiver.isConnected())
        {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();

        }else
        {

        }

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private void start_tracking() {

        handler = new Handler();
        t = new Runnable() {
            @Override
            public void run() {
                get_customer_location();
               
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(t, 0);


    }


    private void get_customer_location() {

        get_delivery_boy_location = new StringRequest(getString(R.string.base_url) + getString(R.string.customerlocation)+AppController.getInstance().customer_id,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("get_customer_location",response);

                            try {

                                JSONObject jsonObject = new JSONObject(response);
//                                Double.parseDouble(jsonObject.getString("latitude")),Double.parseDouble(jsonObject.getString("langitude"))


                               if(marker!=null)
                               {
                                   marker.remove();
                               }

                                View customMarkerView;
                                TextView time;
                                Bitmap returnedBitmap;
                                Canvas canvas;
                                Drawable drawable;

                                customMarkerView = layoutInflater.inflate(R.layout.customer_marker, null);
                                time = (TextView) customMarkerView.findViewById(R.id.time);
                                time.setText("Customer Location");


                                customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                                customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
                                customMarkerView.buildDrawingCache();
                                returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                                        Bitmap.Config.ARGB_8888);
                                canvas = new Canvas(returnedBitmap);
                                canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                                drawable = customMarkerView.getBackground();
                                if (drawable != null)
                                    drawable.draw(canvas);
                                customMarkerView.draw(canvas);


                                marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(jsonObject.getString("latitude")),Double.parseDouble(jsonObject.getString("langitude"))))
                                        .icon(BitmapDescriptorFactory.fromBitmap(returnedBitmap)));



                            } catch (Exception e) {

                                Log.e("getdeliveryboylocation", "" + e.toString());
                            }
                        }



                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {


                    String error_msg = "";

                    Log.e("error", "" + volleyError.toString());

                    loading.dismiss();

                    if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                        error_msg = "No Internet Connection";

                    } else if (volleyError instanceof AuthFailureError) {
                        error_msg = "Error Occured, Please try later";

                    } else if (volleyError instanceof ServerError) {
                        error_msg = "Server Error, Please try later";

                    } else if (volleyError instanceof NetworkError) {
                        error_msg = "Network Error, Please try later";

                    } else if (volleyError instanceof ParseError) {
                        error_msg = "Error Occured, Please try later";
                    }


                    Toast toast = Toast.makeText(Track.this," "+error_msg+" ",Toast.LENGTH_LONG);
                    toast.getView().setBackgroundResource(R.color.colorPrimary);
                    toast.show();
                }


        })  ;

        AppController.getInstance().getRequestQueue().add(get_delivery_boy_location);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause", "onPause");

        if (handler != null) {
            handler.removeCallbacks(t);
        }

        stopLocationUpdates();
    }

    protected void createLocationRequest() {
//        Log.e("createLocationRequest","createLocationRequest");


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {

//        Log.e("buildGoogleApiClient","buildGoogleApiClient");


        mGoogleApiClient = new GoogleApiClient.Builder(Track.this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
//        Log.e("checkPlayServices","checkPlayServices");


        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(Track.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,Track.this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(Track.this, "This device is not supported.", Toast.LENGTH_LONG).show();

            }
            return false;
        }
        return true;
    }

    protected void stopLocationUpdates() {
        Log.e("stopLocationUpdates","stopLocationUpdates");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("onConnectionFailed",  connectionResult.getErrorCode()+"");

    }



    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location

        Log.e("onConnected","onConnected");

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.e("onConnectionSuspended","onConnectionSuspended");

        mGoogleApiClient.connect();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            Log.e("onStart","connect");

        }

    }


    private void update_order_status(final String status_value) {

        loading.show();
        update_order_status = new StringRequest(Request.Method.POST, getString(R.string.base_url) + getString(R.string.changestatus),

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("response", "" + response);

                        if (response.contains("success")) {

                            if(status_value.contains("Picked Up"))
                            {
                                verify.setEnabled(true);
                                AppController.getInstance().status="Picked Up";
                                AppController.getInstance().changed=true;
                                startActivity(new Intent(Track.this,Feedback.class).putExtra("to","Restaurant"));

                            }else if(status_value.contains("Shipped"))
                            {
                                AppController.getInstance().status="Shipped";
                                AppController.getInstance().changed=true;
                                startActivity(new Intent(Track.this,Feedback.class).putExtra("to","Customer"));

                            }

                        }

                        loading.dismiss();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                String error_msg = "";

                Log.e("error", "" + volleyError.toString());

                loading.dismiss();

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                    error_msg = "No Internet Connection";

                } else if (volleyError instanceof AuthFailureError) {
                    error_msg = "Error Occured, Please try later";

                } else if (volleyError instanceof ServerError) {
                    error_msg = "Server Error, Please try later";

                } else if (volleyError instanceof NetworkError) {
                    error_msg = "Network Error, Please try later";

                } else if (volleyError instanceof ParseError) {
                    error_msg = "Error Occured, Please try later";
                }

                Toast.makeText(Track.this, error_msg, Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // TODO Auto-generated method stub

                Map<String, String> params = new HashMap<String, String>();


                params.put("id", "" + AppController.getInstance().id);
                params.put("status", status_value);
                params.put("distance", dis_trav);

                Log.e("params", "" + params.toString());

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        AppController.getInstance().getRequestQueue().add(update_order_status);


    }



    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.back:finish();
                break;

            case R.id.details:
                if(delivery_address_bar.getVisibility()==View.VISIBLE)
                {
                    delivery_address_bar.setVisibility(View.GONE);
                    restaurant_address_bar.setVisibility(View.GONE);
                }else
                {
                    delivery_address_bar.setVisibility(View.VISIBLE);
                    restaurant_address_bar.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.picked_up:
                update_order_status("Picked Up");

                break;

            case R.id.verify:
                update_order_status("Shipped");

                break;

            default:
                break;
        }

    }



    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service

                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.e("Background Task", e.toString());
                loading.dismiss();
            }

//            Log.e("data",data);

            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;


            try {
                jObject = new JSONObject(jsonData[0]);
                parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                dis_trav = parser.get_dist_trav();

//                Log.e("jObject", jObject.toString());

            } catch (Exception e) {

                e.printStackTrace();
                loading.dismiss();
            }

//            Log.e("routes",routes.toString());

            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {



            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            PolylineOptions lineOptions = null;
            lineOptions = new PolylineOptions();

            View customMarkerView;
            TextView time;
            Bitmap returnedBitmap;
            Canvas canvas;
            Drawable drawable;

            customMarkerView = layoutInflater.inflate(R.layout.custom_restaurant, null);
            time = (TextView) customMarkerView.findViewById(R.id.time);
            time.setText(AppController.getInstance().restaurant_name + " \n" + ( newsdf.format(calendar.getTimeInMillis()-Integer.parseInt(AppController.getInstance().preparation_time)*60*1000)));


            customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
            customMarkerView.buildDrawingCache();
            returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            canvas = new Canvas(returnedBitmap);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
            drawable = customMarkerView.getBackground();
            if (drawable != null)
                drawable.draw(canvas);
            customMarkerView.draw(canvas);


            Log.e("source","while drawing"+source_location.toString());

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(source_location.getLatitude(),source_location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(returnedBitmap)));





            customMarkerView = layoutInflater.inflate(R.layout.custom_marker, null);
            time = (TextView) customMarkerView.findViewById(R.id.time);


            try {
                time.setText(AppController.getInstance().firstname + " \n" + newsdf.format((Date) sdf_changed.parse(AppController.getInstance().delivered_on)));
            } catch (Exception e) {
                Log.e("date parse error C", e.toString());
            }


            customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
            customMarkerView.buildDrawingCache();
            returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            canvas = new Canvas(returnedBitmap);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
            drawable = customMarkerView.getBackground();
            if (drawable != null)
                drawable.draw(canvas);
            customMarkerView.draw(canvas);



//            try {
//                time.setText("" + newsdf.format((Date) sdf.parse(AppController.getInstance().delivered_on)) + "\n" + "Delivery Location");
//            } catch (Exception e) {
//                Log.e("date parse error", e.toString());
//            }

            Log.e("dest","while drawing"+destination_location.toString());

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(destination_location.getLatitude(),destination_location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(returnedBitmap)));


            lineOptions.color(Color.parseColor("#008000"));

            ArrayList<LatLng> points = null;


            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();


                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                    builder.include(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);

            }

            // Drawing polyline in the Google Map for the i-th route


            mMap.addPolyline(lineOptions);


            builder.include(new LatLng(source_location.getLatitude(),source_location.getLongitude()));
            builder.include(new LatLng(destination_location.getLatitude(), destination_location.getLongitude()));


            LatLngBounds bounds = builder.build();


            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

            Log.e("bound", 200 + "");

            drawn=true;

            loading.dismiss();

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.e("Exception ", "while downloading url" + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    protected void startLocationUpdates() {

//        Log.e("startLocationUpdates","startLocationUpdates");


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
        }

    }



    @Override
    public void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if (mMap == null) {
            mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);

        }

        if(AppController.getInstance().status.equals("Picked Up"))
        {
            picked_up.setVisibility(View.INVISIBLE);

        }

        if(AppController.getInstance().status.equals("Shipped"))
        {
            picked_up.setVisibility(View.INVISIBLE);
            verify.setVisibility(View.INVISIBLE);
        }

        Log.e("order_type",AppController.getInstance().order_type);

       if(AppController.getInstance().order_type.contains("1"))
       {
           Log.e("order_type","true");
           start_tracking();
       }

    }

    private void buildAlertMessageNoGps() {
        // TODO Auto-generated method stub


        final AlertDialog.Builder builder = new AlertDialog.Builder(Track.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                dialog.cancel();

            }
        });
        final AlertDialog alert = builder.create();
        alert.show();

    }

}