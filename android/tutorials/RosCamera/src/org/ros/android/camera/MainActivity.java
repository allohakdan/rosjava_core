/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import org.ros.rosjava.android.CameraPublisher;
import org.ros.rosjava.android.views.CameraPreviewView;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {
  
  private final CameraPublisher publisher;
  
  private CameraPreviewView preview;
  private int cameraId;
  
  public MainActivity() {
    publisher = new CameraPublisher();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    preview = new CameraPreviewView(this);
    setContentView(preview);
  }

  @Override
  protected void onResume() {
    super.onResume();
    cameraId = 1;
    preview.setCamera(Camera.open(cameraId));
    preview.setPreviewCallback(publisher);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      int numCameras = Camera.getNumberOfCameras();
      cameraId = (cameraId + 1) % numCameras;
      Log.i("RosCamera", "Switching to " + cameraId);
      preview.releaseCamera();
      preview.setCamera(Camera.open(cameraId));
    }
    return true;
  }
 
  @Override
  protected void onPause() {
    super.onPause();
    preview.releaseCamera();
    publisher.stop();
  }

}