/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class PlayerItemRewindFastForwardIntegrationTest extends AbstractTwoItemIntegrationTest {

    public static final boolean LEGACY_MODE = false;
    private static final String NAME = "player_rewindfastforward";

    private static @Nullable RewindFastforwardType STATE1, STATE2;
    private static final @Nullable RewindFastforwardType STATE_BETWEEN = null;

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData(TestInfo testInfo) throws InterruptedException {
        @NonNull
        RewindFastforwardType localState1, localState2;
        if (isLegacyTest(testInfo)) {
            // In legacy, FASTFORWARD < REWIND
            STATE1 = RewindFastforwardType.FASTFORWARD;
            STATE2 = RewindFastforwardType.REWIND;
        } else {
            // In non-legacy, FASTFORWARD (serialized as 1) > REWIND (-1)
            STATE1 = RewindFastforwardType.REWIND;
            STATE2 = RewindFastforwardType.FASTFORWARD;
        }
        localState1 = (@NonNull RewindFastforwardType) STATE1;
        localState2 = (@NonNull RewindFastforwardType) STATE2;
        assert localState1 != null;
        assert localState2 != null;

        PlayerItem item = (PlayerItem) ITEMS.get(NAME);

        item.setState(localState1);

        beforeStore = ZonedDateTime.now();
        Thread.sleep(10);
        service.store(item);
        afterStore1 = ZonedDateTime.now();
        Thread.sleep(10);
        item.setState(localState2);
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
        return (@NonNull RewindFastforwardType) STATE1;
    }

    @Override
    protected State getSecondItemState() {
        return (@NonNull RewindFastforwardType) STATE2;
    }

    @Override
    protected @Nullable State getQueryItemStateBetween() {
        return STATE_BETWEEN;
    }
}
