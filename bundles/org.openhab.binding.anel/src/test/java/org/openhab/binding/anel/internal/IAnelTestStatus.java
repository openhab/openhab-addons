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
package org.openhab.binding.anel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Some constants used in the unit tests.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public interface IAnelTestStatus {

    String STATUS_INVALID_NAME = """
            NET-PwrCtrl:NET-CONTROL    :192.168.6.63:255.255.255.0:192.168.6.1:0.4.163.21.4.71:\
            Nr. 1,0:Nr. 2,1:Nr: 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,0:Nr. 8,0:248:80:NET-PWRCTRL_04.6:H:xor:\
            """;
    String STATUS_HUT_V61_POW = """
            NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:\
            Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:\
            IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_06.1:h:\
            p:225.9:0.0004:50.056:0.04:0.00:0.0:1.0000:xor:\
            """;
    String STATUS_HUT_V61_SENSOR = """
            NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:\
            Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:\
            IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_06.1:h:\
            n:s:20.61:40.7:7.0:xor:\
            """;
    String STATUS_HUT_V61_POW_SENSOR = """
            NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:\
            Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:\
            IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_06.1:h:\
            p:225.9:0.0004:50.056:0.04:0.00:0.0:1.0000:s:20.61:40.7:7.0:xor\
            """;
    String STATUS_HUT_V5 = """
            NET-PwrCtrl:ANEL1          :192.168.0.244:255.255.255.0:192.168.0.1:0.5.163.14.7.91:\
            hoch,0:links hoch,0:runter,0:rechts run,0:runter,0:hoch,0:links runt,0:rechts hoc,0:0:80:\
            WHN_UP,1,1:LI_DOWN,1,1:RE_DOWN,1,1:LI_UP,1,1:RE_UP,1,1:DOWN,1,1:DOWN,1,1:UP,1,1:27.3°C:NET-PWRCTRL_05.0\
            """;
    String STATUS_HUT_V65 = """
            NET-PwrCtrl:NET-CONTROL    :192.168.0.64:255.255.255.0:192.168.6.1:0.5.163.17.9.116:\
            Nr.1,0:Nr.2,1:Nr.3,0:Nr.4,1:Nr.5,0:Nr.6,1:Nr.7,0:Nr.8,1:248:80:\
            IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,1,0:IO-6,1,0:IO-7,1,0:IO-8,1,0:27.0�C:NET-PWRCTRL_06.5:h:n:xor:\
            """;
    String STATUS_HOME_V46 = """
            NET-PwrCtrl:NET-CONTROL    :192.168.0.63:255.255.255.0:192.168.6.1:0.5.163.21.4.71:\
            Nr. 1,1:Nr. 2,0:Nr. 3,1:Nr. 4,0:Nr. 5,1:Nr. 6,0:Nr. 7,1:Nr. 8,0:248:80:NET-PWRCTRL_04.6:H:xor:\
            """;
}
