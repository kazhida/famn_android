package com.abplus.anco;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Vector;

/**
 *  Ancoフレームワークの基本となるActivity
 *
 *  フレームワークといっても、このファイル(クラス)だけで完結する
 *  とても小さなものです。
 *
 *  このActivityは、その中にWebViewを１つだけ持つRelativeLayoutからなっていて、
 *  OverrideしたtopUrl()で、指定したURLのコンテンツをWebViewに表示します。
 *  これで、HTML&JavaScriptの範囲内でできることはできるようになります。
 *  Webアプリからの移植のみであれば、これだけで十分かもしれません。
 *
 *  ただそれだけ、というのでは何かと不便になるのは目に見えているので、
 *  JAVAとJavaScriptを橋渡しするためのインターフェースを2種類用意してあります。
 *
 *  1つは、addJsInterface()メソッドで、JAVAのオブジェクト（のインスタンス）を、
 *  JavaScript側に渡し、それを使って、JavaScriptでは作れない機能を加えることができます。
 *  これは単に、WebViewクラスのaddJavascriptInterface()メソッドそのままなので、
 *  WebViewを使っていれば、普通にできることですけどね。
 *  この仕組みを使って組み込まれたddmsオブジェクトを使って、
 *  LogCatに対してログ出力できるようになっています。
 *
 *  ただ、JAVAとJavaScript間で直接やりとりできるのは、数値と論理値と文字列といった
 *  基本的なデータ型だけなので、複雑なことをやろうとすると結構きつかったりします。
 *
 *  そこで、もう一つのインターフェースとして、インテントを使ったものを用意してあります。
 *  IntentHandlerインターフェースを実装したクラスを、addIntentHandler()メソッドで
 *  登録しておくと、JavaScriptでのhrefな呼び出しに応答して、処理を実行することができます。
 *  AndroidManifest.xmlでIntent-filterの設定もしておく必要がありますけど。
 *  仕組みとしては、WebViewからAndroidOSに対してURIで要求を出して、OSが全Activityの
 *  なかから、Intent-filterで、わざわざ、もとからWebViewの置いてあるActivityを探し出して、
 *  それを実行するという、いうなれば、ひとりRESTfulなインターフェースです。
 *  回りくどいですが、RESTfulなAPIというのは、Webアプリではよくある話なので、
 *  相性がいいんじゃないかと思います。
 *
 * Author:  kazhida
 * Created: 12/03/17 13:52
 */
public abstract class AncoActivity extends Activity {

    public interface IntentHandler {
        /**
         * インテントに反応するメソッド
         *
         * @param intent    Activityに渡されたインテント
         */
        public void onNewIntent(Intent intent);

        /**
         * schemeによるフィルタ
         * nullを返すとすべてのschemeを受け入れると見なされる。
         *
         * @return  受け付けるscheme
         */
        public String acceptableScheme();

        /**
         * hostによるフィルタ
         * nullを返すとすべてのhostを受け入れると見なされる。
         *
         * @return  受け付けるhost
         */
        public String acceptableHost();
    }

    public final static double VERSION         = 1.0;
    public final static int    MEASURE_VERSION = 1;
    public final static int    MINOR_VERSION   = 0;
    public final static int    RELEASE_REV     = 1;
    public final static String DEFAULT_LOG_TAG = "ANCO";

    private RelativeLayout          root;
    private AncoView                webView;
    private Vector<IntentHandler>   handlers = new Vector<IntentHandler>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  root要素は、RelativeLayout
        root = new RelativeLayout(this);
        root.setLayoutParams(new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));

        //  主役のWebView(実際には、機能を拡張したAncoView)。
        webView = new AncoView(this);

        root.addView(webView);
        setContentView(root);

        beforeInitialLoad();

        //  topページを表示
        changePage(topUrl());
    }

    /**
     * インテントに対する応答
     *
     * @param intent    Activityに渡されたインテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        for (IntentHandler handler: handlers) {
            if (handler != null) {
                Uri uri = intent.getData();
                String scheme = handler.acceptableScheme();
                String host   = handler.acceptableHost();

                //  フィルタでふるい落とす
                if (scheme != null && !scheme.equals(uri.getScheme())) continue;
                if (host   != null && !host.equals(uri.getHost()))     continue;

                //  実行
                handler.onNewIntent(intent);
            }
        }
    }

    /*---------------------------------------------*
        セットアップ関連のメソッド
     *---------------------------------------------*/

    /**
     * 最初のページをロードする前に呼ばれるフック
     */
    protected abstract void beforeInitialLoad();

    /**
     * 最初にロードするURLは自分で決める
     *
     * @return  最初にロードするURL
     */
    protected abstract String topUrl();

    /**
     * WebViewClientをカスタマイズしたい場合は、これをOverrideする。
     * nullを返すと、WebViewClientを設定しない
     *
     * @return  このクラスでは普通のWebViewClientを返す。
     */
    protected WebViewClient createWebViewClient() {
        return new WebViewClient();
    }

    /**
     * WebChromeClientをカスタマイズしたい場合は、これをOverrideする。
     * nullを返すと、ChromeViewClientを設定しない。
     *
     * @return  このクラスでは、普通のWebChromeClientを返す。
     */
    protected WebChromeClient createWebChromeClient() {
        return new WebChromeClient();
    }

    /**
     * Viewを追加する
     *
     * @param view  追加するView
     */
    public void appendView(View view) {
        root.addView(view);
    }

    /**
     * インテント・ハンドラーを追加するメソッド
     *
     * @param handler   追加するハンドラー
     * @return          追加できたらtrue
     */
    public boolean addIntentHandler(IntentHandler handler) {
        return handlers.add(handler);
    }

    /**
     * インテント・ハンドラーを削除するメソッド
     *
     * @param handler   削除するハンドラー
     * @return          削除できたらtrue
     */
    public boolean removeIntentHandler(IntentHandler handler) {
        return handlers.remove(handler);
    }

    /*---------------------------------------------*
       コンテンツを使うためのプロパティ
    *---------------------------------------------*/

    /**
     * ルート・コンテンツ
     *
     * @return  ルート・コンテンツ
     */
    protected RelativeLayout root() {
        return root;
    }

    /**
     * ウェブ・ビュー
     *
     * @return  ウェブ・ビュー
     */
    protected AncoView webView() {
        return webView;
    }

    /**
     * ページの遷移
     *
     * @param url   行き先のURL
     */
    public void changePage(String url) {
        webView.loadUrl(url);
        try {
            webView.requestFocus();
        } catch (Exception e) {
            Log.e(DEFAULT_LOG_TAG, e.getLocalizedMessage());
        }
    }

    /*---------------------------------------------*
       イベントハンドラのOverride
    *---------------------------------------------*/

    @Override
    public void onDestroy() {
        webView.loadUrl("about:blank");
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

    /**
     * Ancoフレームワーク用のウェブ・ビュー
     *
     */
    public class AncoView extends WebView {

        AncoView(AncoActivity anco) {
            super(anco);

            //  めいっぱい広げる
            setLayoutParams(new LinearLayout.LayoutParams(
                                   ViewGroup.LayoutParams.MATCH_PARENT,
                                   ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

            //  WebChromeClientとWebViewClientの設定
            WebChromeClient wcc = anco.createWebChromeClient();
            if (wcc != null) setWebChromeClient(wcc);

            WebViewClient wvc = anco.createWebViewClient();
            if (wvc != null) setWebViewClient(wvc);

            setInitialScale(100);
            setVerticalScrollBarEnabled(false);
            setHorizontalScrollBarEnabled(false);

            WebSettings setting = getSettings();
            setting.setJavaScriptEnabled(true);
            setting.setJavaScriptCanOpenWindowsAutomatically(true);
//            setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//          setBuiltInZoomControls(false);

            //  ddmsという名前のインターフェースで、LogCat用のログを吐けるようにする。
            addJavascriptInterface(new LogCat(), "ddms");
        }
    }

    /**
     *  DDMSを介したログ出力用のクラス
     */
    class LogCat {

        public int debug(String tag, String msg) {
            return Log.d(tag, msg);
        }

        public int d(String msg) {
            return Log.d(DEFAULT_LOG_TAG, msg);
        }

        public int error(String tag, String msg) {
            return Log.e(tag, msg);
        }

        public int e(String msg) {
            return Log.e(DEFAULT_LOG_TAG, msg);
        }

        public int information(String tag, String msg) {
            return Log.i(tag, msg);
        }

        public int i(String msg) {
            return Log.i(DEFAULT_LOG_TAG, msg);
        }

        public int verbose(String tag, String msg) {
            return Log.v(tag, msg);
        }

        public int v(String msg) {
            return Log.v(DEFAULT_LOG_TAG, msg);
        }

        public int warn(String tag, String msg) {
            return Log.w(tag, msg);
        }

        public int w(String msg) {
            return Log.w(DEFAULT_LOG_TAG, msg);
        }
    }
}
