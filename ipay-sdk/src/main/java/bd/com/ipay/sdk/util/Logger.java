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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

import bd.com.ipay.sdk.IPaySDK;

/**
 * @author iPay Bangladesh Ltd.
 * @since 1.0.0-SNAPSHOT
 */
public class Logger {
	private static final int MAX_LOG_LENGTH = 4000;

	private Logger() {
		// Prohibiting from creation of an instance
	}

	public static int v(@NonNull String tag, @NonNull String msg) {
		return println(Log.VERBOSE, tag, msg);
	}

	public static int v(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.VERBOSE, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int v(@NonNull Class<?> tag, @NonNull String msg) {
		return v(smartTag(tag), msg);
	}

	public static int v(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return v(smartTag(tag), msg, tr);
	}

	public static int d(@NonNull String tag, @NonNull String msg) {
		return println(Log.DEBUG, tag, msg);
	}

	public static int d(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.DEBUG, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int d(@NonNull Class<?> tag, @NonNull String msg) {
		return d(smartTag(tag), msg);
	}

	public static int d(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return d(smartTag(tag), msg, tr);
	}

	public static int i(@NonNull String tag, @NonNull String msg) {
		return println(Log.INFO, tag, msg);
	}

	public static int i(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.INFO, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int i(@NonNull Class<?> tag, @NonNull String msg) {
		return i(smartTag(tag), msg);
	}

	public static int i(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return i(smartTag(tag), msg, tr);
	}

	public static int w(@NonNull String tag, @NonNull String msg) {
		return println(Log.WARN, tag, msg);
	}

	public static int w(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.WARN, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int w(@NonNull String tag, @NonNull Throwable tr) {
		return println(Log.WARN, tag, getStackTraceString(tr));
	}

	public static int w(@NonNull Class<?> tag, @NonNull String msg) {
		return w(smartTag(tag), msg);
	}

	public static int w(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return w(smartTag(tag), msg, tr);
	}

	public static int w(@NonNull Class<?> tag, @NonNull Throwable tr) {
		return w(smartTag(tag), tr);
	}

	public static int wtf(@NonNull String tag, @NonNull String msg) {
		return println(Log.WARN, tag, msg);
	}

	public static int wtf(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.WARN, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int wtf(@NonNull String tag, @NonNull Throwable tr) {
		return println(Log.WARN, tag, getStackTraceString(tr));
	}

	public static int wtf(@NonNull Class<?> tag, @NonNull String msg) {
		return wtf(smartTag(tag), msg);
	}

	public static int wtf(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return wtf(smartTag(tag), msg, tr);
	}

	public static int wtf(@NonNull Class<?> tag, @NonNull Throwable tr) {
		return wtf(smartTag(tag), tr);
	}

	public static int e(@NonNull String tag, @NonNull String msg) {
		return println(Log.WARN, tag, msg);
	}

	public static int e(@NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
		return println(Log.WARN, tag, String.format("%s\n%s", msg, getStackTraceString(tr)));
	}

	public static int e(@NonNull String tag, @NonNull Throwable tr) {
		return println(Log.WARN, tag, getStackTraceString(tr));
	}

	public static int e(@NonNull Class<?> tag, @NonNull String msg) {
		return e(smartTag(tag), msg);
	}

	public static int e(@NonNull Class<?> tag, @NonNull String msg, @Nullable Throwable tr) {
		return e(smartTag(tag), msg, tr);
	}

	public static int e(@NonNull Class<?> tag, @NonNull Throwable tr) {
		return e(smartTag(tag), tr);
	}

	private static int println(int priority, String tag, String message) {
		if (IPaySDK.isDebugLogEnabled()) {
			int i = 0;
			final int length = message.length();
			int result = -1;
			while (i < length) {
				int newLine = message.indexOf('\n', i);
				newLine = newLine != -1 ? newLine : length;
				do {
					int end = Math.min(newLine, i + MAX_LOG_LENGTH);
					result = Log.println(priority, tag, message.substring(i, end));
					i = end;
				} while (i < newLine);
				i++;
			}
			return result;
		} else {
			return -1;
		}
	}

	private static String getStackTraceString(@Nullable Throwable tr) {
		if (tr == null) {
			return "";
		}

		// This is to reduce the amount of log spew that apps do in the non-error
		// condition of the network being unavailable.
		Throwable temp = tr;
		while (temp != null) {
			if (temp instanceof UnknownHostException) {
				return "";
			}
			temp = temp.getCause();
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	@NonNull
	private static String smartTag(@NonNull Class<?> tag) {
		final String[] splitterString;
		if (tag.getCanonicalName() != null) {
			splitterString = tag.getCanonicalName().split("\\.");
		} else {
			splitterString = new String[0];
		}
		final StringBuilder tagBuilder = new StringBuilder();
		for (int i = 0; i < splitterString.length; i++) {
			if (i != splitterString.length - 1 && splitterString[i].length() > 0) {
				tagBuilder.append(splitterString[i].charAt(0)).append(".");
			} else {
				tagBuilder.append(splitterString[i]);
			}
		}
		return tagBuilder.toString();
	}
}
