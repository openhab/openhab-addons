/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

/**
 * Error response of the Controller for a Long Poll API call.
 *
 * @author Stefan Kästle - Initial contribution
 */
public class LongPollError {

    public static final int SUBSCRIPTION_INVALID = -32001;

    /**
     * {
     * "jsonrpc":"2.0",
     * "error": {
     * "code":-32001,
     * "message":"No subscription with id: e8fei62b0-0"
     * }
     * }
     */

    public class ErrorInfo {
        public int code;
        public String message;
    }

    public String jsonrpc;
    public ErrorInfo error;
}
