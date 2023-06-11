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
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to clean up string values
 *
 * @author Jeroen Idserda - Initial contribution
 */
public class StringAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) throws Exception {
        String val = v;
        if (val != null) {
            return val.trim();
        }
        return val;
    }

    @Override
    public String marshal(String v) throws Exception {
        return v;
    }
}
