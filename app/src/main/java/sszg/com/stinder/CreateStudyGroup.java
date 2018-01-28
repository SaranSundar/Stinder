package sszg.com.stinder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

public class CreateStudyGroup extends AppCompatActivity {

    private static final int REQUEST_CODE = 4567;
    private Button dateTimeButton;
    private Button uploadButton;
    private ImageView groupPicImageView;
    private HashMap<String, String> groupInfo;
    private DatabaseReference myRef;
    private StorageReference sref;
    private Uri downloadLink = null;
    private boolean attemptingToUpload = false;

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
        // Write a message to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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
        myRef.push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Stored...", Toast.LENGTH_SHORT).show();
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
}
