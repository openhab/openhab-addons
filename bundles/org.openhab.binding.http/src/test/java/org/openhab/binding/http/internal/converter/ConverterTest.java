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
package org.openhab.binding.http.internal.converter;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.http.internal.config.HttpChannelConfig;
import org.openhab.binding.http.internal.transform.NoOpValueTransformation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ConverterTest} is a test class for state converters
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ConverterTest {

    @Test
    public void numberItemConverter() {
        NumberItemConverter converter = new NumberItemConverter(this::updateState, this::postCommand,
                this::sendHttpValue, NoOpValueTransformation.getInstance(), NoOpValueTransformation.getInstance(),
                new HttpChannelConfig());

        // without unit
        Assertions.assertEquals(new DecimalType(1234), converter.toState("1234"));

        // unit in transformation result
        Assertions.assertEquals(new QuantityType<>(100, SIUnits.CELSIUS), converter.toState("100°C"));

        // no valid value
        Assertions.assertEquals(UnDefType.UNDEF, converter.toState("W"));
        Assertions.assertEquals(UnDefType.UNDEF, converter.toState(""));
    }

    @Test
    public void numberItemConverterWithUnit() {
        HttpChannelConfig channelConfig = new HttpChannelConfig();
        channelConfig.unit = "W";
        NumberItemConverter converter = new NumberItemConverter(this::updateState, this::postCommand,
                this::sendHttpValue, NoOpValueTransformation.getInstance(), NoOpValueTransformation.getInstance(),
                channelConfig);

        // without unit
        Assertions.assertEquals(new QuantityType<>(500, Units.WATT), converter.toState("500"));

        // no valid value
        Assertions.assertEquals(UnDefType.UNDEF, converter.toState("100foo"));
        Assertions.assertEquals(UnDefType.UNDEF, converter.toState("foo"));
        Assertions.assertEquals(UnDefType.UNDEF, converter.toState(""));
    }

    @Test
    public void stringTypeConverter() {
        GenericItemConverter converter = createConverter(StringType::new);
        Assertions.assertEquals(new StringType("Test"), converter.toState("Test"));
    }

    @Test
    public void decimalTypeConverter() {
        GenericItemConverter converter = createConverter(DecimalType::new);
        Assertions.assertEquals(new DecimalType(15.6), converter.toState("15.6"));
    }

    @Test
    public void pointTypeConverter() {
        GenericItemConverter converter = createConverter(PointType::new);
        Assertions.assertEquals(new PointType(new DecimalType(51.1), new DecimalType(7.2), new DecimalType(100)),
                converter.toState("51.1, 7.2, 100"));
    }

    private void sendHttpValue(String value) {
    }

    private void updateState(State state) {
    }

    public void postCommand(Command command) {
    }

    public GenericItemConverter createConverter(Function<String, State> fcn) {
        return new GenericItemConverter(fcn, this::updateState, this::postCommand, this::sendHttpValue,
                NoOpValueTransformation.getInstance(), NoOpValueTransformation.getInstance(), new HttpChannelConfig());
    }
}
