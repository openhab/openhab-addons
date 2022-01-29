/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.amplipi.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * An audio source
 **/
@Schema(description = "An audio source ")
public class Source {

    @Schema
    /**
     * Unique identifier
     **/
    private Integer id;

    @Schema(required = true)
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    /**
     * Connected audio source * Digital Stream ('stream=SID') where SID is the ID of the connected stream * Analog RCA
     * Input ('local') connects to the RCA inputs associated * Nothing ('') behind the scenes this is muxed to a digital
     * output
     **/
    private String input = "";

    /**
     * Unique identifier
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Source id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Friendly name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Source name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Connected audio source * Digital Stream (&#39;stream&#x3D;SID&#39;) where SID is the ID of the connected stream *
     * Analog RCA Input (&#39;local&#39;) connects to the RCA inputs associated * Nothing (&#39;&#39;) behind the scenes
     * this is muxed to a digital output
     *
     * @return input
     **/
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Source input(String input) {
        this.input = input;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Source {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
