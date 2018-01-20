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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.utils.PaletteTransformation;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.utils.Utils;
import com.nerdgeeks.foodmap.model.PlaceDeatilsModel;
import com.nerdgeeks.foodmap.view.FadeInNetworkImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hp on 11/20/2016.
 */

public class GMapsAdapter extends RecyclerView.Adapter<GMapsAdapter.ListHolder> {
    private ArrayList<PlaceDeatilsModel> mapListModels;
    private OnItemClickListener onItemClickListener;
    private Activity mContext;
    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();
    private int lastPosition = -1;
    private boolean isEnabled;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public GMapsAdapter(ArrayList<PlaceDeatilsModel> mapListModels, Activity context) {
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

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bindHolder(holder, holder.getAdapterPosition());
            }
        });
    }

    private void bindHolder(final ListHolder holder, final int position) {
        //adding custom font
        final Typeface ThemeFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/HelveticaNeue.ttf");
        holder.tName.setTypeface(ThemeFont);
        holder.tVicnity.setTypeface(ThemeFont);
        holder.tRate.setTypeface(ThemeFont);
        holder.tOpen.setTypeface(ThemeFont);

        String imgUrl = mapListModels.get(position).getThumbUrl();

        String ratings = mapListModels.get(position).getResRating();

        // Get the menu item image resource ID.
        holder.tName.setText(mapListModels.get(position).getResName());
        holder.tVicnity.setText(mapListModels.get(position).getResVicnity());

        if (ratings == null) {
            holder.ratingBar.setRating(0);
            holder.tRate.setText("N/A");
        } else {
            holder.ratingBar.setRating(Float.parseFloat(ratings));
            holder.tRate.setText(ratings);
        }

        Picasso.with(mContext)
                .load(mapListModels.get(position).getIconUrl())
                .into(holder.iconView);

        Boolean OPEN = Boolean.parseBoolean(mapListModels.get(position).getResOpen());

        if (OPEN == false) {
            holder.tOpen.setText("Closed");
        } else if (OPEN == true) {
            holder.tOpen.setText("OPEN");
        }

        Picasso
                .with(mContext)
                .load(imgUrl).transform(PaletteTransformation.instance())
                .into(holder.thumb, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {

                        Bitmap bitmap = ((BitmapDrawable) holder.thumb.getDrawable()).getBitmap(); // Ew!

                        if (bitmap != null && !bitmap.isRecycled()) {
                            Palette palette = PaletteTransformation.getPalette(bitmap);

                            if (palette != null) {
                                Palette.Swatch s = palette.getVibrantSwatch();
                                if (s == null) {
                                    s = palette.getDarkVibrantSwatch();
                                }
                                if (s == null) {
                                    s = palette.getLightVibrantSwatch();
                                }
                                if (s == null) {
                                    s = palette.getMutedSwatch();
                                }

                                if (s != null && position >= 0 && position < mapListModels.size()) {
                                    Utils.animateViewColor(holder.mRelative, Color.BLACK, s.getRgb());
                                }
                            }
                        }
                    }
                });

        if (imgUrl != null) {
            holder.getThumb.setImageUrl(imgUrl, mImageLoader);
        } else {
            holder.getThumb.setImageUrl(null, null);
        }

        holder.cardView.setCardBackgroundColor(Color.GRAY);

        if (isEnabled){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(view, position);
                }
            });
        }

        setAnimation(holder.itemView, position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Add a new item to the RecyclerView on a predefined position
    public void AddItems(int position, PlaceDeatilsModel data) {
        mapListModels.add(position, data);
        notifyItemInserted(position);
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

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
        private FadeInNetworkImageView thumb;
        private FadeInNetworkImageView getThumb;
        private RatingBar ratingBar;
        private View mRelative;

        ListHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;

            cardView = (CardView) itemView.findViewById(R.id.card);
            tName = (TextView) itemView.findViewById(R.id.nName);
            tVicnity = (TextView) itemView.findViewById(R.id.nVicnity);
            tRate = (TextView) itemView.findViewById(R.id.nRate);
            tOpen = (TextView) itemView.findViewById(R.id.nOpen);
            iconView = (ImageView) itemView.findViewById(R.id.icon);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rate);
            thumb = (FadeInNetworkImageView) itemView.findViewById(R.id.thumbnails);
            getThumb = (FadeInNetworkImageView) itemView.findViewById(R.id.thumb);
            mRelative = (RelativeLayout) itemView.findViewById(R.id.thumbHolder);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onClick(v, getAdapterPosition());
        }
    }
}
