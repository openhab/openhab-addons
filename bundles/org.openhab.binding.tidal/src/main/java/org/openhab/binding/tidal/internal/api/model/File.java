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
package org.openhab.binding.tidal.internal.api.model;

import com.google.gson.annotations.JsonAdapter;

/**
 * Tidal Api Album data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class File {
    private String href;
    private Meta meta;

    public class Meta {
        private int width;
        private int height;
    }

    public Meta getMeta() {
        return meta;
    }

    public String getHref() {
        return href;
    }

}
