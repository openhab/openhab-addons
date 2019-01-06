package org.openhab.binding.gruenbecksoftener.data;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SoftenerDataXmlAdapter extends XmlAdapter<MapWrapper, Map<String, Object>> {

    @Override
    public Map<String, Object> unmarshal(MapWrapper v) throws Exception {
        return new HashMap<>();
        // return Arrays.stream(v).collect(Collectors.toMap(Element::getLocalName, Element::getTextContent));
    }

    @Override
    public MapWrapper marshal(Map<String, Object> v) throws Exception {
        return null;
        // return v.entrySet().stream().map(entry -> new MapElements(entry.getKey(), entry.getValue()))
        // .toArray(MapElements[]::new);
    }

}
