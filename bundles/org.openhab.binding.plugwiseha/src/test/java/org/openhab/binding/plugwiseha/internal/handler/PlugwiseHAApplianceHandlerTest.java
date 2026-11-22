/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.mockito.Mockito.*;
import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Tests channel updates with independently available boiler measurements.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHAApplianceHandlerTest {

    private final Thing thing = ThingBuilder.create(THING_TYPE_APPLIANCE_BOILER, "test").build();
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final PlugwiseHAApplianceHandler handler = new PlugwiseHAApplianceHandler(thing);

    @Test
    void boilerTemperatureWithoutReturnTemperatureDoesNotInterruptUpdates() {
        Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("""
                <appliance>
                    <point_log>
                        <type>boiler_temperature</type>
                        <unit>C</unit>
                        <measurement>50</measurement>
                    </point_log>
                </appliance>
                """);
        handler.setCallback(callback);

        handler.refreshChannel(appliance, channel(APPLIANCE_RETURNWATERTEMPERATURE_CHANNEL));
        handler.refreshChannel(appliance, channel(APPLIANCE_BOILERTEMPERATURE_CHANNEL));

        verify(callback).stateUpdated(channel(APPLIANCE_BOILERTEMPERATURE_CHANNEL),
                new QuantityType<>(50, SIUnits.CELSIUS));
        verifyNoMoreInteractions(callback);
    }

    @Test
    void returnTemperatureIsPublishedWithoutBoilerTemperature() {
        Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("""
                <appliance>
                    <point_log>
                        <type>return_water_temperature</type>
                        <unit>C</unit>
                        <measurement>35</measurement>
                    </point_log>
                </appliance>
                """);
        handler.setCallback(callback);

        handler.refreshChannel(appliance, channel(APPLIANCE_RETURNWATERTEMPERATURE_CHANNEL));

        verify(callback).stateUpdated(channel(APPLIANCE_RETURNWATERTEMPERATURE_CHANNEL),
                new QuantityType<>(35, SIUnits.CELSIUS));
    }

    @Test
    void absentMeasurementCollectionsDoNotInterruptChannelUpdates() {
        Appliance appliance = (Appliance) new PlugwiseHAXStream().fromXML("<appliance/>");
        handler.setCallback(callback);

        handler.refreshChannel(appliance, channel(APPLIANCE_TEMPERATURE_CHANNEL));
        handler.refreshChannel(appliance, channel(APPLIANCE_BURNER_STARTS_CHANNEL));
        handler.refreshChannel(appliance, channel(APPLIANCE_BURNER_OP_TIME_CHANNEL));
        handler.refreshChannel(appliance, channel(APPLIANCE_RETURNWATERTEMPERATURE_CHANNEL));
        handler.refreshChannel(appliance, channel(APPLIANCE_LOCK_CHANNEL));

        verify(callback).stateUpdated(channel(APPLIANCE_LOCK_CHANNEL), UnDefType.UNDEF);
        verifyNoMoreInteractions(callback);
    }

    private ChannelUID channel(String id) {
        return new ChannelUID(thing.getUID(), id);
    }
}
