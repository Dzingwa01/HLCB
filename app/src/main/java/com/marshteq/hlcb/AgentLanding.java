package com.marshteq.hlcb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.iamhabib.easy_preference.EasyPreference;
import com.marshteq.hlcb.Adapters.AssignedStockAdapter;
import com.marshteq.hlcb.Helpers.Credentials;
import com.marshteq.hlcb.Models.Product;
import com.marshteq.hlcb.Models.UserPref;
import com.novoda.merlin.Connectable;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AgentLanding extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    int back_pressed_count = 0;
    SharedPreferences sp;
    Merlin merlin;
    MerlinsBeard merlinsBeard;

    private List<Product> products = new ArrayList<>();
    private RecyclerView recyclerView;
    private AssignedStockAdapter mAdapter;
    SwipeRefreshLayout pullToRefresh;
    RecyclerView.LayoutManager mLayoutManager;
    ProgressBar progressBar;
    private static boolean refreshing = false;
    private String user_id, picture_url;
    private String user_email, first_name, id_number, gender, phone_number, last_name;
    TextView user_details;
    TextView user_email_textview;
    CircleImageView img;
    View hView;
    String Base_URL;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    Intent intent = new Intent(AgentLanding.this, AgentDashboard.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent1 = new Intent(AgentLanding.this, AgentNotifications.class);
                    startActivity(intent1);
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        hView = navigationView.getHeaderView(0);
        Credentials credentials = EasyPreference.with(getApplicationContext()).getObject("server_details", Credentials.class);
        Base_URL = credentials.server_url;
        back_pressed_count = 0;
        UserPref pref = EasyPreference.with(getApplicationContext()).getObject("user_pref", UserPref.class);
        String name = pref.name;
        String surname = pref.surname;
        last_name = surname;
        String email = pref.email;
        phone_number = pref.contact_number;
        gender = pref.gender;
        user_id = pref.id;
        picture_url = pref.profile_picture_url;
        Log.d("full name", name +" "+surname);
        user_email = email;
        user_details = (TextView) hView.findViewById(R.id.user_name);
        user_email_textview = (TextView) hView.findViewById(R.id.user_email);
        img = hView.findViewById(R.id.user_image);
        Picasso.get().load(Base_URL+"storage/"+picture_url).placeholder(R.drawable.placeholder).into(img);
        user_details.setText(name + "  " + surname);
        user_email_textview.setText(email);

        sp = getSharedPreferences("login", MODE_PRIVATE);
        //Log.d("Payments", "Payments");
        if (!sp.getBoolean("logged", true)) {
            Intent intent = new Intent(AgentLanding.this, LoginActivity.class);
            startActivity(intent);
        }
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                // Do something you haz internet!
            }
        });
        merlinsBeard = MerlinsBeard.from(this);
        if (merlinsBeard.isConnected()) {
            // Connected, do something!

        } else {
            // Disconnected, do something!
            Toast.makeText(this,"Yo are not connected to the internet",Toast.LENGTH_LONG);
        }

        recyclerView = (RecyclerView)findViewById(R.id.wash_requests_list);
        mLayoutManager = new LinearLayoutManager(AgentLanding.this);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        sp = getSharedPreferences("login", MODE_PRIVATE);
        //Log.d("Payments", "Payments");
        if (!sp.getBoolean("logged", true)) {
            Intent intent = new Intent(AgentLanding.this, LoginActivity.class);
            startActivity(intent);
        }

        recyclerView = (RecyclerView)findViewById(R.id.wash_requests_list);
        mLayoutManager = new LinearLayoutManager(AgentLanding.this);
        progressBar = (ProgressBar) findViewById(R.id.progress);
//        getAssignedStock();
    }



    @Override
    public void onBackPressed() {
        back_pressed_count++;
        if (back_pressed_count == 1) {
            Toast.makeText(AgentLanding.this, "Press Back again to Logout", Toast.LENGTH_SHORT).show();
        } else {
            sp = getSharedPreferences("login", MODE_PRIVATE);
            sp.edit().putBoolean("logged", false).apply();
//                AppDatabase.destroyInstance();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.agent_landing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout_client) {
            // Handle the camera action
            showLogoutConfirmDialog();
        }else if(id == R.id.nav_settings_client){
            Intent intent = new Intent(AgentLanding.this,SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_help_client) {
            Intent intent = new Intent(AgentLanding.this,HelpActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_stock_management) {
            Intent intent = new Intent(AgentLanding.this,StockManagement.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showLogoutConfirmDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AgentLanding.this);
        LayoutInflater inflater = LayoutInflater.from(AgentLanding.this);
        final View view = inflater.inflate(R.layout.logout_dialog, null);
        alertDialogBuilder.setView(view);
//        alertDialogBuilder.setTitle("Image Caption");
//        final EditText captionText = view.findViewById(R.id.caption_text);
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                });
            }
        });
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sp = getSharedPreferences("login", MODE_PRIVATE);
                        sp.edit().putBoolean("logged", false).apply();
//                        AppDatabase.destroyInstance();
                        finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
