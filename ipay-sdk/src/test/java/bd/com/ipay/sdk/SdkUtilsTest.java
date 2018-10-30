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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import bd.com.ipay.sdk.exception.IPaySDKException;
import bd.com.ipay.sdk.exception.IPaySDKInitializeException;
import bd.com.ipay.sdk.util.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class SdkUtilsTest {
	@Test
	public void testIsValidIPayCheckoutUrlMethod() {
		assertTrue(SDKUtils.isValidIPayCheckoutUrl("https://app.ipay.com.bd/checkout/pay/IPAY-123456"));
		assertFalse(SDKUtils.isValidIPayCheckoutUrl("https://www.google.com/checkout/pay/IPAY-123456"));
	}

	@Test
	public void testNotNullMethod() {
		final String valueNonNull = "Hello world";
		final String valueNull = null;

		assertTrue(SDKUtils.notNull(valueNonNull, "valueNonNull"));
		try {
			SDKUtils.notNull(valueNull, "valueNull");
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IPaySDKException);
			assertTrue(e.getCause() instanceof NullPointerException);
			assertEquals(e.getMessage(), "Argument '" + "valueNull" + "' cannot be null");
		}

		final IPaySDK.InitializeCallback initializeCallback = new IPaySDK.InitializeCallback() {
			@Override
			public void onInitializationSuccess() {
			}

			@Override
			public void onFailure(Throwable tr) {
				assertTrue(tr instanceof IPaySDKInitializeException);
				assertTrue(tr.getCause() instanceof NullPointerException);
				assertEquals(tr.getMessage(), "Argument '" + "valueNull" + "' cannot be null");
			}
		};
		assertTrue(SDKUtils.notNull(valueNonNull, "valueNonNull", initializeCallback));
		assertFalse(SDKUtils.notNull(valueNull, "valueNull", initializeCallback));
	}

	@Test
	public void testIsIPayAppInstalledMethod() {
		assertFalse(SDKUtils.isIPayAppInstalled(RuntimeEnvironment.application, false));
	}

	@Test
	public void testHasInternetPermissionsMethod() {
		assertFalse(SDKUtils.hasInternetPermissions(RuntimeEnvironment.application, false));
		try {
			SDKUtils.hasInternetPermissions(RuntimeEnvironment.application, true);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IPaySDKException);
			if (e.getCause() instanceof IllegalStateException) {
				assertEquals(e.getMessage(), Constants.NO_INTERNET_PERMISSION_REASON);
			} else {
				assertTrue(e.getCause() instanceof NullPointerException);
			}
		}
	}

	@Test
	public void testIsValidUrlSchemeAddedMethod() {
		assertFalse(SDKUtils.isValidUrlSchemeAdded(RuntimeEnvironment.application, false));
	}

	@Test
	public void testHasIPayCheckoutActivityMethod() {
		assertTrue(SDKUtils.hasIPayCheckoutActivity(RuntimeEnvironment.application, false));
	}

}
