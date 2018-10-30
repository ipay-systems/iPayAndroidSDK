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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import bd.com.ipay.R;
import bd.com.ipay.sdk.IPaySDK;
import bd.com.ipay.sdk.util.Logger;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public class IPayCheckoutActivity extends IPaySDKBaseActivity {
	private static final Class<IPayCheckoutActivity> TAG = IPayCheckoutActivity.class;
	private static final int THIRD_PARTY_APP_TO_IPAY_APP_REQUEST_CODE = 0xc001;
	public static final String IPAY_CHECKOUT_URL_KEY = "ipay_checkout_url";
	public static final String CHECKOUT_COMPLETE_START_COMPONENT_KEY = "checkout_complete_start_component";

	private final Handler backPressHandler = new Handler();
	private final Runnable backPressHandlerRunnable = new Runnable() {
		@Override
		public void run() {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};

	private ComponentName checkoutActivityCallbackComponentName;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		Logger.d(TAG, "onCreate(Bundle " + savedInstanceState + ")");

		if (!IPaySDK.isInitialized()) {
			IPaySDK.initialize(getApplicationContext());
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipay_checkout);
		if (checkIsCallbackIntent(getIntent())) {
			final String checkoutActivityName = IPaySDK.getCheckoutCallBackActivity();
			if (checkoutActivityName != null)
				checkoutActivityCallbackComponentName = new ComponentName(getPackageName(), checkoutActivityName);
			//noinspection ConstantConditions
			performCheckoutCallbackAction(getIntent().getData());
		} else {
			performCheckoutAction(getIntent());
		}
	}

	private void performCheckoutAction(Intent intent) {
		Logger.d(TAG, "performCheckoutAction(intent " + intent + ")");
		final String checkoutUrl = intent.getStringExtra(IPayCheckoutActivity.IPAY_CHECKOUT_URL_KEY);
		if (intent.hasExtra(CHECKOUT_COMPLETE_START_COMPONENT_KEY) &&
				intent.getBooleanExtra(CHECKOUT_COMPLETE_START_COMPONENT_KEY, false)) {
			final String checkoutActivityName = IPaySDK.getCheckoutCallBackActivity();
			if (checkoutActivityName != null)
				checkoutActivityCallbackComponentName = new ComponentName(getPackageName(), checkoutActivityName);
		}
		Intent checkoutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
		checkoutIntent.setPackage(IPaySDK.IPAY_APP_PACKAGE_NAME);
		try {
			ActivityCompat.startActivityForResult(this, checkoutIntent, THIRD_PARTY_APP_TO_IPAY_APP_REQUEST_CODE, null);
		} catch (Exception e) {
			Logger.e(TAG, e);
			if (getCallingActivity() != null) {
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	}

	private void performCheckoutCallbackAction(@NonNull Uri data) {
		Logger.d(TAG, "performCheckoutCallbackAction(data " + data + ")");
		Intent intent = new Intent();
		final String urlScheme = data.getScheme();
		String checkoutId = data.toString().replaceAll(urlScheme + "://(.*)/(.*)", "$1");
		String checkoutStatus = data.toString().replaceAll(urlScheme + "://(.*)/(.*)", "$2");
		intent.putExtra(IPaySDK.CHECKOUT_STATUS_KEY, IPaySDK.CheckoutStatus.getValue(checkoutStatus));
		intent.putExtra(IPaySDK.CHECKOUT_ID_KEY, checkoutId);
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

	private boolean checkIsCallbackIntent(@Nullable Intent intent) {
		if (intent != null && intent.getAction() != null) {
			switch (intent.getAction()) {
				case Intent.ACTION_VIEW:
					return intent.getData() != null;
				default:
					return false;
			}
		}
		return false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (checkIsCallbackIntent(intent)) {
			backPressHandler.removeCallbacks(backPressHandlerRunnable);
			//noinspection ConstantConditions
			performCheckoutCallbackAction(intent.getData());
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case THIRD_PARTY_APP_TO_IPAY_APP_REQUEST_CODE:
				switch (resultCode) {
					case RESULT_CANCELED:
						backPressHandler.postDelayed(backPressHandlerRunnable, 1000);
						break;
				}
				break;
		}
	}
}
