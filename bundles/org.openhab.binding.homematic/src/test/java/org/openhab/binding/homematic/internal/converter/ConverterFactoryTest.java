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
package org.openhab.binding.homematic.internal.converter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homematic.internal.converter.type.DecimalTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OnOffTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OpenClosedTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.PercentTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.QuantityTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.StringTypeConverter;

/**
 * Tests for {@link ConverterFactory}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ConverterFactoryTest {

    @Test
    public void testTypesOfCreatedConverters() throws ConverterException {
        assertThat(ConverterFactory.createConverter("Switch"), instanceOf(OnOffTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Rollershutter"), instanceOf(PercentTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Dimmer"), instanceOf(PercentTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Contact"), instanceOf(OpenClosedTypeConverter.class));
        assertThat(ConverterFactory.createConverter("String"), instanceOf(StringTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Number"), instanceOf(DecimalTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Number:Temperature"), instanceOf(QuantityTypeConverter.class));
        assertThat(ConverterFactory.createConverter("Number:Percent"), instanceOf(QuantityTypeConverter.class));
    }
}
