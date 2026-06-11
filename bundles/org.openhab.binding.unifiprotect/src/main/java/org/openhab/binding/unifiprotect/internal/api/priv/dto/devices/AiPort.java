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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.devices;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectAdoptableDevice;

/**
 * AI Port device model for UniFi Protect (AI processing unit)
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AiPort extends UniFiProtectAdoptableDevice {

    public Double cpuLoad;
    public Long memoryUsed;
    public Long memoryTotal;
    public String platform;
    public Features features;

    public static class Features {
        public Boolean hasSmartDetect;
        public Boolean hasFaceDetection;
        public Boolean hasLicensePlateRecognition;
    }
}
