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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author MikeTheTux - Initial contribution
 */
public class Settings {
    private byte cuttingHeight;
    private Headlight headlight;

    public byte getCuttingHeight() {
        return cuttingHeight;
    }

    public void setCuttingHeight(byte cuttingHeight) {
        this.cuttingHeight = cuttingHeight;
    }

    public Headlight getHeadlight() {
        return headlight;
    }

    public void setHeadlight(Headlight headlight) {
        this.headlight = headlight;
    }
}
