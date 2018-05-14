/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

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
            val = StringUtils.trimToEmpty(val);
        }
        return val;
    }

    @Override
    public String marshal(String v) throws Exception {
        return v;
    }
}
