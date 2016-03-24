package com.udacity.firebase.shoppinglistplusplus.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.udacity.firebase.shoppinglistplusplus.R;

public class NavigatorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int navigationItemId;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        navigationItemId = item.getItemId();
        //Check to see which item was being clicked and perform appropriate action
        switch (navigationItemId){
            case R.id.navigation_item_1:
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navigation_item_2:
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navigation_item_3:
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}