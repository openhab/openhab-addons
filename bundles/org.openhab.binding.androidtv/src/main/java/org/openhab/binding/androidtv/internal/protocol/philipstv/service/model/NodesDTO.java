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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvSettingsCurrentDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class NodesDTO {

    @JsonProperty("nodeid")
    private int nodeid;

    public void setNodeid(int nodeid) {
        this.nodeid = nodeid;
    }

    public int getNodeid() {
        return nodeid;
    }

    @Override
    public String toString() {
        return "NodesItem{" + "nodeid = '" + nodeid + '\'' + "}";
    }
}
