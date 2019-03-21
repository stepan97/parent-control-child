package com.example.userasef.parentcontrolappchild.Screen1_Page;

import com.example.userasef.parentcontrolappchild.IBasePresenter;
import com.example.userasef.parentcontrolappchild.IBaseView;
import com.example.userasef.parentcontrolappchild.data.payload.ChildPayload;

public interface IScreen1_Contract {
    interface View extends IBaseView{
        void showErrorMessage(String msg);
    }

    interface Presenter extends IBasePresenter{
        void sendActivationCode(ChildPayload childPayload);
    }
}
