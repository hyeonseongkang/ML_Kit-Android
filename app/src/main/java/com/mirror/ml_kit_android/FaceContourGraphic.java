package com.mirror.ml_kit_android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;

public class FaceContourGraphic extends GraphicOverlay.Graphic{

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float BOX_STROKE_WIDTH = 10.0f;

    private final Paint facePositionPaint;
    private final Paint boxPaint;
    private final Paint facePaint;

    private volatile Face face;

    public FaceContourGraphic(GraphicOverlay overlay) {
        super(overlay);

        facePositionPaint = new Paint();
        facePositionPaint.setColor(Color.RED);

        boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        facePaint = new Paint();
        facePaint.setColor(Color.BLUE);
    }

    public void updateFace(Face face) {
        this.face = face;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Face face = this.face;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);

        List<FaceContour> contour = face.getAllContours();

        // 얼굴 점 찍는 부분
        /*
        1. 얼굴 외각
        2. 왼쪽 눈썹 위
        3. 왼쪽 눈썹 아래
        4. 오른쪽 눈썹 위
        5. 오른쪽 눈썹 아래
        6. 왼쪽 눈 - 가로 길이: 8 - 4, 세로 길이: 12 - 4
        7. 오른쪽 눈 - 가로 길이: 8 - 4, 세로 길이: 12 - 4
        8. 윗 입술
        9. 윗 치아
        10. 아랫 치아
        11. 아랫 입술
        12. 코 세로
        13. 코 가로
        */
        for (FaceContour faceContour : contour) {
          for (PointF point : faceContour.getPoints()) {
            float px = translateX(point.x);
            float py = translateY(point.y);
              canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
          }
        }




        // left Eye Position
        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        if (leftEye != null) {
          canvas.drawCircle(
              translateX(leftEye.getPosition().x),
              translateY(leftEye.getPosition().y),
                  FACE_POSITION_RADIUS,
                  facePaint);

        }

        // Right Eye Position
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (rightEye != null) {
            canvas.drawCircle(
                    translateX(rightEye.getPosition().x),
                    translateY(rightEye.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePaint);
        }

        FaceLandmark leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK);
        if (leftCheek != null) {
            canvas.drawCircle(
                    translateX(leftCheek.getPosition().x),
                    translateY(leftCheek.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePaint);
        }

        FaceLandmark rightCheek =
                face.getLandmark(FaceLandmark.RIGHT_CHEEK);
        if (rightCheek != null) {
            canvas.drawCircle(
                    translateX(rightCheek.getPosition().x),
                    translateY(rightCheek.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePaint);
        }
    }
}
