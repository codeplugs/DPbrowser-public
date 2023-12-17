package id.cybercode.dpbrowser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import java.lang.Object;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    TextView txtTabCount;
    WebView webView;
    ProgressBar progressBar;
    private int currentTabIndex;
    private RelativeLayout webPads,rLayout;
    private AutoCompleteTextView et;
    private ArrayList<Tab> tabs = new ArrayList<>();
    private static class Tab {
        Tab(WebView w) {

            this.webView = w;
        }
        WebView webView;

    }

    private Tab getCurrentTab() {
        return tabs.get(currentTabIndex);
    }
    private WebView getCurrentWebView() {
        return getCurrentTab().webView;
    }
    private URL getHost(){
        URL aURL = null;
        try {
            aURL = new URL("https://googlw.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return  aURL;

    }

    static class ArrayAdapterWithCurrentItem<T> extends ArrayAdapter<T> {
        int currentIndex;

        ArrayAdapterWithCurrentItem(Context context, int resource, T[] objects, int currentIndex) {
            super(context, resource, objects);
            this.currentIndex = currentIndex;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            //view.setBackgroundColor(Color.parseColor("#1b5e20"));
            Drawable d = getContext().getResources().getDrawable(position == this.currentIndex ? R.drawable.ic_location : R.drawable.empty, null);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24.0f, getContext().getResources().getDisplayMetrics());
            d.setBounds(0, 0, size, size);
            textView.setCompoundDrawablesRelative(d, null, null, null);
            textView.setCompoundDrawablePadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12.0f, getContext().getResources().getDisplayMetrics()));
            return view;
        }
    }


    public void onBackPressed() {
        if (getCurrentWebView().canGoBack()) {
            getCurrentWebView().goBack();
            Log.v("tabsize1", String.valueOf(tabs.size()));
        }else if (this.tabs.size() > 1) {

            closeCurrentTab();
        } else if (this.tabs.size() == 1) {
            //exitDialog();
            rLayout.removeAllViews();
            finishAndRemoveTask();
        } else {

            Log.v("tabsize", String.valueOf(tabs.size()));
            super.onBackPressed();
        }
    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setTabCountText(int count) {
        if (txtTabCount != null) {
            txtTabCount.setText(String.valueOf(count));
        }
    }


    String url = "http://google.com";

//    final String filename= URLUtil.guessFileName(URLUtil.guessUrl(url));

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getSupportActionBar().hide();

/*et.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					webView.loadUrl(et.getText().toString());
					return true;
				}
				return false;
			}
		});  */
        txtTabCount = findViewById(R.id.tabs);
        webPads = findViewById(R.id.webPad);
        rLayout = findViewById(R.id.rLayout);
        webView = findViewById(R.id.web);
        progressBar = findViewById(R.id.progress);
        et = findViewById(R.id.et);
        final ImageView goback = (ImageView) findViewById(R.id.rowback);
        final ImageView goforward = (ImageView) findViewById(R.id.rowforward);

//Forward Button Action
        goforward.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Go Forward if canGoForward is frue

                if(webView.canGoForward()){
                    webView.goForward();
                }
            }
        });

//Back Button Action
        goback.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Going back if canGoBack true
                if(webView.canGoBack()){
                    webView.goBack();
                }
            }
        });

         txtTabCount.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 //newTab("azhar");
                 showOpenTabs();
             }
         });

        et.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loadWebview(et.getText().toString(),getCurrentWebView());
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
       webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new myWebViewclient());
        //webView.loadUrl(url);

        newTab("google.com");



        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
                }
            }
        });

         webView.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 String url = null, imageUrl = null;
                 WebView.HitTestResult r = ((WebView) v).getHitTestResult();
                 switch (r.getType()) {
                     case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                         url = r.getExtra();
                         break;
                     case WebView.HitTestResult.IMAGE_TYPE:
                         imageUrl = r.getExtra();
                         break;
                     case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                     case WebView.HitTestResult.EMAIL_TYPE:
                     case WebView.HitTestResult.UNKNOWN_TYPE:
                         Handler handler = new Handler();
                         Message message = handler.obtainMessage();
                         ((WebView)v).requestFocusNodeHref(message);
                         url = message.getData().getString("url");
                         if ("".equals(url)) {
                             url = null;
                         }
                         imageUrl = message.getData().getString("src");
                         if ("".equals(imageUrl)) {
                             imageUrl = null;
                         }
                         if (url == null && imageUrl == null) {
                             return false;
                         }
                         break;
                     default:
                         return false;
                 }
                 showLongPressMenu(url, imageUrl);
                 return true;
             }
         });       //  ==================== START HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. YOU CAN COMMENT IT OUT IF YOUR APPLICATION DOES NOT REQUIRE FILE DOWNLOAD. IT WAS ADDED ON REQUEST ======//

        webView.setDownloadListener(new DownloadListener() {
            String fileName = MimeTypeMap.getFileExtensionFromUrl(url);
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
        //  ==================== END HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. YOU CAN COMMENT IT OUT IF YOUR APPLICATION DOES NOT REQUIRE FILE DOWNLOAD. IT WAS ADDED ON REQUEST ======//



    }


    public class myWebViewclient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);

            if (url.startsWith("intent://")) {
                try {
                    Context context = view.getContext();
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                    if (intent != null) {
                        view.stopLoading();

                        PackageManager packageManager = context.getPackageManager();
                        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        if (info != null) {
                            context.startActivity(intent);
                        } else {
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            view.loadUrl(fallbackUrl);

                            // or call external broswer
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
//                    context.startActivity(browserIntent);
                        }

                        return true;
                    }
                } catch (URISyntaxException e) {

                        Log.e("i", "Can't resolve intent://", e);

                }
            }

            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
            //webView.loadUrl("file:///android_asset/lost.html");
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.cancel();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            et.setText(url);
            et.setSelection(0);
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }




    private void loadWebview(String url, WebView webview) {
        url = url.trim();
        String guess = URLUtil.guessUrl(url);
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            url = "https://www.google.com/search?q=" + url;
        } else {
            url = guess;
        }
        webview.loadUrl(url);
        webview.requestFocus();
    }



    private void showOpenTabs() {
        String[] items = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            items[i] = tabs.get(i).webView.getTitle();
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
        @SuppressLint("InflateParams") View titleView = getLayoutInflater().inflate(R.layout.dialog, null);
        alertDialog.setCustomTitle(titleView);
        alertDialog.setCancelable(false);

        ArrayAdapter<String> adapter = new ArrayAdapterWithCurrentItem<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                items,
                currentTabIndex);


        alertDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.switchToTab(which);
            }
        });

        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton("New tab", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.newTab("https://google.com");
                switchToTab(tabs.size() - 1);
            }



        });

        alertDialog.show();


    }

    private void switchToTab(int tab) {
        //savedWebviewInstance();
        getCurrentWebView().setVisibility(View.GONE);
        currentTabIndex = tab;
        getCurrentWebView().setVisibility(View.VISIBLE);
        if(getCurrentWebView().getProgress()== 100){
            progressBar.setVisibility(View.GONE);
        }else{
            progressBar.setProgress(getCurrentWebView().getProgress());
        }
        et.setText(getCurrentWebView().getUrl());
        getCurrentWebView().requestFocus();
    }

    private void newTab(String url) {

        WebView webView = createWebView();
        webView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        //webView.setVisibility(View.GONE);
        Tab tab = new Tab(webView);
        tabs.add(tab);
        webPads.addView(webView);
        setTabCountText(tabs.size());
        loadWebview(url, webView);
    }

    private void closeCurrentTab() {

        ((RelativeLayout) findViewById(R.id.webPad)).removeView(getCurrentWebView());
        getCurrentWebView().destroy();
        tabs.remove(currentTabIndex);
        if (currentTabIndex >= tabs.size()) {
            currentTabIndex = tabs.size() - 1;
        }
        if (currentTabIndex == -1) {
            newTab("");
            currentTabIndex = 0;
        }


        getCurrentWebView().setVisibility(View.VISIBLE);
        if(getCurrentWebView().getProgress()== 100){
            progressBar.setVisibility(View.GONE);
        }else{
            progressBar.setProgress(getCurrentWebView().getProgress());
        }
        et.setText(getCurrentWebView().getUrl());
        txtTabCount.setText(String.valueOf(tabs.size()));
        getCurrentWebView().requestFocus();


    }

    private void showLongPressMenu(String linkUrl, String imageUrl) {
        String url;
        String title;
        String[] options = new String[]{"Open in new tab", "Copy URL", "Show full URL", "Download"};

        if (imageUrl == null) {
            if (linkUrl == null) {
                throw new IllegalArgumentException("Bad null arguments in showLongPressMenu");
            } else {
                // Text link
                url = linkUrl;
                title = linkUrl;
            }
        } else {
            if (linkUrl == null) {
                // Image without link
                url = imageUrl;
                title = "Image: " + imageUrl;
            } else {
                // Image with link
                url = linkUrl;
                title = linkUrl;
                String[] newOptions = new String[options.length + 1];
                System.arraycopy(options, 0, newOptions, 0, options.length);
                newOptions[newOptions.length - 1] = "Image Options";
                options = newOptions;
            }
        }
        new AlertDialog.Builder(MainActivity.this).setTitle(title).setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    newTab(url);
                    break;
                case 1:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    assert clipboard != null;
                    ClipData clipData = ClipData.newPlainText("URL", url);
                    clipboard.setPrimaryClip(clipData);
                    break;
                case 2:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Full URL")
                            .setMessage(url)
                            .setPositiveButton("OK", (dialog1, which1) -> {})
                            .show();
                    break;
                case 3:
                    //startDownload(url, null);
                    break;
                case 4:
                    showLongPressMenu(null, imageUrl);
                    break;
            }
        }).show();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView() {
         webView = new WebView(this);


        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowUniversalAccessFromFileURLs(true);
       settings.setDomStorageEnabled(true);
       // settings.setBuiltInZoomControls(globalPreferences.getBoolean("ZoomWeb", true));
        settings.setDisplayZoomControls(false);
        settings.setLoadsImagesAutomatically(true);
        //settings.setTextZoom(globalPreferences.getInt("fontControl",100));
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setUseWideViewPort(true);
        webView.setWebViewClient(new myWebViewclient());
        //settings.setJavaScriptCanOpenWindowsAutomatically(globalPreferences.getBoolean("SiteOpenWindow", true));
        //settings.setSupportMultipleWindows(globalPreferences.getBoolean("SiteOpenWindow", true));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
                }
            }
        });

        return webView;
    }




}

