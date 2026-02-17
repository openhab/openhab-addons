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
package org.openhab.binding.linkplay.internal.client.http.dto;

/**
 * Bluetooth pair status.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class BTPairStatus {

    public Result result;

    public enum Result {
        NOT_PAIRED(0),
        DISCONNECTED(1),
        CONNECTING(2),
        CONNECTED(3);

        public final int code;

        Result(int code) {
            this.code = code;
        }

        public static Result fromCode(int code) {
            switch (code) {
                case 0:
                    return NOT_PAIRED;
                case 1:
                    return DISCONNECTED;
                case 2:
                    return CONNECTING;
                case 3:
                    return CONNECTED;
                default:
                    return NOT_PAIRED;
            }
        }
    }
}
