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
