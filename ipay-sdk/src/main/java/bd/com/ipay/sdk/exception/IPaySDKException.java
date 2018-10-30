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
package bd.com.ipay.sdk.exception;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public class IPaySDKException extends RuntimeException {

	public IPaySDKException() {
		super();
	}

	public IPaySDKException(@NonNull String message) {
		super(message);
	}

	public IPaySDKException(@NonNull String message, @Nullable Throwable throwable) {
		super(message, throwable);
	}
}
