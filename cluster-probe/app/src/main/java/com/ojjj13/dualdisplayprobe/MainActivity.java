package com.ojjj13.dualdisplayprobe;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private PatternSurface pattern;
    protected String role() { return "MAIN"; }
    protected Class<? extends Activity> otherActivity() { return ClusterActivity.class; }

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        Button open = new Button(this);
        open.setText(role().equals("MAIN") ? "Open CLUSTER as a separate task" : "Open MAIN task");
        open.setOnClickListener(v -> startActivity(new Intent(this, otherActivity())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        root.addView(open, new LinearLayout.LayoutParams(-1, -2));
        pattern = new PatternSurface(this, role());
        root.addView(pattern, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }
    private void update(String state) {
        if (pattern != null) {
            int displayId = pattern.getDisplay() == null ? -1 : pattern.getDisplay().getDisplayId();
            pattern.status = "task " + getTaskId() + " | display " + displayId + " | " + state;
        }
    }
    @Override protected void onResume() { super.onResume(); update("RESUMED"); }
    @Override protected void onPause() { update("PAUSED (surface continues)"); super.onPause(); }
    @Override protected void onStop() { update("STOPPED"); super.onStop(); }
    @Override public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config); update("CONFIG CHANGED");
    }
    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused); update(focused ? "FOCUSED" : "UNFOCUSED");
    }
}
