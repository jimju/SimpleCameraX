package com.jimju.simplecamerax.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;

public class PhotoFragment extends Fragment {
    private final static String FILE_NAME_KEY = "file_name";
    public  static PhotoFragment create(File image){
        Bundle arguments = new Bundle();
        arguments.putString(FILE_NAME_KEY,image.getAbsolutePath());
        PhotoFragment photoFragment = new PhotoFragment();
        photoFragment.setArguments(arguments);
        return photoFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new ImageView(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        String file = arguments.getString(FILE_NAME_KEY);
        Glide.with(this).load(file).into((ImageView) view);
    }

}
