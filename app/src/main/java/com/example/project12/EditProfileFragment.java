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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.project12.models.Profile;
import com.example.project12.ui.viewmodel.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileFragment extends DialogFragment {
    private static final int RC_PICK_PHOTO = 2001;

    private ProfileViewModel vm;
    private ImageView ivPhoto;
    private EditText etName, etPassword;
    private Spinner spCurrency;
    private Button btnChoosePhoto, btnSave, btnCancel;
    private Uri selectedPhotoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        ivPhoto       = view.findViewById(R.id.ivEditPhoto);
        etName        = view.findViewById(R.id.etEditName);
        etPassword    = view.findViewById(R.id.etEditPassword);
        spCurrency    = view.findViewById(R.id.spEditCurrency);
        btnChoosePhoto= view.findViewById(R.id.btnChoosePhoto);
        btnSave       = view.findViewById(R.id.btnSaveProfile);

        // Initialize currency spinner
        String[] currencies = {"ILS", "USD", "EUR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(adapter);

        // Observe current profile
        vm.getCurrentProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;
            etName.setText(profile.getName());
            int pos = adapter.getPosition(profile.getCurrency());
            if (pos >= 0) spCurrency.setSelection(pos);
            String url = profile.getPhotoUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).into(ivPhoto);
            }
        });
        vm.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
        });

        // Photo chooser
        btnChoosePhoto.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(pick, RC_PICK_PHOTO);
        });

        // Save changes
        btnSave.setOnClickListener(v -> {
            Profile cur = vm.getCurrentProfile().getValue();
            if (cur == null) return;

            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }
            cur.setName(newName);
            cur.setCurrency(spCurrency.getSelectedItem().toString());

            // Change password if needed
            String newPass = etPassword.getText().toString();
            if (!newPass.isEmpty()) {
                vm.changePassword(newPass);
            }

            // Upload photo if selected, then save profile
            if (selectedPhotoUri != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference ref = FirebaseStorage.getInstance()
                        .getReference("profile_photos/" + uid + ".jpg");
                ref.putFile(selectedPhotoUri)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return ref.getDownloadUrl();
                        })
                        .addOnSuccessListener(uri -> {
                            cur.setPhotoUrl(uri.toString());
                            vm.saveProfile(cur);
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                vm.saveProfile(cur);
                dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            ivPhoto.setImageURI(selectedPhotoUri);
        }
    }
}
