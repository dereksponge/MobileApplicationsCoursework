package alarm.applicationname.company.coursework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class UserGuide extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/UserGuide.html");
        setContentView(webView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
