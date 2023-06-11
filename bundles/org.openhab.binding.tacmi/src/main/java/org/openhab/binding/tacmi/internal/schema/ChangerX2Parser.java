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
package org.openhab.binding.tacmi.internal.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import org.attoparser.ParseException;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.schema.ChangerX2Entry.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiPageParser} class parses the 'changerx2' page from the CMI and
 * maps it to the results
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class ChangerX2Parser extends AbstractSimpleMarkupHandler {

    private final Logger logger = LoggerFactory.getLogger(ChangerX2Parser.class);

    static enum ParserState {
        INIT,
        INPUT,
        INPUT_DATA,
        SELECT,
        SELECT_OPTION,
        UNKNOWN
    }

    private final String channelName;
    private @Nullable String curOptionId;
    private ParserState parserState = ParserState.INIT;
    private @Nullable String address;
    private @Nullable String addressFieldName;
    private @Nullable String optionFieldName;
    private @Nullable OptionType optionType;
    private @Nullable StringBuilder curOptionValue;
    private Map<String, @Nullable String> options;

    public ChangerX2Parser(String channelName) {
        super();
        this.options = new LinkedHashMap<>();
        this.channelName = channelName;
    }

    @Override
    public void handleDocumentStart(final long startTimeNanos, final int line, final int col) throws ParseException {
        this.parserState = ParserState.INIT;
        this.options.clear();
    }

    @Override
    public void handleDocumentEnd(final long endTimeNanos, final long totalTimeNanos, final int line, final int col)
            throws ParseException {
        if (this.parserState != ParserState.INIT) {
            logger.debug("Parserstate == Init expected, but is {}", this.parserState);
        }
    }

    @Override
    @NonNullByDefault({})
    public void handleStandaloneElement(final String elementName, final Map<String, String> attributes,
            final boolean minimized, final int line, final int col) throws ParseException {

        logger.debug("Error parsing options for {}: Unexpected StandaloneElement in {}{}: {} [{}]", channelName, line,
                col, elementName, attributes);
    }

    @Override
    @NonNullByDefault({})
    public void handleOpenElement(final String elementName, final Map<String, String> attributes, final int line,
            final int col) throws ParseException {

        String id = attributes == null ? null : attributes.get("id");

        if (this.parserState == ParserState.INIT && "input".equals(elementName) && "changeadr".equals(id)) {
            this.parserState = ParserState.INPUT;
            if (attributes == null) {
                this.address = null;
                this.addressFieldName = null;
            } else {
                this.addressFieldName = attributes.get("name");
                this.address = attributes.get("value");
            }
        } else if ((this.parserState == ParserState.INIT || this.parserState == ParserState.INPUT)
                && "select".equals(elementName)) {
            this.parserState = ParserState.SELECT;
            this.optionFieldName = attributes == null ? null : attributes.get("name");
        } else if ((this.parserState == ParserState.INIT || this.parserState == ParserState.INPUT)
                && "br".equals(elementName)) {
            // ignored
        } else if ((this.parserState == ParserState.INIT || this.parserState == ParserState.INPUT)
                && "input".equals(elementName) && "changeto".equals(id)) {
            this.parserState = ParserState.INPUT_DATA;
            if (attributes != null) {
                this.optionFieldName = attributes.get("name");
                String type = attributes.get("type");
                if ("number".equals(type)) {
                    this.optionType = OptionType.NUMBER;
                    // we transfer the limits from the input elemnt...
                    this.options.put(ChangerX2Entry.NUMBER_MIN, attributes.get(ChangerX2Entry.NUMBER_MIN));
                    this.options.put(ChangerX2Entry.NUMBER_MAX, attributes.get(ChangerX2Entry.NUMBER_MAX));
                    this.options.put(ChangerX2Entry.NUMBER_STEP, attributes.get(ChangerX2Entry.NUMBER_STEP));
                } else {
                    logger.warn("Error parsing options for {}: Unhandled input field in {}:{}: {}", channelName, line,
                            col, attributes);
                }
            }
        } else if (this.parserState == ParserState.SELECT && "option".equals(elementName)) {
            this.parserState = ParserState.SELECT_OPTION;
            this.optionType = OptionType.SELECT;
            this.curOptionValue = new StringBuilder();
            this.curOptionId = attributes == null ? null : attributes.get("value");
        } else {
            logger.debug("Error parsing options for {}: Unexpected OpenElement in {}:{}: {} [{}]", channelName, line,
                    col, elementName, attributes);
        }
    }

    @Override
    public void handleCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        if (this.parserState == ParserState.INPUT && "input".equals(elementName)) {
            this.parserState = ParserState.INIT;
        } else if (this.parserState == ParserState.SELECT && "select".equals(elementName)) {
            this.parserState = ParserState.INIT;
        } else if (this.parserState == ParserState.SELECT_OPTION && "option".equals(elementName)) {
            this.parserState = ParserState.SELECT;
            StringBuilder sb = this.curOptionValue;
            String value = sb != null && sb.length() > 0 ? sb.toString().trim() : null;
            this.curOptionValue = null;
            String id = this.curOptionId;
            this.curOptionId = null;
            if (value != null) {
                if (id == null || id.trim().isEmpty()) {
                    logger.debug("Error parsing options for {}: Got option with empty 'value' in {}:{}: [{}]",
                            channelName, line, col, value);
                    return;
                }
                // we use the value as key and the id as value, as we have to map from the value to the id...
                @Nullable
                String prev = this.options.put(value, id);
                if (prev != null && !prev.equals(value)) {
                    logger.debug("Error parsing options for {}: Got duplicate options in {}:{} for {}: {} and {}",
                            channelName, line, col, value, prev, id);
                }
            }
        } else {
            logger.debug("Error parsing options for {}: Unexpected CloseElement in {}:{}: {}", channelName, line, col,
                    elementName);
        }
    }

    @Override
    public void handleAutoCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected AutoCloseElement in {}:{}: {}", line, col,
                elementName == null ? "<null>" : elementName);
    }

    @Override
    public void handleUnmatchedCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected UnmatchedCloseElement in {}:{}: {}", line, col,
                elementName == null ? "<null>" : elementName);
    }

    @Override
    public void handleDocType(final @Nullable String elementName, final @Nullable String publicId,
            final @Nullable String systemId, final @Nullable String internalSubset, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected DocType in {}:{}: {}/{}/{}/{}", line, col, elementName, publicId, systemId,
                internalSubset);
    }

    @Override
    public void handleComment(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {
        logger.debug("Unexpected comment in {}:{}: {}", line, col,
                buffer == null ? "<null>" : new String(buffer, offset, len));
    }

    @Override
    public void handleCDATASection(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {
        logger.debug("Unexpected CDATA in {}:{}: {}", line, col,
                buffer == null ? "<null>" : new String(buffer, offset, len));
    }

    @Override
    public void handleText(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {

        if (buffer == null) {
            return;
        }

        if (this.parserState == ParserState.SELECT_OPTION) {
            // logger.debug("Text {}:{}: {}", line, col, new String(buffer, offset, len));
            StringBuilder sb = this.curOptionValue;
            if (sb != null) {
                sb.append(buffer, offset, len);
            }
        } else if (this.parserState == ParserState.INIT && len == 1 && buffer[offset] == '\n') {
            // single newline - ignore/drop it...
        } else if (this.parserState == ParserState.INPUT) {
            // this is a label next to the value input field - we currently have no use for it so
            // it's dropped...
        } else {
            logger.debug("Error parsing options for {}: Unexpected Text {}:{}: (ctx: {} len: {}) '{}' ",
                    this.channelName, line, col, this.parserState, len, new String(buffer, offset, len));
        }
    }

    @Override
    public void handleXmlDeclaration(final @Nullable String version, final @Nullable String encoding,
            final @Nullable String standalone, final int line, final int col) throws ParseException {
        logger.debug("Unexpected XML Declaration {}:{}: {} {} {}", line, col, version, encoding, standalone);
    }

    @Override
    public void handleProcessingInstruction(final @Nullable String target, final @Nullable String content,
            final int line, final int col) throws ParseException {
        logger.debug("Unexpected ProcessingInstruction {}:{}: {} {}", line, col, target, content);
    }

    @Nullable
    protected ChangerX2Entry getParsedEntry() {
        String addressFieldName = this.addressFieldName;
        String address = this.address;
        String optionFieldName = this.optionFieldName;
        OptionType optionType = this.optionType;
        if (address == null || addressFieldName == null || optionType == null || optionFieldName == null) {
            return null;
        }
        return new ChangerX2Entry(addressFieldName, address, optionFieldName, optionType, this.options);
    }
}
