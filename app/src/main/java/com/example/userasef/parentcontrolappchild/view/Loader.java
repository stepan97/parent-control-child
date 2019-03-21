package com.example.userasef.parentcontrolappchild.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.userasef.parentcontrolappchild.R;

public class Loader extends ConstraintLayout {

    private ImageView logoImage;
    private ObjectAnimator objectAnimator;

    public Loader(Context context) {
        super(context);
        init(context);
    }

    public Loader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Loader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.loader, null);
        logoImage = view.findViewById(R.id.loaderImageView);
        addView(view,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setVisibility(int visibility) {
        setAnimation(visibility);
        super.setVisibility(visibility);
    }

    private void setAnimation(int visibility) {
        if (objectAnimator == null) {
            objectAnimator = new ObjectAnimator();
            objectAnimator.setDuration(1000);
            objectAnimator.setProperty(ROTATION_Y);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.setFloatValues(360.0f, 0.0f);
        }
        if (visibility == INVISIBLE || visibility == GONE){
//            objectAnimator.setTarget(null);
//            objectAnimator = null;
            return;
        }

        // Aply animation to image view
        objectAnimator.setTarget(logoImage);
        objectAnimator.start();
    }
}
