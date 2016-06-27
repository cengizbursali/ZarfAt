package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

public class FriendsList extends BaseActivity {
    private static final String LOG_TAG = FriendsList.class.getSimpleName();
    private FriendAdapter mFriendAdapter;
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        initializeScreen();

        Firebase currentUserFriendsRef = new Firebase(Constants.FIREBASE_URL_USER_FRIENDS).child(mEncodedEmail);

        mFriendAdapter = new FriendAdapter(FriendsList.this, User.class,
                R.layout.single_user_item, currentUserFriendsRef);

        mListView.setAdapter(mFriendAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                User user = mFriendAdapter.getItem(position);
                intent.putExtra(Constants.KEY_ENCODED_EMAIL, user.getEmail());
                intent.putExtra(Constants.KEY_USER_NAME, user.getName());
                setResult(Constants.FRIEND_LIST_REQUEST_CODE, intent);
                Log.i(LOG_TAG, "selected user: " + user.getName());
                finish();
            }
        });
    }

    /**
     * Cleanup the adapter when activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendAdapter.cleanup();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_friends_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Launch AddFriendActivity to find and add user to current user's friends list
     * when the button AddFriend is pressed
     */
    public void onAddFriendPressed(View view) {
        Intent intent = new Intent(FriendsList.this, AddFriendActivity.class);
        startActivity(intent);
    }

}