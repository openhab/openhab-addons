/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanBaseConfig {
    public String enoceanId;

    // TODO uncomment this line if ESH can parse parameter value list (issue eclipse/smarthome #6146)
    // public ArrayList<String> receivingEEPId;
    protected ArrayList<String> rEEPId;

    public List<String> getReceivingEEPId() {
        return rEEPId;
    }

    public void setReceivingEEPId(String receivingEEPId) {
        rEEPId = new ArrayList<String>(Arrays.asList(receivingEEPId.split(",")));
    }

    public void setReceivingEEPId(ArrayList<String> receivingEEPId) {
        rEEPId = new ArrayList<>();
        rEEPId.addAll(receivingEEPId);
    }

    public byte[] getEnOceanId() {
        return HexUtils.hexToBytes(enoceanId);
    }
}
