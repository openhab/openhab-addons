/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

/**
 * Device access-method settings.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceAccessMethodSettings {
    public Nfc nfc;
    public Bt btTap;
    public Bt btButton;
    public Bt btShake;
    public MobileWave mobileWave;
    public Wave wave;
    public PinCode pinCode;
    public Face face;
    public QrCode qrCode;
    public TouchPass touchPass;

    public abstract static class EnabledFlag {
        public Boolean enabled;
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
        public Boolean pinCodeShuffle;
    }

    public static class Face extends EnabledFlag {
        public String antiSpoofingLevel; // high, medium, no
        public String detectDistance; // near, medium, far
    }

    public static class QrCode extends EnabledFlag {
    }

    public static class TouchPass extends EnabledFlag {
    }
}
