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
package bd.com.ipay.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import bd.com.ipay.BuildConfig;
import bd.com.ipay.sdk.activity.IPayCheckoutActivity;
import bd.com.ipay.sdk.activity.IPayWebCheckoutActivity;
import bd.com.ipay.sdk.exception.IPaySDKException;
import bd.com.ipay.sdk.exception.IPaySDKInitializeException;
import bd.com.ipay.sdk.exception.InvalidCheckoutUrlException;
import bd.com.ipay.sdk.model.CheckoutCallbackActionUrls;
import bd.com.ipay.sdk.util.Constants;
import bd.com.ipay.sdk.util.Logger;

/**
 * This class is designed to initialize and customize the uses of iPay SDK.
 *
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public final class IPaySDK {

	/**
	 * Log Message Identifier.
	 */
	private static final Class<IPaySDK> TAG = IPaySDK.class;

	/**
	 * The key for the application ID in the Android manifest.
	 */
	private static final String LOG_SDK_EVENTS_ENABLED_PROPERTY = "bd.com.ipay.sdk.LogSdkEventsEnabled";

	/**
	 * The key for the callback off set in the Android manifest.
	 */
	private static final String CALLBACK_REQUEST_CODE_PROPERTY = "bd.com.ipay.sdk.CallbackRequestCode";

	/**
	 * The key for the callback off set in the Android manifest.
	 */
	private static final String CALLBACK_ACTIVITY_NAME_PROPERTY = "bd.com.ipay.sdk.CallbackActivityName";

	/**
	 * request code to create Checkout Activity.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int DEFAULT_CHECKOUT_REQUEST_CODE = 0xcafe;

	/**
	 * iPay Bangladesh Ltd. android app package name.
	 */
	public static final String IPAY_APP_PACKAGE_NAME = "bd.com.ipay.android";

	/**
	 * checkout status key
	 */
	public static final String CHECKOUT_STATUS_KEY = "checkout_status";
	/**
	 * checkout id key
	 */
	public static final String CHECKOUT_ID_KEY = "checkout_id";

	/**
	 * Variable to identify whether the debug log should be enabled or not.
	 */
	private static volatile boolean debugLogEnabled = BuildConfig.DEBUG;

	/**
	 * Variable to store checkout activity start request code.
	 */
	private static volatile int checkoutRequestCode = DEFAULT_CHECKOUT_REQUEST_CODE;

	/**
	 * Variable to store the canonical name of the activity to call after performing checkout.
	 */
	private static volatile String checkoutCallBackActivity;

	/**
	 * local variable to keep if the SDK has been initialized or not.
	 */
	private static Boolean sdkInitialized = false;

	private IPaySDK() {
		// Prohibiting from creation of an instance
	}

	/**
	 * As iPay SDK initializes automatically(<b>This function is called automatically on app start up</b>),
	 * there is no necessity to call this method. Although this method can be called manually if needed.
	 * This method mainly verifies few cases to check if the user mobile environment is suitable enough
	 * to perform iPay checkout.
	 *
	 * @param applicationContext The application context
	 */
	public static synchronized void initialize(@NonNull final Context applicationContext) {
		initialize(applicationContext, null);
	}

	/**
	 * As iPay SDK initializes automatically(<b>This function is called automatically on app start up</b>),
	 * there is no necessity to call this method. Although this method can be called manually if needed.
	 * This method mainly verifies few cases to check if the user mobile environment is suitable enough
	 * to perform iPay checkout.
	 *
	 * @param applicationContext The application context
	 * @param initializeCallback An initialize callback to get the status of the initialization.
	 */
	@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
	public static synchronized void initialize(@NonNull final Context applicationContext, @Nullable InitializeCallback initializeCallback) {
		try {
			if (IPaySDK.isInitialized()) {
				if (initializeCallback != null) {
					initializeCallback.onInitializationSuccess();
				}
			} else {
				//Checks if param applicationContext is null or not.
				if (!SDKUtils.notNull(applicationContext, "applicationContext", initializeCallback)) {
					return;
				}

				IPaySDK.initDefaultsFromMetadata(applicationContext);
				if (!SDKUtils.hasIPayCheckoutActivity(applicationContext, initializeCallback, false)) {
					return;
				}
				if (!SDKUtils.isValidUrlSchemeAdded(applicationContext, initializeCallback, false)) {
					return;
				}

				// No need to exit from initialization for these check
				SDKUtils.hasInternetPermissions(applicationContext, false);
				SDKUtils.isIPayAppInstalled(applicationContext, false);

				// At this point the SDK is successfully initialized.
				sdkInitialized = true;
				SDKUtils.printSomeFancyIPaySDK();
				if (initializeCallback != null)
					initializeCallback.onInitializationSuccess();
			}
		} catch (Exception e) {
			Logger.e(TAG, e);
			final IPaySDKInitializeException iPaySDKInitializeException;

			if (e instanceof IPaySDKInitializeException) {
				iPaySDKInitializeException = (IPaySDKInitializeException) e;
			} else {
				iPaySDKInitializeException = new IPaySDKInitializeException(e.getMessage(), e);
			}
			if (initializeCallback != null) {
				initializeCallback.onFailure(iPaySDKInitializeException);
			} else {
				throw e;
			}
		}
	}

	/**
	 * When the sdk initialization starts, SDK module needs to setup few values from AndroidManifest.xml
	 * Application metadata. This method performs that.
	 *
	 * @param context an android context
	 * @throws IPaySDKInitializeException if the metadata found from the
	 *                                    AndroidManifest.xml doesn't match the expected class type.
	 * @throws IllegalArgumentException   check {@link IPaySDK#setCheckoutRequestCode(int)}
	 */
	private static void initDefaultsFromMetadata(@NonNull Context context) {
		SDKUtils.notNull(context, "context");
		final ApplicationInfo applicationInfo;

		try {
			applicationInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return;
		}

		if (applicationInfo == null || applicationInfo.metaData == null) {
			return;
		}

		// Load metadata only before initializing the sdk
		if (!IPaySDK.isInitialized()) {

			// Getting SDK Events Log Enable Property
			try {
				setDebugLogEnabled(applicationInfo.metaData.getBoolean(LOG_SDK_EVENTS_ENABLED_PROPERTY, true));
			} catch (ClassCastException e) {
				throw new IPaySDKInitializeException("Logging enable property must be boolean", e);
			}

			// Getting Callback Request Code Property
			try {
				setCheckoutRequestCode(applicationInfo.metaData.getInt(CALLBACK_REQUEST_CODE_PROPERTY, DEFAULT_CHECKOUT_REQUEST_CODE));
			} catch (ClassCastException e) {
				throw new IPaySDKInitializeException("Checkout Request Code value must be int", e);
			} catch (IllegalArgumentException e) {
				throw new IPaySDKInitializeException(String.format("%s for %s", e.getMessage(), CALLBACK_REQUEST_CODE_PROPERTY), e);
			}

			// Getting the name of the callback Activity
			String tempCheckoutCallbackActivity;
			try {
				tempCheckoutCallbackActivity = applicationInfo.metaData.getString(CALLBACK_ACTIVITY_NAME_PROPERTY, null);

				if (tempCheckoutCallbackActivity == null || tempCheckoutCallbackActivity.isEmpty()) {
					return;
				}
				if (tempCheckoutCallbackActivity.startsWith(".")) {
					tempCheckoutCallbackActivity = context.getPackageName() + tempCheckoutCallbackActivity;
				}
				setCheckoutCallBackActivity(context, tempCheckoutCallbackActivity);
			} catch (ClassCastException e) {
				throw new IPaySDKInitializeException("Checkout Callback Activity class name must be String", e);
			} catch (IllegalArgumentException e) {
				throw new IPaySDKInitializeException(e.getMessage(), e);
			}
		}
	}

	/**
	 * If the device have iPay app installed, for a valid checkout url, this method will perform a checkout through
	 * iPay app. In case of iPay isn't present it will open Play Store to install the iPay app.
	 *
	 * @param activity    An android Activity
	 * @param checkoutUrl iPay checkout url
	 * @return
	 */
	@SuppressWarnings("unused")
	public static CheckoutState performCheckout(@NonNull Activity activity, @NonNull String checkoutUrl) {
		return performCheckout(activity, checkoutUrl, false);
	}

	/**
	 * If the device have iPay app installed, for a valid checkout url, this method will perform a checkout through
	 * iPay app. In case of iPay isn't present it can to do two things depending on the param shouldThrow value.
	 * If the value is true then it will throw an {@link IPaySDKException} exception. Otherwise it will
	 * open Play Store to install the iPay app.
	 *
	 * @param activity    An android Activity
	 * @param checkoutUrl iPay checkout url
	 * @param shouldThrow should the method throw an exception for error or not
	 * @throws IPaySDKException if the param shouldThrow is true then this method will throw
	 *                          {@link IPaySDKException} if IPay app isn't installed on device.
	 */
	@SuppressWarnings("WeakerAccess")
	public static CheckoutState performCheckout(@NonNull Activity activity, @NonNull String checkoutUrl, boolean shouldThrow) {
		return performCheckout(activity, checkoutUrl, shouldThrow, false);
	}

	/**
	 * If the device have iPay app installed, for a valid checkout url, this method will perform a checkout through
	 * iPay app. In case of iPay isn't present it can to do two things depending on the param shouldThrow value.
	 * If the value is true then it will throw an {@link IPaySDKException} exception. Otherwise it will
	 * open Play Store to install the iPay app. When the param useCallbackActivity is true, after completing a checkout.
	 * IPaySDK will send necessary data to the provided callback activity not to the activity from it was called.
	 *
	 * @param activity            An android Activity
	 * @param checkoutUrl         iPay checkout url
	 * @param shouldThrow         Should the method throw an exception for error or not
	 * @param useCallbackActivity Should the method send the data to another activity for completing
	 *                            the checkout or not
	 * @return the state of the checkout.
	 * @throws IPaySDKException if the param shouldThrow is true then this method will throw
	 *                          {@link IPaySDKException} if IPay app isn't installed on device.
	 */
	@SuppressWarnings({"WeakerAccess"})
	public static CheckoutState performCheckout(@NonNull Activity activity, @NonNull String checkoutUrl, boolean shouldThrow, boolean useCallbackActivity) {
		try {
			SDKUtils.notNull(activity, "activity");
			SDKUtils.notNull(checkoutUrl, "checkoutUrl");


			if (!SDKUtils.isValidIPayCheckoutUrl(checkoutUrl)) {
				throw new InvalidCheckoutUrlException("Provided url isn't valid to perform checkout");
			}

			if (SDKUtils.isIPayAppInstalled(activity, shouldThrow)) {
				if (!IPaySDK.isInitialized()) {
					IPaySDK.initialize(activity.getApplicationContext());
				}
				Intent intent = new Intent(activity, IPayCheckoutActivity.class);
				intent.putExtra(IPayCheckoutActivity.IPAY_CHECKOUT_URL_KEY, checkoutUrl);
				if (useCallbackActivity && TextUtils.isEmpty(getCheckoutCallBackActivity())) {
					throw new IPaySDKException(Constants.CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_DECLARED_REASON);
				} else {
					intent.putExtra(IPayCheckoutActivity.CHECKOUT_COMPLETE_START_COMPONENT_KEY, useCallbackActivity);
				}
				if (useCallbackActivity) {
					ActivityCompat.startActivity(activity, intent, null);
				} else {
					ActivityCompat.startActivityForResult(activity, intent, getCheckoutRequestCode(), null);
				}
				return CheckoutState.PROCESSING;
			} else {
				SDKUtils.openIPayInPlayStore(activity);
				return CheckoutState.IPAY_APP_NOT_INSTALLED;
			}
		} catch (InvalidCheckoutUrlException e) {
			if (shouldThrow) {
				throw e;
			} else {
				Logger.e(TAG, e);
				return CheckoutState.INVALID_CHECKOUT_URL;
			}
		} catch (IPaySDKException e) {
			if (shouldThrow) {
				throw e;
			} else {
				Logger.e(TAG, e);
				if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().equals(Constants.CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_DECLARED_REASON))
					return CheckoutState.CHECKOUT_COMPLETE_ACTIVITY_NOT_FOUND;
				else
					return CheckoutState.UNABLE_TO_PROCESS;
			}
		} catch (IllegalStateException e) {
			if (shouldThrow) {
				throw new IPaySDKException(e.getMessage(), e);
			} else {
				if (e.getMessage() != null && e.getMessage().equals(Constants.NO_IPAY_APP_INSTALLED_REASON)) {
					Logger.e(TAG, e);
					return CheckoutState.IPAY_APP_NOT_INSTALLED;
				} else {
					Logger.e(TAG, e);
					return CheckoutState.INVALID_CHECKOUT_URL;
				}
			}
		} catch (Exception e) {
			if (shouldThrow) {
				throw new IPaySDKException(e.getMessage(), e);
			} else {
				Logger.e(TAG, e);
				return CheckoutState.UNABLE_TO_PROCESS;
			}
		}
	}

	/**
	 * For a valid checkout url, this method will perform a checkout through iPay app. In case of iPay isn't present
	 * it will checkout via {@link android.webkit.WebView}. IPaySDK will send necessary data to the calling activity.
	 *
	 * @param activity                   An android Activity
	 * @param checkoutUrl                iPay checkout url
	 * @param checkoutCallbackActionUrls all three callback urls which was give to iPay during the creation of the checkout
	 * @return the state of the checkout.
	 */
	public static CheckoutState performCheckoutWithFallback(@NonNull Activity activity, @NonNull String checkoutUrl, @NonNull CheckoutCallbackActionUrls checkoutCallbackActionUrls) {
		return performCheckoutWithFallback(activity, checkoutUrl, false, checkoutCallbackActionUrls);
	}

	/**
	 * For a valid checkout url, this method will perform a checkout through iPay app. In case of iPay isn't present
	 * it will checkout via {@link android.webkit.WebView}. When the param useCallbackActivity is true, after completing a checkout.
	 * IPaySDK will send necessary data to the provided callback activity not to the activity from it was called.
	 *
	 * @param activity                   An android Activity
	 * @param checkoutUrl                iPay checkout url
	 * @param useCallbackActivity        Should the method send the data to another activity for completing
	 *                                   the checkout or not
	 * @param checkoutCallbackActionUrls all three callback urls which was give to iPay during the creation of the checkout
	 * @return the state of the checkout.
	 */
	public static CheckoutState performCheckoutWithFallback(@NonNull Activity activity, @NonNull String checkoutUrl, boolean useCallbackActivity, @NonNull CheckoutCallbackActionUrls checkoutCallbackActionUrls) {
		try {
			SDKUtils.notNull(activity, "activity");
			SDKUtils.notNull(checkoutUrl, "checkoutUrl");
			SDKUtils.notNull(checkoutCallbackActionUrls, "checkoutCallbackActionUrls");
		} catch (Exception e) {
			return CheckoutState.UNABLE_TO_PROCESS;
		}
		if (SDKUtils.isValidCheckoutCallbackActionUrls(checkoutCallbackActionUrls)) {
			return CheckoutState.INVALID_CHECKOUT_CALLBACK_URLS;
		}

		try {
			final CheckoutState checkoutState = performCheckout(activity, checkoutUrl, true);
			switch (checkoutState) {
				case IPAY_APP_NOT_INSTALLED:
					Intent intent = new Intent(activity, IPayWebCheckoutActivity.class);
					intent.putExtra(IPayCheckoutActivity.IPAY_CHECKOUT_URL_KEY, checkoutUrl);
					intent.putExtra(IPayWebCheckoutActivity.THIRD_PARTY_CHECKOUT_CALLBACK_URL_KEY, checkoutCallbackActionUrls);
					if (useCallbackActivity && TextUtils.isEmpty(getCheckoutCallBackActivity())) {
						return CheckoutState.CHECKOUT_COMPLETE_ACTIVITY_NOT_FOUND;
					} else {
						intent.putExtra(IPayCheckoutActivity.CHECKOUT_COMPLETE_START_COMPONENT_KEY, useCallbackActivity);
					}
					if (useCallbackActivity) {
						ActivityCompat.startActivity(activity, intent, null);
					} else {
						ActivityCompat.startActivityForResult(activity, intent, getCheckoutRequestCode(), null);
					}
					return CheckoutState.PROCESSING;
				default:
					return checkoutState;
			}
		} catch (IPaySDKException e) {
			if (e.getMessage() != null && e.getMessage().equals(Constants.NO_IPAY_APP_INSTALLED_REASON)) {
				Intent intent = new Intent(activity, IPayWebCheckoutActivity.class);
				intent.putExtra(IPayCheckoutActivity.IPAY_CHECKOUT_URL_KEY, checkoutUrl);
				intent.putExtra(IPayWebCheckoutActivity.THIRD_PARTY_CHECKOUT_CALLBACK_URL_KEY, checkoutCallbackActionUrls);
				if (useCallbackActivity && TextUtils.isEmpty(getCheckoutCallBackActivity())) {
					return CheckoutState.CHECKOUT_COMPLETE_ACTIVITY_NOT_FOUND;
				} else {
					intent.putExtra(IPayCheckoutActivity.CHECKOUT_COMPLETE_START_COMPONENT_KEY, useCallbackActivity);
				}
				if (useCallbackActivity) {
					ActivityCompat.startActivity(activity, intent, null);
				} else {
					ActivityCompat.startActivityForResult(activity, intent, getCheckoutRequestCode(), null);
				}
				return CheckoutState.PROCESSING;
			} else {
				return CheckoutState.UNABLE_TO_PROCESS;
			}
		}
	}

	/**
	 * Indicates whether the iPay SDK has been initialized.
	 *
	 * @return true if initialized, false if not
	 */
	public static synchronized boolean isInitialized() {
		return sdkInitialized;
	}

	/**
	 * @return boolean value of debug log enabled or not.
	 */
	public static boolean isDebugLogEnabled() {
		return IPaySDK.debugLogEnabled;
	}

	/**
	 * Sets value to identify whether the SDK should Log SDK events.
	 *
	 * @param debugLogEnabled indicates boolean value, whether the log should be printed or not.
	 */
	@SuppressWarnings("WeakerAccess")
	public static void setDebugLogEnabled(boolean debugLogEnabled) {
		IPaySDK.debugLogEnabled = debugLogEnabled;
	}

	/**
	 * @return value of request code which used to start activity result of
	 * {@link bd.com.ipay.sdk.activity.IPayCheckoutActivity}.
	 */
	@SuppressWarnings("WeakerAccess")
	public static int getCheckoutRequestCode() {
		return IPaySDK.checkoutRequestCode;
	}

	/**
	 * Sets request code for starting checkout activity.
	 *
	 * @param requestCode Value for request code.
	 * @throws IllegalArgumentException if the param value is less than 0.
	 */
	@SuppressWarnings("WeakerAccess")
	public static void setCheckoutRequestCode(int requestCode) {
		if (requestCode <= 0) {
			throw new IllegalArgumentException("request code must be greater than 0");
		}
		IPaySDK.checkoutRequestCode = requestCode;
	}

	/**
	 * @return Name of checkout callback activity name.
	 */
	@SuppressWarnings("WeakerAccess")
	@Nullable
	public static String getCheckoutCallBackActivity() {
		return IPaySDK.checkoutCallBackActivity;
	}

	/**
	 * @param context                  An android context
	 * @param checkoutCallBackActivity Callback activity canonical name
	 * @throws IllegalArgumentException if the activity does not exists or not declared in
	 *                                  AndroidManifest.xml. For more info see
	 *                                  {@link PackageManager#getActivityInfo(ComponentName, int)}
	 */
	@SuppressWarnings("WeakerAccess")
	@SuppressLint("WrongConstant")
	public static void setCheckoutCallBackActivity(@NonNull Context context, @NonNull String checkoutCallBackActivity) {
		SDKUtils.notNull(context, "context");
		SDKUtils.notNull(checkoutCallBackActivity, "checkoutCallBackActivity");

		final ActivityInfo activityInfo;
		try {
			activityInfo = SDKUtils.getActivityInfo(context, checkoutCallBackActivity);
		} catch (PackageManager.NameNotFoundException e) {
			throw new IllegalArgumentException(Constants.CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_FOUND_REASON);
		}
		if (activityInfo == null) {
			throw new IllegalArgumentException(Constants.CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_FOUND_REASON);
		}

		IPaySDK.checkoutCallBackActivity = checkoutCallBackActivity;
	}

	/**
	 * Callback passed to the {@link IPaySDK#initialize(Context)} function.
	 */
	public interface InitializeCallback {
		/**
		 * Called when the sdk has been initialized successfully.
		 */
		void onInitializationSuccess();

		/**
		 * Called when the sdk initialized failed with an known error case.
		 *
		 * @param tr thrown error.
		 */
		void onFailure(Throwable tr);
	}

	/**
	 * Values to mark Checkout State
	 */
	public enum CheckoutState {
		CHECKOUT_COMPLETE_ACTIVITY_NOT_FOUND, IPAY_APP_NOT_INSTALLED, INVALID_CHECKOUT_URL, UNABLE_TO_PROCESS, PROCESSING, INVALID_CHECKOUT_CALLBACK_URLS
	}

	/**
	 * Values to define Checkout Status
	 */
	public enum CheckoutStatus {
		SUCCESS, FAILED, CANCELLED;

		@Nullable
		public static CheckoutStatus getValue(@NonNull String name) {
			for (CheckoutStatus checkoutStatus : CheckoutStatus.values()) {
				if (checkoutStatus.toString().toLowerCase().equals(name)) {
					return checkoutStatus;
				}
			}
			return null;
		}
	}
}
