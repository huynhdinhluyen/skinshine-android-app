package com.example.skinshine.ui.analyse;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.skinshine.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class AnalyseFragment extends Fragment {
    private static final String TAG = "AnalyseFragment";
    private AnalyseViewModel viewModel;
    private PreviewView cameraPreview;
    private ImageView capturedImageView;
    private CardView resultCard;
    private TextView resultText, treatmentText;
    private Button captureButton, helpButton, retryButton;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;

    // Permission launcher for camera
    private ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setupCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analyse, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AnalyseViewModel.class);

        // Initialize views
        cameraPreview = root.findViewById(R.id.camera_preview);
        capturedImageView = root.findViewById(R.id.captured_image);
        resultCard = root.findViewById(R.id.result_card);
        resultText = root.findViewById(R.id.result_text);
        captureButton = root.findViewById(R.id.capture_button);
        helpButton = root.findViewById(R.id.help_button);
        retryButton = root.findViewById(R.id.retry_button);
        treatmentText = root.findViewById(R.id.treatment_text);

        // Set up observers
        viewModel.getSkinResult().observe(getViewLifecycleOwner(), result -> {
            resultText.setText("Loại da của bạn: " + result);
            resultCard.setVisibility(View.VISIBLE);
        });

        viewModel.getTreatmentSchedule().observe(getViewLifecycleOwner(), schedule -> {
            treatmentText.setText(schedule);
        });

        viewModel.getIsProcessing().observe(getViewLifecycleOwner(), isProcessing -> {
            captureButton.setEnabled(!isProcessing);
            helpButton.setEnabled(!isProcessing);
            captureButton.setText(isProcessing ? "Đang phân tích..." : "Chụp ảnh");
        });

        // Set up button click listeners
        captureButton.setOnClickListener(v -> captureImage());
        helpButton.setOnClickListener(v -> showHelp());
        retryButton.setOnClickListener(v -> resetCamera());

        // Check camera permission
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Load ONNX model
        viewModel.loadModel(requireContext());

        return root;
    }

    private void showHelp() {
        // Display help information
        Toast.makeText(requireContext(),
                "Đảm bảo camera thấy cả gương mặt và điều kiện ánh sáng tốt để có kết quả chính xác hơn.",
                Toast.LENGTH_LONG).show();
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCase();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Có lỗi xảy ra khi cấu hình camera", e);
                Toast.makeText(getContext(), "Có lỗi xảy ra khi cấu hình camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCase() {
        if (cameraProvider == null) return;

        // Unbind previous use cases
        cameraProvider.unbindAll();

        // Set up preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        // Set up image capture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Select front camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        // Bind use cases to camera
        try {
            cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Log.e(TAG, "Có lỗi xảy ra khi cấu hình camera", e);
            Toast.makeText(getContext(), "Có lỗi xảy ra khi cấu hình camera: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void captureImage() {
        if (imageCapture == null) return;

        // Show processing state
        viewModel.setProcessing(true);

        // Capture the image
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        try {
                            // Convert image to bitmap
                            Bitmap bitmap = imageToBitmap(imageProxy);

                            // Display captured image
                            capturedImageView.setImageBitmap(bitmap);
                            capturedImageView.setVisibility(View.VISIBLE);
                            cameraPreview.setVisibility(View.GONE);

                            // Show retry button
                            captureButton.setVisibility(View.GONE);
                            helpButton.setVisibility(View.GONE);
                            retryButton.setVisibility(View.VISIBLE);

                            // Process image with ONNX model
                            viewModel.processSkinImage(bitmap);
                        } catch (Exception e) {
                            Log.e(TAG, "Có lỗi xảy ra khi xử lý ảnh:", e);
                            Toast.makeText(getContext(), "Có lỗi xảy ra khi xử lý ảnh: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            viewModel.setProcessing(false);
                        } finally {
                            // Close the image
                            imageProxy.close();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Có lỗi xảy ra khi chụp ảnh", exception);
                        Toast.makeText(getContext(), "Có lỗi xảy ra khi chụp ảnh: " +
                                exception.getMessage(), Toast.LENGTH_SHORT).show();
                        viewModel.setProcessing(false);
                    }
                });
    }

    private void resetCamera() {
        // Hide captured image and show camera preview
        capturedImageView.setVisibility(View.GONE);
        cameraPreview.setVisibility(View.VISIBLE);

        // Hide retry button
        captureButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);

        // Hide results card
        resultCard.setVisibility(View.GONE);

        // Reset any other state as needed
        captureButton.setText("CAPTURE");
        captureButton.setEnabled(true);
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Handle rotation based on image orientation
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            // For front camera, we might need to flip horizontally
            matrix.postScale(-1, 1); // Flip horizontally for front camera
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
        }

        return bitmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}