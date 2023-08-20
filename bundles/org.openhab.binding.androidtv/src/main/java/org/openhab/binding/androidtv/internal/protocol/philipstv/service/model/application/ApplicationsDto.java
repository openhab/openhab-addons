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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link AvailableAppsDto}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class ApplicationsDto {

    @JsonProperty
    private String label = "";

    @JsonProperty
    private String id = "";

    @JsonProperty
    private String type = "";

    @JsonProperty
    private IntentDto intent;

    @JsonProperty
    private int order;

    public ApplicationsDto(IntentDto intent) {
        this.intent = intent;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setIntent(IntentDto intent) {
        this.intent = intent;
    }

    public IntentDto getIntent() {
        return intent;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "ApplicationsItem{" + "label = '" + label + '\'' + ",id = '" + id + '\'' + ",type = '" + type + '\''
                + ",intent = '" + intent + '\'' + ",order = '" + order + '\'' + "}";
    }
}
