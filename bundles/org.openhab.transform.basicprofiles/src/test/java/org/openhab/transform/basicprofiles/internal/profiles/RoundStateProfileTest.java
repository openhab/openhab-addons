/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.math.RoundingMode;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.Command;

/**
 * Basic unit tests for {@link RoundStateProfile}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class RoundStateProfileTest {

    @BeforeEach
    public void setup() {
        // initialize parser with ImperialUnits, otherwise units like °F are unknown
        @SuppressWarnings("unused")
        Unit<Temperature> fahrenheit = ImperialUnits.FAHRENHEIT;
    }

    @Test
    public void testParsingParameters() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 2, "NOT_SUPPORTED");

        assertThat(roundProfile.scale, is(2));
        assertThat(roundProfile.roundingMode, is(RoundingMode.HALF_UP));
    }

    @Test
    public void testDecimalTypeOnCommandFromItem() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 2);

        Command cmd = new DecimalType(23.333);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(23.33));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithNegativeScale() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, -2);

        Command cmd = new DecimalType(1234.333);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(1200.0));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithCeiling() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 0, RoundingMode.CEILING.name());

        Command cmd = new DecimalType(23.3);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(24.0));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithFloor() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 0, RoundingMode.FLOOR.name());

        Command cmd = new DecimalType(23.6);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(23.0));
    }

    @Test
    public void testQuantityTypeOnCommandFromItem() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 1);

        Command cmd = new QuantityType<Temperature>("23.333 °C");
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        @SuppressWarnings("unchecked")
        QuantityType<Temperature> qtResult = (QuantityType<Temperature>) result;
        assertThat(qtResult.doubleValue(), is(23.3));
        assertThat(qtResult.getUnit(), is(SIUnits.CELSIUS));
    }

    private RoundStateProfile createProfile(ProfileCallback callback, Integer scale) {
        return createProfile(callback, scale, null);
    }

    private RoundStateProfile createProfile(ProfileCallback callback, Integer scale, @Nullable String mode) {
        ProfileContext context = mock(ProfileContext.class);
        Configuration config = new Configuration();
        config.put(RoundStateProfile.PARAM_SCALE, scale);
        config.put(RoundStateProfile.PARAM_MODE, mode == null ? RoundingMode.HALF_UP.name() : mode);
        when(context.getConfiguration()).thenReturn(config);

        return new RoundStateProfile(callback, context);
    }
}
