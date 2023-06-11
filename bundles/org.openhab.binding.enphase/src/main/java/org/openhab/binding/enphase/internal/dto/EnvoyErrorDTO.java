/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal.dto;

/**
 * Data class for handling errors returned by the Envoy gateway.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class EnvoyErrorDTO {
    public int status;
    public String error;
    public String info;
    public String moreInfo;

    @Override
    public String toString() {
        return "EnvoyErrorDTO [status=" + status + ", error=" + error + ", info=" + info + ", moreInfo=" + moreInfo
                + "]";
    }
}
