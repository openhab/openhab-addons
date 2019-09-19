/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.transform.jsonpath.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class JSonPathTransformationServiceTest {

    private JSonPathTransformationService processor;

    @Before
    public void init() {
        processor = new JSonPathTransformationService();
    }

    @Test
    public void testTransformByJSon() throws TransformationException {

        String json = "{'store':{'book':[{'category':'reference','author':'Nigel Rees','title': 'Sayings of the Century', 'price': 8.95  } ],  'bicycle': { 'color': 'red',  'price': 19.95} }}";
        // method under test
        String transformedResponse = processor.transform("$.store.book[0].author", json);

        // Asserts
        Assert.assertEquals("Nigel Rees", transformedResponse);
    }

    private static final String jsonArray = "[" + //
            "{ \"id\":1, \"name\":\"bob\", \"empty\":null }," + //
            "{ \"id\":2, \"name\":\"alice\" }" + //
            "]";

    @Test
    public void testValidPath1() throws TransformationException {
        String transformedResponse = processor.transform("$[0].name", jsonArray);
        assertEquals("bob", transformedResponse);
    }

    @Test
    public void testValidPath2() throws TransformationException {
        String transformedResponse = processor.transform("$[1].id", jsonArray);
        assertEquals("2", transformedResponse);
    }

    @Test(expected = TransformationException.class)
    public void testInvalidPathThrowsException() throws TransformationException {
        processor.transform("$$", jsonArray);
    }

    @Test(expected = TransformationException.class)
    public void testPathMismatchReturnNull() throws TransformationException {
        processor.transform("$[5].id", jsonArray);
    }

    @Test(expected = TransformationException.class)
    public void testInvalidJsonReturnNull() throws TransformationException {
        processor.transform("$", "{id:");
    }

    @Test
    public void testNullValue() throws TransformationException {
        String transformedResponse = processor.transform("$[0].empty", jsonArray);
        assertEquals(null, transformedResponse);
    }

    @Test
    public void testIndefinite_filteredToSingle() throws TransformationException {
        String transformedResponse = processor.transform("$.*[?(@.name=='bob')].id", jsonArray);
        assertEquals("1", transformedResponse);
    }

    @Test
    public void testIndefinite_notFiltered() throws TransformationException {
        String transformedResponse = processor.transform("$.*.id", jsonArray);
        assertEquals("[1, 2]", transformedResponse);
    }

    @Test
    public void testIndefinite_noMatch() throws TransformationException {
        String transformedResponse = processor.transform("$.*[?(@.name=='unknown')].id", jsonArray);
        assertEquals("NULL", transformedResponse);
    }

    @Test
    public void testBooleanList() throws TransformationException {
        final String list = "[true, false, true, true, false]";
        final String json = "{\"data\":" + list + "}";
        String transformedResponse = processor.transform("$.data", json);
        assertEquals(list, transformedResponse);
    }

    @Test
    public void testNumberList() throws TransformationException {
        final String list = "[0, 160, 253, -9, 21]";
        final String json = "{\"data\":" + list + "}";
        String transformedResponse = processor.transform("$.data", json);
        assertEquals(list, transformedResponse);
    }

    @Test
    public void testDoubleList() throws TransformationException {
        final String list = "[1.0, 2.16, 5.253, -3.9, 21.21]";
        final String json = "{\"data\":" + list + "}";
        String transformedResponse = processor.transform("$.data", json);
        assertEquals(list, transformedResponse);
    }

    @Test
    public void testStringList() throws TransformationException {
        final String list = "[\"test1\", \"test2\", \"test3\", \"test4\"]";
        final String json = "{\"data\":" + list + "}";
        String transformedResponse = processor.transform("$.data", json);
        assertEquals(list, transformedResponse);
    }
}
