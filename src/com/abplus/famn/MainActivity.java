package com.abplus.famn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.ScaleAnimation;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import com.abplus.anco.AncoActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;


public class MainActivity extends AncoActivity {
    private final String APP_URL = "http://famn.mobi";
    private MenuItem    faceItem = null;
    private MenuItem    usersItem = null;
    private ViewGroup   compose = null;
    private AdView      adView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView().setId(R.id.web_content);
        compose = appendComposePanel();
        adView = appendAdView();
        layoutWebView();
        acceptEmoji();

        findViewById(R.id.compose_button).setOnClickListener(new ComposeListener());
    }

    /**
     * 書込パネルを作って、アクティビティに追加する
     */
    private ViewGroup appendComposePanel() {
        ViewGroup result = compose;

        if (result == null) {
            result = (ViewGroup)getLayoutInflater().inflate(R.layout.compose_panel, null);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            result.setLayoutParams(params);

            result.findViewById(R.id.sample_image).setVisibility(View.GONE);
            result.setVisibility(View.GONE);

            EditText text = (EditText)result.findViewById(R.id.compose_text);
            text.setText(null);
            text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (adView != null) {
                        if (hasFocus) {
                            adView.setVisibility(View.GONE);
                        } else {
                            adView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            appendView(result);
        }

        return result;
    }

    /**
     * 広告ビューを作って、アクティビティに追加する
     */
    private AdView appendAdView() {
        AdView result = adView;

        if (result == null) {
            result = new AdView(this, AdSize.BANNER, getString(R.string.mediation_id));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            result.setLayoutParams(params);
            result.setId(R.id.ad_view);
            appendView(result);

            AdRequest adRequest = new AdRequest();

            result.loadAd(adRequest);
        }

        return result;
    }

    /**
     * WebViewのレイアウトを書込パネルの下になるようにする
     */
    private void layoutWebView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (compose != null) params.addRule(RelativeLayout.BELOW, R.id.compose_panel);
        if (adView  != null) params.addRule(RelativeLayout.ABOVE, R.id.ad_view);

        webView().setLayoutParams(params);
    }

    /**
     * 絵文字を入力できるようにする。
     */
    private void acceptEmoji() {
        EditText text = (EditText)findViewById(R.id.compose_text);
        Bundle bundle = text.getInputExtras(true);
        if (bundle != null) {
            bundle.putBoolean("allowEmoji", true);
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
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
        usersItem = menu.findItem(R.id.menu_users);

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

    /**
     * 書込パネルの表示/非表示切り替え(トグル)
     */
    private void toggleComposePanel() {
        if (findViewById(R.id.compose_panel).getVisibility() == View.GONE) {
            showComposePanel();
        } else {
            hideComposePanel(false);
        }
    }

    /**
     * 書込パネルを表示する
     */
    private void showComposePanel() {
        ViewGroup compose = (ViewGroup)findViewById(R.id.compose_panel);

        if (compose.getVisibility() == View.GONE) {
            Spinner spinner = (Spinner)compose.findViewById(R.id.face_spinner);
            FaceManager manager = FaceManager.sharedInstance();
            spinner.setAdapter(manager.getAdapter(this));

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

    /**
     * 書込パネルをかくす
     *
     * @param disable   アイコンを無効にする場合はtrue
     */
    private void hideComposePanel(boolean disable) {
        ViewGroup compose = (ViewGroup)findViewById(R.id.compose_panel);
        compose.setVisibility(View.GONE);

        MenuItem face = getFaceItem();
        if (face != null) {
            face.setChecked(false);
            face.setEnabled(!disable);
        }
    }

    private MenuItem getFaceItem() {
        //  onCreateOptionsMenuのときに保持した値を使う（いいのか？）
        return faceItem;
    }

    private MenuItem getUsersItem() {
        //  onCreateOptionsMenuのときに保持した値を使う（いいのか？）
        return usersItem;
    }

    /**
     * ログアウト処理
     */
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

        private ProgressDialog dialog = null;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return !(url.equals(APP_URL) || url.startsWith(APP_URL + "/")) &&
                    super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (dialog == null) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage(getString(R.string.loading));
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            if (url.startsWith("http")) dialog.show();
        }

        private boolean isAppRoot(String url) {
            Log.d("famn.log", "loaded from " + url);
            //  リダイレクト前のurlがくるっぽいので、不格好な比較で判定する。
            return  url.equals(APP_URL) ||
                    url.equals(APP_URL + "/") ||
                    url.equals(APP_URL + "/session/new#/") ||
                    url.equals(APP_URL + "/entries/new#/");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (dialog != null) dialog.dismiss();

            String cookie = CookieManager.getInstance().getCookie(url);

            if (isAppRoot(url)) {
                boolean aruji = false;
                for (String pair : cookie.split(";")) {
                    String[] kv = pair.split("=");
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if (key.equals("my_face")) {
                        FaceManager.sharedInstance().setFace(val);
                    } else if (key.equals("aruji")) {
                        aruji = true;
                    }
                }
                MenuItem item = getUsersItem();
                if (item != null) item.setVisible(aruji);

                showComposePanel();
            } else {
                hideComposePanel(true);
            }

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (dialog != null) dialog.dismiss();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    private class ComposeListener implements View.OnClickListener {

        private String messageText() {
            EditText view = (EditText)findViewById(R.id.compose_text);
            return view.getText().toString();
        }

        private int faceIndex() {
            Spinner spinner = (Spinner)findViewById(R.id.face_spinner);
            FaceManager.FaceItem item = (FaceManager.FaceItem)spinner.getSelectedItem();
            return item == null ? 0 : item.getIndex();
        }

        private void clearText() {
            EditText text = (EditText)findViewById(R.id.compose_text);
            text.setText(null);
        }

        @Override
        public void onClick(View v) {
            String params = "message=" + messageText() + "&face=" + faceIndex();
            clearText();
            hideComposePanel(false);
            webView().postUrl(APP_URL + "/entries", params.getBytes());
        }
    }
}
