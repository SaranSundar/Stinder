package sszg.com.stinder;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;

public class CreateStudyGroup extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int REQUEST_CODE = 4567;
    private Button dateTimeButton;
    private Button uploadButton;
    private ImageView groupPicImageView;
    private HashMap<String, String> groupInfo;
    private DatabaseReference myRef;
    private StorageReference sref;
    private Uri downloadLink = null;
    private boolean attemptingToUpload = false;
    private String latitude = "", longitude = "";

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_study_group);
        dateTimeButton = (Button) findViewById(R.id.dateTimeButton);
        dateTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDate();
            }
        });
        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToFirebase();
            }
        });
        groupPicImageView = (ImageView) findViewById(R.id.groupPicImageView);
        groupPicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
            }
        });

        if (ActivityCompat.checkSelfPermission(CreateStudyGroup.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateStudyGroup.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateStudyGroup.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            // Write you code here if permission already given.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            } else
                Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();
        }

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("groups/");

        sref = FirebaseStorage.getInstance().getReference();
    }

    public void uploadToFirebase() {
        groupInfo = new HashMap<>();
        if (downloadLink == null && attemptingToUpload) {
            Toast.makeText(getApplicationContext(), "Image not uploaded, wait and try again", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (downloadLink != null) {
                groupInfo.put("image", String.valueOf(downloadLink));
            } else {
                groupInfo.put("image", "n/a");
            }
        }
        TextView groupNameTextView = (TextView) findViewById(R.id.groupNameTextView);
        TextView classNameTextView = (TextView) findViewById(R.id.classNameTextView);
        TextView roomNumberTextView = (TextView) findViewById(R.id.roomNumberTextView);
        TextView additionalInfoTextView = (TextView) findViewById(R.id.additionalInfoTextView);

        groupInfo.put("groupName", String.valueOf(groupNameTextView.getText()));
        groupInfo.put("className", String.valueOf(classNameTextView.getText()));
        groupInfo.put("roomNumber", String.valueOf(roomNumberTextView.getText()));
        groupInfo.put("additionalInfo", String.valueOf(additionalInfoTextView.getText()));
        groupInfo.put("date", String.valueOf(dateTimeButton.getText()));
        groupInfo.put("longitude", longitude);
        groupInfo.put("latitude", latitude);
        myRef.push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Stored...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                }

            }

        });

        groupInfo.put("mac-id", MainActivity.getMacAddr());
        myRef = database.getReference("users/");
        myRef.push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Joined", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    public void uploadFile(Uri imageUri) {
        if (imageUri != null) {

            final StorageReference imageRef = sref.child("/media") // folder path in firebase storage
                    .child(imageUri.getLastPathSegment());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                            // Get the download URL
                            Uri downloadUri = snapshot.getMetadata().getDownloadUrl();
                            downloadLink = downloadUri;
                            // use this download url with imageview for viewing & store this linke to firebase message data
                            Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                            ;
                            attemptingToUpload = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            attemptingToUpload = false;
                            // show message on failure may be network/disk ?
                            Toast.makeText(getApplicationContext(), "Image Not Uploaded", Toast.LENGTH_SHORT).show();
                            ;
                        }
                    });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        }
        try {
            switch (requestCode) {

                case REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        //data gives you the image uri. Try to convert that to bitmap
                        Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        groupPicImageView.setImageBitmap(bitmap);
                        attemptingToUpload = true;
                        uploadFile(imageUri);
                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        attemptingToUpload = false;
                        Log.e("IMAGEUPLOAD", "Selecting picture cancelled");
                    }
                    break;
                case 1000:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            // All required changes were successfully made
                            getLocation();
                            break;
                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("IMAGEUPLOAD", "Exception in onActivityResult : " + e.getMessage());
        }
    }

    public void chooseDate() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String finalText = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                        dateTimeButton.setText(finalText);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                chooseTime();
                            }
                        }, 100);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void chooseTime() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hour,
                                          int minute) {
                        String finalText = String.valueOf(dateTimeButton.getText());
                        String hourText = hour % 12 == 0 ? "12" : Integer.toString(hour % 12);
                        String minuteText = minute < 10 ? "0" + Integer.toString(minute) : Integer.toString(minute);
                        if (hour >= 12)
                            finalText += "  " + hourText + ":" + minuteText + " PM";
                        else
                            finalText += "  " + hourText + ":" + minuteText + " AM";

                        dateTimeButton.setText(finalText);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /*Ending the updates for the location service*/
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        settingRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /*Method to get the enable location settings dialog*/
    public void settingRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);    // 10 seconds, in milliseconds
        mLocationRequest.setFastestInterval(1000);   // 1 second, in milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(CreateStudyGroup.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }

        });
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            /*Getting the location after aquiring location service*/
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = (String.valueOf(mLastLocation.getLatitude()));
                longitude = (String.valueOf(mLastLocation.getLongitude()));
            } else {
                /*if there is no last known location. Which means the device has no data for the loction currently.
                * So we will get the current location.
                * For this we'll implement Location Listener and override onLocationChanged*/
                Log.i("Current Location", "No data for location found");

                if (!mGoogleApiClient.isConnected())
                    mGoogleApiClient.connect();

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, CreateStudyGroup.this);
            }
        }
    }

    /*When Location changes, this method get called. */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        latitude = (String.valueOf(mLastLocation.getLatitude()));
        longitude = (String.valueOf(mLastLocation.getLongitude()));
    }
}
