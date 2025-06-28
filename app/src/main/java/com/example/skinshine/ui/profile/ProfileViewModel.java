package com.example.skinshine.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;
import com.example.skinshine.data.repository.UserRepository;
import com.example.skinshine.data.repository.impl.UserRepositoryImpl;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final LiveData<Result<User>> currentUser;
    private final MediatorLiveData<Boolean> isUserAuthenticated;
    private final MediatorLiveData<Boolean> isLoading;
    private final MediatorLiveData<String> errorMessage;

    public ProfileViewModel() {
        this.userRepository = new UserRepositoryImpl();
        this.currentUser = userRepository.getCurrentUser();
        this.isUserAuthenticated = new MediatorLiveData<>();
        this.isLoading = new MediatorLiveData<>();
        this.errorMessage = new MediatorLiveData<>();

        // Observe authentication state
        isUserAuthenticated.setValue(FirebaseAuth.getInstance().getCurrentUser() != null);

        // Observe loading state
        isLoading.addSource(currentUser, result -> {
            if (result != null) {
                isLoading.setValue(result.isLoading());
            }
        });

        // Observe error state
        errorMessage.addSource(currentUser, result -> {
            if (result != null && result.isError()) {
                errorMessage.setValue(result.getMessage());
            } else if (result != null && result.isSuccess()) {
                errorMessage.setValue(null); // Clear error when successful
            }
        });
    }

    public LiveData<Result<User>> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsUserAuthenticated() {
        return isUserAuthenticated;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshUser() {
        userRepository.refreshCurrentUser();
    }

    public void updateProfile(String name, String phone, UserRepository.UserCallback callback) {
        userRepository.updateUserProfile(name, phone, callback);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        isUserAuthenticated.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userRepository instanceof UserRepositoryImpl) {
            ((UserRepositoryImpl) userRepository).detachListener();
        }
    }
}
