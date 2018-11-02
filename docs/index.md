---
layout: default
---

## i. Download the iPay SDK for Android

[![Download SDK](./assets/img/button_download-ipay-sdk.png)](https://repo1.maven.org/maven2/bd/com/ipay/sdk/sdk-android/1.0.1/sdk-android-1.0.1.aar)

## ii. Import the iPay SDK

To use the iPay SDK in a project, simply add the dependency to your build.gradle or you can download and add Android Archive Library(AAR) file to your libs folder.

`Important: If you're adding the SDK to an existing project, start at step 3.`

### Install via Central Dependency
1.  _Go to Android Studio, \| New Project \| Minimum SDK._
2.  _Select API 16: Android 4.1 ( Jelly Bean ) or higher and create your new project._
3.  _In your project, open your_app_module \| Gradle Scripts \| build.gradle (Project) and add the following line to the <span style="color:#00B2A2">dependencies{ }</span> section._
```groovy
implementation "bd.com.ipay.sdk:sdk-android:1.0.1"
```
4.  _Build your project._

### Install via Downloaded AAR file
1.  _Go to Android Studio, \| New Project \| Minimum SDK._
2.  _Select API 16: Android 4.1 ( Jelly Bean ) or higher and create your new project._
3. Go to Project Root Folder \| app. create a new folder, rename it to libs.
4.  _In your project, open your_app_module \| Gradle Scripts \| build.gradle (Project) and add the following line to the <span style="color:#00B2A2">dependencies{ }</span> section._
<br/><br/>
```groovy
implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
```
5.  _Build your project._

`Important: If your project compile SDK version is below 28 then add the following lines`
```groovy
configurations.all {
    resolutionStrategy {
        force "com.android.support:support-compat:$targetSupportLibraryVersion"
        force "com.android.support:support-annotations:$targetSupportLibraryVersion"
    }
}
```

## iii. Edit Your Resources and Manifest

Create strings for your callback url scheme and for those needed to receive checkout status result in a **New Activity** add the CallbackActivityName to your AndroidManifest.xml. Also you can enable/disable SDK initialize logs.

`Important: Create a callback url scheme from iPay app. Please contact with us in order to create this url scheme.`

1.  _Open your <span style="color:#00B2A2">/app/res/values/strings.xml</span> file._
2.   _Add the following:_
<br/><br/>
```xml
    <string name="ipay_callback_url_scheme" translatable="false">YOUR_CALLBACK_CODE_FROM_IPAY_APP</string>
```
3. _Open the /app/manifest/AndroidManifest.xml file._
4. _Add the following meta-data elements:_
<br/><br/>
```xml
    <application>
        <!-- Optional metadata field. If you want to get callback to a new activity,
            then add the following like where the value will be the name of the callback activity 
            you want to open.-->
        <meta-data
            android:name="bd.com.ipay.sdk.CallbackActivityName"
            android:value="bd.com.ipay.activity.IPayCheckoutCallBackActivity" />
        <!-- Optional metadata field. If you don't want to print anything in the log, 
             use false as value.-->
        <meta-data
            android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
            android:value="true" />
        <!-- Optional metadata field. update the value field by your own choice. 
             Use this value to receive request result from onActivityResult-->
        <meta-data
            android:name="bd.com.ipay.sdk.CallbackRequestCode"
            android:value="1234" />
    </application>
```

## iv. Add checkout code snippet


Follow the **iPay Payment Gateway API guide for merchants(Version 1.2.1)** to create an `paymentUrl`. Use this `paymentUrl` to perform checkout through your android application.

The simplest way to perform iPay checkout through iPay Android app from your app is to add the following lines of code.
```java
// sample java checkout code.
public void iPayCheckoutPayment(Activity activity, String paymentUrl) {
    final CheckoutState checkoutState = IPaySDK.performCheckout(activity, paymentUrl);
}
```
```kotlin
 // sample kotlin checkout code.
 fun iPayCheckoutPayment(activity : Activity, paymentUrl : String) {
    val checkoutState = IPaySDK.performCheckout(activity, paymentUrl)  
 }
```

In case of iPay android isn't present, it will open play store to download the iPay app. Also you will the checkout result on `onActivityResult`

If you are willing to receive result in a new activity, use the following lines of code.

```java
// sample java checkout code.
private final boolean SHOULD_THROW_ERROR_WHILE_PERFORMING_CHECKOUT = true;
private final boolean SHOULD_RECEIVE_RESULT_ON_NEW_ACTIVITY = true;
public void iPayCheckoutPayment(Activity activity, String paymentUrl) {
    try {
        final CheckoutState checkoutState = IPaySDK.performCheckout(activity, paymentUrl, SHOULD_THROW_ERROR_WHILE_PERFORMING_CHECKOUT,SHOULD_RECEIVE_RESULT_ON_NEW_ACTIVITY);
    } catch(Exception e) {
        // perform checkout failed exceptions
    }
}
```

```kotlin
// sample kotlin checkout code.
const val SHOULD_THROW_ERROR_WHILE_PERFORMING_CHECKOUT = true
const val SHOULD_RECEIVE_RESULT_ON_NEW_ACTIVITY = true

fun iPayCheckoutPayment(activity : Activity, paymentUrl : String) {
    try {
        val checkoutState = IPaySDK.performCheckout(activity, paymentUrl, SHOULD_THROW_ERROR_WHILE_PERFORMING_CHECKOUT, SHOULD_RECEIVE_RESULT_ON_NEW_ACTIVITY) 
    }catch(e: Exception) {
        // perform checkout failed exceptions
    }
}
```

## iv. Check the status of checkout

On your `onActivityResult` you will receive the CheckoutStatus and the checkoutId.

```java
// sample java checkout code.
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    final int iPayCheckoutRequestCode = IPaySDK.getCheckoutRequestCode();
    super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case iPayCheckoutRequestCode:
				switch (resultCode) {
					case RESULT_OK:
					    if (data != null) {
					        final int checkoutId = data.getStringExtra(IPaySDK.CHECKOUT_ID_KEY);
					        final CheckoutStatus checkoutStatus = (CheckoutStatus) data.getSerializableExtra(IPaySDK.CHECKOUT_STATUS_KEY);
					        switch(checkoutStatus) {
					            case SUCCESS:
					                // call success callback url.
					                break;
					            case FAILED:
					                // call failed callback url.
					                break;
					            case CANCELLED:
					                // call cancelled callback url.
					                break;					                
					        }
					    }
					break;
				}
			break;
		}
}
```

```kotlin
// sample kotlin checkout code.
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val iPayCheckoutRequestCode = IPaySDK.getCheckoutRequestCode()
    when (requestCode) {
        iPayCheckoutRequestCode -> {
            when (resultCode) {
                RESULT_OK -> {
                    if (data != null) {
                        val checkoutId = data.getStringExtra(IPaySDK.CHECKOUT_ID_KEY)
                        val checkoutStatus = data.getSerializableExtra(IPaySDK.CHECKOUT_STATUS_KEY)  as CheckoutStatus
                        when(checkoutStatus) {
                            SUCCESS -> {
                                // call success callback url.
                            }
                            FAILED -> {
                                // call failed callback url.
                            }
                            CANCELLED -> {
                                // call cancelled callback url.
                            }
                        }                  
                    }
                }                
            }
        }
    }
}
```

`Important: You have to call the success/cancelled/failed callback api manually depending on the CheckoutStatus`

If you are willing to receive result in a new activity, get checkoutStatus and checkoutId from intent.

```java
// sample java checkout code.
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(getIntent() != null) {
        final int checkoutId = getIntent().getStringExtra(IPaySDK.CHECKOUT_ID_KEY);
        final CheckoutStatus checkoutStatus = (CheckoutStatus) getIntent().getSerializableExtra(IPaySDK.CHECKOUT_STATUS_KEY);
        switch(checkoutStatus) {
            case SUCCESS:
                // call success callback url.
                break;
            case FAILED:
                // call failed callback url.
                break;
            case CANCELLED:
                // call cancelled callback url.
                break;					                
        }
    }
}
```

```kotlin
// sample kotlin checkout code.
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val checkoutId = intent.getStringExtra(IPaySDK.CHECKOUT_ID_KEY)
    val checkoutStatus = intent.getSerializableExtra(IPaySDK.CHECKOUT_STATUS_KEY) as CheckoutStatus
    when(checkoutStatus) {
        SUCCESS -> {
            // call success callback url.
        }
        FAILED -> {
            // call failed callback url.
        }
        CANCELLED ->{
         // call cancelled callback url.
        }
    }
}
```
