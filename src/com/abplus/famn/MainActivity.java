package com.abplus.famn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.abplus.anco.AncoActivity;

public class MainActivity extends AncoActivity {
    private final String APP_URL = "http://famn.mobi";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void beforeInitialLoad() {
        WebSettings settings = webView().getSettings();
        String userAgent = settings.getUserAgentString();
        settings.setUserAgentString(userAgent + " famn.content_only");
    }

    @Override
    protected String topUrl() {
        return APP_URL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_reload:
                webView().reload();
                return true;
            case R.id.menu_home:
                webView().loadUrl(APP_URL);
                return true;
            case R.id.menu_logout:
                logout();
                return true;
            case R.id.menu_about:
                webView().loadUrl(APP_URL + "/infos/about");
                return true;
            case R.id.menu_setting:
                webView().loadUrl(APP_URL + "/account/edit");
                return true;
            case R.id.menu_users:
                webView().loadUrl(APP_URL + "/users");
                return true;
            case R.id.menu_compose:
                toggleComposePanel();
                return true;
        }
        return false;
    }

    private void toggleComposePanel() {
        //とりあえず、書込ページに行くようにしておく
        webView().loadUrl(APP_URL + "/entries/new");
    }

    private void logout() {
        // 確認ダイアログの生成
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setTitle(getString(R.string.confirm_title));
        alertDlg.setMessage(getString(R.string.confirm_logout));
        alertDlg.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        webView().postUrl(APP_URL + "/session", "_method=delete".getBytes());
                    }
                });
        alertDlg.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // 表示
        alertDlg.create().show();
    }

    @Override
    protected WebViewClient createWebViewClient() {
        return new FamnViewClient();
    }

    private class FamnViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return !(url.equals(APP_URL) || url.startsWith(APP_URL + "/")) &&
                    super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished (WebView view, String url) {
            Log.d("famn.log", "loaded from " + url);
            super.onPageFinished(view, url);
        }
    }
}
