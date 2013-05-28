package com.abplus.famn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.ScaleAnimation;
import android.webkit.*;
import android.widget.*;
import com.abplus.actionbarcompat.ActionBarActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;


public class MainActivity extends ActionBarActivity {
    private final String APP_URL = "http://famn.mobi";
    private MenuItem    faceItem = null;
    private MenuItem    usersItem = null;
    private AdView      adView = null;
    private WebView     webView = null;
    private SlidingMenu slidingMenu = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        slidingMenu = initSlidingMenu();
        adView = appendAdView();
        webView = appendWebView();

        EditText text = (EditText)findViewById(R.id.compose_text);
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
        Bundle bundle = text.getInputExtras(true);
        if (bundle != null) {
            bundle.putBoolean("allowEmoji", true);
        }

        findViewById(R.id.compose_button).setOnClickListener(new ComposeListener());

        webView.loadUrl(APP_URL);
    }

    /**
     * 広告ビューを作って、アクティビティに追加する
     */
    private AdView appendAdView() {
        AdView result = adView;

        if (result == null) {
            result = new AdView(this, AdSize.BANNER, getString(R.string.publisher_id));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            result.setLayoutParams(params);

            FrameLayout adPanel = (FrameLayout)findViewById(R.id.ad_view_panel);
            adPanel.addView(result);

            AdRequest adRequest = new AdRequest();

            result.loadAd(adRequest);
        }

        return result;
    }

    private WebView appendWebView() {
        WebView result = webView;

        if (result == null) {
            result = new WebView(this);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            result.setLayoutParams(params);

            FrameLayout webPanel = (FrameLayout)findViewById(R.id.web_panel);
            webPanel.addView(result);

            result.setWebChromeClient(new WebChromeClient());
            result.setWebViewClient(new FamnViewClient());

            result.setInitialScale(100);
            result.setVerticalScrollBarEnabled(false);
            result.setHorizontalScrollBarEnabled(false);

            WebSettings settings = result.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setUserAgentString(settings.getUserAgentString() + " famn.content_only");
        }

        return result;
    }

    private SlidingMenu initSlidingMenu() {
        SlidingMenu result = slidingMenu;

        if (result == null) {
            result = new SlidingMenu(this);

            result.setMode(SlidingMenu.LEFT);
            result.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            result.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
            result.setShadowDrawable(R.drawable.sdm_shadow);
            result.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            result.setFadeDegree(0.35f);
            result.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

            result.setMenu(R.layout.slidingmenumain);
        }

        return result;
    }

    @Override
    public void onDestroy() {
        webView.loadUrl("about:blank");
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //  戻るボタンで戻る
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);

        //  後で使うのでとっておく
        faceItem = menu.findItem(R.id.menu_compose);
        usersItem = menu.findItem(R.id.menu_users);

        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                slidingMenu.toggle();
                return true;
            case R.id.menu_reload:
                webView.reload();
                return true;
            case R.id.menu_home:
                webView.loadUrl(APP_URL);
                return true;
            case R.id.menu_logout:
                logout();
                return true;
            case R.id.menu_about:
                webView.loadUrl(APP_URL + "/infos/about");
                return true;
            case R.id.menu_setting:
                webView.loadUrl(APP_URL + "/account/edit");
                return true;
            case R.id.menu_users:
                webView.loadUrl(APP_URL + "/users");
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
                        webView.postUrl(APP_URL + "/session", "_method=delete".getBytes());
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

            if (isAppRoot(url) && cookie != null) {
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
            webView.postUrl(APP_URL + "/entries", params.getBytes());
        }
    }
}
