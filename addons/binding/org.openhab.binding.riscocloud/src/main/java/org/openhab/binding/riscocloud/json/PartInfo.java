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

public class PartInfo {

    @SerializedName("armedStr")
    @Expose
    private String armedStr;
    @SerializedName("disarmedStr")
    @Expose
    private String disarmedStr;
    @SerializedName("partarmedStr")
    @Expose
    private String partarmedStr;

    public String getArmedStr() {
        return armedStr;
    }

    public void setArmedStr(String armedStr) {
        this.armedStr = armedStr;
    }

    public String getDisarmedStr() {
        return disarmedStr;
    }

    public void setDisarmedStr(String disarmedStr) {
        this.disarmedStr = disarmedStr;
    }

    public String getPartarmedStr() {
        return partarmedStr;
    }

    public void setPartarmedStr(String partarmedStr) {
        this.partarmedStr = partarmedStr;
    }

}
