package sszg.com.stinder;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class URFGADAP extends RecyclerView.Adapter<URFGADAP.SimpleItem> {

    private DatabaseReference myRef = null;
    //  Data
    private List<GroupChatBox> groupChatBoxes = new ArrayList<>();

    private Context context;

    public URFGADAP(final Context context) {
        this.context = context;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        myRef = database.getReference("users/");


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupChatBoxes.clear();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //"https://firebasestorage.googleapis.com/v0/b/stinder-3b469.appspot.com/o/media%2Fimage%3A83941?alt=media&token=64944f86-a065-463b-bc32-fa3869e7b91f"
                //Log.d("FIREBASE:", "Value is DAT: " +dataSnapshot.getValue());
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    boolean shouldDisplay = false;
                    //GroupChatBox bb = singleSnapshot.getValue(GroupChatBox.class);
                    String[] values = singleSnapshot.getValue().toString().split(",");
                    String[] objValues = new String[values.length];
                    for (int i = 0; i < values.length; i++) {
                        String[] splits = values[i].split("=", 2);
                        Log.d("SPLIT: ", splits[0] + " " + splits[1]);
                        String name = splits[0];
                        String val = splits[1];
                        val = val.replace("}", "");
                        val = val.replace("{", "");
                        if (name.contains("date")) {
                            objValues[0] = val;
                        } else if (name.contains("image")) {
                            objValues[1] = val;
                        } else if (name.contains("group")) {
                            objValues[2] = val;
                        } else if (name.contains("room")) {
                            objValues[3] = val;
                        } else if (name.contains("additional")) {
                            objValues[4] = val;
                        } else if (name.contains("class")) {
                            objValues[5] = val;
                        } else if (name.contains("longitude")) {
                            objValues[6] = val;
                        } else if (name.contains("latitude")) {
                            objValues[7] = val;
                        } else if (val.contains(MainActivity.getMacAddr())) {
                            shouldDisplay = true;
                        }
                        Log.d("FUCKFUCKFUCK", values[i]);

                    }
                    GroupChatBox box = new GroupChatBox(objValues[0], objValues[1], objValues[2], objValues[3], objValues[4], objValues[5], objValues[6], objValues[7]);
                    boolean found = false;
                    for (GroupChatBox box1 : groupChatBoxes) {
                        if (box.getRoomNumber().equals(box1.getRoomNumber())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {

                        if (shouldDisplay) {
                            Log.d("FUCK", box.toString());
                            groupChatBoxes.add(box);
                        }

                    }
                    //Log.d("FIREBASE:", "BOX IS:  " + box.toString());
                }
//                HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
//                assert value != null;
//                String groupName = value.get("groupName");
//                String className = value.get("className");
//                String roomNumber = value.get("roomNumber");
//                String dateTime = value.get("dateTime");
//                String image = value.get("image");
//                String additionalInfo = value.get("additionalInfo");
//                GroupChatBox box = new GroupChatBox(groupName, className, roomNumber, dateTime, image, additionalInfo);
//                groupChatBoxes.add(box);
//                Log.d("FIREBASE:", "Value is: " + box.toString());
                for (GroupChatBox b : groupChatBoxes) {
                    Log.d("FIREBASE: ", "VAL: " + b.toString());
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE:", "Failed to read value.", error.toException());
            }
        });
    }


    @Override
    public SimpleItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_box, parent, false);

        return new SimpleItem(v);
    }

    @Override
    public void onBindViewHolder(SimpleItem holder, int position) {
        final GroupChatBox groupChatBox = groupChatBoxes.get(position);
        holder.cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PerAct.class);
                intent.putExtra("parcel_data", groupChatBox);
                context.startActivity(intent);
            }
        });
        holder.groupNameTextView.setText(groupChatBox.getGroupName());
        holder.dateTimeTextView.setText(groupChatBox.getDateTime());
        holder.classNameTextView.setText(groupChatBox.getClassName());
        if (groupChatBox.getImage().contains("firebase")) {
            Picasso.with(context).load(groupChatBox.getImage()).into(holder.groupImageView);
        }
        holder.locationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Longitude: " + groupChatBox.getLongitude() + " Latitude: " + groupChatBox.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.d("COUNT:", "" + groupChatBoxes.size());
        return groupChatBoxes != null ? groupChatBoxes.size() : 0;
    }

    protected static class SimpleItem extends RecyclerView.ViewHolder {
        TextView groupNameTextView, dateTimeTextView, classNameTextView;
        ImageView groupImageView, locationImageView;
        ConstraintLayout cl;

        public SimpleItem(View itemView) {
            super(itemView);
            cl = (ConstraintLayout) itemView.findViewById(R.id.bg);
            groupNameTextView = (TextView) itemView.findViewById(R.id.groupNameTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);
            classNameTextView = (TextView) itemView.findViewById(R.id.classTextView);
            groupImageView = (ImageView) itemView.findViewById(R.id.groupImageView);
            locationImageView = (ImageView) itemView.findViewById(R.id.locationImageView);
        }
    }
}
