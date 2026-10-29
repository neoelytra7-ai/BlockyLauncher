package com.example.screenanim;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
  @SuppressWarnings("deprecation")
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    SharedPreferences s;
    try { s = getSharedPreferences("cfg", MODE_WORLD_READABLE); }
    catch (SecurityException e) { s = getSharedPreferences("cfg", MODE_PRIVATE);
      Toast.makeText(this, "Module not active in LSPosed", Toast.LENGTH_LONG).show(); }
    final SharedPreferences sp = s;
    LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
    int pd = (int) (20 * getResources().getDisplayMetrics().density); l.setPadding(pd, pd * 2, pd, pd);
    final Spinner off = sp(Anims.NAMES, sp.getInt("off", 0)), on = sp(Anims.NAMES, sp.getInt("on", 0)),
        ez = sp(Anims.EASINGS, sp.getInt("ease", 0));
    final CheckBox fl = new CheckBox(this); fl.setText("Brightness flash (ROM gamma)"); fl.setChecked(sp.getBoolean("flash", false));
    Button save = new Button(this); save.setText("Save");
    save.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
      sp.edit().putInt("off", off.getSelectedItemPosition()).putInt("on", on.getSelectedItemPosition())
        .putInt("ease", ez.getSelectedItemPosition()).putBoolean("flash", fl.isChecked()).commit();
      Toast.makeText(MainActivity.this, "Saved. Lock/unlock to test.", Toast.LENGTH_SHORT).show(); }});
    l.addView(t("Screen OFF animation")); l.addView(off);
    l.addView(t("Screen ON animation")); l.addView(on);
    l.addView(t("Easing")); l.addView(ez); l.addView(fl); l.addView(save);
    setContentView(new ScrollView(this) {{ addView(l); }});
  }
  TextView t(String s) { TextView v = new TextView(this); v.setText(s); v.setPadding(0, 24, 0, 4); return v; }
  Spinner sp(String[] a, int sel) {
    Spinner s = new Spinner(this);
    s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, a)); s.setSelection(sel); return s;
  }
}
