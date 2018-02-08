package com.nerdgeeks.foodmap.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nerdgeeks.foodmap.model.PlaceModel;
import com.nerdgeeks.foodmap.utils.PaletteTransformation;
import com.nerdgeeks.foodmap.utils.Utils;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hp on 11/20/2016.
 */

public class GMapsAdapter extends RecyclerView.Adapter<GMapsAdapter.ListHolder> {
    private ArrayList<PlaceModel> mapListModels;
    private OnItemClickListener onItemClickListener;
    private Activity mContext;
    private boolean isEnabled;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public GMapsAdapter(ArrayList<PlaceModel> mapListModels, Activity context) {
        this.mapListModels = mapListModels;
        this.mContext = context;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ListHolder(rootView, onItemClickListener);

    }

    @Override
    public void onBindViewHolder(final ListHolder holder, int position) {

        //mContext.runOnUiThread(() -> bindHolder(holder, position));

        position = holder.getAdapterPosition();

        //adding custom font
        final Typeface ThemeFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/HelveticaNeue.ttf");
        holder.tName.setTypeface(ThemeFont);
        holder.tVicnity.setTypeface(ThemeFont);
        holder.tRate.setTypeface(ThemeFont);
        holder.tOpen.setTypeface(ThemeFont);

        String ratings = ""+mapListModels.get(position).getRating().toString();

        if (ratings.equals("0.0")) {
            holder.ratingBar.setRating(0);
            holder.tRate.setText("N/A");
        } else {
            holder.ratingBar.setRating(Float.parseFloat(ratings));
            holder.tRate.setText(ratings);
        }


        try {
            if (mapListModels.get(position).getOpeningHours().getOpenNow()) {
                holder.tOpen.setText("OPEN");
            } else {
                holder.tOpen.setText("Closed");
            }
        } catch (Exception e){
            holder.tOpen.setText("N/A");
        }

        // Get the menu item image resource ID.
        holder.tName.setText(mapListModels.get(position).getName());
        holder.tVicnity.setText(mapListModels.get(position).getVicinity());

        Picasso.with(mContext).load(mapListModels.get(position).getIcon()).into(holder.iconView);

        holder.cardView.setCardBackgroundColor(Color.WHITE);

        if (isEnabled){
            //holder.cardView.setOnClickListener(view -> onItemClickListener.onClick(view, position));
        }
    }

//    private void bindHolder(final ListHolder holder, final int position) {
//        //adding custom font
//        final Typeface ThemeFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/HelveticaNeue.ttf");
//        holder.tName.setTypeface(ThemeFont);
//        holder.tVicnity.setTypeface(ThemeFont);
//        holder.tRate.setTypeface(ThemeFont);
//        holder.tOpen.setTypeface(ThemeFont);
//
//        String ratings = mapListModels.get(position).getRating().toString();
//
//        // Get the menu item image resource ID.
//        holder.tName.setText(mapListModels.get(position).getName());
//        holder.tVicnity.setText(mapListModels.get(position).getVicinity());
//
//        if (ratings == null) {
//            holder.ratingBar.setRating(0);
//            holder.tRate.setText("N/A");
//        } else {
//            holder.ratingBar.setRating(Float.parseFloat(ratings));
//            holder.tRate.setText(ratings);
//        }
//
//        if (mapListModels.contains("opening_hours")) {
//            if (mapListModels.get(position).getOpeningHours().getOpenNow()) {
//                holder.tOpen.setText("OPEN");
//            } else {
//                holder.tOpen.setText("Closed");
//            }
//        }
//
//        Picasso.with(mContext).load(mapListModels.get(position).getIcon()).into(holder.iconView);
//
//        holder.cardView.setCardBackgroundColor(Color.BLACK);
//
//        if (isEnabled){
//            //holder.cardView.setOnClickListener(view -> onItemClickListener.onClick(view, position));
//        }
//    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Add a new item to the RecyclerView on a predefined position
    public void AddItems(int position, PlaceModel data) {
        mapListModels.add(position, data);
        notifyItemInserted(position);
    }

//    private void setAnimation(View viewToAnimate, int position)
//    {
//        // If the bound view wasn't previously displayed on screen, it's animated
//        if (position > lastPosition)
//        {
//            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
//            viewToAnimate.startAnimation(animation);
//            lastPosition = position;
//        }
//    }

    public void isOnItemClickListener(boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    @Override
    public int getItemCount() {
        return mapListModels.size();
    }

    class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tName;
        private TextView tVicnity;
        private TextView tRate;
        private TextView tOpen;
        private OnItemClickListener onItemClickListener;
        private CardView cardView;
        private ImageView iconView;
        private RatingBar ratingBar;

        ListHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;

            cardView = itemView.findViewById(R.id.card);
            tName = itemView.findViewById(R.id.nName);
            tVicnity = itemView.findViewById(R.id.nVicnity);
            tRate = itemView.findViewById(R.id.nRate);
            tOpen = itemView.findViewById(R.id.nOpen);
            iconView = itemView.findViewById(R.id.icon);
            ratingBar = itemView.findViewById(R.id.rate);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onClick(v, getAdapterPosition());
        }
    }
}
