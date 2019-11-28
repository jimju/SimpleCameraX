package com.jimju.simplecamerax.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jimju.simplecamerax.BuildConfig;
import com.jimju.simplecamerax.R;
import com.jimju.simplecamerax.utils.ViewExtensions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    public final static String KEY_ROOT_DIRECTORY = "root_folder";
    public final static String[] EXTENSION_WHITELIST = new String[]{"JPG"};
    private File rootDirectory;
    private List<File> mediaList;
    private ViewPager mediaViewPager;

    private class MediaPagerAdapter extends FragmentStatePagerAdapter {


        public MediaPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return PhotoFragment.create(mediaList.get(position));
        }

        @Override
        public int getCount() {
            return mediaList.size();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        rootDirectory = new File(arguments.getString(KEY_ROOT_DIRECTORY));
        mediaList = new ArrayList();
        for (File f : rootDirectory.listFiles()) {
            mediaList.add(f);
        }

        mediaViewPager = view.findViewById(R.id.photo_view_pager);
        mediaViewPager.setOffscreenPageLimit(2);
        MediaPagerAdapter adapter = new MediaPagerAdapter(getChildFragmentManager());
        mediaViewPager.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewExtensions.padWithDisplayCutout(view.findViewById(R.id.cutout_safe_area));
        }

        view.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File mediaFile = mediaList.get(mediaViewPager.getCurrentItem());
                Context appContext = requireContext().getApplicationContext();
                Intent intent = new Intent();
                String extension = ViewExtensions.extension(mediaFile);
                String mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                Uri uri = FileProvider.getUriForFile(appContext, BuildConfig.APPLICATION_ID + ".provider",mediaFile);
                intent.putExtra(Intent.EXTRA_STREAM,uri);
                intent.setType(mediaType);
                intent.setAction(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent,getString(R.string.share_hint)));
            }
        });

        view.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaList.isEmpty()){
                    return;
                }
                Context context = getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog);
                AlertDialog dialog = builder.setTitle(getString(R.string.delete_title))
                        .setMessage(getString(R.string.delete_dialog))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File f = mediaList.get(mediaViewPager.getCurrentItem());
                                mediaList.remove(f);
                                mediaViewPager.getAdapter().notifyDataSetChanged();
                                if (mediaList.isEmpty()) {
                                    getFragmentManager().popBackStack();
                                }
                            }
                        }).setNegativeButton(android.R.string.no, null).create();
                ViewExtensions.showImmersive(dialog);
            }
        });

    }

}
