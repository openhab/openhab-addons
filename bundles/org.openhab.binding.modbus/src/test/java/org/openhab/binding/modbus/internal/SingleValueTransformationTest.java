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
package org.openhab.binding.modbus.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class SingleValueTransformationTest {

    @Test
    public void testTransformationOldStyle() {
        SingleValueTransformation transformation = new SingleValueTransformation("REGEX(myregex:foo(.*))");
        assertEquals("REGEX", transformation.transformationServiceName);
        assertEquals("myregex:foo(.*)", transformation.transformationServiceParam);
    }

    @Test
    public void testTransformationOldStyle2() {
        SingleValueTransformation transformation = new SingleValueTransformation("REG_(EX(myregex:foo(.*))");
        assertEquals("REG_", transformation.transformationServiceName);
        assertEquals("EX(myregex:foo(.*)", transformation.transformationServiceParam);
    }

    @Test
    public void testTransformationNewStyle() {
        SingleValueTransformation transformation = new SingleValueTransformation("REGEX:myregex(.*)");
        assertEquals("REGEX", transformation.transformationServiceName);
        assertEquals("myregex(.*)", transformation.transformationServiceParam);
    }

    @Test
    public void testTransformationNewStyle2() {
        SingleValueTransformation transformation = new SingleValueTransformation("REGEX::myregex(.*)");
        assertEquals("REGEX", transformation.transformationServiceName);
        assertEquals(":myregex(.*)", transformation.transformationServiceParam);
    }

    @Test
    public void testTransformationEmpty() {
        SingleValueTransformation transformation = new SingleValueTransformation("");
        assertFalse(transformation.isIdentityTransform());
        assertEquals("", transformation.transform(Mockito.mock(BundleContext.class), "xx"));
    }

    @Test
    public void testTransformationNull() {
        SingleValueTransformation transformation = new SingleValueTransformation(null);
        assertFalse(transformation.isIdentityTransform());
        assertEquals("", transformation.transform(Mockito.mock(BundleContext.class), "xx"));
    }

    @Test
    public void testTransformationDefault() {
        SingleValueTransformation transformation = new SingleValueTransformation("deFault");
        assertTrue(transformation.isIdentityTransform());
        assertEquals("xx", transformation.transform(Mockito.mock(BundleContext.class), "xx"));
    }

    @Test
    public void testTransformationDefaultChainedWithStatic() {
        SingleValueTransformation transformation = new SingleValueTransformation("static");
        assertFalse(transformation.isIdentityTransform());
        assertEquals("static", transformation.transform(Mockito.mock(BundleContext.class), "xx"));
    }
}
