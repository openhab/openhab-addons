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
package org.openhab.binding.lametrictime.internal.api.cloud.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo for icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Icons {
    private IconsMetadata meta;
    private List<Icon> data = new ArrayList<Icon>();

    public IconsMetadata getMeta() {
        return meta;
    }

    public void setMeta(IconsMetadata meta) {
        this.meta = meta;
    }

    public Icons withMeta(IconsMetadata meta) {
        this.meta = meta;
        return this;
    }

    public List<Icon> getData() {
        return data;
    }

    public void setData(List<Icon> data) {
        this.data = data;
    }

    public Icons withData(List<Icon> data) {
        this.data = data;
        return this;
    }
}
