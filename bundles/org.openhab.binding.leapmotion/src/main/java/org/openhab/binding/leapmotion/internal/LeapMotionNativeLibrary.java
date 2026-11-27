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
package org.openhab.binding.leapmotion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.LoggerFactory;

/**
 * The {@link LeapMotionNativeLibrary} checks whether the native Leap Motion library can be loaded.
 *
 * The Leap Motion SDK terminates the JVM with {@link System#exit(int)} if its native library cannot be loaded, e.g. on
 * platforms for which none is shipped. This class must therefore be consulted before any class of the SDK that uses the
 * native library, such as {@code Controller}, is touched. It must not reference such classes itself.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public final class LeapMotionNativeLibrary {

    private static final String LIBRARY_NAME = "LeapJava";

    private static final boolean AVAILABLE = load();

    private LeapMotionNativeLibrary() {
    }

    /**
     * @return true if the native library is available, so the Leap Motion SDK can be used
     */
    public static boolean isAvailable() {
        return AVAILABLE;
    }

    private static boolean load() {
        try {
            // same call as in the SDK, but without terminating the JVM on failure
            System.loadLibrary(LIBRARY_NAME);
            return true;
        } catch (UnsatisfiedLinkError e) {
            LoggerFactory.getLogger(LeapMotionNativeLibrary.class).warn(
                    "The native Leap Motion library '{}' cannot be loaded, the binding is not functional on this platform: {}",
                    LIBRARY_NAME, e.getMessage());
            return false;
        }
    }
}
