/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimeValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSWeekdayValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcResourceInteractionServiceTest {

    private IhcResourceInteractionService ihcResourceInteractionService;
    private final String url = "https://1.1.1.1/ws/ResourceInteractionService";

    // @formatter:off
    private final String query =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soapenv:Body>\n"
          + "  <ns1:getRuntimeValue1 xmlns:ns1=\"utcs\">%s</ns1:getRuntimeValue1>\n"
          + " </soapenv:Body>\n"
          + "</soapenv:Envelope>\n";

    private final String response11111 =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">11111</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">dataline_output</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSBooleanValue\">\n" +
            "<ns2:value xsi:type=\"xsd:boolean\">true</ns2:value>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response22222 =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">22222</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_temperature</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSFloatingPointValue\">\n" +
            "<ns2:maximumValue xsi:type=\"xsd:double\">1000.0</ns2:maximumValue>\n" +
            "\n" +
            "<ns2:minimumValue xsi:type=\"xsd:double\">-1000.0</ns2:minimumValue>\n" +
            "\n" +
            "<ns2:floatingPointValue xsi:type=\"xsd:double\">24.399999618530273</ns2:floatingPointValue>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response33333 =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">33333</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_enum</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSEnumValue\">\n" +
            "<ns2:definitionTypeID xsi:type=\"xsd:int\">4236359</ns2:definitionTypeID>\n" +
            "\n" +
            "<ns2:enumValueID xsi:type=\"xsd:int\">4236872</ns2:enumValueID>\n" +
            "\n" +
            "<ns2:enumName xsi:type=\"xsd:string\">test value</ns2:enumName>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response44444 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">44444</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_integer</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSIntegerValue\">\n" +
            "<ns2:integer xsi:type=\"xsd:int\">424561</ns2:integer>\n" +
            "\n" +
            "<ns2:maximumValue xsi:type=\"xsd:int\">2147483647</ns2:maximumValue>\n" +
            "\n" +
            "<ns2:minimumValue xsi:type=\"xsd:int\">-2147483648</ns2:minimumValue>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response55555 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">55555</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_timer</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSTimerValue\">\n" +
            "<ns2:milliseconds xsi:type=\"xsd:long\">13851</ns2:milliseconds>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response66666 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">66666</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_weekday</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSWeekdayValue\">\n" +
            "<ns2:weekdayNumber xsi:type=\"xsd:int\">2</ns2:weekdayNumber>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response77777 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">77777</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_date</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSDateValue\">\n" +
            "<ns2:day xsi:type=\"xsd:byte\">22</ns2:day>\n" +
            "\n" +
            "<ns2:month xsi:type=\"xsd:byte\">10</ns2:month>\n" +
            "\n" +
            "<ns2:year xsi:type=\"xsd:short\">2018</ns2:year>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String response88888 = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getRuntimeValue2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">88888</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\">resource_time</ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSTimeValue\">\n" +
            "<ns2:hours xsi:type=\"xsd:int\">16</ns2:hours>\n" +
            "\n" +
            "<ns2:minutes xsi:type=\"xsd:int\">32</ns2:minutes>\n" +
            "\n" +
            "<ns2:seconds xsi:type=\"xsd:int\">45</ns2:seconds>\n" +
            "</ns1:value>\n" +
            "</ns1:getRuntimeValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String updateOkResult =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:setResourceValue2 xmlns:ns1=\"utcs\" xsi:type=\"xsd:boolean\">true</ns1:setResourceValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String updateFailureResult =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:setResourceValue2 xmlns:ns1=\"utcs\" xsi:type=\"xsd:boolean\">false</ns1:setResourceValue2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";


    private final String update100001 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSBooleanValue\">\n"
          + "    <q1:value>true</q1:value>\n"
          + "   </value>\n"
          + "   <resourceID>100001</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>\n";

    private final String update100011 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSBooleanValue\">\n"
          + "    <q1:value>true</q1:value>\n"
          + "   </value>\n"
          + "   <resourceID>100011</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>\n";

    final String update200002 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSFloatingPointValue\">\n"
          + "    <q1:maximumValue>1000.0</q1:maximumValue>\n"
          + "    <q1:minimumValue>-1000.0</q1:minimumValue>\n"
          + "    <q1:floatingPointValue>24.1</q1:floatingPointValue>\n"
          + "   </value>\n"
          + "   <resourceID>200002</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>\n";

    private final String update300003 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSEnumValue\">\n"
          + "    <q1:definitionTypeID>11111</q1:definitionTypeID>\n"
          + "    <q1:enumValueID>22222</q1:enumValueID>\n"
          + "    <q1:enumName>test123</q1:enumName>\n"
          + "   </value>\n"
          + "   <resourceID>300003</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final String update400004 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSIntegerValue\">\n"
          + "    <q1:maximumValue>1000</q1:maximumValue>\n"
          + "    <q1:minimumValue>-1000</q1:minimumValue>\n"
          + "    <q1:integer>201</q1:integer>\n"
          + "   </value>\n"
          + "   <resourceID>400004</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final  String update500005 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSTimerValue\">\n"
          + "    <q1:milliseconds>2134</q1:milliseconds>\n"
          + "   </value>\n"
          + "   <resourceID>500005</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final String update600006 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSWeekdayValue\">\n"
          + "    <q1:weekdayNumber>4</q1:weekdayNumber>\n"
          + "   </value>\n"
          + "   <resourceID>600006</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final String update700007 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSDateValue\">\n"
          + "    <q1:month>3</q1:month>\n"
          + "    <q1:year>2018</q1:year>\n"
          + "    <q1:day>24</q1:day>\n"
          + "   </value>\n"
          + "   <resourceID>700007</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final String update800008 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soap:Body>\n"
          + "  <setResourceValue1 xmlns=\"utcs\">\n"
          + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSTimeValue\">\n"
          + "    <q1:hours>15</q1:hours>\n"
          + "    <q1:minutes>34</q1:minutes>\n"
          + "    <q1:seconds>45</q1:seconds>\n"
          + "   </value>\n"
          + "   <resourceID>800008</resourceID>\n"
          + "   <isValueRuntime>true</isValueRuntime>\n"
          + "  </setResourceValue1>\n"
          + " </soap:Body>\n"
          + "</soap:Envelope>";

    private final String resourceValueNotificationsQuery =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:utcs=\"utcs\">\n" +
            " <soapenv:Body>\n" +
            "  <utcs:waitForResourceValueChanges1>1</utcs:waitForResourceValueChanges1>\n" +
            " </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private final String resourceValueNotificationsResponse ="<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:waitForResourceValueChanges2 xmlns:ns1=\"utcs\">\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">10454030</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns2=\"utcs.values\"  xsi:type=\"ns2:WSDateValue\">\n" +
            "<ns2:day xsi:type=\"xsd:byte\">28</ns2:day>\n" +
            "\n" +
            "<ns2:month xsi:type=\"xsd:byte\">9</ns2:month>\n" +
            "\n" +
            "<ns2:year xsi:type=\"xsd:short\">2018</ns2:year>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">10454541</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns3=\"utcs.values\"  xsi:type=\"ns3:WSTimeValue\">\n" +
            "<ns3:hours xsi:type=\"xsd:int\">10</ns3:hours>\n" +
            "\n" +
            "<ns3:minutes xsi:type=\"xsd:int\">20</ns3:minutes>\n" +
            "\n" +
            "<ns3:seconds xsi:type=\"xsd:int\">30</ns3:seconds>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">10447883</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns4=\"utcs.values\"  xsi:type=\"ns4:WSIntegerValue\">\n" +
            "<ns4:integer xsi:type=\"xsd:int\">456789</ns4:integer>\n" +
            "\n" +
            "<ns4:maximumValue xsi:type=\"xsd:int\">2147483647</ns4:maximumValue>\n" +
            "\n" +
            "<ns4:minimumValue xsi:type=\"xsd:int\">-2147483648</ns4:minimumValue>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">4133210</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns5=\"utcs.values\"  xsi:type=\"ns5:WSBooleanValue\">\n" +
            "<ns5:value xsi:type=\"xsd:boolean\">false</ns5:value>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">3988827</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns6=\"utcs.values\"  xsi:type=\"ns6:WSBooleanValue\">\n" +
            "<ns6:value xsi:type=\"xsd:boolean\">true</ns6:value>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">4159252</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns7=\"utcs.values\"  xsi:type=\"ns7:WSFloatingPointValue\">\n" +
            "<ns7:maximumValue xsi:type=\"xsd:double\">1000.0</ns7:maximumValue>\n" +
            "\n" +
            "<ns7:minimumValue xsi:type=\"xsd:double\">-1000.0</ns7:minimumValue>\n" +
            "\n" +
            "<ns7:floatingPointValue xsi:type=\"xsd:double\">24.5</ns7:floatingPointValue>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">3515401</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns8=\"utcs.values\"  xsi:type=\"ns8:WSWeekdayValue\">\n" +
            "<ns8:weekdayNumber xsi:type=\"xsd:int\">2</ns8:weekdayNumber>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSResourceValueEnvelope\">\n" +
            "<ns1:resourceID xsi:type=\"xsd:int\">7419663</ns1:resourceID>\n" +
            "\n" +
            "<ns1:isValueRuntime xsi:type=\"xsd:boolean\">true</ns1:isValueRuntime>\n" +
            "\n" +
            "<ns1:typeString xsi:type=\"xsd:string\"></ns1:typeString>\n" +
            "\n" +
            "<ns1:value xmlns:ns9=\"utcs.values\"  xsi:type=\"ns9:WSEnumValue\">\n" +
            "<ns9:definitionTypeID xsi:type=\"xsd:int\">4236359</ns9:definitionTypeID>\n" +
            "\n" +
            "<ns9:enumValueID xsi:type=\"xsd:int\">4236872</ns9:enumValueID>\n" +
            "\n" +
            "<ns9:enumName xsi:type=\"xsd:string\">testVal</ns9:enumName>\n" +
            "</ns1:value>\n" +
            "</ns1:arrayItem>\n" +
            "</ns1:waitForResourceValueChanges2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";
    // @formatter:on

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcResourceInteractionService = spy(new IhcResourceInteractionService(url, 0));
        doNothing().when(ihcResourceInteractionService).openConnection(eq(url));

        doReturn(response11111).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 11111)),
                ArgumentMatchers.anyInt());
        doReturn(response22222).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 22222)),
                ArgumentMatchers.anyInt());
        doReturn(response33333).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 33333)),
                ArgumentMatchers.anyInt());
        doReturn(response44444).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 44444)),
                ArgumentMatchers.anyInt());
        doReturn(response55555).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 55555)),
                ArgumentMatchers.anyInt());
        doReturn(response66666).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 66666)),
                ArgumentMatchers.anyInt());
        doReturn(response77777).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 77777)),
                ArgumentMatchers.anyInt());
        doReturn(response88888).when(ihcResourceInteractionService).sendQuery(eq(String.format(query, 88888)),
                ArgumentMatchers.anyInt());

        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update100001),
                ArgumentMatchers.anyInt());
        doReturn(updateFailureResult).when(ihcResourceInteractionService).sendQuery(eq(update100011),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update200002),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update300003),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update400004),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update500005),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update600006),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update700007),
                ArgumentMatchers.anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(update800008),
                ArgumentMatchers.anyInt());

        doReturn(resourceValueNotificationsResponse).when(ihcResourceInteractionService)
                .sendQuery(eq(resourceValueNotificationsQuery), ArgumentMatchers.anyInt());
    }

    @Test
    public void testWSBooleanValueQuery() throws IhcExecption {
        final WSBooleanValue val = (WSBooleanValue) ihcResourceInteractionService.resourceQuery(11111);
        assertEquals(11111, val.getResourceID());
        assertEquals(true, val.isValue());
    }

    @Test
    public void testWSFloatingPointValueQuery() throws IhcExecption {
        final WSFloatingPointValue val = (WSFloatingPointValue) ihcResourceInteractionService.resourceQuery(22222);
        assertEquals(22222, val.getResourceID());
        assertEquals(24.399999618530273, val.getFloatingPointValue(), 0.000001);
        assertEquals(-1000.0, val.getMinimumValue(), 0.01);
        assertEquals(1000.0, val.getMaximumValue(), 0.01);
    }

    @Test
    public void testWSEnumValueQuery() throws IhcExecption {
        final WSEnumValue val = (WSEnumValue) ihcResourceInteractionService.resourceQuery(33333);
        assertEquals(33333, val.getResourceID());
        assertEquals(4236359, val.getDefinitionTypeID());
        assertEquals(4236872, val.getEnumValueID());
        assertEquals("test value", val.getEnumName());
    }

    @Test
    public void testWSIntegerValueQuery() throws IhcExecption {
        final WSIntegerValue val = (WSIntegerValue) ihcResourceInteractionService.resourceQuery(44444);
        assertEquals(44444, val.getResourceID());
        assertEquals(424561, val.getInteger());
        assertEquals(-2147483648, val.getMinimumValue());
        assertEquals(2147483647, val.getMaximumValue());
    }

    @Test
    public void testWSTimerValueQuery() throws IhcExecption {
        final WSTimerValue val = (WSTimerValue) ihcResourceInteractionService.resourceQuery(55555);
        assertEquals(55555, val.getResourceID());
        assertEquals(13851, val.getMilliseconds());
    }

    @Test
    public void testWSWeekdayValueQuery() throws IhcExecption {
        final WSWeekdayValue val = (WSWeekdayValue) ihcResourceInteractionService.resourceQuery(66666);
        assertEquals(66666, val.getResourceID());
        assertEquals(2, val.getWeekdayNumber());
    }

    @Test
    public void testWSDateValueQuery() throws IhcExecption {
        final WSDateValue val = (WSDateValue) ihcResourceInteractionService.resourceQuery(77777);
        assertEquals(77777, val.getResourceID());
        assertEquals(2018, val.getYear());
        assertEquals(10, val.getMonth());
        assertEquals(22, val.getDay());
    }

    @Test
    public void testWSTimeValueQuery() throws IhcExecption {
        final WSTimeValue val = (WSTimeValue) ihcResourceInteractionService.resourceQuery(88888);
        assertEquals(88888, val.getResourceID());
        assertEquals(16, val.getHours());
        assertEquals(32, val.getMinutes());
        assertEquals(45, val.getSeconds());
    }

    @Test
    public void testWSBooleanValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSBooleanValue(100001, true));
        assertEquals(true, result);
    }

    @Test
    public void testWSBooleanValueUpdateFailure() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSBooleanValue(100011, true));
        assertEquals(false, result);
    }

    @Test
    public void testWSFloatingPointValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService
                .resourceUpdate(new WSFloatingPointValue(200002, 24.1, -1000.0, 1000.0));
        assertEquals(true, result);
    }

    @Test
    public void testWSEnumValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSEnumValue(300003, 11111, 22222, "test123"));
        assertEquals(true, result);
    }

    @Test
    public void testWSIntegerValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSIntegerValue(400004, 201, -1000, 1000));
        assertEquals(true, result);
    }

    @Test
    public void testWSTimerValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSTimerValue(500005, 2134));
        assertEquals(true, result);
    }

    @Test
    public void testWSWeekdayValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSWeekdayValue(600006, 4));
        assertEquals(true, result);
    }

    @Test
    public void testWSDateValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService
                .resourceUpdate(new WSDateValue(700007, (short) 2018, (byte) 3, (byte) 24));
        assertEquals(true, result);
    }

    @Test
    public void testWSTimeValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSTimeValue(800008, 15, 34, 45));
        assertEquals(true, result);
    }

    @Test
    public void testResourceValueNotifications() throws IhcExecption, SocketTimeoutException {
        final List<WSResourceValue> list = ihcResourceInteractionService.waitResourceValueNotifications(1);
        assertEquals(8, list.size());

        List<WSResourceValue> found = new ArrayList<WSResourceValue>();

        for (WSResourceValue val : list) {
            switch (val.getResourceID()) {
                case 10454030:
                    assertEquals(2018, ((WSDateValue) val).getYear());
                    assertEquals(9, ((WSDateValue) val).getMonth());
                    assertEquals(28, ((WSDateValue) val).getDay());
                    found.add(val);
                    break;
                case 10454541:
                    assertEquals(10, ((WSTimeValue) val).getHours());
                    assertEquals(20, ((WSTimeValue) val).getMinutes());
                    assertEquals(30, ((WSTimeValue) val).getSeconds());
                    found.add(val);
                    break;
                case 10447883:
                    assertEquals(456789, ((WSIntegerValue) val).getInteger());
                    assertEquals(-2147483648, ((WSIntegerValue) val).getMinimumValue());
                    assertEquals(2147483647, ((WSIntegerValue) val).getMaximumValue());
                    found.add(val);
                    break;
                case 4133210:
                    assertEquals(false, ((WSBooleanValue) val).isValue());
                    found.add(val);
                    break;
                case 3988827:
                    assertEquals(true, ((WSBooleanValue) val).isValue());
                    found.add(val);
                    break;
                case 4159252:
                    assertEquals(24.50, ((WSFloatingPointValue) val).getFloatingPointValue(), 0.01);
                    assertEquals(-1000.00, ((WSFloatingPointValue) val).getMinimumValue(), 0.01);
                    assertEquals(1000.00, ((WSFloatingPointValue) val).getMaximumValue(), 0.01);
                    found.add(val);
                    break;
                case 3515401:
                    assertEquals(2, ((WSWeekdayValue) val).getWeekdayNumber());
                    found.add(val);
                    break;
                case 7419663:
                    assertEquals(4236359, ((WSEnumValue) val).getDefinitionTypeID());
                    assertEquals(4236872, ((WSEnumValue) val).getEnumValueID());
                    assertEquals("testVal", ((WSEnumValue) val).getEnumName());
                    found.add(val);
                    break;
            }
        }
        assertEquals(8, found.size());
    }
}
