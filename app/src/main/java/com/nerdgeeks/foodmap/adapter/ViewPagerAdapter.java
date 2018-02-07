package com.nerdgeeks.foodmap.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nerdgeeks.foodmap.model.PhotoModel;
import com.nerdgeeks.foodmap.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * Created by hp on 10/4/2016.
 */

public class ViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<PhotoModel> mImageList;
    private LayoutInflater inflater;

    public ViewPagerAdapter(Context context, ArrayList<PhotoModel> mImageList){
        this.mContext = context;
        this.mImageList = mImageList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position){
        View view = inflater.inflate(R.layout.viewpage_item,viewGroup,false);
        assert view != null;
        ImageView imageView = view.findViewById(R.id.full_image);

        PhotoModel photo = mImageList.get(position);

        Picasso.with(this.mContext)
                .load(photo.getPhotoUrl())
                .fit()
                .into(imageView);

        viewGroup.addView(view,0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object object){
        viewGroup.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
