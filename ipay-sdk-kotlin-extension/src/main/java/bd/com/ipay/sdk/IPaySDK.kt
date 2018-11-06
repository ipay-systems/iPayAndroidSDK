package bd.com.ipay.sdk

import android.app.Activity
import android.content.Context
import bd.com.ipay.sdk.exception.IPaySDKException
import bd.com.ipay.sdk.model.CheckoutCallbackActionUrls

/**
 * As iPay SDK initializes automatically(<b>This function is called automatically on app
 * start up</b>), there is no necessity to call this method. Although this method can be called
 * manually if needed. This method mainly verifies few cases to check if the user mobile
 * environment is suitable enough to perform iPay checkout.
 *
 */
@Suppress("unused")
fun Context.initializeIPaySDK() =
        initializeIPaySDK(null)

/**
 * As iPay SDK initializes automatically(<b>This function is called automatically on app
 * start up</b>), there is no necessity to call this method. Although this method can be called
 * manually if needed. This method mainly verifies few cases to check if the user mobile
 * environment is suitable enough to perform iPay checkout.
 *
 * @param initializeCallback An initialize callback to get the status of the initialization.
 */
fun Context.initializeIPaySDK(initializeCallback: IPaySDK.InitializeCallback?) =
        IPaySDK.initialize(this, initializeCallback)

/**
 * If the device have iPay app installed, for a valid checkout url, this method will perform a
 * checkout through iPay app. In case of iPay isn't present it will open Play Store to install
 * the iPay app.
 *
 * @param checkoutUrl iPay checkout url
 * @return
 */
@Suppress("unused")
fun Activity.performCheckout(checkoutUrl: String) =
        performCheckout(checkoutUrl, false)

/**
 * If the device have iPay app installed, for a valid checkout url, this method will perform a
 * checkout through iPay app. In case of iPay isn't present it can to do two things depending on
 * the param shouldThrow value. If the value is true then it will throw an {@link IPaySDKException}
 * exception. Otherwise it will
 * open Play Store to install the iPay app.
 *
 * @param checkoutUrl iPay checkout url
 * @param shouldThrow should the method throw an exception for error or not
 * @throws IPaySDKException if the param shouldThrow is true then this method will throw
 *                          {@link IPaySDKException} if IPay app isn't installed on device.
 */
fun Activity.performCheckout(checkoutUrl: String, shouldThrow: Boolean) =
        performCheckout(checkoutUrl, shouldThrow, false)

/**
 * If the device have iPay app installed, for a valid checkout url, this method will perform a
 * checkout through iPay app. In case of iPay isn't present it can to do two things depending on
 * the param shouldThrow value. If the value is true then it will throw an {@link IPaySDKException}
 * exception. Otherwise it will open Play Store to install the iPay app. When the param
 * useCallbackActivity is true, after completing a checkout. IPaySDK will send necessary data to
 * the provided callback activity not to the activity from it was called.
 *
 * @param checkoutUrl         iPay checkout url
 * @param shouldThrow         Should the method throw an exception for error or not
 * @param useCallbackActivity Should the method send the data to another activity for completing
 *                            the checkout or not
 * @return the state of the checkout.
 * @throws IPaySDKException if the param shouldThrow is true then this method will throw
 *                          {@link IPaySDKException} if IPay app isn't installed on device.
 */
fun Activity.performCheckout(checkoutUrl: String,
                             shouldThrow: Boolean, useCallbackActivity: Boolean) =
        IPaySDK.performCheckout(this, checkoutUrl, shouldThrow, useCallbackActivity)!!

/**
 * For a valid checkout url, this method will perform a checkout through iPay app. In case of
 * iPay isn't present it will checkout via {@link android.webkit.WebView}.
 * IPaySDK will send necessary data to the calling activity.
 *
 * @param checkoutUrl                iPay checkout url
 * @param checkoutCallbackActionUrls all three callback urls which was give to iPay during the
 * creation of the checkout
 * @return the state of the checkout.
 */
@Suppress("unused")
fun Activity.performCheckoutWithFallback(checkoutUrl: String,
                                         checkoutCallbackActionUrls: CheckoutCallbackActionUrls) =
        performCheckoutWithFallback(checkoutUrl, false, checkoutCallbackActionUrls)

/**
 * For a valid checkout url, this method will perform a checkout through iPay app. In case of iPay
 * isn't present it will checkout via {@link android.webkit.WebView}. When the param
 * useCallbackActivity is true, after completing a checkout. IPaySDK will send necessary data to
 * the provided callback activity not to the activity from it was called.
 *
 * @param checkoutUrl                iPay checkout url
 * @param useCallbackActivity        Should the method send the data to another activity for
 * completing the checkout or not
 * @param checkoutCallbackActionUrls all three callback urls which was give to iPay during the
 * creation of the checkout
 * @return the state of the checkout.
 */
fun Activity.performCheckoutWithFallback(checkoutUrl: String, useCallbackActivity: Boolean,
                                         checkoutCallbackActionUrls: CheckoutCallbackActionUrls) =
        IPaySDK.performCheckoutWithFallback(this, checkoutUrl, useCallbackActivity,
                checkoutCallbackActionUrls)!!

/**
 * @param checkoutCallBackActivity Callback activity canonical name
 * @throws IllegalArgumentException if the activity does not exists or not declared in
 *                                  AndroidManifest.xml. For more info see
 *                                  {@link PackageManager#getActivityInfo(ComponentName, int)}
 */
@Suppress("unused")
fun Context.setCheckoutCallBackActivity(checkoutCallBackActivity: String) =
        IPaySDK.setCheckoutCallBackActivity(this, checkoutCallBackActivity)