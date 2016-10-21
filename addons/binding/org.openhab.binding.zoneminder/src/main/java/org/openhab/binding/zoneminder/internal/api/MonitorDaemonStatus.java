/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "status", "statustext" })
public class MonitorDaemonStatus {

    @JsonProperty("status")
    private Boolean status;
    @JsonProperty("statustext")
    private String statustext;

    /**
     *
     * @return
     *         The status
     */
    @JsonProperty("status")
    public Boolean getStatus() {
        return status;
    }

    /**
     *
     * @param status
     *            The status
     */
    @JsonProperty("status")
    public void setStatus(Boolean status) {
        this.status = status;
    }

    /**
     *
     * @return
     *         The statustext
     */
    @JsonProperty("statustext")
    public String getStatustext() {
        return statustext;
    }

    /**
     *
     * @param statustext
     *            The statustext
     */
    @JsonProperty("statustext")
    public void setStatustext(String statustext) {
        this.statustext = statustext;
    }

}
