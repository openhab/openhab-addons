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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class SwitchItemIntegrationTest extends AbstractTwoItemIntegrationTest {

    public static final boolean LEGACY_MODE = false;
    private static final String NAME = "switch";
    private static final OnOffType STATE1 = OnOffType.OFF;
    private static final OnOffType STATE2 = OnOffType.ON;
    // There is no OnOffType state value between OFF and ON.
    // Omit extended query tests AbstractTwoItemIntegrationTest by setting stateBetween to null.
    private static final @Nullable OnOffType STATE_BETWEEN = null;

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData() throws InterruptedException {
        SwitchItem item = (SwitchItem) ITEMS.get(NAME);
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
