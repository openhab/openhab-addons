/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Pojo for audio update result.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class AudioUpdateResult {
    private Success success;

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public AudioUpdateResult withSuccess(Success success) {
        this.success = success;
        return this;
    }

    public static class Success {
        private Audio data;

        public Audio getData() {
            return data;
        }

        public void setData(Audio data) {
            this.data = data;
        }

        public Success withData(Audio data) {
            this.data = data;
            return this;
        }
    }
}
