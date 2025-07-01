package com.example.skinshine.ui.order_history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.data.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orders;

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history,
                parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textOrderStatus, textOrderDate, textOrderItems, textOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderItems = itemView.findViewById(R.id.textOrderItems);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
        }

        void bind(Order order) {
            // Order ID
            textOrderId.setText("Đơn hàng #" + (order.getId() != null ? order.getId().substring(0, 8) : "N/A"));

            // Order Date
            if (order.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                textOrderDate.setText("Ngày đặt: " + sdf.format(order.getCreatedAt()));
            } else {
                textOrderDate.setText("Ngày đặt: Không rõ");
            }

            // Order Total
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalAmount()));

            // Order Items Summary
            List<CartItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                String firstItemName = items.get(0).getProductName();
                if (items.size() > 1) {
                    int otherItemsCount = items.size() - 1;
                    textOrderItems.setText("Sản phẩm: " + firstItemName + " và " + otherItemsCount + " sản phẩm khác");
                } else {
                    textOrderItems.setText("Sản phẩm: " + firstItemName);
                }
            } else {
                textOrderItems.setText("Không có thông tin sản phẩm");
            }

            // Order Status
            setStatusAppearance(textOrderStatus, order.getStatus());
        }

        private void setStatusAppearance(TextView statusView, String status) {
            if (status == null) status = "PENDING_PAYMENT";

            Context context = statusView.getContext();
            String statusText;
            int backgroundResId;

            switch (status.toUpperCase()) {
                case "PROCESSING":
                    statusText = "Đang xử lý";
                    backgroundResId = R.drawable.status_background_processing;
                    break;
                case "DELIVERED":
                    statusText = "Đã giao";
                    backgroundResId = R.drawable.status_background_delivered;
                    break;
                case "CANCELLED":
                    statusText = "Đã hủy";
                    backgroundResId = R.drawable.status_background_cancelled;
                    break;
                case "PENDING_PAYMENT":
                default:
                    statusText = "Chờ thanh toán";
                    backgroundResId = R.drawable.status_background_pending;
                    break;
            }
            statusView.setText(statusText);
            statusView.setBackground(ContextCompat.getDrawable(context, backgroundResId));
        }
    }
}
