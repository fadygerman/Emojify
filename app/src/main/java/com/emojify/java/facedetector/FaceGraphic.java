/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emojify.java.facedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import com.emojify.GraphicOverlay;
import com.emojify.GraphicOverlay.Graphic;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType;

import java.util.Locale;

/**
 * Graphic instance for rendering emojis within the associated graphic overlay view.
 */
public class FaceGraphic extends Graphic {
  private static final String JOY_EMOJI = "\uD83D\uDE02"; // 😂
  private static final String GRINNING_EMOJI = "\uD83D\uDE00"; // 😀
  private static final String SMILING_EMOJI = "\uD83D\ude42"; // 🙂
  private static final String NEUTRAL_EMOJI = "\uD83D\uDE10"; // 😐
  private static final String WINKING_EMOJI = "\uD83D\uDE09"; // 😉
  private static final String SLEEPING_EMOJI = "\uD83D\uDE34"; // 😴
  private static final float FACE_POSITION_RADIUS = 8.0f;
  private static final float ID_TEXT_SIZE = 30.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;
  private final Paint facePositionPaint;
  private volatile Face face;
  private float lipDistance;

  FaceGraphic(GraphicOverlay overlay, Face face) {
    super(overlay);

    this.face = face;
    facePositionPaint = new Paint();
  }

  /**
   * Draws the emojis over the faces depending on expression features.
   */
  @Override
  public void draw(Canvas canvas) {

    Face face = this.face;
    if (face == null) {
      return;
    }

    // Get center points of box around face
    float x = translateX(face.getBoundingBox().centerX());
    float y = translateY(face.getBoundingBox().centerY());

    // Calculate helpful positions
    float xOffset = scale(face.getBoundingBox().width() / 2.0f);
    float yOffset = scale(face.getBoundingBox().height() / 2.0f);
    float left = x - xOffset;
    float top = y - yOffset;
    float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
    float yLabelOffset = (face.getTrackingId() == null) ? 0 : -lineHeight;

    // Calculate face size
    float faceWidth = face.getBoundingBox().width();
    float faceHeight = face.getBoundingBox().height();

    // Paint object for debugging labels
    Paint labelPaint = new Paint();
    labelPaint.setColor(Color.WHITE);
    labelPaint.setTextSize(ID_TEXT_SIZE);

    // Emoji paint object
    Paint emojiPaint = new Paint();
    float emojiSize = (faceWidth + faceHeight) * 1.7f;
    emojiPaint.setTextSize(emojiSize);
    emojiPaint.setAlpha(200);

    // Get mouth information e.g. distance between upper and lower lip
    FaceContour upperLipBottom = face.getContour(9);
    FaceContour lowerLipBottom = face.getContour(11);
    if (upperLipBottom != null && lowerLipBottom != null) {
      lipDistance = lowerLipBottom.getPoints().get(4).y - upperLipBottom.getPoints().get(4).y;
    }

    // Determine which emoji to display
    String emoji = NEUTRAL_EMOJI;
    if (face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null &&
            face.getLeftEyeOpenProbability() > 0.5 && face.getRightEyeOpenProbability() > 0.5) // Both eyes open
    {
      if (face.getSmilingProbability() != null && face.getSmilingProbability() > 0.99 && (lipDistance/emojiSize) > .05) {
        emoji = JOY_EMOJI;
      } else if (face.getSmilingProbability() != null && face.getSmilingProbability() > 0.75 && (lipDistance/emojiSize) > .03) {
        emoji = GRINNING_EMOJI;
      } else if (face.getSmilingProbability() != null && face.getSmilingProbability() >= 0.5) {
        emoji = SMILING_EMOJI;
      }
    } else if (face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null) {
      if (face.getLeftEyeOpenProbability() < 0.5 && face.getRightEyeOpenProbability() < 0.5) {
        emoji = SLEEPING_EMOJI;
      } else if (face.getLeftEyeOpenProbability() < 0.5 || face.getRightEyeOpenProbability() < 0.5) {
        emoji = WINKING_EMOJI;
      }
    }

    // Draw the emoji
    canvas.drawText(
            emoji,
            x - (emojiSize / 1.7f),
            y + emojiSize / 2.8f,
            emojiPaint);

    // for debugging
//    canvas.drawText(
//            "Distance: " + lipDistance,
//            left,
//            top += yLabelOffset,
//            labelPaint
//    );
//    yLabelOffset += lineHeight;
//
//    canvas.drawText(
//            "Emoji Size: " + emojiSize,
//            left,
//            top += yLabelOffset,
//            labelPaint
//    );
    yLabelOffset += lineHeight;

  }
}
