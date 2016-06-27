package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;

public class FriendAdapter extends FirebaseListAdapter<User> {
    private static final String LOG_TAG = FriendAdapter.class.getSimpleName();

    public FriendAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                         Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View view, final User friend) {
        view.setTag(friend.getEmail());
        ((TextView) view.findViewById(R.id.user_name)).setText(friend.getName());
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_user_image);
//        if(friend.getGender() != null){
//            int iconResource = friend.getGender().equals(mActivity.getString(R.string.text_female)) ?
//                    R.drawable.avatar_female : R.drawable.avatar_male;
//            imageView.setImageResource(iconResource);
//        }
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(String.valueOf(friend.getName().charAt(0)).toUpperCase(),
                        ContextCompat.getColor(mActivity, R.color.black));

        imageView.setImageDrawable(drawable);
    }

}
