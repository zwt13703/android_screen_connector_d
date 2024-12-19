package com.oatnewuohz.helloandroid2;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class AutoClickService extends AccessibilityService {
    private int clickCount = 0;
    private int targetClicks = 16;
    private int x = 0;
    private int y = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        if (intent != null) {
            final int x = intent.getIntExtra("x", 0);
            final int y = intent.getIntExtra("y", 0);
            final int clicks = intent.getIntExtra("clicks", 16);
            final int interval = intent.getIntExtra("interval", 100);

            new Thread(() -> {
                for (int i = 0; i < clicks && !Thread.interrupted(); i++) {
                    performClick(x, y);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                stopSelf();
            }).start();
        }
        return START_NOT_STICKY;
    }

    private void performClick(int x, int y) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 1));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
    }
}