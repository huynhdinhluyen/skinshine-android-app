package com.example.skinshine.ui.analyse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class AnalyseViewModel extends ViewModel {
    private static final String TAG = "AnalyseViewModel";
    private final MutableLiveData<String> skinResult = new MutableLiveData<>();
    private final MutableLiveData<String> treatmentSchedule = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final String[] skinTypes = {
            "Normal", "Dry", "Oily", "Acne", "Blackheads", "Wrinkles", "Dark Spots"
    };

    private final String[] treatmentSchedules = {
            // Normal
            "Maintain a balanced routine: gentle cleanser, moisturizer, sunscreen. Exfoliate 1-2x/week.",
            // Dry
            "Use hydrating cleanser, rich moisturizer, avoid hot water. Apply hyaluronic acid and use sunscreen.",
            // Oily
            "Cleanse twice daily, use oil-free moisturizer, non-comedogenic sunscreen. Exfoliate 2x/week.",
            // Acne
            "Use gentle cleanser, salicylic acid or benzoyl peroxide treatments, oil-free moisturizer, sunscreen.",
            // Blackheads
            "Cleanse with salicylic acid, exfoliate regularly, use clay masks, non-comedogenic products.",
            // Wrinkles
            "Apply retinoids at night, use antioxidant serum, moisturizer, and broad-spectrum sunscreen daily.",
            // Dark Spots
            "Use vitamin C serum, sunscreen daily, gentle exfoliation, consider niacinamide or licorice extract."
    };

    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private boolean modelLoaded = false;

    public LiveData<String> getSkinResult() {
        return skinResult;
    }

    public LiveData<String> getTreatmentSchedule() {
        return treatmentSchedule;
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing.setValue(processing);
    }

    public void loadModel(Context context) {
        if (modelLoaded) return;

        executorService.execute(() -> {
            try {
                ortEnvironment = OrtEnvironment.getEnvironment();
                OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();

                File modelFile = new File(context.getCacheDir(), "skin_model.onnx");
                if (!modelFile.exists()) {
                    copyModelFromAssets(context, modelFile);
                }

                ortSession = ortEnvironment.createSession(modelFile.getAbsolutePath(), sessionOptions);

                mainHandler.post(() -> modelLoaded = true);
            } catch (IOException | OrtException e) {
                Log.e(TAG, "Error loading model", e);
                mainHandler.post(() -> {
                    skinResult.setValue("Error loading model: " + e.getMessage());
                    treatmentSchedule.setValue("");
                    isProcessing.setValue(false);
                });
            }
        });
    }

    private void copyModelFromAssets(Context context, File outputFile) throws IOException {
        try (InputStream is = context.getAssets().open("skin_model.onnx");
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
        }
    }

    public void processSkinImage(Bitmap bitmap) {
        isProcessing.setValue(true);

        executorService.execute(() -> {
            try {
                if (!modelLoaded || ortSession == null) {
                    throw new IllegalStateException("Model not loaded");
                }

                float[] inputData = preprocessBitmap(bitmap, 224, 224);
                long[] shape = new long[]{1, 3, 224, 224};
                OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, FloatBuffer.wrap(inputData), shape);

                OrtSession.Result result = ortSession.run(Collections.singletonMap(ortSession.getInputNames().iterator().next(), inputTensor));
                float[][] output = (float[][]) result.get(0).getValue();

                int predictedIdx = argmax(output[0]);
                String predictedSkinType = skinTypes[predictedIdx];
                String schedule = treatmentSchedules[predictedIdx];

                mainHandler.post(() -> {
                    skinResult.setValue(predictedSkinType);
                    treatmentSchedule.setValue(schedule);
                    isProcessing.setValue(false);
                });

                inputTensor.close();
                result.close();
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing skin image", e);
                mainHandler.post(() -> {
                    skinResult.setValue("Error analyzing skin: " + e.getMessage());
                    treatmentSchedule.setValue("");
                    isProcessing.setValue(false);
                });
            }
        });
    }

    private float[] preprocessBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        int[] pixels = new int[targetWidth * targetHeight];
        resized.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight);

        float[] input = new float[3 * targetWidth * targetHeight];
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int idx = y * targetWidth + x;
                int pixel = pixels[idx];
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;
                input[0 * targetWidth * targetHeight + idx] = r;
                input[1 * targetWidth * targetHeight + idx] = g;
                input[2 * targetWidth * targetHeight + idx] = b;
            }
        }
        return input;
    }

    private int argmax(float[] array) {
        int maxIdx = 0;
        float maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        if (ortSession != null) {
            try {
                ortSession.close();
            } catch (OrtException e) {
                Log.e(TAG, "Error closing ONNX session", e);
            }
        }
        if (ortEnvironment != null) {
            ortEnvironment.close();
        }
    }
}