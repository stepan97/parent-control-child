package com.example.userasef.parentcontrolappchild.utils;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ActivityUtil {
    public static void pushFragment(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager, @IdRes int resId, boolean addToBackStack){
        if(fragmentManager != null){
            FragmentTransaction transaction =fragmentManager.beginTransaction();
            if(addToBackStack){
                transaction.add(resId, fragment, fragment.getClass().getSimpleName());
                transaction.addToBackStack(fragment.getClass().getSimpleName());
            }else{
                transaction.replace(resId, fragment, fragment.getClass().getSimpleName());
            }

            transaction.commit();
        }
    }

    public static void backToHomeScreen(@NonNull FragmentManager fragmentManager){
        if(fragmentManager != null){
            int backStackCount = fragmentManager.getBackStackEntryCount();

            for (int i = 0; i < backStackCount; i++) {
                int backId = fragmentManager.getBackStackEntryAt(i).getId();

                fragmentManager.popBackStack(backId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.popBackStack();
            }
        }
    }

    public static void goToPreviousFragment(@NonNull FragmentManager fragmentManager){
        if(fragmentManager != null){
            fragmentManager.popBackStack();
        }
    }
}
