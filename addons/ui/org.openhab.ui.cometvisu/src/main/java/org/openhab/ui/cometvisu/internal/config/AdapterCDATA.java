/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for marshaling CDATA content
 * 
 * @author Tobias Bräutigam
 *
 */
public class AdapterCDATA extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) throws Exception {
        return String.format("<![CDATA[ {} ]]>", v);
    }

    @Override
    public String marshal(String v) throws Exception {
        return v;
    }

}
