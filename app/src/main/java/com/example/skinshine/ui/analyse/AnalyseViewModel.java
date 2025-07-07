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
//            "Normal", "Dry", "Oily", "Acne", "Blackheads", "Wrinkles", "Dark Spots"
            "Bình thường", "Khô", "Da dầu", "Mụn trứng cá", "Mụn đầu đen", "Nếp nhăn", "Đốm đen"
    };

    private final String[] treatmentSchedules = {
            // Bình thường
            "Duy trì thói quen cân bằng: sữa rửa mặt dịu nhẹ, kem dưỡng ẩm, kem chống nắng. Tẩy tế bào chết 1-2 lần/tuần.",
            // Khô
            "Sử dụng sữa rửa mặt dưỡng ẩm, kem dưỡng ẩm giàu dưỡng chất, tránh nước nóng. Thoa axit hyaluronic và sử dụng kem chống nắng.",
            // Da dầu
            "Rửa mặt hai lần mỗi ngày, sử dụng kem dưỡng ẩm không chứa dầu, kem chống nắng không gây mụn. Tẩy tế bào chết 2 lần/tuần.",
            // Mụn trứng cá
            "Sử dụng sữa rửa mặt dịu nhẹ, phương pháp điều trị bằng axit salicylic hoặc benzoyl peroxide, kem dưỡng ẩm không chứa dầu, kem chống nắng.",
            // Mụn đầu đen
            "Rửa mặt bằng axit salicylic, tẩy tế bào chết thường xuyên, sử dụng mặt nạ đất sét, các sản phẩm không gây mụn.",
            // Nếp nhăn
            "Thoa retinoid vào ban đêm, sử dụng huyết thanh chống oxy hóa, kem dưỡng ẩm và kem chống nắng phổ rộng hàng ngày.",
            // Đốm đen
            "Sử dụng huyết thanh vitamin C, kem chống nắng hàng ngày, tẩy tế bào chết nhẹ nhàng, cân nhắc sử dụng niacinamide hoặc chiết xuất cam thảo."
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
                Log.e(TAG, "Có lỗi xảy ra khi tải model", e);
                mainHandler.post(() -> {
                    skinResult.setValue("Có lỗi xảy ra khi tải model: " + e.getMessage());
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
                    throw new IllegalStateException("Không tải được model");
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
                Log.e(TAG, "Có lỗi xảy ra khi phaân tích hình ảnh:", e);
                mainHandler.post(() -> {
                    skinResult.setValue("Có lỗi xảy ra khi phaân tích hình ảnh: " + e.getMessage());
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
                Log.e(TAG, "Có lỗi xảy ra khi đóng tiến trình ONNX", e);
            }
        }
        if (ortEnvironment != null) {
            ortEnvironment.close();
        }
    }
}