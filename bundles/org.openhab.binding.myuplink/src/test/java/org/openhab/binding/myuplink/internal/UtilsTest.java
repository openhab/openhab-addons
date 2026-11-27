/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Tests for {@link Utils}.
 *
 * @author Michael Loercher - initial contribution
 */
@NonNullByDefault
public class UtilsTest {

    private Channel channelWithExpression(String expression) {
        return ChannelBuilder.create(new ChannelUID(new ThingUID("myuplink:device:test"), "47011"), "Number")
                .withProperties(Map.of(PARAMETER_NAME_VALIDATION_REGEXP, expression)).build();
    }

    @Test
    public void legacyDefaultExpressionIsMigratedAndAcceptsNegativeValues() {
        String expr = Utils.getValidationExpression(channelWithExpression("[0-9]+"));
        assertThat(expr, is(DEFAULT_VALIDATION_EXPRESSION));
        assertThat("-1".matches(expr), is(true));
        assertThat("5".matches(expr), is(true));
        assertThat("-1.5".matches(expr), is(false));
    }

    @Test
    public void legacyDecimalExpressionIsMigratedAndAcceptsNegativeValues() {
        String expr = Utils.getValidationExpression(channelWithExpression("[0-9]+(\\.[0-9]+)?"));
        assertThat(expr, is(DECIMAL_VALIDATION_EXPRESSION));
        assertThat("-1.5".matches(expr), is(true));
        assertThat("0.5".matches(expr), is(true));
    }

    @Test
    public void otherExpressionsAreKeptUnchanged() {
        assertThat(Utils.getValidationExpression(channelWithExpression("[0-9]+:-?[0-9,]+")), is("[0-9]+:-?[0-9,]+"));
    }
}
