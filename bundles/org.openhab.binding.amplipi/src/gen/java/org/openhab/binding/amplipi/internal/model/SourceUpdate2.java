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
 * Partial reconfiguration of a specific audio Source
 **/
@Schema(description = "Partial reconfiguration of a specific audio Source ")
public class SourceUpdate2 {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    private String input;

    @Schema(required = true)
    private Integer id;

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

    public SourceUpdate2 name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get input
     *
     * @return input
     **/
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public SourceUpdate2 input(String input) {
        this.input = input;
        return this;
    }

    /**
     * Get id
     * minimum: 0
     * maximum: 4
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SourceUpdate2 id(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SourceUpdate2 {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
