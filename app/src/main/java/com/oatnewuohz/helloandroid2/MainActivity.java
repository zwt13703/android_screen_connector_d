package com.oatnewuohz.helloandroid2;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.provider.Settings;
import android.content.Intent;
import android.net.Uri;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WindowManager windowManager;
    private View clickButton;
    private View targetPoint;
    private View panelView;
    private View panelButton;
    private WindowManager.LayoutParams clickButtonParams;
    private WindowManager.LayoutParams targetPointParams;
    private WindowManager.LayoutParams panelParams;
    private WindowManager.LayoutParams panelButtonParams;
    private float initialTouchX, initialTouchY;
    private float initialX, initialY;
    private boolean isDragging = false;
    private boolean isPanelExpanded = false;
    private boolean isDragEnabled = true;
    private int clickCount = 16;
    private int clickInterval = 100; // 默认点击间隔为100ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建悬浮窗按钮
        createFloatingButtons();
        // 创建面板
        createPanel();
    }

    private void createPanel() {
        // 创建面板按钮
        panelButton = LayoutInflater.from(this).inflate(R.layout.panel_button, null);
        panelButtonParams = new WindowManager.LayoutParams(
                150,
                150,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        panelButtonParams.gravity = Gravity.TOP | Gravity.END;
        panelButtonParams.x = 0;
        panelButtonParams.y = 100;

        // 创建面板视图
        panelView = LayoutInflater.from(this).inflate(R.layout.panel_layout, null);
        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.TOP | Gravity.END;
        panelParams.x = 0;
        panelParams.y = 100;

        setupPanelControls();

        // 添加面板按钮到窗口
        windowManager.addView(panelButton, panelButtonParams);
    }

    private void setupPanelControls() {
        // 设置面板按钮点击事件
        panelButton.setOnClickListener(v -> {
            togglePanel();
        });

        // 设置面板最小化按钮点击事件
        ImageButton minimizeButton = panelView.findViewById(R.id.minimize_button);
        minimizeButton.setOnClickListener(v -> {
            togglePanel();
        });

        // 设置面板拖动
        panelView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = panelParams.x;
                    initialY = panelParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getRawX() - initialTouchX;
                    float deltaY = event.getRawY() - initialTouchY;
                    panelParams.x = (int) (initialX + deltaX);
                    panelParams.y = (int) (initialY + deltaY);
                    windowManager.updateViewLayout(panelView, panelParams);
                    
                    // 同步更新按钮位置
                    panelButtonParams.x = panelParams.x;
                    panelButtonParams.y = panelParams.y;
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;
            }
            return false;
        });

        // 设置拖动控制复选框
        CheckBox enableDragCheckbox = panelView.findViewById(R.id.enable_drag_checkbox);
        enableDragCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDragEnabled = isChecked;
            updateButtonsTouchListeners();
        });

        // 设置点击次数输入框
        EditText clicksInput = panelView.findViewById(R.id.clicks_input);
        clicksInput.setText(String.valueOf(clickCount));
        clicksInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                try {
                    clickCount = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    clicksInput.setText(String.valueOf(clickCount));
                }
            }
        });

        // 设置点击间隔输入框
        EditText intervalInput = panelView.findViewById(R.id.interval_input);
        intervalInput.setText(String.valueOf(clickInterval));
        intervalInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                try {
                    clickInterval = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    intervalInput.setText(String.valueOf(clickInterval));
                }
            }
        });
    }

    private void updateButtonsTouchListeners() {
        // 更新点击按钮的触摸监听器
        clickButton.setOnTouchListener((v, event) -> {
            // 如果面板展开，只允许拖动
            if (isPanelExpanded) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = clickButtonParams.x;
                        initialY = clickButtonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        clickButtonParams.x = (int) (initialX + (int) (event.getRawX() - initialTouchX));
                        clickButtonParams.y = (int) (initialY + (int) (event.getRawY() - initialTouchY));
                        windowManager.updateViewLayout(clickButton, clickButtonParams);
                        return true;
                }
                return false;
            }
            // 面板收起时，允许点击
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 获取输入的点击次数
                EditText clicksInput = panelView.findViewById(R.id.clicks_input);
                String clicksStr = clicksInput.getText().toString();
                int clicks = 16; // 默认值
                try {
                    clicks = Integer.parseInt(clicksStr);
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
                
                // 执行点击
                performClick(clicks);
                return true;
            }
            return false;
        });

        // 更新目标点的触摸监听器
        targetPoint.setOnTouchListener((v, event) -> {
            // 如果面板展开，允许拖动
            if (isPanelExpanded) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = targetPointParams.x;
                        initialY = targetPointParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        targetPointParams.x = (int) initialX + (int) (event.getRawX() - initialTouchX);
                        targetPointParams.y = (int) initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(targetPoint, targetPointParams);
                        return true;
                }
            }
            return false;
        });
    }

    private void togglePanel() {
        isPanelExpanded = !isPanelExpanded;
        if (isPanelExpanded) {
            // 显示面板
            windowManager.removeView(panelButton);
            windowManager.addView(panelView, panelParams);
        } else {
            // 显示面板按钮
            windowManager.removeView(panelView);
            windowManager.addView(panelButton, panelButtonParams);
        }
        // 更新按钮状态
        updateButtonsTouchListeners();
    }

    private void createFloatingButtons() {
        // 创建点击按钮
        clickButton = LayoutInflater.from(this).inflate(R.layout.click_button, null);
        clickButtonParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        clickButtonParams.gravity = Gravity.TOP | Gravity.START;
        clickButtonParams.x = 100;
        clickButtonParams.y = 100;

        // 创建目标点
        targetPoint = LayoutInflater.from(this).inflate(R.layout.target_point, null);
        targetPointParams = new WindowManager.LayoutParams(
                60,
                60,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        targetPointParams.gravity = Gravity.TOP | Gravity.START;
        targetPointParams.x = 300;
        targetPointParams.y = 300;

        // 添加按钮到窗口
        windowManager.addView(clickButton, clickButtonParams);
        windowManager.addView(targetPoint, targetPointParams);

        // 初始化按钮的触摸监听器
        updateButtonsTouchListeners();
    }

    private void performClick(int clicks) {
        // 执行点击
        Intent intent = new Intent(this, AutoClickService.class);
        intent.putExtra("x", targetPointParams.x);
        intent.putExtra("y", targetPointParams.y);
        intent.putExtra("clicks", clicks);
        intent.putExtra("interval", clickInterval);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickButton != null && clickButton.isAttachedToWindow()) {
            windowManager.removeView(clickButton);
        }
        if (targetPoint != null && targetPoint.isAttachedToWindow()) {
            windowManager.removeView(targetPoint);
        }
        if (panelView != null && panelView.isAttachedToWindow()) {
            windowManager.removeView(panelView);
        }
        if (panelButton != null && panelButton.isAttachedToWindow()) {
            windowManager.removeView(panelButton);
        }
    }
}