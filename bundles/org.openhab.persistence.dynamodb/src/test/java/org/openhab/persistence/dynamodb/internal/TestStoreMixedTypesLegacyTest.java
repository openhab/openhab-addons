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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class TestStoreMixedTypesLegacyTest extends TestStoreMixedTypesTest {

    public static final boolean LEGACY_MODE = true;

    @Override
    protected PlayPauseType[] expectedPlayerItem() {
        return new PlayPauseType[] { PlayPauseType.PAUSE };
    }

    @Override
    protected StringType[] expectedStringItem() {
        return new StringType[] { StringType.valueOf("a1"), StringType.valueOf("b1"), StringType.valueOf("PAUSE") };
    }

    @Override
    protected OnOffType[] expectedSwitchItem() {
        return new OnOffType[] { /* 33.14 */OnOffType.ON, /* 66.28 */ OnOffType.ON, OnOffType.ON, OnOffType.OFF, };
    }

    @Override
    protected DecimalType[] expectedNumberItem() {
        return new DecimalType[] { DecimalType.valueOf("33.14"), DecimalType.valueOf("66.28"),
                /* on */DecimalType.valueOf("1"), /* off */DecimalType.valueOf("0") };
    }
}
