/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.handler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.errors.LGThinqException;
import org.openhab.binding.lgthinq.internal.LGThinqBindingConstants;
import org.openhab.binding.lgthinq.internal.LGThinqConfiguration;
import org.openhab.binding.lgthinq.lgapi.LGApiClientService;
import org.openhab.binding.lgthinq.lgapi.LGApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.model.ACOpMode;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGBridgeHandlerTest}
 *
 * @author Nemer Daud - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class LGBridgeHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(LGBridgeHandlerTest.class);

    @org.junit.jupiter.api.Test
    void initialize() {
        Bridge fakeThing = mock(Bridge.class);
        LGThinqBindingConstants.THINQ_CONNECTION_DATA_FILE = "/tmp/token.json";
        LGBridgeHandler b = new LGBridgeHandler(fakeThing);
        LGBridgeHandler spyBridge = spy(b);
        doReturn(new LGThinqConfiguration("nemer.daud@gmail.com", "@Apto94&J4V4", "BR", "pt-BR", 60)).when(spyBridge)
                .getConfigAs(any(Class.class));
        spyBridge.initialize();
        LGApiClientService service = LGApiV1ClientServiceImpl.getInstance();
        try {
            // String json = service.startMonitor("d7ee2251-e4bb-14a8-9d96-60ab14f3c836");
            // service.stopMonitor("d27cc560-7149-11d3-80b6-7440bec3653e", "n-d27cc560-7149-11d3-80b6-7440bec3653e");
            // String workId = service.startMonitor("d27cc560-7149-11d3-80b6-7440bec3653e");
            // String workId = "n-d27cc560-7149-11d3-80b6-7440bec3653e";
            // service.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // Thread.sleep(1000);
            // service.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // Thread.sleep(1000);
            // service.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // ACSnapShot ac = service.getAcDeviceData("d7ee2251-e4bb-14a8-9d96-60ab14f3c836");
            // boolean ok = service.turnDevicePower("d7ee2251-e4bb-14a8-9d96-60ab14f3c836",
            // DevicePowerState.DV_POWER_ON);
            // boolean ok = service.changeOperationMode("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACOpMode.COOL);
            // ACSnapShot ac = service.getAcDeviceData("d7ee2251-e4bb-14a8-9d96-60ab14f3c836");
            // service.changeFanSpeed("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACFanSpeed.F3);
            // service.changeTargetTemperature("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACTargetTmp._21);

            service.changeOperationMode("d27cc560-7149-11d3-80b6-7440bec3653e", ACOpMode.FAN);
            // System.out.println("AC power ON:" + workId);
        } catch (LGThinqException e) {
            logger.error("Error testing facade", e);
            // } catch (InterruptedException | IOException e) {
            // e.printStackTrace();
        }
    }
}
