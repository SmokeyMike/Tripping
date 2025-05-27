package com.example.project12;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.project12.R;
import com.example.project12.models.Profile;
import com.example.project12.ui.viewmodel.ProfileViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileFragment extends DialogFragment {
    private static final int RC_PICK_PHOTO = 2001;

    private ProfileViewModel vm;
    private ImageView ivPhoto;
    private EditText etName, etPassword;
    private Spinner spCurrency;
    private Button btnChoosePhoto, btnSave;

    private Uri selectedPhotoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        ivPhoto     = v.findViewById(R.id.ivEditPhoto);
        etName      = v.findViewById(R.id.etEditName);
        etPassword  = v.findViewById(R.id.etEditPassword);
        spCurrency  = v.findViewById(R.id.spEditCurrency);
        btnChoosePhoto = v.findViewById(R.id.btnChoosePhoto);
        btnSave     = v.findViewById(R.id.btnSaveProfile);

        // pre-fill current values
        vm.getCurrentProfile().observe(getViewLifecycleOwner(), p -> {
            if (p == null) return;
            etName.setText(p.getName());
            // assume you've populated your Spinner with currencies already
            int idx = ((ArrayAdapter<String>)spCurrency.getAdapter())
                    .getPosition(p.getCurrency());
            spCurrency.setSelection(idx);
            if (p.getPhotoUrl() != null) {
                Glide.with(this).load(p.getPhotoUrl()).into(ivPhoto);
            }
        });
        vm.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
        });

        btnChoosePhoto.setOnClickListener(c -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, RC_PICK_PHOTO);
        });

        btnSave.setOnClickListener(sv -> {
            Profile cur = vm.getCurrentProfile().getValue();
            if (cur == null) return;

            cur.setName(etName.getText().toString().trim());
            cur.setCurrency(spCurrency.getSelectedItem().toString());

            vm.saveProfile(cur);

            String newPass = etPassword.getText().toString();
            if (!newPass.isEmpty()) {
                vm.changePassword(newPass);
            }

            // if photo was chosen, upload it and then update URL in profile object:
            if (selectedPhotoUri != null) {
                // inside btnSave onClick, after selectedPhotoUri != null:
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference ref = FirebaseStorage.getInstance()
                        .getReference("profile_photos/" + uid + ".jpg");

                ref.putFile(selectedPhotoUri)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return ref.getDownloadUrl();
                        })
                        .addOnSuccessListener(uri -> {
                            // … same code to saveProfile
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(),
                                            "Upload failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        });

            }

            // dismiss fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(EditProfileFragment.this)
                    .commit();
        });
    }

    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == RC_PICK_PHOTO && res == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            ivPhoto.setImageURI(selectedPhotoUri);
        }
    }
}
