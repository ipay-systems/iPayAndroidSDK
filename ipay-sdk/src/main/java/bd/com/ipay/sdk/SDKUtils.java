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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import bd.com.ipay.sdk.activity.IPayCheckoutActivity;
import bd.com.ipay.sdk.exception.IPaySDKException;
import bd.com.ipay.sdk.exception.IPaySDKInitializeException;
import bd.com.ipay.sdk.model.CheckoutCallbackActionUrls;
import bd.com.ipay.sdk.util.Constants;
import bd.com.ipay.sdk.util.Logger;

/**
 * <p>
 * This class designed for getting information  and validating various case to check
 * whether the system is perfect to run the SDK or not.
 * </p>
 * <p>
 * A number of the methods in this class are for debugging or informational purposes.
 * Most of the methods may not be needed to call by the developers.
 * </p>
 *
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public final class SDKUtils {
	/**
	 * Log Message Identifier.
	 */
	private static final Class<SDKUtils> TAG = SDKUtils.class;

	/**
	 * This is an invalid url scheme added to IPaySDK string resource folder. The reason of using this
	 * url is to verify whether the IPay SDK user added a valid deeplink callback url to their project or not.
	 */
	private static final String INVALID_IPAY_CALLBACK_URL_SCHEME = "ipay_callback_url_scheme";

	/**
	 * Checkout Url host name.
	 */
	public static final String IPAY_COM_BD = "ipay.com.bd";

	/**
	 * @param checkoutUrl Url to validate
	 * @return true if the url is a valid url to checkout through ipay, if not then false.
	 */
	public static boolean isValidIPayCheckoutUrl(@NonNull final String checkoutUrl) {
		SDKUtils.notNull(checkoutUrl, "checkoutUrl");
		try {
			final Uri uri = Uri.parse(checkoutUrl);
			return !TextUtils.isEmpty(uri.getHost()) && uri.getHost().contains(IPAY_COM_BD);
		} catch (Exception ex) {
			Logger.e(TAG, ex);
		}
		return false;
	}

	/**
	 * Validation method to check, whether the variable is null or not.
	 * Use case of This method is to throw NullPointerException with a proper message.
	 *
	 * @param arg  Validating argument
	 * @param name Validation variable name
	 * @return true if not null. else throws exception.<b>(This method will never return false as a result)</b>
	 * @throws IPaySDKException Wraps NullPointerException.
	 *                          If the argument is null when the param initializeCallback is also null.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static boolean notNull(@Nullable Object arg, @NonNull String name) {
		return notNull(arg, name, null);
	}

	/**
	 * Validation method to check, whether the variable is null or not.
	 * Use case of This method is to throw NullPointerException with a proper message, if the argument
	 * initializeCallback is null otherwise it will return true or false.
	 * <b>N.B.: This method is for internal use only.</b>
	 *
	 * @param arg                Validating argument
	 * @param name               Validation variable name
	 * @param initializeCallback Callback instance to return exception as initialize fallback
	 * @return If the param initializeCallback is not null, then true if the param arg is not null.
	 * otherwise false and a failure callback will be done.
	 * If the param initializeCallback is null then true if the param arg is not null but throws
	 * exception for arg null case and will never return false as a result.
	 * @throws IPaySDKException Wraps NullPointerException.
	 *                          If the argument is null when the param initializeCallback is also null.
	 */
	protected static boolean notNull(@Nullable Object arg, @NonNull String name, @Nullable IPaySDK.InitializeCallback initializeCallback) {
		if (arg == null) {
			NullPointerException nullPointerException = new NullPointerException("Argument '" + name + "' cannot be null");
			if (initializeCallback != null) {
				initializeCallback.onFailure(new IPaySDKInitializeException(nullPointerException.getMessage(), nullPointerException));
				return false;
			} else {
				throw new IPaySDKException(nullPointerException.getMessage(), nullPointerException);
			}
		} else {
			return true;
		}
	}

	/**
	 * @param context     An android context
	 * @param shouldThrow Should the method throw an exception for error or not
	 * @return True when the IPayCheckoutActivity is declared in AndroidManifest.xml otherwise false.
	 * @throws IPaySDKInitializeException If the param shouldThrow is true method will throw the exception
	 *                                    when the IPayCheckoutActivity is not declared in AndroidManifest.xml
	 *                                    or present.
	 */
	@SuppressWarnings("unused")
	public static boolean hasIPayCheckoutActivity(@NonNull Context context, boolean shouldThrow) {
		return hasIPayCheckoutActivity(context, null, shouldThrow);
	}

	/**
	 * @param context            An android context
	 * @param initializeCallback Callback instance to return exception as initialize fallback
	 * @param shouldThrow        should the method throw an exception for error or not
	 * @return True when the IPayCheckoutActivity is declared in AndroidManifest.xml otherwise false.
	 * @throws IPaySDKInitializeException If the param shouldThrow is true method will throw the exception
	 *                                    when the IPayCheckoutActivity is not declared in AndroidManifest.xml
	 *                                    or present.
	 */
	protected static boolean hasIPayCheckoutActivity(@NonNull Context context, @Nullable IPaySDK.InitializeCallback initializeCallback, boolean shouldThrow) {
		SDKUtils.notNull(context, "context", initializeCallback);
		final ActivityInfo activityInfo;
		try {
			activityInfo = getActivityInfo(context, IPayCheckoutActivity.class.getName());
		} catch (Exception e) {
			if (shouldThrow) {
				if (e instanceof PackageManager.NameNotFoundException) {
					throw new IPaySDKInitializeException(Constants.IPAY_CHECKOUT_ACTIVITY_NOT_FOUND_REASON, e);
				} else {
					throw new IPaySDKInitializeException(e.getMessage(), e);
				}
			} else {
				Logger.e(TAG, e);
				return false;
			}
		}
		return activityInfo != null;
	}

	/**
	 * @param context   An android context
	 * @param className name of the android activity class
	 * @return ActivityInfo of the given activity name.
	 */
	@SuppressLint("WrongConstant")
	@Nullable
	protected static ActivityInfo getActivityInfo(@NonNull Context context, @NonNull String className) throws PackageManager.NameNotFoundException, NullPointerException {
		SDKUtils.notNull(context, "context");

		final PackageManager packageManager = context.getPackageManager();
		if (packageManager != null) {
			final ComponentName componentName = new ComponentName(context, className);
			return packageManager.getActivityInfo(componentName, PackageManager.GET_ACTIVITIES);
		} else {
			throw new NullPointerException("Package Manager Not Found.");
		}
	}

	/**
	 * Checks if iPay is installed or not.
	 * <p>In case of iPay app isn't install it is recommended that prompt user to install iPay app.
	 * To perform install iPay app via Play Store please see/use {@link SDKUtils#openIPayInPlayStore(Context)}</p>
	 *
	 * @param context     An android context
	 * @param shouldThrow should the method throw an exception for error or not
	 * @return whether iPay App is installed or not.
	 */
	public static boolean isIPayAppInstalled(@NonNull Context context, boolean shouldThrow) {
		try {
			return isThisAppAvailable(context, IPaySDK.IPAY_APP_PACKAGE_NAME, shouldThrow);
		} catch (IPaySDKException e) {
			if (e.getCause() instanceof PackageManager.NameNotFoundException) {
				throw new IllegalStateException(Constants.NO_IPAY_APP_INSTALLED_REASON);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Checks if Chrome is installed or not.
	 *
	 * @param context An android context
	 * @return whether Chrome App is installed or not.
	 */
	public static boolean isChromeInstalled(@NonNull Context context) {
		return isThisAppAvailable(context, Constants.CHROME_STABLE_PACKAGE, false) ||
				isThisAppAvailable(context, Constants.CHROME_BETA_PACKAGE, false) ||
				isThisAppAvailable(context, Constants.CHROME_DEV_PACKAGE, false) ||
				isThisAppAvailable(context, Constants.CHROME_LOCAL_PACKAGE, false);
	}

	/**
	 * Checks if given appPackageName is installed or not.
	 *
	 * @param context        An android context
	 * @param appPackageName app package name
	 * @param shouldThrow    should the method throw an exception for error or not
	 * @return whether gib App is installed or not.
	 */
	private static boolean isThisAppAvailable(@NonNull Context context, @NonNull String appPackageName, boolean shouldThrow) {
		SDKUtils.notNull(context, "context");
		SDKUtils.notNull(appPackageName, "appPackageName");
		try {
			final PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				return packageManager.getPackageInfo(appPackageName, 0) != null;
			} else {
				throw new NullPointerException("Package Manager Not Found.");
			}
		} catch (Exception e) {
			Logger.e(TAG, e);
			if (shouldThrow) {
				throw new IPaySDKException(e.getMessage(), e);
			} else {
				Logger.e(TAG, e);
				return false;
			}
		}
	}

	/**
	 * Checks whether <uses-permission android:name="android.permission.INTERNET" /> Added to the
	 * AndroidManifest or not.
	 *
	 * @param context     An android context
	 * @param shouldThrow Should the method throw an exception for error or not
	 * @return If the application has the network access permission or not.
	 */
	@SuppressWarnings("unused")
	public static boolean hasInternetPermissions(@NonNull Context context, boolean shouldThrow) {
		SDKUtils.notNull(context, "context");
		try {
			final PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) ==
						PackageManager.PERMISSION_DENIED) {
					throw new IllegalStateException(Constants.NO_INTERNET_PERMISSION_REASON);
				}
			} else {
				throw new NullPointerException("Package Manager Not Found.");
			}
			return true;
		} catch (Exception e) {
			if (shouldThrow) {
				throw new IPaySDKException(e.getMessage(), e);
			} else {
				Logger.e(TAG, e);
				return false;
			}
		}
	}


	/**
	 * Checks if the callback deeplink url is a valid one or not. This method will also check if the
	 * url was also provided by the developers or not.
	 *
	 * @param context     An android context
	 * @param shouldThrow Should the method throw an exception for error or not
	 * @return If the deeplink url was provided or not. also checks provided url scheme is valid or not.
	 * @throws IPaySDKInitializeException If the param shouldThrow is true method will throw the exception
	 *                                    for an invalid url callback scheme.
	 */
	@SuppressWarnings("unused")
	public static boolean isValidUrlSchemeAdded(Context context, boolean shouldThrow) {
		return isValidUrlSchemeAdded(context, null, shouldThrow);
	}

	/**
	 * Checks if the callback deeplink url is a valid one or not. This method will also check if the
	 * url was also provided by the developers or not.
	 *
	 * @param context            An android context
	 * @param initializeCallback Callback instance to return exception as initialize fallback
	 * @param shouldThrow        Should the method throw an exception for error or not
	 * @return If the deeplink url was provided or not. also checks provided url scheme is valid or not.
	 * @throws IPaySDKInitializeException If the param shouldThrow is true method will throw the exception
	 *                                    for an invalid url callback scheme.
	 */
	protected static boolean isValidUrlSchemeAdded(Context context, IPaySDK.InitializeCallback initializeCallback, boolean shouldThrow) {
		SDKUtils.notNull(context, "context", initializeCallback);
		try {
			int id = context.getResources().getIdentifier("ipay_callback_url_scheme", "string", context.getPackageName());
			if (id == -1) {
				throw new IllegalStateException(Constants.NO_URL_SCHEME_ADDED_REASON);
			}
			String string = context.getString(id);
			if (string.isEmpty() || string.equals(INVALID_IPAY_CALLBACK_URL_SCHEME)) {
				throw new IllegalStateException(Constants.NO_URL_SCHEME_ADDED_REASON);
			}
			if (!string.startsWith("ipay")) {
				throw new IllegalStateException(Constants.INVALID_URL_SCHEME_ADDED_REASON);
			}
			return true;
		} catch (Exception e) {
			if (initializeCallback != null) {
				initializeCallback.onFailure(new IPaySDKInitializeException(e.getMessage(), e));
				return false;
			} else {
				if (shouldThrow) {
					throw new IPaySDKException(e.getMessage(), e);
				} else {
					Logger.e(TAG, e);
					return false;
				}
			}
		}
	}

	/**
	 * Opens the play store to install iPay.
	 *
	 * @param context An android context
	 */
	public static void openIPayInPlayStore(@NonNull Context context) {
		SDKUtils.notNull(context, "context");
		try {
			// Try to open via Play Store App
			startViewUri(context, "market://details?id=" + IPaySDK.IPAY_APP_PACKAGE_NAME);
		} catch (ActivityNotFoundException anfe) {
			try {
				// Fail callback case for opening Play Store App. This one might open via browser.
				startViewUri(context, "http://play.google.com/store/apps/details?id=" + IPaySDK.IPAY_APP_PACKAGE_NAME);
			} catch (Exception e) {
				Logger.e(TAG, e);
			}
		}
	}

	public static boolean isValidCheckoutCallbackActionUrls(@Nullable CheckoutCallbackActionUrls checkoutCallbackActionUrls) {
		return checkoutCallbackActionUrls == null ||
				checkoutCallbackActionUrls.getCancelledUrl().equals(Constants.EMPTY_STRING) ||
				checkoutCallbackActionUrls.getFailedUrl().equals(Constants.EMPTY_STRING) ||
				checkoutCallbackActionUrls.getSuccessUrl().equals(Constants.EMPTY_STRING);
	}

	/**
	 * Opens Browser/App that handle the given uri.
	 *
	 * @param context An android context
	 * @param uri     Uri to open/view
	 */
	private static void startViewUri(@NonNull Context context, @NonNull String uri) {
		SDKUtils.notNull(context, "context");
		SDKUtils.notNull(uri, "uri");
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
	}

	/**
	 * Just fancy artwork. :)
	 */
	protected static void printSomeFancyIPaySDK() {
		//***********************************************************************************************************
		//***********************************************************************************************************
		//**                                                                                                       **
		//**  	              ___         ___                                ___                         ___       **
		//**                 /\  \       /\  \                              /\__\         _____         /|  |      **
		//**	___         /::\  \     /::\  \         ___                /:/ _/_       /::\  \       |:|  |      **
		//**   /\__\       /:/\:\__\   /:/\:\  \       /|  |              /:/ /\  \     /:/\:\  \      |:|  |      **
		//**  /:/__/      /:/ /:/  /  /:/ /::\  \     |:|  |             /:/ /::\  \   /:/  \:\__\   __|:|  |      **
		//** /::\  \     /:/_/:/  /  /:/_/:/\:\__\    |:|  |            /:/_/:/\:\__\ /:/__/ \:|__| /\ |:|__|____  **
		//** \/\:\  \__  \:\/:/  /   \:\/:/  \/__/  __|:|__|            \:\/:/ /:/  / \:\  \ /:/  / \:\/:::::/__/  **
		//**    \:\/\__\  \::/__/     \::/__/      /::::\  \             \::/ /:/  /   \:\  /:/  /   \::/~~/~      **
		//**     \::/  /   \:\  \      \:\  \      ~~~~\:\  \             \/_/:/  /     \:\/:/  /     \:\~~\       **
		//**     /:/  /     \:\__\      \:\__\          \:\__\              /:/  /       \::/  /       \:\__\      **
		//**     \/__/       \/__/       \/__/           \/__/              \/__/         \/__/         \/__/      **
		//**                                                                                                       **
		//***********************************************************************************************************
		//***********************************************************************************************************
		Logger.i(TAG, "***********************************************************************************************************");
		Logger.i(TAG, "***********************************************************************************************************");
		Logger.i(TAG, "**                                                                                                       **");
		Logger.i(TAG, "**                  ___         ___                                ___                         ___       **");
		Logger.i(TAG, "**                 /\\  \\       /\\  \\                              /\\__\\         _____         /|  |      **");
		Logger.i(TAG, "**    ___         /::\\  \\     /::\\  \\         ___                /:/ _/_       /::\\  \\       |:|  |      **");
		Logger.i(TAG, "**   /\\__\\       /:/\\:\\__\\   /:/\\:\\  \\       /|  |              /:/ /\\  \\     /:/\\:\\  \\      |:|  |      **");
		Logger.i(TAG, "**  /:/__/      /:/ /:/  /  /:/ /::\\  \\     |:|  |             /:/ /::\\  \\   /:/  \\:\\__\\   __|:|  |      **");
		Logger.i(TAG, "** /::\\  \\     /:/_/:/  /  /:/_/:/\\:\\__\\    |:|  |            /:/_/:/\\:\\__\\ /:/__/ \\:|__| /\\ |:|__|____  **");
		Logger.i(TAG, "** \\/\\:\\  \\__  \\:\\/:/  /   \\:\\/:/  \\/__/  __|:|__|            \\:\\/:/ /:/  / \\:\\  \\ /:/  / \\:\\/:::::/__/  **");
		Logger.i(TAG, "**  ~~\\:\\/\\__\\  \\::/__/     \\::/__/      /::::\\  \\             \\::/ /:/  /   \\:\\  /:/  /   \\::/~~/~      **");
		Logger.i(TAG, "**     \\::/  /   \\:\\  \\      \\:\\  \\      ~~~~\\:\\  \\             \\/_/:/  /     \\:\\/:/  /     \\:\\~~\\       **");
		Logger.i(TAG, "**     /:/  /     \\:\\__\\      \\:\\__\\          \\:\\__\\              /:/  /       \\::/  /       \\:\\__\\      **");
		Logger.i(TAG, "**     \\/__/       \\/__/       \\/__/           \\/__/              \\/__/         \\/__/         \\/__/      **");
		Logger.i(TAG, "**                                                                                                       **");
		Logger.i(TAG, "***********************************************************************************************************");
		Logger.i(TAG, "***********************************************************************************************************");
	}
}
