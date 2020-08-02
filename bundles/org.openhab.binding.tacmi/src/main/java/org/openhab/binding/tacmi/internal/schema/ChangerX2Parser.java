/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.attoparser.ParseException;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.schema.ChangerX2Entry.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiPageParser} class parses the 'changerx2' page from the CMI and
 * maps it to the results
 *
 * @author Christian Niessner (marvkis) - Initial contribution
 */
public class ChangerX2Parser extends AbstractSimpleMarkupHandler {

    private final Logger logger = LoggerFactory.getLogger(ApiPageParser.class);

    static enum ParserState {
        Init,
        Input,
        InputData,
        Select,
        SelectOption,
        Unknown
    }

    private @Nullable String curOptionId;
    private @NonNull ParserState parserState = ParserState.Init;
    private @Nullable String address;
    private @Nullable String addressFieldName;
    private @Nullable String optionFieldName;
    private @Nullable OptionType optionType;
    private @Nullable StringBuilder curOptionValue;
    private @NonNull Map<@NonNull String, @Nullable String> options;

    public ChangerX2Parser() {
        super();
        this.options = new LinkedHashMap<>();
    }

    @Override
    public void handleDocumentStart(final long startTimeNanos, final int line, final int col) throws ParseException {
        this.parserState = ParserState.Init;
        this.options.clear();
    }

    @Override
    public void handleDocumentEnd(final long endTimeNanos, final long totalTimeNanos, final int line, final int col)
            throws ParseException {
        if (this.parserState != ParserState.Init) {
            logger.debug("Parserstate == Init expected, but is {}", this.parserState);
        }
    }

    @Override
    public void handleStandaloneElement(final String elementName, final Map<String, String> attributes,
            final boolean minimized, final int line, final int col) throws ParseException {

        logger.info("Unexpected StandaloneElement in {}{}: {} [{}]", line, col, elementName, attributes);
    }

    @Override
    public void handleOpenElement(final String elementName, final Map<String, String> attributes, final int line,
            final int col) throws ParseException {

        String id = attributes == null ? null : attributes.get("id");

        if (this.parserState == ParserState.Init && "input".equals(elementName) && "changeadr".equals(id)) {
            this.parserState = ParserState.Input;
            if (attributes == null) {
                this.address = null;
                this.addressFieldName = null;
            } else {
                this.addressFieldName = attributes.get("name");
                this.address = attributes.get("value");
            }
        } else if ((this.parserState == ParserState.Init || this.parserState == ParserState.Input)
                && "select".equals(elementName)) {
            this.parserState = ParserState.Select;
            this.optionFieldName = attributes == null ? null : attributes.get("name");
        } else if ((this.parserState == ParserState.Init || this.parserState == ParserState.Input)
                && "br".equals(elementName)) {
            // ignored
        } else if ((this.parserState == ParserState.Init || this.parserState == ParserState.Input)
                && "input".equals(elementName) && "changeto".equals(id)) {
            this.parserState = ParserState.InputData;
            if (attributes != null) {
                this.optionFieldName = attributes.get("name");
                String type = attributes.get("type");
                if ("number".equals(type)) {
                    this.optionType = OptionType.Number;
                    // we transfer the limits from the input elemnt...
                    this.options.put(ChangerX2Entry.NUMBER_MIN, attributes.get(ChangerX2Entry.NUMBER_MIN));
                    this.options.put(ChangerX2Entry.NUMBER_MAX, attributes.get(ChangerX2Entry.NUMBER_MAX));
                    this.options.put(ChangerX2Entry.NUMBER_STEP, attributes.get(ChangerX2Entry.NUMBER_STEP));
                } else {
                    logger.warn("Unhandled input field in {}:{}: {}", line, col, attributes);
                }
            }
        } else if (this.parserState == ParserState.Select && "option".equals(elementName)) {
            this.parserState = ParserState.SelectOption;
            this.optionType = OptionType.Select;
            this.curOptionValue = new StringBuilder();
            this.curOptionId = attributes == null ? null : attributes.get("value");
        } else {
            logger.info("Unexpected OpenElement in {}:{}: {} [{}]", line, col, elementName, attributes);
        }
    }

    @Override
    public void handleCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        if (this.parserState == ParserState.Input && "input".equals(elementName)) {
            this.parserState = ParserState.Init;
        } else if (this.parserState == ParserState.Select && "select".equals(elementName)) {
            this.parserState = ParserState.Init;
        } else if (this.parserState == ParserState.SelectOption && "option".equals(elementName)) {
            this.parserState = ParserState.Select;
            StringBuilder sb = this.curOptionValue;
            String value = sb != null && sb.length() > 0 ? sb.toString().trim() : null;
            this.curOptionValue = null;
            String id = this.curOptionId;
            this.curOptionId = null;
            if (value != null) {
                if (id == null || !StringUtils.isNotBlank(id)) {
                    logger.info("Got option with empty 'value' in {}:{}: [{}]", line, col, value);
                    return;
                }
                // we use the value as key and the id as value, as we have to map from the value to the id...
                @Nullable
                String prev = this.options.put(value, id);
                if (prev != null && !prev.equals(value)) {
                    logger.info("Got duplicate options in {}:{} for {}: {} and {}", line, col, value, prev, id);
                }
            }
        } else {
            logger.info("Unexpected CloseElement in {}:{}: {}", line, col, elementName);
        }
    }

    @Override
    public void handleAutoCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.info("Unexpected AutoCloseElement in {}:{}: {}", line, col, elementName);
    }

    @Override
    public void handleUnmatchedCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.info("Unexpected UnmatchedCloseElement in {}:{}: {}", line, col, elementName);
    }

    @Override
    public void handleDocType(final @Nullable String elementName, final @Nullable String publicId,
            final @Nullable String systemId, final @Nullable String internalSubset, final int line, final int col)
            throws ParseException {
        logger.info("Unexpected DocType in {}:{}: {}/{}/{}/{}", line, col, elementName, publicId, systemId,
                internalSubset);
    }

    @Override
    public void handleComment(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {
        logger.info("Unexpected comment in {}:{}: {}", line, col, new String(buffer, offset, len));
    }

    @Override
    public void handleCDATASection(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {
        logger.info("Unexpected CDATA in {}:{}: {}", line, col, new String(buffer, offset, len));
    }

    @Override
    public void handleText(final char @Nullable [] buffer, final int offset, final int len, final int line,
            final int col) throws ParseException {

        if (buffer == null) {
            return;
        }

        if (this.parserState == ParserState.SelectOption) {
            // logger.debug("Text {}:{}: {}", line, col, new String(buffer, offset, len));
            StringBuilder sb = this.curOptionValue;
            if (sb != null) {
                sb.append(buffer, offset, len);
            }
        } else if (this.parserState == ParserState.Init && len == 1 && buffer[offset] == '\n') {
            // single newline - ignore/drop it...
        } else {
            logger.info("Unexpected Text {}:{}: ({}) {} ", line, col, len, new String(buffer, offset, len));
        }
    }

    @Override
    public void handleXmlDeclaration(final @Nullable String version, final @Nullable String encoding,
            final @Nullable String standalone, final int line, final int col) throws ParseException {
        logger.info("Unexpected XML Declaration {}:{}: {} {} {}", line, col, version, encoding, standalone);
    }

    @Override
    public void handleProcessingInstruction(final @Nullable String target, final @Nullable String content,
            final int line, final int col) throws ParseException {
        logger.info("Unexpected ProcessingInstruction {}:{}: {} {}", line, col, target, content);
    }

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