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
package org.openhab.binding.networkupstools.internal.nut;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class to process NUT List results.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
final class NutResponseReader {

    private static final String BEGIN_LIST = "BEGIN LIST %s";
    private static final String END_LIST = "END LIST %s";
    private static final Pattern LIST_ROW_RESPONSE_PATTERN = Pattern.compile("([^\"]+)\"(.+)\"$");
    private static final Pattern GET_VAR_RESPONSE_PATTERN = Pattern.compile("VAR ([^\\s]+) ([^\"]+)\"(.+)\"$");

    private final Logger logger = LoggerFactory.getLogger(NutResponseReader.class);

    /**
     * Parses a NUT returned VAR.
     *
     * @param ups The ups the variable is for
     * @param nut The name of the variable
     * @param reader The reader containing the data
     * @return variable value for given nut variable name
     * @throws NutException Exception thrown in case of read errors
     */
    public String parseVariable(final String ups, final String nut, final NutSupplier<String> reader)
            throws NutException {
        final String line = reader.get();

        if (line == null) {
            throw new NutException(
                    String.format("Variable '%s' for ups '%s' could not be read because nothing received", nut, ups));
        }
        logger.trace("Line read:{}", line);
        final Matcher matcher = GET_VAR_RESPONSE_PATTERN.matcher(line);

        if (matcher.find() && matcher.groupCount() == 3) {
            final String matchedUps = matcher.group(1).trim();
            final String matchedNut = matcher.group(2).trim();
            final String value = stripVariable(matcher.group(3));

            if (!ups.equals(matchedUps)) {
                throw new NutException(
                        String.format("Returned value '%s' didn't match expected ups '%s'", matchedUps, ups));
            }
            if (!nut.equals(matchedNut)) {
                throw new NutException(
                        String.format("Returned value '%s' didn't match expected nut '%s'", matchedNut, nut));
            }
            return value;
        }
        throw new NutException(String.format("Variable '%s' for ups '%s' could not be read: %s", nut, ups, line));
    }

    /**
     * Parses a NUT returned LIST.
     *
     * @param type nut data type to expect in the data
     * @param reader The reader containing the data
     * @param variables The map to store the read nut variables
     * @return Map of variable name and variable value pairs
     * @throws NutException Exception thrown in case of read errors
     */
    public Map<String, String> parseList(final String type, final NutSupplier<String> reader) throws NutException {
        final Map<String, String> variables = new HashMap<>();
        logger.trace("Reading {}", type);
        validateBegin(type, reader);
        final int stripBeginLength = type.length() + 1;
        final String endString = String.format(END_LIST, type);
        String line = null;
        boolean endFound = false;

        while (!endFound) {
            line = reader.get();
            if (line == null) {
                throw new NutException("Unexpected end of data while reading " + type);
            }
            logger.trace("Line read:{}", line);
            endFound = endString.equals(line);
            if (!endFound) {
                addRow(variables, line, stripBeginLength);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("List '{}' read. {} variables read", type, variables.size());
        }
        return variables;
    }

    private void validateBegin(final String type, final NutSupplier<String> reader) throws NutException {
        final String beginString = String.format(BEGIN_LIST, type);
        String line;

        do {
            line = reader.get();
            logger.trace("Line read:{}", line);
            if (line == null) {
                throw new NutException("Could not find the begin string pattern in the data while reading " + type);
            }
        } while (!beginString.equals(line));
        logger.trace("Begin of list '{}' found", type);
    }

    private void addRow(final Map<String, String> map, final String row, final int offset) {
        final String substring = row.substring(offset);
        final Matcher matcher = LIST_ROW_RESPONSE_PATTERN.matcher(substring);

        if (matcher.find() && matcher.groupCount() == 2) {
            final String nut = matcher.group(1).trim();
            final String value = stripVariable(matcher.group(2));

            map.put(nut, value);
            logger.trace("Read nut variable '{}':{}", nut, value);
        } else {
            logger.debug("Unrecognized nut results: {}", row);
        }
    }

    private String stripVariable(final String rawVariable) {
        return rawVariable.replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\").trim();
    }
}
