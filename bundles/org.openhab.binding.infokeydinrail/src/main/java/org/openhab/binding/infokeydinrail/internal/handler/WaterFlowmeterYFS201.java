/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.infokeydinrail.internal.handler;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import org.openhab.binding.infokeydinrail.internal.PinMapper;
import org.openhab.binding.infokeydinrail.internal.WaterFlowmeterYFS201Runnable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

/**
 * The {@link WaterFlowmeterYFS201} is responsible for handling hall effect water flowmeter
 *
 *
 * This GPIO provider implements the YF-S201 as native device.
 * </p>
 *
 * <p>
 * The YF-S201 sensor listen to a pin and inform openhab to make calculations.
 * </p>
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */

public class WaterFlowmeterYFS201 extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Integer pinNo;
    private WaterFlowmeterYFS201Runnable theThread;
    DecimalFormat df = new DecimalFormat("0.00");
    private GpioPinDigitalInput dataPin;
    private float littre;
    private Long timeMilli;
    private GpioController gpio;

    /**
     * the polling interval mcp23071 check interrupt register (optional, defaults to 50ms)
     */

    public WaterFlowmeterYFS201(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            logger.debug("WaterFlowmeterYFS201 initializing");
            checkConfiguration();

            logger.debug("2.1 -> WaterFlowmeterYFS201Runnable initializing");

            // create gpio controller
            gpio = GpioFactory.getInstance();

            logger.debug("2.2 -> WaterFlowmeterYFS201Runnable provisionin pin {}", pinNo);
            // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
            dataPin = gpio.provisionDigitalInputPin(PinMapper.getRaspiPin(pinNo), PinPullResistance.PULL_UP);

            dataPin.addTrigger(new GpioCallbackTrigger(PinState.LOW, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // logger.debug("2.3 -> WaterFlowmeterYFS201Runnable handleGpioPinDigitalStateChangeEvent");
                    if (theThread.getStart_counter()) {
                        theThread.updateCounter();
                    }

                    // logger.info("4. date.getTime : {} , timeMilli + 3000 : {} / {}", date.getTime(), timeMilli +
                    // 3000,
                    // (date.getTime() >= timeMilli + 3000));

                    if (System.currentTimeMillis() >= timeMilli + 5000) {
                        theThread.updateConsumptionCounter(true);
                    } else {
                        theThread.updateConsumptionCounter(false);
                    }

                    timeMilli = System.currentTimeMillis();

                    return null;
                }
            }));

            timeMilli = System.currentTimeMillis();
            // set shutdown state for this input pin
            dataPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);

            theThread = new WaterFlowmeterYFS201Runnable(this, this.thing, littre);
            theThread.start();
            logger.debug("WaterFlowmeterYFS201 initializing ended");
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        pinNo = Integer.parseInt((configuration.get(WATER_FLOWMETER_PIN)).toString());
        littre = Float.parseFloat((configuration.get(WATER_FLOWMETER_L)).toString());
        logger.debug("WaterFlowmeterYFS201 checkConfiguration {}", pinNo);
    }

    public void updateValue(ChannelUID channelUID, Object value) {
        // logger.debug("4. updateValue {} / {}", channelUID.toString(), value);
        try {
            if (value instanceof String) {
                updateState(channelUID, new StringType((String) value));
            } else if (value instanceof Double) {
                updateState(channelUID, new DecimalType((double) value));
            }
        } catch (Exception ex) {
            logger.debug("Oops:", ex);
        }
    }

    @Override
    public void dispose() {
        theThread.endThread();
        gpio.shutdown();
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        synchronized (this) {
            super.channelLinked(channelUID);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }
}
