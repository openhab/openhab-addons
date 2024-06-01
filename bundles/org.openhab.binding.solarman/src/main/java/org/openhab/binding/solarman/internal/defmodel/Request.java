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
package org.openhab.binding.solarman.internal.defmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Catalin Sanda - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
    private Integer start;
    private Integer end;
    @JsonProperty("mb_functioncode")
    private Integer mbFunctioncode;

    public Request() {
    }

    public Request(Integer mbFunctioncode, Integer start, Integer end) {
        this.mbFunctioncode = mbFunctioncode;
        this.start = start;
        this.end = end;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getMbFunctioncode() {
        return mbFunctioncode;
    }

    public void setMbFunctioncode(Integer mbFunctioncode) {
        this.mbFunctioncode = mbFunctioncode;
    }
}
