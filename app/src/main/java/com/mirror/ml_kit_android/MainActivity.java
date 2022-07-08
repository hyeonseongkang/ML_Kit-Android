package com.mirror.ml_kit_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    static final int REQUEST_CODE = 1;

    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;


    ImageView imageView;
    Bitmap bitmap;
    Uri uri;
    InputImage image;

    AppCompatButton getImageButton, detectionFaceButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        imageView = (ImageView) findViewById(R.id.imageView);

        getImageButton = (AppCompatButton) findViewById(R.id.getImageButton);
        detectionFaceButton = (AppCompatButton) findViewById(R.id.detectionFaceButton);

        getImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        detectionFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // detector에 대한 옵션 설정
                FaceDetectorOptions highAccuracyOpts =
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .build();

                // detector생성
                FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        processFaceContourDetectionResult(faces);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        e.printStackTrace();
                                    }
                                });
            }
        });
    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Task completed successfully
        if (faces.size() == 0) {
            Toast.makeText(this, "No face found", Toast.LENGTH_SHORT).show();
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
    }



    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = imageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = imageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_CODE) {
            uri = data.getData();
            setImage(uri);
        }
    }

    // uri를 비트맵으로 변환시킨후 이미지뷰에 띄워주고 InputImage를 생성하는 메서드
    private void setImage(Uri uri) {
        try{
            InputStream in = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in);
            if (bitmap != null) {
                // Get the dimensions of the View
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

                int targetWidth = targetedSize.first;
                int maxHeight = targetedSize.second;

                // Determine how much to scale down the image
                float scaleFactor =
                        Math.max(
                                (float) bitmap.getWidth() / (float) targetWidth,
                                (float) bitmap.getHeight() / (float) maxHeight);

                Bitmap resizedBitmap =
                        Bitmap.createScaledBitmap(
                                bitmap,
                                (int) (bitmap.getWidth() / scaleFactor),
                                (int) (bitmap.getHeight() / scaleFactor),
                                true);

                imageView.setImageBitmap(resizedBitmap);
                image = InputImage.fromBitmap(resizedBitmap, 0);
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}