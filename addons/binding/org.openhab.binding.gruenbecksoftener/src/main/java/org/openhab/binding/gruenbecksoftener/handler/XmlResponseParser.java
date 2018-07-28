package org.openhab.binding.gruenbecksoftener.handler;

import java.io.StringReader;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.openhab.binding.gruenbecksoftener.json.SoftenerXmlResponse;

class XmlResponseParser implements Function<String, SoftenerXmlResponse> {

    @Override
    public SoftenerXmlResponse apply(String responseBody) {
        try {
            JAXBContext context = JAXBContext.newInstance(SoftenerXmlResponse.class);
            return (SoftenerXmlResponse) context.createUnmarshaller().unmarshal(new StringReader(responseBody));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}