package sszg.com.stinder;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.SimpleItem> {

    private DatabaseReference myRef = null;
    //  Data
    private List<String> groupChatBoxes = new ArrayList<>();

    private Context context;

    public RedditAdapter(final Context context) {
        this.context = context;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        myRef = database.getReference("reddit/");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                groupChatBoxes.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String[] values = snapshot.getValue().toString().split(",");
                    String[] objValues = new String[values.length];
                    for (int i = 0; i < values.length; i++) {
                        String[] splits = values[i].split("=", 2);
                        String val = splits[1].replace("}","");
                        val = val.replace("{","");
                        groupChatBoxes.add(val);
                    }

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
                .inflate(R.layout.red_layout, parent, false);

        return new SimpleItem(v);
    }

    @Override
    public void onBindViewHolder(final SimpleItem holder, int position) {
        final String groupChatBox = groupChatBoxes.get(position);
        holder.classTextView.setText(groupChatBox);
        holder.bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Opening form " + holder.classTextView.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("COUNT:", "" + groupChatBoxes.size());
        return groupChatBoxes != null ? groupChatBoxes.size() : 0;
    }

    protected static class SimpleItem extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView classTextView;
        private ConstraintLayout bg;


        public SimpleItem(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            classTextView = (TextView) itemView.findViewById(R.id.classTextView);
            bg = (ConstraintLayout)itemView.findViewById(R.id.bg);

        }
    }
}
