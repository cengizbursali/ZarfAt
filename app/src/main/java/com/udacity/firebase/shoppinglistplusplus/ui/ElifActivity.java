package com.udacity.firebase.shoppinglistplusplus.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ElifActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<ElifWorkout>> expandableListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elif);

        expandableListDetail = new HashMap<>();
        getWorkoutData();
    }

    private void setExpandableList(){
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new ElifExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        expandableListTitle.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });
    }

    private void getWorkoutData() {
        Firebase messagesRef = new Firebase("https://elifpolatshoppinglis.firebaseio.com/workout/");
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot children: dataSnapshot.getChildren()){
                    List<ElifWorkout> list = new ArrayList<>();
                    for(DataSnapshot dss : children.getChildren()){
                        ElifWorkout workout = dss.getValue(ElifWorkout.class);
                        list.add(workout);
                    }
                    if (!list.isEmpty()) {
                        expandableListDetail.put(children.getKey(), list);
                    }
                }
                setExpandableList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}