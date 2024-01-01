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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link WindowContact2Handler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class WindowContact2HandlerTest extends WindowContactHandlerTest {

    @Override
    protected WindowContactHandler createFixture() {
        return new WindowContact2Handler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT_2;
    }

    @Test
    void testUpdateChannelsBypassService() {
        String json = """
                {
                  "@type": "bypassState",
                  "state": "BYPASS_INACTIVE",
                  "configuration": {
                    "enabled": false,
                    "timeout": 5,
                    "infinite": false
                  }
                }
                """;

        JsonElement jsonObject = JsonParser.parseString(json);
        getFixture().processUpdate("Bypass", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BYPASS_STATE), OnOffType.OFF);

        json = """
                {
                  "@type": "bypassState",
                  "state": "BYPASS_ACTIVE",
                  "configuration": {
                    "enabled": false,
                    "timeout": 5,
                    "infinite": false
                  }
                }
                """;

        jsonObject = JsonParser.parseString(json);
        getFixture().processUpdate("Bypass", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BYPASS_STATE), OnOffType.ON);

        json = """
                {
                  "@type": "bypassState",
                  "state": "UNKNOWN",
                  "configuration": {
                    "enabled": false,
                    "timeout": 5,
                    "infinite": false
                  }
                }
                """;

        jsonObject = JsonParser.parseString(json);
        getFixture().processUpdate("Bypass", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BYPASS_STATE), UnDefType.UNDEF);
    }

    @Test
    void testUpdateChannelsCommunicationQualityService() {
        String json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "UNKNOWN"
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(0));

        json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "GOOD"
                }
                """;
        jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(4));
    }
}
