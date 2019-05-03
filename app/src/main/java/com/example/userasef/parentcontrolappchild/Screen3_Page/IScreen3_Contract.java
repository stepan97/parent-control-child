package com.example.userasef.parentcontrolappchild.Screen3_Page;

import com.example.userasef.parentcontrolappchild.IBasePresenter;
import com.example.userasef.parentcontrolappchild.IBaseView;

public interface IScreen3_Contract {
    interface View extends IBaseView {
        void showErrorMessage(String msg);
    }

    interface Presenter extends IBasePresenter {
        void sendFirebaseToken();
    }
}
