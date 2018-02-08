package com.nerdgeeks.foodmap.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nerdgeeks.foodmap.Api.ApiClient;
import com.nerdgeeks.foodmap.Api.ApiInterface;
import com.nerdgeeks.foodmap.app.AppData;
import com.nerdgeeks.foodmap.model.PlaceModel;
import com.nerdgeeks.foodmap.model.PlaceModelCall;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.adapter.GMapsAdapter;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import java.util.ArrayList;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nerdgeeks.foodmap.app.AppConfig.*;


public class ResultFragment extends Fragment implements
        ConnectivityReceiver.ConnectivityReceiverListener,SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_PARAM1 = "param1";
    private String type;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GMapsAdapter mapAdapter;
    private RecyclerView mRecyclerView;
    private PrefManager prefManager;
    private boolean isConnected;
    private View snackView;

    public ResultFragment() {
        // Required empty public constructor
    }

    public static ResultFragment newInstance(String param1) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    private Context mContext;

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        snackView = rootView.findViewById(R.id.fragment_list);
        prefManager = new PrefManager(getContext());
        isConnected = ConnectivityReceiver.isConnected();

        //Adding RecyclerView
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(this::loadData);

        final FloatingActionButton fab = rootView.findViewById(R.id.filterAction);
        fab.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(mContext, view);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            MenuPopupHelper menuHelper = new MenuPopupHelper(mContext, (MenuBuilder) popup.getMenu(), view);
            menuHelper.setForceShowIcon(true);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case 0:
                        onFilterAction(item.toString());
                        break;
                    case 1:
                        onFilterAction(item.toString());
                        break;
                    case 2:
                        onFilterAction(item.toString());
                        break;
                    case 3:
                        onFilterAction(item.toString());
                        break;
                    case 4:
                        onFilterAction(item.toString());
                        break;
                    default:
                        break;
                }
                return true;
            });
            menuHelper.show();
        });

        return rootView;
    }

    private void onFilterAction(String s) {
        if (AppData.placeModels.isEmpty()) {
            if (prefManager.isPrefAvailable(type)){
                setRecyclerAdapter(filterByRating(prefManager.readData(type), s));
            }
        } else {
            setRecyclerAdapter(filterByRating(AppData.placeModels, s));
        }
    }

    private ArrayList<PlaceModel> filterByRating(ArrayList<PlaceModel> models, String query) {

        final ArrayList<PlaceModel> filteredModelList = new ArrayList<>();
        for (PlaceModel model : models) {
            String rating = model.getRating().toString().split("\\.")[0];
            if (rating.equals(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    private void loadData(){
        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        if(!isConnected){
            if (prefManager.isPrefAvailable(type)){
                setRecyclerAdapter(prefManager.readData(type));
            } else {
                showSnackMessage(INTERNET_ERROR);
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            setRecyclerAdapter(AppData.placeModels);
        }
    }

    private OnItemClickListener recyclerRowClickListener = (v, position) -> {

        Intent detailIntent = new Intent(getActivity(), InfoActivity.class);
        detailIntent.putExtra("position", position);
        detailIntent.putExtra("placeId", AppData.placeModels.get(position).getPlaceId());
        startActivity(detailIntent);
    };

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private void showSnackMessage(String message) {
        int color = Color.RED;
        int TIME_OUT = Snackbar.LENGTH_SHORT;

        Snackbar snackbar = Snackbar
                .make(snackView, message, TIME_OUT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void setRecyclerAdapter(ArrayList<PlaceModel> placeModels){

        // stopping swipe refresh
        swipeRefreshLayout.setRefreshing(false);

        mapAdapter = new GMapsAdapter(placeModels, getActivity());
        mapAdapter.notifyDataSetChanged();
        //Nearby restaurant List added to RecyclerView
        if (isConnected){
            mapAdapter.isOnItemClickListener(true);
            mapAdapter.setOnItemClickListener(recyclerRowClickListener);
        } else {
            mapAdapter.isOnItemClickListener(false);
        }
        mRecyclerView.setAdapter(mapAdapter);
    }

}
