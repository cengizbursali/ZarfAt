package com.udacity.firebase.shoppinglistplusplus.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.Message;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

public class ShowMessageActivity extends AppCompatActivity {

    private final String TAG = ShowMessageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        overridePendingTransition(R.anim.trans_bottom_in, R.anim.trans_bottom_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(R.string.title_show_message));
        }

        TextView tvSender = (TextView) findViewById(R.id.tv_add_sender);
        TextView tvMessageContext = (TextView) findViewById(R.id.tv_message_context);

        Intent intent = getIntent();
        if (intent != null) {
            Message message = (Message) intent.getSerializableExtra(Constants.KEY_MESSAGE_OBJECT);
            tvSender.setText(message.getCreatorName());
            tvMessageContext.setText(message.getContext());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_top_in, R.anim.trans_top_out);
    }

}
