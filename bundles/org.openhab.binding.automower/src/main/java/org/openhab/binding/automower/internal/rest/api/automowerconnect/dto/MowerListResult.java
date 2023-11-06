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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

import java.util.List;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class MowerListResult {
    private List<Mower> data;

    public List<Mower> getData() {
        return data;
    }

    public void setData(List<Mower> data) {
        this.data = data;
    }
}
