package com.example.skinshine;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.skinshine.databinding.ActivityMainBinding;
import com.example.skinshine.ui.cart.CartViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.zalopay.sdk.ZaloPaySDK;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CartViewModel cartViewModel;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;
    private String currentUserRole = "customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupSystemBars();
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        handleUserRoleFromIntent();
        setupNavigation();
        setupAuthStateListener();
        ZaloPaySDK.init(2553, vn.zalopay.sdk.Environment.SANDBOX);
    }

    private void handleUserRoleFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_ROLE")) {
            String roleFromIntent = intent.getStringExtra("USER_ROLE");
            currentUserRole = roleFromIntent != null ? roleFromIntent : "customer";
        } else {
            currentUserRole = "customer";
        }
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        if ("staff".equals(currentUserRole)) {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.staff_bottom_nav_menu);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_staff_dashboard,
                    R.id.navigation_staff_orders,
                    R.id.navigation_staff_customers,
                    R.id.navigation_staff_inventory,
                    R.id.navigation_member)
                    .build();

            NavigationUI.setupWithNavController(navView, navController);
            navController.navigate(R.id.navigation_staff_dashboard);

        } else {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_nav_menu);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_category,
                    R.id.navigation_analyse,
                    R.id.navigation_order_history,
                    R.id.navigation_member)
                    .build();

            NavigationUI.setupWithNavController(navView, navController);
            navController.navigate(R.id.navigation_home);
        }
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.registerFragment ||
                    destination.getId() == R.id.cartFragment ||
                    destination.getId() == R.id.productDetailFragment ||
                    destination.getId() == R.id.productComparisonFragment ||
                    destination.getId() == R.id.checkoutFragment ||
                    destination.getId() == R.id.staffOrderDetailFragment ||
                    destination.getId() == R.id.customerOrdersFragment) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_green, getTheme()));
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                currentUserRole = "customer";
                setupNavigation();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleUserRoleFromIntent();
        setupNavigation();
        ZaloPaySDK.getInstance().onResult(intent);
    }
}