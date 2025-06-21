package com.example.skinshine;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.skinshine.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_category, R.id.navigation_cart, R.id.navigation_order_history, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.cartFragment ||
                    destination.getId() == R.id.registerFragment ||
                    destination.getId() == R.id.productDetailFragment) {
                binding.navView.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateBottomNavCartBadge() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.navView.getMenu().findItem(R.id.navigation_cart).setTitle("Giỏ hàng");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("cartItems")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int totalItems = 0;
                    for (var doc : snapshot) {
                        Long quantity = doc.getLong("quantity");
                        if (quantity != null) {
                            totalItems += quantity.intValue();
                        }
                    }

                    String title = totalItems > 0 ? "Giỏ hàng (" + totalItems + ")" : "Giỏ hàng";
                    binding.navView.getMenu().findItem(R.id.navigation_cart).setTitle(title);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavCartBadge();
    }
}