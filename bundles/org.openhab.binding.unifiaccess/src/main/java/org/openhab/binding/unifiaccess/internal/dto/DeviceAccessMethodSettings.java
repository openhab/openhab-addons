/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.unifiaccess.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Device access-method settings.
 * <p>
 * The API may return settings either directly at the top level or nested under
 * an {@code access_methods} wrapper object. The {@code enabled} fields may be
 * {@code Boolean} ({@code true}/{@code false}) or {@code String}
 * ({@code "yes"}/{@code "no"}). This class handles both formats via the
 * {@link EnabledFlag} helper.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DeviceAccessMethodSettings {
    /**
     * If the API response nests settings under {@code "access_methods"}, Gson
     * populates this field. Call {@link #resolveAccessMethods()} to normalize.
     */
    public @Nullable DeviceAccessMethodSettings accessMethods;

    /** Device ID returned alongside settings (may be null). */
    public @Nullable String deviceId;

    public @Nullable Nfc nfc;
    public @Nullable Bt btTap;
    public @Nullable Bt btButton;
    public @Nullable Bt btShake;
    public @Nullable MobileWave mobileWave;
    public @Nullable Wave wave;
    public @Nullable PinCode pinCode;
    public @Nullable Face face;
    public @Nullable QrCode qrCode;
    public @Nullable TouchPass touchPass;

    /**
     * Normalizes the response: if the API nested settings under
     * {@code access_methods}, returns that inner object; otherwise returns
     * {@code this}.
     */
    public DeviceAccessMethodSettings resolveAccessMethods() {
        DeviceAccessMethodSettings inner = this.accessMethods;
        if (inner != null) {
            return inner;
        }
        return this;
    }

    /**
     * Base class for access method enabled flags. Handles both Boolean and
     * String ({@code "yes"}/{@code "no"}) representations from the API.
     */
    public abstract static class EnabledFlag {
        /**
         * Raw enabled value from JSON. May be deserialized as Boolean or left
         * null if the API returns a string like "yes"/"no". Use
         * {@link #isEnabled()} for reliable reads.
         */
        @SerializedName("enabled")
        public @Nullable Object enabledRaw;

        /**
         * Returns whether this access method is enabled, handling both Boolean
         * and String representations.
         */
        public boolean isEnabled() {
            Object val = enabledRaw;
            if (val instanceof Boolean b) {
                return b;
            }
            if (val instanceof String s) {
                return "yes".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
            }
            return false;
        }

        /**
         * Sets the enabled state. Stores as Boolean for API compatibility.
         */
        public void setEnabled(boolean enabled) {
            this.enabledRaw = enabled;
        }
    }

    public static class Nfc extends EnabledFlag {
    }

    public static class Bt extends EnabledFlag {
    }

    public static class MobileWave extends EnabledFlag {
    }

    public static class Wave extends EnabledFlag {
    }

    public static class PinCode extends EnabledFlag {
        public @Nullable Object pinCodeShuffle;

        public boolean isShuffleEnabled() {
            Object val = pinCodeShuffle;
            if (val instanceof Boolean b) {
                return b;
            }
            if (val instanceof String s) {
                return "yes".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
            }
            return false;
        }

        public void setShuffleEnabled(boolean enabled) {
            this.pinCodeShuffle = enabled;
        }
    }

    public static class Face extends EnabledFlag {
        public @Nullable String antiSpoofingLevel; // high, medium, no
        public @Nullable String detectDistance; // near, medium, far
    }

    public static class QrCode extends EnabledFlag {
    }

    public static class TouchPass extends EnabledFlag {
    }
}
