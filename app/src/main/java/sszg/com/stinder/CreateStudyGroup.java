package sszg.com.stinder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class CreateStudyGroup extends AppCompatActivity {

    private Button dateTimeButton;

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

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("study_groups");

        myRef.setValue("Hello, World!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("FIREBASE:", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE:", "Failed to read value.", error.toException());
            }
        });
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
                        String hourText = hour % 12 == 0 ? "12" : Integer.toString(hour%12);
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
