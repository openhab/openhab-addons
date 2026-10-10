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
package org.openhab.binding.shelly.internal.api2.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link Shelly2DeviceJsonDTO} includes constants and structures of the Gen2+ device-level (Sys) configuration and
 * services, e.g. the real-time debug log streamed over the {@code /debug/log} WebSocket.
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly2DeviceJsonDTO {
    public static final String SHELLYRPC_METHOD_SYS_SETCONFIG = "Sys.SetConfig";

    public static final String SHELLY2_DEBUGLOG_ENDPOINT = "/debug/log";

    public static final int SHELLY2_DEBUGLOG_LEVEL_ERROR = 0;
    public static final int SHELLY2_DEBUGLOG_LEVEL_WARN = 1;
    public static final int SHELLY2_DEBUGLOG_LEVEL_INFO = 2;
    public static final int SHELLY2_DEBUGLOG_LEVEL_DEBUG = 3;
    public static final int SHELLY2_DEBUGLOG_LEVEL_VERBOSE = 4;

    public static class Shelly2SysSetConfigParams {
        public Shelly2SysConfig config;
    }

    public static class Shelly2SysConfig {
        public Shelly2SysConfigDebug debug;
    }

    public static class Shelly2SysConfigDebug {
        public Shelly2SysConfigDebugWebSocket websocket;
    }

    public static class Shelly2SysConfigDebugWebSocket {
        public Boolean enable;
    }

    /**
     * One log line as sent by the device over the {@code /debug/log} WebSocket.
     */
    public static class Shelly2DebugLogMessage {
        public @Nullable Double ts;
        public @Nullable Integer level;
        public @Nullable String data;
    }
}
