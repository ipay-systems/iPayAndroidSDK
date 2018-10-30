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
package bd.com.ipay.sdk.util;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public class Constants {
	private Constants() {
		// Prohibiting from creation of an instance
	}

	public static final String EMPTY_STRING = "";
	public static final String IPAY_CHECKOUT_ACTIVITY_NOT_FOUND_REASON =
			"IPayCheckoutActivity is not declared in the AndroidManifest.xml or not being merged with the Merged AndroidManifest.xml. " +
					"If it's not being merged then add the following lines inside to quote to AndroidManifest.xml\n" +
					"<activity\n" +
					"            android:name=\".sdk.activity.IPayCheckoutActivity\"\n" +
					"            android:launchMode=\"singleInstance\"\n" +
					"            android:screenOrientation=\"portrait\">\n" +
					"            <intent-filter>\n" +
					"                <action android:name=\"android.intent.action.VIEW\" />\n" +
					"\n" +
					"                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
					"                <category android:name=\"android.intent.category.BROWSABLE\" />\n" +
					"\n" +
					"                <data android:scheme=\"@string/ipay_callback_url_scheme\" />\n" +
					"            </intent-filter>\n" +
					"        </activity>";

	public static final String CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_FOUND_REASON =
			"Given callback Activity is not declared in the AndroidManifest.xml or not being implemented";

	public static final String CHECKOUT_COMPLETE_CALLBACK_ACTIVITY_NOT_DECLARED_REASON =
			"Name of the Callback Activity after completing checkout isn't given in AndroidManifest.xml. Please add a valid " +
					"Checkout Callback Activity Name to your AndroidManifest.xml like below\n" +
					"<meta-data\n" +
					"            android:name=\"bd.com.ipay.sdk.CallbackActivityName\"\n" +
					"            android:value=\"CHECKOUT_CALLBACK_ACTIVITY_NAME\"\n" +
					"            />";

	public static final String NO_INTERNET_PERMISSION_REASON =
			"No internet permissions granted for the app, please add " +
					"<uses-permission android:name=\"android.permission.INTERNET\" /> " +
					"to your AndroidManifest.xml.";

	public static final String NO_IPAY_APP_INSTALLED_REASON =
			"IPay app isn't installed or the app is disabled. This issue will not cause any exception or crash while performing the checkout through SDK." +
					"SDK will open play store to install the ipay app." +
					"Our recommendation is to prompt a dialog to install iPay - Bangladesh app(https://play.google.com/store/apps/details?id=bd.com.ipay.android)" +
					"to get a seamless payment experience.";

	public static final String NO_URL_SCHEME_ADDED_REASON =
			"Callback url scheme is not added for the app, please add " +
					"<string name=\"ipay_callback_url_scheme\">IPAY_PROVIDED_URL_SCHEME</string>" +
					"to your strings.xml. Please change the IPAY_PROVIDED_APP_ID with the url scheme iPay Provided.";

	public static final String INVALID_URL_SCHEME_ADDED_REASON =
			"Callback url scheme doesn't start with prefix `ipay`, please add a valid url scheme to your strings.xml like below\n" +
					"<string name=\"ipay_callback_url_scheme\">IPAY_PROVIDED_URL_SCHEME</string>";

	public static final String CHROME_STABLE_PACKAGE = "com.android.chrome";
	public static final String CHROME_BETA_PACKAGE = "com.chrome.beta";
	public static final String CHROME_DEV_PACKAGE = "com.chrome.dev";
	public static final String CHROME_LOCAL_PACKAGE = "com.google.android.apps.chrome";

}
