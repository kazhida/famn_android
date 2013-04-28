package com.abplus.famn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import com.abplus.anco.AncoActivity;

public class MainActivity extends AncoActivity {
    private final String APP_URL = "http://famn.mobi";
    private MenuItem faceItem = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView().setId(R.id.web_content);
        appendComposePanel();
        layoutWebView();

        findViewById(R.id.compose_button).setOnClickListener(new ComposeListener());
    }

    private void appendComposePanel() {
        ViewGroup compose = (ViewGroup)getLayoutInflater().inflate(R.layout.compose_panel, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        compose.setLayoutParams(params);

        compose.findViewById(R.id.sample_image).setVisibility(View.GONE);
        compose.setVisibility(View.GONE);

        appendView(compose);
    }

    private void layoutWebView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.BELOW, R.id.compose_panel);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        webView().setLayoutParams(params);
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

        //  後で使うのでとっておく
        faceItem = menu.findItem(R.id.menu_compose);

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
        if (findViewById(R.id.compose_panel).getVisibility() == View.GONE) {
            showComposePanel();
        } else {
            hideComposePanel(false);
        }
    }

    private void showComposePanel() {
        ViewGroup compose = (ViewGroup)findViewById(R.id.compose_panel);

        if (compose.getVisibility() == View.GONE) {
            Spinner spinner = (Spinner)compose.findViewById(R.id.face_spinner);
            FaceManager manager = FaceManager.sharedInstance();
            spinner.setAdapter(manager.getAdapter(this));

            EditText text = (EditText)compose.findViewById(R.id.compose_text);
            text.setText(null);

            ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
            animation.setDuration(200);
            compose.startAnimation(animation);
            compose.setVisibility(View.VISIBLE);
        }
        MenuItem face = getFaceItem();
        if (face != null) {
            face.setEnabled(true);
            face.setChecked(true);
        }
    }

    private void hideComposePanel(boolean disable) {
        ViewGroup compose = (ViewGroup)findViewById(R.id.compose_panel);
        compose.setVisibility(View.GONE);

        MenuItem face = getFaceItem();
        if (face != null) {
            face.setChecked(false);
            face.setEnabled(! disable);
        }
    }

    private MenuItem getFaceItem() {
        //  onCreateOptionsMenuのときに保持した値を使う（いいのか？）
        return faceItem;
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

            if (url.equals(APP_URL) || url.equals(APP_URL + "/")) {
                showComposePanel();
            } else {
                hideComposePanel(true);
            }

            super.onPageFinished(view, url);
        }
    }

    private class ComposeListener implements View.OnClickListener {

        private String messageText() {
            EditText view = (EditText)findViewById(R.id.compose_text);
            return view.getText().toString();
        }

        private int faceIndex() {
            Spinner spinner = (Spinner)findViewById(R.id.face_spinner);
            int index = spinner.getSelectedItemPosition();
            return index + 1;
        }

        @Override
        public void onClick(View v) {
            String params = "message=" + messageText() + "&face=" + faceIndex();
            webView().postUrl(APP_URL + "/entries", params.getBytes());
        }
    }
}
