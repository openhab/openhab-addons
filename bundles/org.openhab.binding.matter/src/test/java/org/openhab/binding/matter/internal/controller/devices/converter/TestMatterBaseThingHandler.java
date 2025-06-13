/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Test handler for converter tests
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TestMatterBaseThingHandler extends MatterBaseThingHandler {
    public static final ThingTypeUID THING_TYPE_TEST = new ThingTypeUID("matter", "test");

    private final MatterControllerClient mockClient;

    public TestMatterBaseThingHandler(MatterBridgeClient bridgeClient, BaseThingHandlerFactory thingHandlerFactory,
            MatterStateDescriptionOptionProvider stateDescriptionProvider,
            MatterChannelTypeProvider channelTypeProvider, MatterConfigDescriptionProvider configDescriptionProvider,
            TranslationService translationService) {
        super(ThingBuilder.create(THING_TYPE_TEST, "test").build(), thingHandlerFactory, stateDescriptionProvider,
                channelTypeProvider, configDescriptionProvider, translationService);
        this.mockClient = new MatterControllerClient();
    }

    @Override
    public @Nullable MatterControllerClient getClient() {
        return mockClient;
    }

    @Override
    public void updateState(String channelId, org.openhab.core.types.State state) {
        super.updateState(channelId, state);
    }

    @Override
    public void triggerChannel(String channelId, String event) {
        super.triggerChannel(channelId, event);
    }

    @Override
    public BigInteger getNodeId() {
        return BigInteger.ONE;
    }

    @Override
    public ThingTypeUID getDynamicThingTypeUID() {
        return THING_TYPE_TEST;
    }

    @Override
    public boolean isBridgeType() {
        return false;
    }

    @Override
    public Integer getPollInterval() {
        return 0;
    }
}
