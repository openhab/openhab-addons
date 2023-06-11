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
package org.openhab.binding.ihc.internal.ws.services;

import org.openhab.binding.ihc.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc.internal.ws.datatypes.WSNumberOfSegments;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.datatypes.WSSegmentationSize;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Class to handle IHC / ELKO LS Controller's controller service.
 *
 * Controller service is used to fetch information from the controller.
 * E.g. Project file or controller status.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcControllerService extends IhcBaseService {

    public IhcControllerService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool, timeout, host, "ControllerService");
    }

    /**
     * Query project information from the controller.
     *
     * @return project information.
     * @throws IhcExecption
     */
    public synchronized WSProjectInfo getProjectInfo() throws IhcExecption {
        String response = sendSoapQuery("getProjectInfo", EMPTY_QUERY);
        return new WSProjectInfo().parseXMLData(response);
    }

    /**
     * Query number of segments project contains.
     *
     * @return number of segments.
     * @throws IhcExecption
     */
    public synchronized int getProjectNumberOfSegments() throws IhcExecption {
        String response = sendSoapQuery("getIHCProjectNumberOfSegments", EMPTY_QUERY);
        return new WSNumberOfSegments().parseXMLData(response).getNumberOfSegments();
    }

    /**
     * Query segmentation size.
     *
     * @return segmentation size in bytes.
     * @throws IhcExecption
     */
    public synchronized int getProjectSegmentationSize() throws IhcExecption {
        String response = sendSoapQuery("getIHCProjectSegmentationSize", EMPTY_QUERY);
        return new WSSegmentationSize().parseXMLData(response).getSegmentationSize();
    }

    /**
     * Query project segment data.
     *
     * @param index segments index.
     * @param major project major revision number.
     * @param minor project minor revision number.
     * @return segments data.
     * @throws IhcExecption
     */
    public synchronized WSFile getProjectSegment(int index, int major, int minor) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <ns1:getIHCProjectSegment1 xmlns:ns1=\"utcs\" xsi:type=\"xsd:int\">%s</ns1:getIHCProjectSegment1>\n"
                + "  <ns2:getIHCProjectSegment2 xmlns:ns2=\"utcs\" xsi:type=\"xsd:int\">%s</ns2:getIHCProjectSegment2>\n"
                + "  <ns3:getIHCProjectSegment3 xmlns:ns3=\"utcs\" xsi:type=\"xsd:int\">%s</ns3:getIHCProjectSegment3>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, index, major, minor);
        String response = sendSoapQuery("getIHCProjectSegment", query);
        return new WSFile().parseXMLData(response);
    }

    /**
     * Query controller current state.
     *
     * @return controller's current state.
     * @throws IhcExecption
     */
    public synchronized WSControllerState getControllerState() throws IhcExecption {
        String response = sendSoapQuery("getState", EMPTY_QUERY);
        return new WSControllerState().parseXMLData(response);
    }

    /**
     * Wait controller state change notification.
     *
     * @param previousState Previous controller state.
     * @param timeoutInSeconds How many seconds to wait notifications.
     * @return current controller state.
     * @throws IhcExecption
     */
    public synchronized WSControllerState waitStateChangeNotifications(WSControllerState previousState,
            int timeoutInSeconds) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soapenv:Body>\n"
                + "  <ns1:waitForControllerStateChange1 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSControllerState\">\n"
                + "   <ns1:state xsi:type=\"xsd:string\">%s</ns1:state>\n"
                + "  </ns1:waitForControllerStateChange1>\n"
                + "  <ns2:waitForControllerStateChange2 xmlns:ns2=\"utcs\" xsi:type=\"xsd:int\">%s</ns2:waitForControllerStateChange2>\n"
                + " </soapenv:Body>\n"
                + "</soapenv:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, previousState.getState(), timeoutInSeconds);
        String response = sendSoapQuery("waitForControllerStateChange", query, getTimeout() + timeoutInSeconds * 1000);
        return new WSControllerState().parseXMLData(response);
    }
}
