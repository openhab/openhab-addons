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
package org.openhab.persistence.dynamodb.internal;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ColorItemIntegrationTest extends AbstractTwoItemIntegrationTest {

    public static final boolean LEGACY_MODE = false;

    private static HSBType color(double hue, int saturation, int brightness) {
        return new HSBType(new DecimalType(hue), new PercentType(saturation), new PercentType(brightness));
    }

    private static HSBType color(String hue, int saturation, int brightness) {
        return new HSBType(new DecimalType(new BigDecimal(hue)), new PercentType(saturation),
                new PercentType(brightness));
    }

    private static final String NAME = "color";
    // values are encoded as <hue>,<saturation>,<brightness>, ordering goes wrt strings
    private static final HSBType STATE1 = color("3.1493842988948932984298384892384823984923849238492839483294893", 50,
            50);
    private static final HSBType STATE2 = color(75, 100, 90);
    private static final HSBType STATE_BETWEEN = color(60, 50, 50);

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData() throws InterruptedException {
        ColorItem item = (ColorItem) ITEMS.get(NAME);
        item.setState(STATE1);
        beforeStore = ZonedDateTime.now();
        Thread.sleep(10);
        service.store(item);
        afterStore1 = ZonedDateTime.now();
        Thread.sleep(10);
        item.setState(STATE2);
        service.store(item);
        Thread.sleep(10);
        afterStore2 = ZonedDateTime.now();

        LOGGER.info("Created item between {} and {}", AbstractDynamoDBItem.DATEFORMATTER.format(beforeStore),
                AbstractDynamoDBItem.DATEFORMATTER.format(afterStore1));
    }

    @Override
    protected String getItemName() {
        return NAME;
    }

    @Override
    protected State getFirstItemState() {
        return STATE1;
    }

    @Override
    protected State getSecondItemState() {
        return STATE2;
    }

    @Override
    protected @Nullable State getQueryItemStateBetween() {
        return STATE_BETWEEN;
    }
}
