# SkinShine Android App

SkinShine is an Android application that helps users analyze their skin, get personalized treatment schedules, browse and buy skincare products, and manage orders—all in one place.

## Table of Contents

1. [Features](#features)  
2. [Architecture](#architecture)  
3. [Getting Started](#getting-started)  
4. [Project Structure](#project-structure)  
5. [Dependencies](#dependencies)  
6. [Usage](#usage)  
7. [Contributing](#contributing)  
8. [License](#license)  

## Features

- **Skin Analysis**  
  - Upload or capture a photo of your face, processed by [`AnalyseViewModel`](app/src/main/java/com/example/skinshine/ui/analyse/AnalyseViewModel.java) using an ONNX model in `app/src/main/assets/skin_model.onnx`.  
- **Personalized Treatment Schedule**  
  - Displays recommended routines based on analysis.  
- **Product Catalog & Search**  
  - Browse categories and brands, powered by [`ProductRepositoryImpl`](app/src/main/java/com/example/skinshine/data/repository/impl/ProductRepositoryImpl.java).  
- **Shopping Cart & Checkout**  
  - Add/remove items in [`CartFragment`](app/src/main/java/com/example/skinshine/ui/cart/CartFragment.java) and place orders via [`CheckoutFragment`](app/src/main/java/com/example/skinshine/ui/checkout/CheckoutFragment.java).  
- **Order Management**  
  - View past orders in [`OrderHistoryFragment`](app/src/main/java/com/example/skinshine/ui/order_history/OrderHistoryFragment.java) and details in [`OrderDetailFragment`](app/src/main/java/com/example/skinshine/ui/order_detail/OrderDetailFragment.java).  
- **Profile & Support**  
  - Edit your profile in [`ProfileFragment`](app/src/main/java/com/example/skinshine/ui/profile/ProfileFragment.java) and navigate to order history or dashboard.  
- **Admin / Staff Dashboard**  
  - Staff can view charts in [`AdminFragment`](app/src/main/java/com/example/skinshine/ui/admin/AdminFragment.java) or manage orders/customers in `ui/staff/*`.  
- **Payment Integration**  
  - ZaloPay SDK integrated in [`PaymentRepositoryImpl`](app/src/main/java/com/example/skinshine/data/repository/impl/PaymentRepositoryImpl.java).  

## Architecture

- MVVM pattern using Android Jetpack:  
  - ViewModels in `ui/**/**ViewModel.java`  
  - LiveData for data binding and state  
- Repository pattern in `data/repository/**` for Firestore, Auth, and network calls  
- Navigation Component configured in [mobile_navigation.xml](app/src/main/res/navigation/mobile_navigation.xml)  
- Singleton helper in [`ComparisonManager`](app/src/main/java/com/example/skinshine/utils/product/ComparisonManager.java)  

## Getting Started

1. **Clone the repo**  
   ```bash
   git clone https://github.com/your-org/skinshine-android-app.git
   cd skinshine-android-app
   ```  
2. **Open in Android Studio**  
   - Import the Gradle project (`build.gradle.kts`).  
3. **Firebase Setup**  
   - Create a Firebase project, enable Authentication & Firestore.  
   - Place `google-services.json` in `app/`.  
4. **ONNX Model**  
   - Copy `skin_model.onnx` to `app/src/main/assets/`.  
5. **ZaloPay Integration**  
   - Follow ZaloPay docs, add your App ID in `local.properties`:  
     ```properties
     ZALOPAY_APP_ID=your_app_id
     ```  
6. **Run the App**  
   - Connect a device or emulator, click **Run** in Android Studio.  

## Project Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/skinshine/
│   │   │   │   ├── ui/            # Fragments, Activities, ViewModels, Adapters
│   │   │   │   ├── data/          # Models, Repositories (impl & interface)
│   │   │   │   ├── utils/         # Helpers like ComparisonManager
│   │   │   ├── res/               # layouts, drawables, navigation, values
│   │   └── assets/skin_model.onnx
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

## Dependencies

- AndroidX (Core, AppCompat, Lifecycle, Navigation, RecyclerView)  
- Material Components  
- Firebase (Auth, Firestore, Storage)  
- ONNX Runtime  
- ZaloPay SDK  
- Glide / Coil for image loading  

## Usage

- **Analyse**: Navigate to Analyse tab → take/upload a photo.  
- **Shop**: Browse Home or Categories → add to cart → checkout.  
- **Orders**: In Profile → Order History → tap on an order to view details.  
- **Admin**: If signed in as admin, access Dashboard from Profile.  

## Contributing

1. Fork the repo  
2. Create a feature branch  
3. Commit changes & push  
4. Open a Pull Request  

Please follow existing code style and MVVM patterns.

## License

This project is licensed under the Apache 2.0 License. See [LICENSE](LICENSE) for details.  
