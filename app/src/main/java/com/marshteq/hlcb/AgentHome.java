package com.marshteq.hlcb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AgentHome extends AppCompatActivity {

    private TextView mTextMessage;
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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                   Intent intent = new Intent(AgentHome.this, AgentDashboard.class);
                   startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent1 = new Intent(AgentHome.this, AgentNotifications.class);
                    startActivity(intent1);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_home);

//        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        pullToRefresh = (SwipeRefreshLayout)findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                getAssignedStock();
            }
        });

        sp = getSharedPreferences("login", MODE_PRIVATE);
        //Log.d("Payments", "Payments");
        if (!sp.getBoolean("logged", true)) {
            Intent intent = new Intent(AgentHome.this, LoginActivity.class);
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
        mLayoutManager = new LinearLayoutManager(AgentHome.this);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        getAssignedStock();
    }

    public void setAdapter(){
//        Log.d("Setting","Adapter");
//        Log.d("Setting9",washRequests.get(0).description);
        mAdapter = new AssignedStockAdapter(products,AgentHome.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(AgentHome.this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void getAssignedStock(){
        products = new ArrayList<Product>();
        RequestQueue requestQueue = Volley.newRequestQueue(AgentHome.this);
        Credentials credentials = EasyPreference.with(AgentHome.this).getObject("server_details", Credentials.class);
        UserPref pref = EasyPreference.with(AgentHome.this).getObject("user_pref", UserPref.class);
        final String url = credentials.server_url;
        String URL = url+"api/get-assigned-stock/"+pref.id;
        if(!refreshing){
            progressBar.setVisibility(View.VISIBLE);
        }
        JsonObjectRequest provinceRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray response_obj = response.getJSONArray("products");
                    Log.d("Response requests",response_obj.toString());
                    if (response_obj.length() > 0) {
                        for (int i = 0; i < response_obj.length(); i++) {
                            JSONObject obj = response_obj.getJSONObject(i);
                            JsonParser parser = new JsonParser();
                            JsonElement element = parser.parse(obj.toString());
                            Gson gson = new Gson();
                            Product request = gson.fromJson(element, Product.class);
                            products.add(request);
                        }
//                        setAdapter();
                    }else{

                        Product product = new Product("empty","","","No Available products at the moment","0",0,"0",null,"");
                        products.add(product);

                    }
                    setAdapter();
                    progressBar.setVisibility(View.INVISIBLE);
                    pullToRefresh.setRefreshing(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Error2",e.getMessage());
                    progressBar.setVisibility(View.INVISIBLE);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d("error", error.toString());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
        requestQueue.add(provinceRequest);
    }

    @Override
    public void onBackPressed() {
        back_pressed_count++;
        if (back_pressed_count == 1) {
            Toast.makeText(AgentHome.this, "Press Back again to Logout", Toast.LENGTH_SHORT).show();
        } else {
            sp = getSharedPreferences("login", MODE_PRIVATE);
            sp.edit().putBoolean("logged", false).apply();
//                AppDatabase.destroyInstance();
            finish();
        }
    }

}
