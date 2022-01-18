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

import java.io.IOException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.errors.LGThinqException;
import org.openhab.binding.lgthinq.internal.LGThinqBindingConstants;
import org.openhab.binding.lgthinq.internal.LGThinqConfiguration;
import org.openhab.binding.lgthinq.lgapi.LGApiClientService;
import org.openhab.binding.lgthinq.lgapi.LGApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.LGApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.model.ACSnapShot;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
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
    void initialize() throws IOException {
        Bridge fakeThing = mock(Bridge.class);
        ThingUID fakeThingUid = mock(ThingUID.class);
        when(fakeThingUid.getId()).thenReturn("fakeBridgeId");
        when(fakeThing.getUID()).thenReturn(fakeThingUid);

        LGThinqBindingConstants.THINQ_CONNECTION_DATA_FILE = "/tmp/token.json";
        LGThinqBindingConstants.BASE_CAP_CONFIG_DATA_FILE = "/tmp/thinq-cap.json";
        LGBridgeHandler b = new LGBridgeHandler(fakeThing);
        LGBridgeHandler spyBridge = spy(b);
        doReturn(new LGThinqConfiguration("nemer.daud@gmail.com", "@Apto94&J4V4", "BR", "pt-BR", 60)).when(spyBridge)
                .getConfigAs(any(Class.class));
        spyBridge.initialize();
        LGApiClientService service1 = LGApiV1ClientServiceImpl.getInstance();
        LGApiClientService service2 = LGApiV2ClientServiceImpl.getInstance();
        try {
            // lgthinq:401:5eb6ed5ed6:d27bdb00-7149-11d3-80b0-7440be92ac08
            String workId = service1.startMonitor("fakeBridgeId", "d27bdb00-7149-11d3-80b0-7440be92ac08");
            ACSnapShot shot = service1.getMonitorData("fakeBridgeId", "d27bdb00-7149-11d3-80b0-7440be92ac08", workId);
            System.out.println(shot);
            // service1.stopMonitor("d27cc560-7149-11d3-80b6-7440bec3653e", "n-d27cc560-7149-11d3-80b6-7440bec3653e");
            // String workId = service1.startMonitor("d27cc560-7149-11d3-80b6-7440bec3653e");
            // String workId = "n-d27cc560-7149-11d3-80b6-7440bec3653e";
            // service1.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // Thread.sleep(1000);
            // service1.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // Thread.sleep(1000);
            // service1.getMonitorData("d27cc560-7149-11d3-80b6-7440bec3653e", workId);
            // ACSnapShot ac = service1.getAcDeviceData("d7ee2251-e4bb-14a8-9d96-60ab14f3c836");
            // boolean ok = service1.turnDevicePower("d7ee2251-e4bb-14a8-9d96-60ab14f3c836",
            // DevicePowerState.DV_POWER_ON);
            // boolean ok = service1.changeOperationMode("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACOpMode.COOL);
            // ACSnapShot ac = service1.getAcDeviceData("d7ee2251-e4bb-14a8-9d96-60ab14f3c836");
            // service1.changeFanSpeed("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACFanSpeed.F3);
            // service1.changeTargetTemperature("d7ee2251-e4bb-14a8-9d96-60ab14f3c836", ACTargetTmp._21);
            // List<LGDevice> devices = service1.listAccountDevices("bridgeTest");
            // devices.forEach((d) -> {
            // try {
            // if (d.getPlatformType().equals(PLATFORM_TYPE_V1)) {
            // ACCapability ac = service1.getDeviceCapability(d.getDeviceId(), d.getModelJsonUri(), true);
            // System.out.println(ac);
            // } else {
            // ACCapability ac = service2.getDeviceCapability(d.getDeviceId(), d.getModelJsonUri(), true);
            // System.out.println(ac);
            // }
            // } catch (LGApiException e) {
            // logger.error("Error getting capabilities", e);
            // }
            // });
            // service1.changeOperationMode("d27cc560-7149-11d3-80b6-7440bec3653e", ACOpMode.FAN);
            System.out.println("AC power ON:");
        } catch (LGThinqException e) {
            logger.error("Error testing facade", e);
            // } catch (InterruptedException | IOException e) {
            // e.printStackTrace();
        }
    }
}
