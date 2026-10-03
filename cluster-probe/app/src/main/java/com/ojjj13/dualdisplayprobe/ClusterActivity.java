package com.ojjj13.dualdisplayprobe;
import android.app.Activity;
public final class ClusterActivity extends MainActivity {
    @Override protected String role() { return "CLUSTER"; }
    @Override protected Class<? extends Activity> otherActivity() { return MainActivity.class; }
}
