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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for bluetooth update result.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BluetoothUpdateResult {
    private Success success;

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public BluetoothUpdateResult withSuccess(Success success) {
        this.success = success;
        return this;
    }

    public static class Success {
        private Bluetooth data;

        public Bluetooth getData() {
            return data;
        }

        public void setData(Bluetooth data) {
            this.data = data;
        }

        public Success withData(Bluetooth data) {
            this.data = data;
            return this;
        }
    }
}
