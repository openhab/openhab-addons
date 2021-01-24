/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Sami Salonen - Initial contribution
 */
public class CascadedValueTransformationImplTest {

    @Test
    public void testTransformation() {
        CascadedValueTransformationImpl transformation = new CascadedValueTransformationImpl(
                "REGEX(myregex:foo(.*))∩REG_(EX(myregex:foo(.*))∩JIHAA:test");
        assertEquals(3, transformation.getTransformations().size());
        assertEquals("REGEX", transformation.getTransformations().get(0).transformationServiceName);
        assertEquals("myregex:foo(.*)", transformation.getTransformations().get(0).transformationServiceParam);

        assertEquals("REG_", transformation.getTransformations().get(1).transformationServiceName);
        assertEquals("EX(myregex:foo(.*)", transformation.getTransformations().get(1).transformationServiceParam);

        assertEquals("JIHAA", transformation.getTransformations().get(2).transformationServiceName);
        assertEquals("test", transformation.getTransformations().get(2).transformationServiceParam);

        assertEquals(3, transformation.toString().split("∩").length);
    }
}
