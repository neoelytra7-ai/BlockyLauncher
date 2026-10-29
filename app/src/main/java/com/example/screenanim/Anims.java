package com.example.screenanim;

public final class Anims {
  public static final String[] NAMES = {
    "Default (ROM)", "Fade", "CRT classic", "CRT vertical collapse", "CRT horizontal collapse",
    "Shrink to dot", "Iris (fast dot)", "Zoom + fade", "Fade + shrink", "Stretch squeeze",
    "Warp line", "Pulse fade", "Glitch", "Bounce collapse", "Slow CRT", "Flicker"
  };
  public static final String[] EASINGS = {"Linear", "Ease in", "Ease out", "Smooth", "Back (overshoot)"};

  static float c(float v, float lo, float hi) { return v < lo ? lo : Math.min(v, hi); }

  static float ease(int e, float t) {
    t = c(t, 0, 1);
    switch (e) {
      case 1: return t * t * t;
      case 2: { float u = 1 - t; return 1 - u * u * u; }
      case 3: return t * t * (3 - 2 * t);
      case 4: { float u = t - 1, k = 1.70158f; return 1 + u * u * ((k + 1) * u + k); }
      default: return t;
    }
  }

  /** t: 1 = screen fully on, 0 = off. out = {scaleX, scaleY, alpha} */
  public static void compute(int id, int easing, float level, float[] out) {
    float t = ease(easing, level), p = 1 - t;
    float sx = 1, sy = 1, a = 1;
    switch (id) {
      case 1: a = t; break;
      case 2: case 14: {
        if (id == 14) p = (float) Math.pow(c(p, 0, 1), 0.7);
        if (p < 0.6f) sy = 1 - p / 0.6f * 0.995f;
        else { sy = 0.005f; sx = 1 - (p - 0.6f) / 0.4f; }
        a = p > 0.9f ? (1 - p) * 10 : 1; break; }
      case 3: sy = t; a = t < .15f ? t / .15f : 1; break;
      case 4: sx = t; a = t < .15f ? t / .15f : 1; break;
      case 5: sx = sy = t; a = t < .1f ? t * 10 : 1; break;
      case 6: sx = sy = t * t; a = t < .1f ? t * 10 : 1; break;
      case 7: sx = sy = 1 + p * .6f; a = t; break;
      case 8: sx = sy = .75f + .25f * t; a = t; break;
      case 9: sx = 1 + p * .4f; sy = t * t; a = t < .2f ? t / .2f : 1; break;
      case 10: sx = 1 + p * 2; sy = t * t * t; a = t < .1f ? t * 10 : 1; break;
      case 11: sx = sy = 1 + .08f * (float) Math.sin(Math.PI * p); a = t; break;
      case 12: sx = 1 + ((float) Math.random() - .5f) * .3f * p; sy = 1 - p * .9f;
               a = t * (.6f + .4f * (float) Math.random()); break;
      case 13: sy = t * (.5f + .5f * (float) Math.abs(Math.cos(p * 7))); a = t < .1f ? t * 10 : 1; break;
      case 15: a = t * (Math.sin(p * 40) > 0 ? 1f : .4f); break;
      default: break;
    }
    out[0] = c(sx, 0.002f, 3f); out[1] = c(sy, 0.002f, 3f); out[2] = c(a, 0f, 1f);
  }
}
