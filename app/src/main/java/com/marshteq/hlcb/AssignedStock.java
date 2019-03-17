package com.marshteq.hlcb;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AssignedStock.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AssignedStock#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignedStock extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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

    public AssignedStock() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AssignedStock.
     */
    // TODO: Rename and change types and number of parameters
    public static AssignedStock newInstance(String param1, String param2) {
        AssignedStock fragment = new AssignedStock();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_assigned_stock, container, false);
        merlin = new Merlin.Builder().withConnectableCallbacks().build(getActivity());
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                // Do something you haz internet!
            }
        });
        merlinsBeard = MerlinsBeard.from(getActivity());
        if (merlinsBeard.isConnected()) {
            // Connected, do something!

        } else {
            // Disconnected, do something!
            Toast.makeText(getActivity(),"Yo are not connected to the internet",Toast.LENGTH_LONG);
        }

        recyclerView = (RecyclerView)v.findViewById(R.id.wash_requests_list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        progressBar = (ProgressBar)v.findViewById(R.id.progress);

        pullToRefresh = (SwipeRefreshLayout)v.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                getAssignedStock();
            }
        });
        getAssignedStock();

        return v;
    }

    public void setAdapter(){
//        Log.d("Setting","Adapter");
//        Log.d("Setting9",washRequests.get(0).description);
        mAdapter = new AssignedStockAdapter(products,getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void getAssignedStock(){
        products = new ArrayList<Product>();
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        Credentials credentials = EasyPreference.with(getActivity()).getObject("server_details", Credentials.class);
        UserPref pref = EasyPreference.with(getActivity()).getObject("user_pref", UserPref.class);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
