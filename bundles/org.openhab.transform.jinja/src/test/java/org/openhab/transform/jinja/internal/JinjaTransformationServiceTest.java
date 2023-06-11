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
package org.openhab.transform.jinja.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.transform.TransformationException;

/**
 * @author Jochen Klein - Initial contribution
 */
public class JinjaTransformationServiceTest {

    private JinjaTransformationService processor;

    @BeforeEach
    public void init() {
        processor = new JinjaTransformationService();
    }

    @Test
    public void testTransformByJSon() throws TransformationException {
        String json = "{\"Time\":\"2019-01-05T22:45:12\",\"AM2301\":{\"Temperature\":4.7,\"Humidity\":99.9},\"TempUnit\":\"C\"}";
        // method under test
        String transformedResponse = processor.transform("{{value_json['AM2301'].Temperature}}", json);

        // Asserts
        assertEquals("4.7", transformedResponse);
    }

    @Test
    public void testStringOnly() throws TransformationException {
        String value = "world";
        // method under test
        String transformedResponse = processor.transform("Hello {{ value }}!", value);

        // Asserts
        assertEquals("Hello world!", transformedResponse);
    }

    @Test
    public void testQuotedStringOnly() throws TransformationException {
        String value = "\"world\"";
        // method under test
        String transformedResponse = processor.transform("Hello {{ value_json }}!", value);

        // Asserts
        assertEquals("Hello world!", transformedResponse);
    }

    @Test
    public void testJsonParsingError() throws TransformationException {
        // when JSON binding parsing failed
        String transformedResponse = processor.transform("Hello {{ value }}!", "{\"string\"{: \"world\"}");

        // then template should be rendered
        assertEquals("Hello {\"string\"{: \"world\"}!", transformedResponse);
    }

    @Test
    public void testTemplateError() {
        assertThrows(TransformationException.class,
                () -> processor.transform("Hello {{{ value_json.string }}!", "{\"string\": \"world\"}"));
    }

    @Test
    public void testMissingVariableError() {
        assertThrows(TransformationException.class,
                () -> processor.transform("Hello {{ missing }}!", "{\"string\": \"world\"}"));
    }

    @Test
    public void testMissingMapKeyError() {
        assertThrows(TransformationException.class,
                () -> processor.transform("Hello {{ value_json.missing }}!", "{\"string\": \"world\"}"));
    }

    @Test
    public void testMissingVariableIsDefined() throws TransformationException {
        // when checking missing variable
        String transformedResponse = processor.transform("{{ missing is defined }}", "{\"string\": \"world\"}");

        // then missing variable is not defined
        assertEquals("false", transformedResponse);
    }

    @Test
    public void testMissingMapKeyIsDefined() throws TransformationException {
        // when checking missing map key
        String transformedResponse = processor.transform("{{ value_json.missing is defined }}",
                "{\"string\": \"world\"}");

        // then missing map key is not defined
        assertEquals("false", transformedResponse);
    }

    @Test
    public void testIsDefined() throws TransformationException {
        // when checking map key
        String transformedResponse = processor.transform("{{ value_json.string is defined }}",
                "{\"string\": \"world\"}");

        // then map key is defined
        assertEquals("true", transformedResponse);
    }
}
