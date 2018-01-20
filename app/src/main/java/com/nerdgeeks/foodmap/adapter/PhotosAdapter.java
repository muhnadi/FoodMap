package com.nerdgeeks.foodmap.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.volley.toolbox.ImageLoader;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.model.PhotoModel;
import com.nerdgeeks.foodmap.view.FadeInNetworkImageView;

import java.util.ArrayList;

/**
 * Created by hp on 11/20/2016.
 */

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ListHolder> {
    private ArrayList<PhotoModel> photoList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private Context mContext;
    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();
    private int lastPosition = -1;
    private static final int TYPE_FULL = 0;
    private static final int TYPE_HALF = 1;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public PhotosAdapter(ArrayList<PhotoModel> photos, Context context){
        this.photoList = photos;
        this.mContext = context;
    }

    @Override
    public ListHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_thumb,parent,false);
        this.mContext = parent.getContext();

        rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                final ViewGroup.LayoutParams lp = rootView.getLayoutParams();
                if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams sglp =
                            (StaggeredGridLayoutManager.LayoutParams) lp;
                    switch (viewType) {
                        case TYPE_FULL:
                            sglp.setFullSpan(true);
                            break;
                        case TYPE_HALF:
                            sglp.setFullSpan(false);
                            sglp.width = rootView.getWidth();
                            break;
                    }
                    rootView.setLayoutParams(sglp);
                    final StaggeredGridLayoutManager lm =
                            (StaggeredGridLayoutManager) ((RecyclerView) parent).getLayoutManager();
                    lm.invalidateSpanAssignments();
                }
                rootView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
        return new ListHolder(rootView,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ListHolder holder, int position) {

        String imgUrl = photoList.get(position).getPhotoUrl();
        holder.thumbView.setImageUrl(imgUrl,mImageLoader);

        holder.thumbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onClick(view, holder.getAdapterPosition());
            }
        });
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        final int modeEight = position % 8;
        switch (modeEight) {
            case 0:
            case 3:
                return TYPE_FULL;
            case 1:
            case 5:
            case 2:
        }
        return TYPE_HALF;
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

    class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private  OnItemClickListener onItemClickListener;
        private FadeInNetworkImageView thumbView;

        ListHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;
            thumbView = (FadeInNetworkImageView) itemView.findViewById(R.id.photo_thumb);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onClick(v, getAdapterPosition());
        }
    }
}
