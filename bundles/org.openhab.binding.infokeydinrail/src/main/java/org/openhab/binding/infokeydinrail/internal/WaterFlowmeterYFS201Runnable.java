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
package org.openhab.binding.infokeydinrail.internal;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.openhab.binding.infokeydinrail.internal.handler.WaterFlowmeterYFS201;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WaterFlowmeterYFS201Runnable} is responsible for handling hall effect water flowmeter
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

public class WaterFlowmeterYFS201Runnable extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static DecimalFormat df = new DecimalFormat("0.00");

    private Thing aThing;
    private Boolean start_counter;
    private long consumption_count = 0;
    private long prev_consumption_count = -1;
    private long count = 0;
    private double prevFlow = -1;
    private Boolean exitFlag = false;
    private WaterFlowmeterYFS201 theHandler;
    private float littre;

    public WaterFlowmeterYFS201Runnable(WaterFlowmeterYFS201 theHandler, Thing aThing, float littre) {
        this.aThing = aThing;
        this.theHandler = theHandler;
        this.littre = littre;
    }

    @Override
    public void run() {

        logger.debug("4.WaterFlowmeterYFS201 exitFlag {}", exitFlag);
        while (!exitFlag) {
            try {

                start_counter = true;
                // logger.debug("5.WaterFlowmeterYFS201 start_counter {}", start_counter);
                Thread.sleep(1000);
                start_counter = false;
                // logger.debug("6.WaterFlowmeterYFS201 start_counter {} count {}", start_counter, count);
                double flow = (count / 7.5); // Pulse frequency (Hz) = 7.5Q, Q is flow rate in L/min.
                // logger.debug("7.WaterFlowmeterYFS201 The flow is: {} Liter/min", df.format(flow));

                Iterator<Channel> it = aThing.getChannels().iterator();
                while (it.hasNext()) {
                    Channel ch = it.next();
                    // logger.debug("8. {} / {} / {}", ch.toString(), ch.getChannelTypeUID(), ch.getUID());
                    if (ch.getUID().toString().contains("#Water_Is_Running") && ((prevFlow == 0) != (flow == 0))) {
                        theHandler.updateValue(ch.getUID(), (flow == 0 ? "Closed" : "Open"));
                    } else if (ch.getUID().toString().contains("#Water_Flow") && (prevFlow != flow)) {
                        theHandler.updateValue(ch.getUID(), df.format(flow) + " L/min");
                    } else if (ch.getUID().toString().contains("#Last_Total_Ammount")
                            && (prev_consumption_count != consumption_count)) {
                        theHandler.updateValue(ch.getUID(), df.format(consumption_count * littre) + " L");
                    }
                }

                if (prevFlow != flow) {
                    prevFlow = flow;
                }

                if (prev_consumption_count != consumption_count) {
                    prev_consumption_count = consumption_count;
                }
                count = 0;

                // logger.debug("6.WaterFlowmeterYFS201 count {} ", count);
                Thread.sleep(2000);
            } catch (Exception ex) {
                logger.debug("Ops", ex);
            }
        }
    }

    public Boolean getStart_counter() {
        return start_counter;
    }

    public void endThread() {
        exitFlag = true;
    }

    public void updateCounter() {
        count += 1;
    }

    public void updateConsumptionCounter(boolean reset) {
        if (reset) {
            consumption_count = 0;
        }
        consumption_count += 1;
    }
}
