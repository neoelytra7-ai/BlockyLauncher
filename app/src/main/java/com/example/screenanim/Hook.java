package com.example.screenanim;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage {
  static XSharedPreferences prefs;
  static boolean active, wasActive, flash, inCall;
  static float[] v = new float[3];
  static float lastLevel; static long lastTime, lastReload;

  @Override public void handleLoadPackage(LoadPackageParam lp) {
    if (!"android".equals(lp.packageName)) return;
    try {
      prefs = new XSharedPreferences("com.example.screenanim", "cfg");
      final Class<?> cf = XposedHelpers.findClass("com.android.server.display.ColorFade", lp.classLoader);
      int cool = 2, warm = 1;
      try { cool = XposedHelpers.getStaticIntField(cf, "MODE_COOL_DOWN"); warm = XposedHelpers.getStaticIntField(cf, "MODE_WARM_UP"); } catch (Throwable ignored) {}
      final int COOL = cool, WARM = warm;

      XposedBridge.hookAllMethods(cf, "draw", new XC_MethodHook() {
        @Override protected void beforeHookedMethod(MethodHookParam p) {
          try {
            float level = (Float) p.args[0];
            long now = System.currentTimeMillis();
            if (now - lastReload > 1000) { prefs.reload(); lastReload = now; }
            int mode = XposedHelpers.getIntField(p.thisObject, "mMode");
            boolean off;
            if (mode == COOL) off = true;
            else if (mode == WARM) off = false;
            else off = (now - lastTime > 500) ? level >= .5f : level < lastLevel;
            lastLevel = level; lastTime = now;
            int id = prefs.getInt(off ? "off" : "on", 0);
            int ease = prefs.getInt("ease", 0);
            flash = prefs.getBoolean("flash", false);
            active = id > 0;
            if (active) {
              Anims.compute(id, ease, level, v);
              wasActive = true;
              inCall = true; XposedHelpers.callMethod(p.thisObject, "scaleQuad", v[0], v[1]); inCall = false;
            } else if (wasActive) {
              wasActive = false;
              inCall = true; XposedHelpers.callMethod(p.thisObject, "scaleQuad", 1f, 1f); inCall = false;
            }
          } catch (Throwable t) { active = false; inCall = false; XposedBridge.log("ScreenAnim draw: " + t); }
        }
      });
      XposedBridge.hookAllMethods(cf, "scaleQuad", new XC_MethodHook() {
        @Override protected void beforeHookedMethod(MethodHookParam p) {
          if (active && !inCall) { p.args[0] = v[0]; p.args[1] = v[1]; }
        }
      });
      XposedBridge.hookAllMethods(cf, "drawFaded", new XC_MethodHook() {
        @Override protected void beforeHookedMethod(MethodHookParam p) {
          if (!active) return;
          p.args[0] = v[2];
          if (!flash) p.args[1] = 1f;
        }
      });
      XposedBridge.log("ScreenAnim: hooked ColorFade");
    } catch (Throwable t) { XposedBridge.log("ScreenAnim init: " + t); }
  }
}
