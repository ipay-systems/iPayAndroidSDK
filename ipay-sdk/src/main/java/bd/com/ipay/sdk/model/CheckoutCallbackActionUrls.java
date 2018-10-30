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
package bd.com.ipay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import bd.com.ipay.sdk.util.Constants;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class CheckoutCallbackActionUrls implements Parcelable {

	@NonNull
	private final String successUrl;
	@NonNull
	private final String failedUrl;
	@NonNull
	private final String cancelledUrl;

	public CheckoutCallbackActionUrls(@NonNull String successUrl, @NonNull String failedUrl, @NonNull String cancelledUrl) {
		this.successUrl = successUrl;
		this.failedUrl = failedUrl;
		this.cancelledUrl = cancelledUrl;
	}

	protected CheckoutCallbackActionUrls(Parcel in) {
		String tempValue;
		// reading value for success callback url
		tempValue = in.readString();

		if (tempValue != null)
			successUrl = tempValue;
		else
			successUrl = Constants.EMPTY_STRING;

		// reading value for failed callback url
		tempValue = in.readString();
		if (tempValue != null)
			failedUrl = tempValue;
		else
			failedUrl = Constants.EMPTY_STRING;

		// reading value for cancelled callback url
		tempValue = in.readString();
		if (tempValue != null)
			cancelledUrl = tempValue;
		else
			cancelledUrl = Constants.EMPTY_STRING;
	}

	public static final Creator<CheckoutCallbackActionUrls> CREATOR = new Creator<CheckoutCallbackActionUrls>() {
		@Override
		public CheckoutCallbackActionUrls createFromParcel(Parcel in) {
			return new CheckoutCallbackActionUrls(in);
		}

		@Override
		public CheckoutCallbackActionUrls[] newArray(int size) {
			return new CheckoutCallbackActionUrls[size];
		}
	};

	@NonNull
	public String getSuccessUrl() {
		return successUrl;
	}

	@NonNull
	public String getFailedUrl() {
		return failedUrl;
	}

	@NonNull
	public String getCancelledUrl() {
		return cancelledUrl;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CheckoutCallbackActionUrls that = (CheckoutCallbackActionUrls) o;

		if (!successUrl.equals(that.successUrl)) return false;
		if (!failedUrl.equals(that.failedUrl)) return false;
		return cancelledUrl.equals(that.cancelledUrl);
	}

	@Override
	public int hashCode() {
		int result = successUrl.hashCode();
		result = 31 * result + failedUrl.hashCode();
		result = 31 * result + cancelledUrl.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "CheckoutCallbackActionUrls{" +
				"successUrl='" + successUrl + '\'' +
				", failedUrl='" + failedUrl + '\'' +
				", cancelledUrl='" + cancelledUrl + '\'' +
				'}';
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(successUrl);
		dest.writeString(failedUrl);
		dest.writeString(cancelledUrl);
	}
}
