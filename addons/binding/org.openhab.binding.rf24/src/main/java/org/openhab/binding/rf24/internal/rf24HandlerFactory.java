/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.handler.PipeFactory;
import org.openhab.binding.rf24.handler.rf24BaseHandler;
import org.openhab.binding.rf24.wifi.Rf24;
import org.openhab.binding.rf24.wifi.WiFi;
import org.openhab.binding.rf24.wifi.WifiOperator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.rf24.Rf24Adapter;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

/**
 * The {@link rf24HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24HandlerFactory extends BaseThingHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(rf24HandlerFactory.class);
    private static final IdUtils ID_UTILS = new IdUtils(Rf24Adapter.MAX_NUMBER_OF_READING_PIPES);
    private static final PipeFactory PIPE_FACTORY = new PipeFactory();
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

    // @formatter:off
    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(rf24BindingConstants.RF24_RECIVER_THING_TYPE);
    // @formatter:on

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        // @formatter:off
        return SUPPORTED_THING_TYPES_UIDS
                .stream()
                .filter(thingTypeUID::equals)
                .findFirst()
                .isPresent();
        // @formatter:on
    }

    private final List<WifiOperator> xs;

    public rf24HandlerFactory() {
        WiFi wifi1 = new Rf24((short) 1, (short) 2, 3, (short) 4, (short) 5, (short) 6); // TODO valid params
        WiFi wifi2 = new Rf24((short) 1, (short) 2, 3, (short) 4, (short) 5, (short) 6); // TODO valid params

        // @formatter:off
        xs = ImmutableList.of(
                new WifiOperator(ID_UTILS, wifi1, new TransmitterId(1), EXECUTOR),
                new WifiOperator(ID_UTILS, wifi2, new TransmitterId(2), EXECUTOR)
        );
        // @formatter:on

    }

    @Override
    protected void activate(ComponentContext componentContext) {
        // @formatter:off
        xs.stream().forEach(WifiOperator::init);
        // @formatter:on
        super.activate(componentContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        // @formatter:off
        xs.stream().forEach(WifiOperator::close);
        // @formatter:on
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        Pipe pipe = PIPE_FACTORY.findPipe(thing);
        return new rf24BaseHandler(thing, findForPipe(pipe), pipe);
    }

    private WifiOperator findForPipe(Pipe pipe) {
        // @formatter:off
        final TransmitterId transmitterId = Optional.of(pipe)
            .map(p -> p.getPipe())
            .map(p -> new HardwareId(p))
            .map(hId -> hId.toCommonId())
            .map(cId -> ID_UTILS.toReceiverId(cId))
            .map(rId -> ID_UTILS.findTransmitterId(rId))
            .orElseThrow((() ->
                    new IllegalArgumentException(String.format("Could not found transmitterId for pipe %s!", pipe.toString()))));

        return xs.stream()
            .filter(x -> x.geTransmitterId().equals(transmitterId))
            .findAny()
            .orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find wifi for pipe %s!", pipe.toString())));
        // @formatter:on
    }
}
