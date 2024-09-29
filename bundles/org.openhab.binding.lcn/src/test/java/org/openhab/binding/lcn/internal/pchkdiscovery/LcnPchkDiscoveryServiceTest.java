/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link LcnPchkDiscoveryService}.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnPchkDiscoveryServiceTest {
    private LcnPchkDiscoveryService s = new LcnPchkDiscoveryService();
    private ServicesResponse r = s.xmlToServiceResponse(RESPONSE);
    private static final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ServicesResponse xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"servicesresponse.xsd\"><Version major=\"1\" minor=\"0\" /><Server requestId=\"1548\" machineId=\"b8:27:eb:fe:a4:bb\" machineName=\"raspberrypi\" osShort=\"Unix/Linux\" osLong=\"Unix/Linux\">LCN-PCHK 3.2.2 running on Unix/Linux</Server><Services /><ExtServices><ExtService name=\"LcnPchkBus\" major=\"1\" minor=\"0\" prot=\"TCP\" localPort=\"4114\">PCHK 3.2.2 bus</ExtService></ExtServices></ServicesResponse>";

    @BeforeEach
    public void setUp() {
        s = new LcnPchkDiscoveryService();
        r = s.xmlToServiceResponse(RESPONSE);
    }

    @Test
    public void testXmlMachineId() {
        assertThat(r.getServer().getMachineId(), is("b8:27:eb:fe:a4:bb"));
    }

    @Test
    public void testXmlMachineName() {
        assertThat(r.getServer().getMachineName(), is("raspberrypi"));
    }

    @Test
    public void testXmlServerContent() {
        assertThat(r.getServer().getContent(), is("LCN-PCHK 3.2.2 running on Unix/Linux"));
    }

    @Test
    public void testXmlPort() {
        assertThat(r.getExtServices().getExtService().getLocalPort(), is(4114));
    }
}
