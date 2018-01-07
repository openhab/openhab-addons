/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks that the given temperature does not differ too much from the last temperature
 * before passing it on to the next publisher.
 *
 * @author Volker Bier - Initial contribution
 */
public class DifferenceCheckingPublisher implements ReadingPublisher<LaCrosseTemperatureReading> {
    private final Logger logger = LoggerFactory.getLogger(DifferenceCheckingPublisher.class);

    private final ReadingPublisher<LaCrosseTemperatureReading> publisher;
    private final float allowedDifference;

    private LaCrosseTemperatureReading lastReading;

    public DifferenceCheckingPublisher(float difference, ReadingPublisher<LaCrosseTemperatureReading> p) {
        allowedDifference = difference;
        publisher = p;
    }

    @Override
    public void publish(LaCrosseTemperatureReading reading) {
        if (lastReading == null
                || Math.abs(reading.getTemperature() - lastReading.getTemperature()) < allowedDifference) {
            publisher.publish(reading);
        } else {
            logger.debug("Ignoring reading {} differing too much from previous value", reading.getTemperature());
        }
    }

    @Override
    public void dispose() {
        publisher.dispose();
    }
}