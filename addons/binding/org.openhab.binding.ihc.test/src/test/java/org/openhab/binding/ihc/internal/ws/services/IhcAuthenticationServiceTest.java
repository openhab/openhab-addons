/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.ihc.internal.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.services.IhcAuthenticationService;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAuthenticationServiceTest {

    private IhcAuthenticationService ihcAuthenticationService;
    private final String url = "https://1.1.1.1/ws/AuthenticationService";

    // @formatter:off
    private final String querySuccesfulLogin =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
          + " <soapenv:Body>\n"
          + "  <authenticate1 xmlns=\"utcs\">\n"
          + "   <password>pass</password>\n"
          + "   <username>user</username>\n"
          + "   <application>treeview</application>\n"
          + "  </authenticate1>\n"
          + " </soapenv:Body>\n"
          + "</soapenv:Envelope>";

    private final String responseSuccesfulLogin =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:authenticate2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSLoginResult\">\n" +
            "<ns1:loggedInUser xsi:type=\"ns1:WSUser\">\n" +
            "<ns1:createdDate xsi:type=\"ns1:WSDate\">\n" +
            "<ns1:day xsi:type=\"xsd:int\">13</ns1:day>\n" +
            "\n" +
            "<ns1:monthWithJanuaryAsOne xsi:type=\"xsd:int\">4</ns1:monthWithJanuaryAsOne>\n" +
            "\n" +
            "<ns1:hours xsi:type=\"xsd:int\">19</ns1:hours>\n" +
            "\n" +
            "<ns1:minutes xsi:type=\"xsd:int\">43</ns1:minutes>\n" +
            "\n" +
            "<ns1:seconds xsi:type=\"xsd:int\">9</ns1:seconds>\n" +
            "\n" +
            "<ns1:year xsi:type=\"xsd:int\">2018</ns1:year>\n" +
            "</ns1:createdDate>\n" +
            "\n" +
            "<ns1:loginDate xsi:type=\"ns1:WSDate\">\n" +
            "<ns1:day xsi:type=\"xsd:int\">28</ns1:day>\n" +
            "\n" +
            "<ns1:monthWithJanuaryAsOne xsi:type=\"xsd:int\">6</ns1:monthWithJanuaryAsOne>\n" +
            "\n" +
            "<ns1:hours xsi:type=\"xsd:int\">20</ns1:hours>\n" +
            "\n" +
            "<ns1:minutes xsi:type=\"xsd:int\">2</ns1:minutes>\n" +
            "\n" +
            "<ns1:seconds xsi:type=\"xsd:int\">28</ns1:seconds>\n" +
            "\n" +
            "<ns1:year xsi:type=\"xsd:int\">2018</ns1:year>\n" +
            "</ns1:loginDate>\n" +
            "\n" +
            "<ns1:username xsi:type=\"xsd:string\">user</ns1:username>\n" +
            "\n" +
            "<ns1:password xsi:type=\"xsd:string\">pass</ns1:password>\n" +
            "\n" +
            "<ns1:email xsi:type=\"xsd:string\" xsi:nil=\"true\">\n" +
            "</ns1:email>\n" +
            "\n" +
            "<ns1:firstname xsi:type=\"xsd:string\" xsi:nil=\"true\">\n" +
            "</ns1:firstname>\n" +
            "\n" +
            "<ns1:lastname xsi:type=\"xsd:string\" xsi:nil=\"true\">\n" +
            "</ns1:lastname>\n" +
            "\n" +
            "<ns1:phone xsi:type=\"xsd:string\" xsi:nil=\"true\">\n" +
            "</ns1:phone>\n" +
            "\n" +
            "<ns1:group xsi:type=\"ns1:WSUserGroup\">\n" +
            "<ns1:type xsi:type=\"xsd:string\">text.usermanager.group_administrators</ns1:type>\n" +
            "</ns1:group>\n" +
            "\n" +
            "<ns1:project xsi:type=\"xsd:string\" xsi:nil=\"true\">\n" +
            "</ns1:project>\n" +
            "</ns1:loggedInUser>\n" +
            "\n" +
            "<ns1:loginWasSuccessful xsi:type=\"xsd:boolean\">true</ns1:loginWasSuccessful>\n" +
            "\n" +
            "<ns1:loginFailedDueToConnectionRestrictions xsi:type=\"xsd:boolean\">false</ns1:loginFailedDueToConnectionRestrictions>\n" +
            "\n" +
            "<ns1:loginFailedDueToInsufficientUserRights xsi:type=\"xsd:boolean\">false</ns1:loginFailedDueToInsufficientUserRights>\n" +
            "\n" +
            "<ns1:loginFailedDueToAccountInvalid xsi:type=\"xsd:boolean\">false</ns1:loginFailedDueToAccountInvalid>\n" +
            "</ns1:authenticate2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String queryLoginFailed =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
          + " <soapenv:Body>\n"
          + "  <authenticate1 xmlns=\"utcs\">\n"
          + "   <password>wrong</password>\n"
          + "   <username>user</username>\n"
          + "   <application>treeview</application>\n"
          + "  </authenticate1>\n"
          + " </soapenv:Body>\n"
          + "</soapenv:Envelope>";

    private final String responseLoginFailed =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:authenticate2 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSLoginResult\">\n" +
            "<ns1:loggedInUser xsi:nil=\"true\" xsi:type=\"ns1:WSUser\">\n" +
            "</ns1:loggedInUser>\n" +
            "\n" +
            "<ns1:loginWasSuccessful xsi:type=\"xsd:boolean\">false</ns1:loginWasSuccessful>\n" +
            "\n" +
            "<ns1:loginFailedDueToConnectionRestrictions xsi:type=\"xsd:boolean\">false</ns1:loginFailedDueToConnectionRestrictions>\n" +
            "\n" +
            "<ns1:loginFailedDueToInsufficientUserRights xsi:type=\"xsd:boolean\">false</ns1:loginFailedDueToInsufficientUserRights>\n" +
            "\n" +
            "<ns1:loginFailedDueToAccountInvalid xsi:type=\"xsd:boolean\">true</ns1:loginFailedDueToAccountInvalid>\n" +
            "</ns1:authenticate2>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";
    // @formatter:on

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcAuthenticationService = spy(new IhcAuthenticationService(url, 0));
        doNothing().when(ihcAuthenticationService).openConnection(eq(url));

        doReturn(responseSuccesfulLogin).when(ihcAuthenticationService).sendQuery(eq(querySuccesfulLogin),
                ArgumentMatchers.anyInt());
        doReturn(responseLoginFailed).when(ihcAuthenticationService).sendQuery(eq(queryLoginFailed),
                ArgumentMatchers.anyInt());
    }

    @Test
    public void testSuccesfulLogin() throws IhcExecption {
        final WSLoginResult result = ihcAuthenticationService.authenticate("user", "pass", "treeview");
        assertEquals(true, result.isLoginWasSuccessful());
        assertEquals(false, result.isLoginFailedDueToAccountInvalid());
        assertEquals(false, result.isLoginFailedDueToConnectionRestrictions());
        assertEquals(false, result.isLoginFailedDueToInsufficientUserRights());
    }

    @Test
    public void testFailedLogin() throws IhcExecption {
        final WSLoginResult result = ihcAuthenticationService.authenticate("user", "wrong", "treeview");
        assertEquals(false, result.isLoginWasSuccessful());
        assertEquals(true, result.isLoginFailedDueToAccountInvalid());
        assertEquals(false, result.isLoginFailedDueToConnectionRestrictions());
        assertEquals(false, result.isLoginFailedDueToInsufficientUserRights());
    }
}
