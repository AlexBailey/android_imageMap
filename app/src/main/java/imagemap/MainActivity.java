package com1032.cw2.ab00631.ab00631_assignment2;

import android.app.*;
import android.content.*;
import android.location.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {


    // These are initiating objects
    private GoogleMap map;
    private LatLng myLocation;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private InputStream stream = null;
    private Button button;
    String stringUri;
    String longitude;
    String latitude;
    float latitudeFloat;
    float longitudeFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("inside oncreate","at the beggining");

        //initialising the variables
        button = (Button) findViewById(R.id.button);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        stringUri = fileUri.toString();
        button = (Button) findViewById(R.id.button);

        // start the image capture Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        Log.d("oncreate, file uri is", fileUri.toString());
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //this loads up the map
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.the_map);
        mf.getMapAsync(this);                  // calls onMapReady when loaded
        Log.d("inside oncreate"," at the end");
    }

    //this gets the latitude and longitude of images
    private LatLng getLongLat(){
        String latitudeREF;
        String longitudeREF;

        ExifInterface exif = null;
        try {
            Log.d("the uri in getLongLat: ", stringUri);
            exif = new ExifInterface(fileUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //initialising location variables
        longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        longitudeREF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        latitudeREF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);



        Log.d("current longitude is ", "" + longitude);
        Log.d("current longitude is", "" + longitude);

        if (latitudeREF.equals("N")){
            latitudeFloat = convertToDegree(latitude);
        } else {
            latitudeFloat = 0 - convertToDegree(latitude);
        }

        if (longitudeREF.equals("E")){
            longitudeFloat = convertToDegree(longitude);
        } else {
            longitudeFloat = 0 - convertToDegree(longitude);
        }

        return new LatLng(latitudeFloat, longitudeFloat);
    }


    //converts previous latitude and longitude outputs to a readable version that's able to be mapped
    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    };

    //This is the button listener method that opens the camera
    private void selectImage() {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                Log.d("oncreate, file uri is", fileUri.toString());
                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        fileUri.getPath(), Toast.LENGTH_LONG).show();
                try {

                    stream = getContentResolver().openInputStream(fileUri);
                    Log.d("my file uri is ", "" + fileUri.getPath().toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "result cancelled", Toast.LENGTH_LONG).show();
            // User cancelled the image capture
        } else {
            Toast.makeText(this, "result failed", Toast.LENGTH_LONG).show();
            // Image capture failed, advise user
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }



    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    //saves the fileUri
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putParcelable("path", fileUri);
        System.out.println(fileUri);
    }


    // initiates the map
    @Override
    public void onMapReady(GoogleMap map) {    // map is loaded but not laid out yet
        this.map = map;
        map.setOnMapLoadedCallback(this);      // calls onMapLoaded when layout done
        Log.d("inside onMapReady", "at the start");
    }

    //displays the marker on the map to where the photo was just taking using the photos EXIF data to get the location
    @Override
    public void onMapLoaded() {


        // code to run when the map has loaded
        //readCities();
        Log.d("inside onmaploaded", "at the start");

        // read user's current location, if possible
        myLocation = getLongLat();
        String longString = String.valueOf(myLocation.longitude);
        String latString = String.valueOf(myLocation.latitude);

        Log.d("in onmap loaded", "" + myLocation.longitude + " " + myLocation.latitude);
        if (myLocation == null) {
            Toast.makeText(this, "Unable to access your location. Consider enabling Location in your device's Settings.", Toast.LENGTH_LONG).show();
        } else {

            map.addMarker(new MarkerOptions()
                            .title("You took that photo here!")
                            .position(myLocation)
                            .snippet("Longitude: " + longitudeFloat + "   " + "Latitude: " + latitudeFloat));
        }
    }


    /*
     * Returns the user's current location as a LatLng object.
     * Returns null if location could not be found (such as in an AVD emulated virtual device).
     */
    private LatLng getMyLocation() {
        // try to get location three ways: GPS, cell/wifi network, and 'passive' mode
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            // fall back to network if GPS is not available
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (loc == null) {
            // fall back to "passive" location if GPS and network are not available
            loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (loc == null) {
            return null;   // could not get user's location
        } else {
            double myLat = loc.getLatitude();
            double myLng = loc.getLongitude();
            return new LatLng(myLat, myLng);
        }
    }

}
