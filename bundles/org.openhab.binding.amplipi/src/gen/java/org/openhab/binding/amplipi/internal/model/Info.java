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
package org.openhab.binding.amplipi.internal.model;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Information about the settings used by the controller
 **/
@Schema(description = "Information about the settings used by the controller ")
public class Info {

    @Schema
    @SerializedName("config_file")
    private String configFile = "Unknown";

    @Schema
    private String version = "Unknown";

    @Schema
    @SerializedName("mock_ctrl")
    private Boolean mockCtrl = false;

    @Schema
    @SerializedName("mock_streams")
    private Boolean mockStreams = false;

    /**
     * Get configFile
     *
     * @return configFile
     **/
    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public Info configFile(String configFile) {
        this.configFile = configFile;
        return this;
    }

    /**
     * Get version
     *
     * @return version
     **/
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Info version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get mockCtrl
     *
     * @return mockCtrl
     **/
    public Boolean getMockCtrl() {
        return mockCtrl;
    }

    public void setMockCtrl(Boolean mockCtrl) {
        this.mockCtrl = mockCtrl;
    }

    public Info mockCtrl(Boolean mockCtrl) {
        this.mockCtrl = mockCtrl;
        return this;
    }

    /**
     * Get mockStreams
     *
     * @return mockStreams
     **/
    public Boolean getMockStreams() {
        return mockStreams;
    }

    public void setMockStreams(Boolean mockStreams) {
        this.mockStreams = mockStreams;
    }

    public Info mockStreams(Boolean mockStreams) {
        this.mockStreams = mockStreams;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Info {\n");

        sb.append("    configFile: ").append(toIndentedString(configFile)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    mockCtrl: ").append(toIndentedString(mockCtrl)).append("\n");
        sb.append("    mockStreams: ").append(toIndentedString(mockStreams)).append("\n");
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
