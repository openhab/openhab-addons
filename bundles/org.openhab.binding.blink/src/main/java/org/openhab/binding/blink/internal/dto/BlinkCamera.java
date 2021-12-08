/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.dto;

/**
 * The {@link BlinkCamera} class is the DTO for cameras returned in the homescreen api call.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
public class BlinkCamera {

    public BlinkCamera(Long networkId, Long cameraId) {
        this.network_id = networkId;
        this.id = cameraId;
    }

    public Long id;
    public Long network_id;
    public String name;

    public boolean enabled;
    public String thumbnail;
    public String status;
    public String battery;

    public Signals signals;

    public class Signals {
        public long wifi;
        public double temp;
    }
}
