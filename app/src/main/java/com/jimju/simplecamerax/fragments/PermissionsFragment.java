package com.jimju.simplecamerax.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavAction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.jimju.simplecamerax.R;

public class PermissionsFragment extends Fragment {
    private final static int PERMISSIONS_REQUEST_CODE = 10;
    private String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.permissionsFragment, true).build();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasPermissions()) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        } else {
            NavController controller = Navigation.findNavController(requireActivity(), R.id.fragment_container);
            controller.navigate(R.id.action_permissions_to_camera, null, navOptions);
        }
    }


    private boolean hasPermissions() {
        for (String s : PERMISSIONS_REQUIRED) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(requireActivity(), s)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(R.id.action_permissions_to_camera, null, navOptions);
            } else {
                Toast.makeText(getContext(), "Permission rquest denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
