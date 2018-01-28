package sszg.com.stinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PerAct extends AppCompatActivity {
    private ImageView groupPicImageView, mapsImageView;
    private TextView groupNameTextView, classNameTextView, roomNumberTextView, dateTimeTextView, additionalInfoTextView;
    private Button joinButton;
    private DatabaseReference myRef;
    private ArrayList<String> vals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_information);
        groupPicImageView = (ImageView) findViewById(R.id.groupPicImageView);
        mapsImageView = (ImageView) findViewById(R.id.mapsImageView);
        groupNameTextView = (TextView) findViewById(R.id.groupNameTextView);
        classNameTextView = (TextView) findViewById(R.id.classNameTextView);
        roomNumberTextView = (TextView) findViewById(R.id.roomNumberTextView);
        dateTimeTextView = (TextView) findViewById(R.id.dateTimeTextView);
        additionalInfoTextView = (TextView) findViewById(R.id.additionalInfoTextView);
        joinButton = (Button) findViewById(R.id.joinButton);

        final GroupChatBox groupChatBox = (GroupChatBox) getIntent().getParcelableExtra("parcel_data");
        mapsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Longitude: " + groupChatBox.getLongitude() + ", Latitude: " + groupChatBox.getLatitude(), Toast.LENGTH_SHORT).show();
                ;
            }
        });
        if (groupChatBox.getImage().contains("firebase")) {
            Picasso.with(getApplicationContext()).load(groupChatBox.getImage()).into(groupPicImageView);
        }
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users/");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String total = singleSnapshot.getValue().toString();
                    if (!vals.contains(total)) {
                        vals.add(total);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        groupNameTextView.setText(groupChatBox.getGroupName());
        classNameTextView.setText(groupChatBox.getClassName());
        roomNumberTextView.setText(groupChatBox.getRoomNumber());
        dateTimeTextView.setText(groupChatBox.getDateTime());
        additionalInfoTextView.setText(groupChatBox.getAdditionalInfo());
        joinButton.setText("Leave Group");
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mac = MainActivity.getMacAddr();

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            if (singleSnapshot.getValue().toString().contains(mac)) {
                                singleSnapshot.getRef().setValue(null);
                                Toast.makeText(getApplicationContext(), "Leaving Group", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


    }

}
