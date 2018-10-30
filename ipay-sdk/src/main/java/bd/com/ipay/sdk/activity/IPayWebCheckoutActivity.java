/*
 * Copyright 2018 iPay Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bd.com.ipay.sdk.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import bd.com.ipay.R;
import bd.com.ipay.sdk.IPaySDK;
import bd.com.ipay.sdk.SDKUtils;
import bd.com.ipay.sdk.model.CheckoutCallbackActionUrls;
import bd.com.ipay.sdk.util.Logger;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public class IPayWebCheckoutActivity extends IPaySDKBaseActivity {
	private static final Class<IPayWebCheckoutActivity> TAG = IPayWebCheckoutActivity.class;
	public static final String THIRD_PARTY_CHECKOUT_CALLBACK_URL_KEY = "THIRD_PARTY_CHECKOUT_CALLBACK_URL";

	private ComponentName checkoutActivityCallbackComponentName;
	private String checkoutUrl;
	private WebView webView;
	private AlertDialog alertDialog;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipay_web_checkout);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final boolean isChromeAvailable = SDKUtils.isChromeInstalled(this);
		webView = findViewById(R.id.web_view);
		final ImageButton closeButton = findViewById(R.id.cancel_button);
		final ProgressBar progressBar = findViewById(R.id.progress_bar);

		final CheckoutCallbackActionUrls checkoutCallbackActionUrls = getIntent().getParcelableExtra(THIRD_PARTY_CHECKOUT_CALLBACK_URL_KEY);
		checkoutUrl = getIntent().getStringExtra(IPayCheckoutActivity.IPAY_CHECKOUT_URL_KEY);

		if (getIntent().hasExtra(IPayCheckoutActivity.CHECKOUT_COMPLETE_START_COMPONENT_KEY) &&
				getIntent().getBooleanExtra(IPayCheckoutActivity.CHECKOUT_COMPLETE_START_COMPONENT_KEY, false)) {
			final String checkoutActivityName = IPaySDK.getCheckoutCallBackActivity();
			if (checkoutActivityName != null)
				checkoutActivityCallbackComponentName = new ComponentName(getPackageName(), checkoutActivityName);
		} else {
			checkoutActivityCallbackComponentName = null;
		}

		if (SDKUtils.isValidCheckoutCallbackActionUrls(checkoutCallbackActionUrls)) {
			finishCheckout(IPaySDK.CheckoutStatus.FAILED);
		}

		progressBar.setMax(100);
		webView.setWebViewClient(new WebViewClient() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				if (request.getUrl().toString().matches(checkoutCallbackActionUrls.getSuccessUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.SUCCESS);
					return false;
				} else if (request.getUrl().toString().matches(checkoutCallbackActionUrls.getFailedUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.FAILED);
					return false;
				} else if (request.getUrl().toString().matches(checkoutCallbackActionUrls.getCancelledUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.CANCELLED);
					return false;
				} else {
					return super.shouldOverrideUrlLoading(view, request);
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.matches(checkoutCallbackActionUrls.getSuccessUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.SUCCESS);
					return false;
				} else if (url.matches(checkoutCallbackActionUrls.getFailedUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.FAILED);
					return false;
				} else if (url.matches(checkoutCallbackActionUrls.getCancelledUrl())) {
					finishCheckout(IPaySDK.CheckoutStatus.CANCELLED);
					return false;
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});

		if (isChromeAvailable) {
			webView.setWebChromeClient(new WebChromeClient() {

			});
		} else {
			progressBar.setVisibility(View.GONE);
		}

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCancelAlertDialog();
			}
		});

		webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setDatabaseEnabled(true);
		webView.loadUrl(checkoutUrl);
	}

	private void finishCheckout(IPaySDK.CheckoutStatus checkoutStatus) {
		final Intent intent = new Intent();
		intent.putExtra(IPaySDK.CHECKOUT_STATUS_KEY, checkoutStatus);
		final Uri uri = Uri.parse(checkoutUrl);
		if (!uri.getPathSegments().isEmpty() && uri.getPathSegments().contains("checkout") && uri.getPathSegments().contains("pay")) {
			intent.putExtra(IPaySDK.CHECKOUT_ID_KEY, uri.getPathSegments().get(uri.getPathSegments().size() - 1));
		}

		if (checkoutActivityCallbackComponentName != null) {
			try {
				intent.setComponent(checkoutActivityCallbackComponentName);
				ActivityCompat.startActivity(this, intent, null);
			} catch (Exception e) {
				Logger.e(TAG, e);
			}
		} else {
			setResult(RESULT_OK, intent);
		}
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				showCancelAlertDialog();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			showCancelAlertDialog();
		}
	}

	private void showCancelAlertDialog() {
		if (alertDialog == null || alertDialog.isShowing()) {
			alertDialog = new AlertDialog.Builder(this)
					.setMessage(R.string.cancel_checkout_transaction_message)
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finishCheckout(IPaySDK.CheckoutStatus.CANCELLED);
						}
					})
					.create();
		}
		alertDialog.show();
	}
}
