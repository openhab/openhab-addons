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
package org.openhab.binding.unifiprotect.internal.dto;

import java.util.List;

/**
 * Doorbell settings available on the NVR.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DoorbellSettings {
    public String defaultMessageText;
    public Double defaultMessageResetTimeoutMs;
    public List<String> customMessages;

    public static class CustomImage {
        public String preview;
        public String sprite;
    }

    public List<CustomImage> customImages;
}
