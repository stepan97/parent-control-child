package com.example.userasef.parentcontrolappchild.Screen1_Page;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.userasef.parentcontrolappchild.R;
import com.example.userasef.parentcontrolappchild.Screen3_Page.Screen3_Fragment;
import com.example.userasef.parentcontrolappchild.data.payload.ChildPayload;
import com.example.userasef.parentcontrolappchild.utils.ActivityUtil;
import com.example.userasef.parentcontrolappchild.view.Loader;

public class Screen1_Fragment extends Fragment implements IScreen1_Contract.View{

    private Button continue_btn;
    private EditText code_EditText;
    private EditText childName_EditText;
    private IScreen1_Contract.Presenter mPresenter;
    private Loader loader;

    public static Screen1_Fragment newInstance(){
        Bundle args = new Bundle();

        Screen1_Fragment fragment = new Screen1_Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Screen1_Fragment(){
        // default constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen1, container, false);

        initView(view);
        initListeners();
        mPresenter = new Screen1_Presenter(this, getContext());

        return view;
    }

    private void initView(View view){
        continue_btn = view.findViewById(R.id.continue_btn);
        code_EditText = view.findViewById(R.id.activation_code_et);
        childName_EditText = view.findViewById(R.id.child_name_et);
        loader = view.findViewById(R.id.loader);
    }

    private void initListeners(){
        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.continue_btn){
                    if(checkFields()){
                        ChildPayload payload = new ChildPayload();
                        payload.setName(childName_EditText.getText().toString());
                        payload.setAccessCode(code_EditText.getText().toString());
                        mPresenter.sendActivationCode(payload);
                    }
                }
            }
        });
    }

    private boolean checkFields(){
        boolean codeOK = true;
        boolean nameOK = true;

        String activation_code = code_EditText.getText().toString();
        String name = childName_EditText.getText().toString();

        if(TextUtils.isEmpty(activation_code) || activation_code.length() != 10)
        {
            codeOK = false;
            code_EditText.setError(getString(R.string.please_enter_code));
        }else if(!TextUtils.isDigitsOnly(activation_code)){
            codeOK = false;
            code_EditText.setError(getString(R.string.must_contain_only_digits));
        }

        if(TextUtils.isEmpty(name) || name.length() < 4)
        {
            nameOK = false;
            childName_EditText.setError("Child name must contain at least 4 characters.");
        }

        return codeOK && nameOK;
    }

    @Override
    public void setLoaderVisibility(int visibility) {
        loader.setVisibility(visibility);
    }

    @Override
    public void goToNextPage() {
        ActivityUtil.pushFragment(Screen3_Fragment.newInstance(), getActivity().getSupportFragmentManager(), R.id.fragment_container_main, true);
    }

    @Override
    public void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
