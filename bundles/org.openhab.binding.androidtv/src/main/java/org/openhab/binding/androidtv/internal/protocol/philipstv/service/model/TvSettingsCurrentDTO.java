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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link TvSettingsCurrentDTO} class defines the Data Transfer Object
 * for the POST Request to Philips TV API /menuitems/settings/current endpoint to retrieve current settings of the tv,
 * e.g. the tv picture brightness.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class TvSettingsCurrentDTO {

    @JsonProperty("nodes")
    private List<NodesDTO> nodes;

    public TvSettingsCurrentDTO() {
    }

    public TvSettingsCurrentDTO(List<NodesDTO> nodes) {
        this.nodes = nodes;
    }

    public void setNodes(List<NodesDTO> nodes) {
        this.nodes = nodes;
    }

    public List<NodesDTO> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "TvSettingsCurrentDTO{" + "nodes = '" + nodes + '\'' + "}";
    }
}
