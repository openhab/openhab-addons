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
package org.openhab.binding.homematic.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.homematic.internal.HomematicBindingConstants;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class HomematicBridgeHandlerTest {

    @Test
    public void testGetRpcCallbackUrlDoesNotContainsSpaces() {
        HttpClient httpClient = Mockito.mock(HttpClient.class);

        Bridge bridge = new BridgeImpl(HomematicBindingConstants.THING_TYPE_BRIDGE, "1234");
        bridge.getConfiguration().put("callbackHost", " 192. 168.1.1");
        assertThat(bridge.getStatus(), is(ThingStatus.UNINITIALIZED));
        HomematicTypeGenerator typeGenerator = mock(HomematicTypeGenerator.class);

        HomematicBridgeHandlerMock handler = new HomematicBridgeHandlerMock(bridge, typeGenerator, "1.2.3.4",
                httpClient);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();

        try {
            verify(callback).statusUpdated(eq(bridge), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)
                    && Objects.equals(arg.getDescription(), "The callback host mut not contain white spaces.")));
        } finally {
            handler.dispose();
        }
    }
}
