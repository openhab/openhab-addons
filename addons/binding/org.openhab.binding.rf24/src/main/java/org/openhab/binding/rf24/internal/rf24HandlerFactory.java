/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.handler.HardwareIdFactory;
import org.openhab.binding.rf24.handler.rf24BaseHandler;
import org.openhab.binding.rf24.internal.serial.ArduinoSerial;
import org.openhab.binding.rf24.internal.serial.FakeSerial;
import org.openhab.binding.rf24.internal.serial.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.common.io.id.TransmitterId;
import pl.grzeslowski.smarthome.rf24.Rf24Adapter;
import pl.grzeslowski.smarthome.rpi.serial.DataRate;
import pl.grzeslowski.smarthome.rpi.serial.Port;

/**
 * The {@link rf24HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24HandlerFactory extends BaseThingHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(rf24HandlerFactory.class);
    private static final OsChecker OS_CHECKER = new OsChecker();
    private static final IdUtils ID_UTILS = new IdUtils(Rf24Adapter.MAX_NUMBER_OF_READING_PIPES);
    private static final HardwareIdFactory HARDWARE_ID_FACTORY = new HardwareIdFactory(ID_UTILS);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory() {
        AtomicInteger id = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("#%s serial listenrs", id.incrementAndGet()));
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        }
    });

    // @formatter:off
    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(rf24BindingConstants.RF24_RECIVER_THING_TYPE);
    // @formatter:on

    private List<ArduinoSerial> serials;

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

    public rf24HandlerFactory() {
        logger.info("isRPi {}", OS_CHECKER.isRpi());

        serials = new ArrayList<>();
        if (OS_CHECKER.isRpi()) {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
            Serial serial = new Serial(DataRate.BAUD_9600, new Port("/dev/ttyACM0"), EXECUTOR, new TransmitterId(1));
            serials.add(serial);
        } else {
            serials.add(new FakeSerial(new TransmitterId(1)));
            serials.add(new FakeSerial(new TransmitterId(2)));
            serials.add(new FakeSerial(new TransmitterId(3)));
        }

      // @formatter:off
        serials.stream().forEach(ArduinoSerial::init);
      // @formatter:on
    }

    // @Override protected void activate(ComponentContext componentContext) { }
    // @Override protected void deactivate(ComponentContext componentContext) { }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        HardwareId hardwareId = HARDWARE_ID_FACTORY.findHardwareId(thing);
        return new rf24BaseHandler(thing, ID_UTILS, findForPipe(hardwareId), hardwareId);
    }

    private ArduinoSerial findForPipe(HardwareId hardwareId) {
        // @formatter:off
        final TransmitterId transmitterId = Optional.of(hardwareId)
            .map(hId -> hId.toCommonId())
            .map(cId -> ID_UTILS.toReceiverId(cId))
            .map(rId -> ID_UTILS.findTransmitterId(rId))
            .orElseThrow((() ->
                    new IllegalArgumentException(String.format("Could not found transmitterId for pipe %s!", hardwareId.toString()))));

        return serials.stream()
            .filter(x -> x.getTransmitterId().equals(transmitterId))
            .findAny()
            .orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find wifi for pipe %s!", hardwareId.toString())));
        // @formatter:on
    }
}
