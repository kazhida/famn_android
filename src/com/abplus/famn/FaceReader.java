package com.abplus.famn;

import android.os.Handler;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/04/28 16:39
 */
public class FaceReader {

    private String domain;
    private String path;

    FaceReader(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    private HttpContext buildHttpContext(DefaultHttpClient httpClient, String cookie) {
        CookieStore store = httpClient.getCookieStore();

        for (String pair : cookie.split(";")) {
            String[] kv = pair.split("=");
            String key = kv[0].trim();
            String val = kv[1].trim();
            if (key.length() > 0) {
                BasicClientCookie c = new BasicClientCookie(key, val);
                c.setDomain(domain);
                c.setPath(path);
                store.addCookie(c);
            }
        }

        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, store);

        return httpContext;
    }

    private String responseToString(HttpResponse response) throws IOException {
        if (response != null) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    return new String(EntityUtils.toByteArray(response.getEntity()));
                }
            }
        }
        return null;
    }

    public void getFaceAsync(String cookie, final Runnable proc) {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        final HttpContext httpContext = buildHttpContext(httpClient, cookie);
        final HttpGet get = new HttpGet(domain + path);
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse response = httpClient.execute(get, httpContext);
                    String result = responseToString(response);
                    if (result != null) {
                        Log.d("famn.log", result);
                    } else {
                        Log.d("famn.log", "no response");
                    }
                } catch (IOException e) {
                    //握りつぶす
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        proc.run();
                    }
                });
            }
        }).start();
    }
}
