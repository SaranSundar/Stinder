package sszg.com.stinder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupInformation extends AppCompatActivity {
    private ImageView groupPicImageView,mapsImageView;
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
                Toast.makeText(getApplicationContext(),"Longitude: " + groupChatBox.getLongitude() + ", Latitude: " + groupChatBox.getLatitude(), Toast.LENGTH_SHORT).show();;
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
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String total = singleSnapshot.getValue().toString();
                    if(!vals.contains(total)){
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
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mac = MainActivity.getMacAddr();
                for(int i=0;i<vals.size();i++){
                    if(vals.get(i).contains(mac) && vals.get(i).contains(String.valueOf(groupNameTextView.getText()))){
                        Toast.makeText(getApplicationContext(), "Error: Already in group", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                HashMap<String, String> groupInfo = new HashMap<>();
                groupInfo.put("mac-id", mac);
                groupInfo.put("image", String.valueOf(groupChatBox.getImage()));
                groupInfo.put("groupName", String.valueOf(groupNameTextView.getText()));
                groupInfo.put("className", String.valueOf(classNameTextView.getText()));
                groupInfo.put("roomNumber", String.valueOf(roomNumberTextView.getText()));
                groupInfo.put("additionalInfo", String.valueOf(additionalInfoTextView.getText()));
                groupInfo.put("date", String.valueOf(dateTimeTextView.getText()));
                groupInfo.put("longitude", groupChatBox.getLongitude());
                groupInfo.put("latitude", groupChatBox.getLatitude());
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
        });


    }

}
