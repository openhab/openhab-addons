/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.transform.feed.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Before;
import org.junit.Test;
import org.openhab.transform.feed.FeedTransformationService;

import com.google.common.base.CharMatcher;

/**
 * Tests for {@link FeedTransformationService}
 *
 * @author Svilen Valkanov
 */
public class FeedTransformationServiceTest {

    /**
     * Default test file name in RSS 2.0 format
     */
    private static final String MOCK_CONTENT_DEFAULT = "rss_2.0";

    /**
     * This file contains only XML declaration and empty RSS tag
     */
    private static final String MOCK_CONTENT_EMPTY = "rss_2.0_empty";

    /**
     * This is invalid XML 1.0 file, because the {@literal <br>
     * } tag is not closed
     */
    private static final String MOCK_CONTENT_INVALID_XML = "rss_2.0_invalid";

    /**
     * This file does not contain {@literal <xml>} declaration
     */
    private static final String MOCK_CONTENT_MISSING_XML_DECLARATION = "rss_2.0_missing_xml_declaration";

    /**
     * Name of the test resources output folder
     */
    private static final String TEST_OUTPUT_FOLDER = "output";

    /**
     * Name of the test resources input folder
     */
    private static final String TEST_INPUT_FOLDER = "input";

    private FeedTransformationService processor;

    @Before
    public void setUp() {
        processor = new FeedTransformationService();
    }

    @Test
    public void testTransformToAtom10() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.ATOM_1_0;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToAtom03() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.ATOM_0_3;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss100() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_1_00;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss090() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_90;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss091Netscape() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_91_NETSCAPE;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss091Userland() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_91_USERLAND;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss092() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_92;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss093() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_93;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransformToRss094() {
        String inputFile = MOCK_CONTENT_DEFAULT;
        String outputFormat = FeedTransformationService.RSS_0_94;
        testTransformation(inputFile, outputFormat, false);
    }

    @Test
    public void testTransormEmptyXML() {
        String inputFile = MOCK_CONTENT_EMPTY;
        String outputFormat = FeedTransformationService.RSS_2_00;
        testTransformation(inputFile, outputFormat, true);
    }

    @Test
    public void testTransformInvalidXML() {
        String inputFile = MOCK_CONTENT_INVALID_XML;
        String outputFormat = FeedTransformationService.RSS_2_00;
        testTransformation(inputFile, outputFormat, true);
    }

    @Test
    public void testMissingXMLDeclaration() {
        String inputFile = MOCK_CONTENT_MISSING_XML_DECLARATION;
        String outputFormat = FeedTransformationService.RSS_2_00;
        testTransformation(inputFile, outputFormat, false);
    }

    private void testTransformation(String inputFileName, String outputFormat, boolean mustThrowException) {
        String inputFilePath = String.format("%s/%s.xml", TEST_INPUT_FOLDER, inputFileName);
        String expectedFilePath = String.format("%s/%s.xml", TEST_OUTPUT_FOLDER, outputFormat);

        try {
            String inputFileContent = getFileAsString(inputFilePath);
            String expectedFileContent = getFileAsString(expectedFilePath);

            String outputFileContent = processor.transform(outputFormat, inputFileContent);
            if (mustThrowException) {
                fail("Expected a Transformation Exception to be thrown !");
            }
            outputFileContent = CharMatcher.BREAKING_WHITESPACE.removeFrom(outputFileContent);
            expectedFileContent = CharMatcher.BREAKING_WHITESPACE.removeFrom(expectedFileContent);
            assertEquals(outputFileContent, expectedFileContent);
        } catch (TransformationException e) {
            if (!mustThrowException) {
                fail("Transformation Exception wasn't expected: " + e.getMessage());
            }
            // The exception was expected, everything is fine
        } catch (IOException e) {
            fail("Unable to read test resource file: " + e.getMessage());
        }
    }

    private String getFileAsString(String inputPath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(inputPath);
        return IOUtils.toString(inputStream);
    }
}
