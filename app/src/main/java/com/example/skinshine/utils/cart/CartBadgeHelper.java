package com.example.skinshine.utils.cart;

import android.view.View;
import android.widget.TextView;

import com.example.skinshine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CartBadgeHelper {

    public static void updateCartBadge(View cartBadgeContainer) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            hideCartBadge(cartBadgeContainer);
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView cartBadge = cartBadgeContainer.findViewById(R.id.cartBadge);

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

                    if (totalItems > 0) {
                        cartBadge.setText(totalItems > 99 ? "99+" : String.valueOf(totalItems));
                        cartBadge.setVisibility(View.VISIBLE);
                    } else {
                        cartBadge.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> hideCartBadge(cartBadgeContainer));
    }

    private static void hideCartBadge(View cartBadgeContainer) {
        TextView cartBadge = cartBadgeContainer.findViewById(R.id.cartBadge);
        cartBadge.setVisibility(View.GONE);
    }
}
