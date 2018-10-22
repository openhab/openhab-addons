/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is generated with http://www.jsonschema2pojo.org/
 * Use json provided by MyElas server and choose these options :
 * Package : org.openhab.binding.myelas.server.handler
 * Class Name : ServerDatasObject
 * Target language : Java
 * Source type : JSON
 * Annotation style : Gson
 * Tick :
 * - Use double numbers
 * - Include getters and setters
 * - Allow additional properties
 *
 * @author Sébastien Cantineau - Initial contribution
 */

public class Part {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("armIcon")
    @Expose
    private String armIcon;
    @SerializedName("detectors")
    @Expose
    private List<Detector> detectors = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArmIcon() {
        return armIcon;
    }

    public void setArmIcon(String armIcon) {
        this.armIcon = armIcon;
    }

    public List<Detector> getDetectors() {
        return detectors;
    }

    public void setDetectors(List<Detector> detectors) {
        this.detectors = detectors;
    }

}
