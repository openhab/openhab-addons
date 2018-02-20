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
 * Checks that the given temperature is in range before passing it on to the next publisher.
 *
 * @author Volker Bier - Initial contribution
 */
public class BoundsCheckingPublisher implements ReadingPublisher<LaCrosseTemperatureReading> {
    private final Logger logger = LoggerFactory.getLogger(BoundsCheckingPublisher.class);

    private final ReadingPublisher<LaCrosseTemperatureReading> publisher;

    private final float minTemp;
    private final float maxTemp;

    public BoundsCheckingPublisher(float min, float max, ReadingPublisher<LaCrosseTemperatureReading> p) {
        minTemp = min;
        maxTemp = max;
        publisher = p;
    }

    @Override
    public void publish(LaCrosseTemperatureReading reading) {
        if (reading.getTemperature() >= minTemp && reading.getTemperature() <= maxTemp) {
            publisher.publish(reading);
        } else {
            logger.debug("Ignoring out of bounds reading {}", reading.getTemperature());
        }
    }

    @Override
    public void dispose() {
        publisher.dispose();
    }
}