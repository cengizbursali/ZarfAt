package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;

/**
 * Populates the list_view_friends_share inside ShareListActivity
 */
public class FriendAdapter extends FirebaseListAdapter<User> {
    private static final String LOG_TAG = FriendAdapter.class.getSimpleName();

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public FriendAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                         Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_user_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final User friend) {
        view.setTag(friend.getEmail());
        ((TextView) view.findViewById(R.id.user_name)).setText(friend.getName());
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_user_image);
        if(friend.getGender() != null){
            int iconResource = friend.getGender().equals(mActivity.getString(R.string.text_female)) ?
                    R.drawable.avatar_female : R.drawable.avatar_male;
            imageView.setImageResource(iconResource);
        }
    }

}
