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

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.attoparser.ParseException;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.binding.tacmi.internal.TACmiChannelTypeProvider;
import org.openhab.binding.tacmi.internal.schema.ApiPageEntry.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiPageParser} class parses the 'API' schema page from the CMI and
 * maps it to our channels
 *
 * @author Christian Niessner (marvkis) - Initial contribution
 */
public class ApiPageParser extends AbstractSimpleMarkupHandler {

    private final Logger logger = LoggerFactory.getLogger(ApiPageParser.class);

    static enum ParserState {
        Init,
        DataEntry
    }

    static enum FieldType {
        Unknown,
        ReadOnly,
        FormValue,
        Button,
        Ignore
    }

    static enum ButtonValue {
        Unknown,
        On,
        Off
    }

    private @NonNull ParserState parserState = ParserState.Init;
    private @NonNull TACmiSchemaHandler taCmiSchemaHandler;
    private @NonNull TACmiChannelTypeProvider channelTypeProvider;
    private boolean configChanged = false;
    private @NonNull FieldType fieldType = FieldType.Unknown;
    private @Nullable String id;
    private @Nullable String address;
    private @Nullable StringBuilder value;
    private @NonNull ButtonValue buttonValue = ButtonValue.Unknown;
    private @NonNull Map<@NonNull String, @Nullable ApiPageEntry> entries;
    private @NonNull Set<@NonNull String> seenNames = new HashSet<>();
    private @NonNull List<@NonNull Channel> channels = new ArrayList<>();

    public ApiPageParser(@NonNull TACmiSchemaHandler taCmiSchemaHandler,
            @NonNull Map<String, @Nullable ApiPageEntry> entries,
            @NonNull TACmiChannelTypeProvider channelTypeProvider) {
        super();
        this.taCmiSchemaHandler = taCmiSchemaHandler;
        this.entries = entries;
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public void handleDocumentStart(final long startTimeNanos, final int line, final int col) throws ParseException {
        this.parserState = ParserState.Init;
        this.seenNames.clear();
        this.channels.clear();
    }

    @Override
    public void handleDocumentEnd(final long endTimeNanos, final long totalTimeNanos, final int line, final int col)
            throws ParseException {
        if (this.parserState != ParserState.Init) {
            logger.debug("Parserstate == Init expected, but is {}", this.parserState);
        }
    }

    @Override
    public void handleStandaloneElement(final @Nullable String elementName,
            final @Nullable Map<String, String> attributes, final boolean minimized, final int line, final int col)
            throws ParseException {

        logger.info("Unexpected StandaloneElement in {}:{}: {} [{}]", line, col, elementName, attributes);
    }

    @Override
    public void handleOpenElement(final @Nullable String elementName, final @Nullable Map<String, String> attributes,
            final int line, final int col) throws ParseException {

        if (this.parserState == ParserState.Init && "div".equals(elementName)) {
            this.parserState = ParserState.DataEntry;
            String classFlags;
            if (attributes == null) {
                classFlags = null;
                this.id = null;
                this.address = null;
            } else {
                this.id = attributes.get("id");
                this.address = attributes.get("adresse");
                classFlags = attributes.get("class");
            }
            this.fieldType = FieldType.ReadOnly;
            this.value = new StringBuilder();
            this.buttonValue = ButtonValue.Unknown;
            if (classFlags != null && StringUtil.isNotBlank(classFlags)) {
                String[] classFlagList = classFlags.split("[ \n\r]");
                for (String classFlag : classFlagList) {
                    if ("changex2".equals(classFlag)) {
                        this.fieldType = FieldType.FormValue;
                    } else if ("buttonx2".equals(classFlag) || "taster".equals(classFlag)) {
                        this.fieldType = FieldType.Button;
                    } else if ("visible0".equals(classFlag)) {
                        this.buttonValue = ButtonValue.Off;
                    } else if ("visible1".equals(classFlag)) {
                        this.buttonValue = ButtonValue.On;
                    } else if ("durchsichtig".equals(classFlag)) { // link
                        this.fieldType = FieldType.Ignore;
                    } else if ("bord".equals(classFlag)) { // special button style - not of our interest...
                    } else {
                        logger.debug("Unhanndled class in {}:{}:{}: '{}' ", id, line, col, classFlag);
                    }
                }
            }
        } else if (this.parserState == ParserState.DataEntry && this.fieldType == FieldType.Button
                && "span".equals(elementName)) {
            // ignored...
        } else {
            logger.info("Unexpected OpenElement in {}:{}: {} [{}]", line, col, elementName, attributes);
        }
    }

    @Override
    public void handleCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        if (this.parserState == ParserState.DataEntry && "div".equals(elementName)) {
            this.parserState = ParserState.Init;
            StringBuilder sb = this.value;
            this.value = null;
            if (sb != null) {
                while (sb.length() > 0 && sb.charAt(0) == ' ') {
                    sb = sb.delete(0, 0);
                }
                if (this.fieldType == FieldType.ReadOnly || this.fieldType == FieldType.FormValue) {
                    int lids = sb.lastIndexOf(":");
                    int fsp = sb.indexOf(" ");
                    if (fsp < 0 || lids < 0 || fsp > lids) {
                        logger.info("Invalid format for setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType, sb);
                    } else {
                        String shortName = sb.substring(0, fsp).trim();
                        String description = sb.substring(fsp + 1, lids).trim();
                        String value = sb.substring(lids + 1).trim();
                        getApiPageEntry(id, line, col, shortName, description, value);
                    }
                } else if (this.fieldType == FieldType.Button) {
                    String sbt = sb.toString().trim().replaceAll("[\r\n ]+", " ");
                    int fsp = sbt.indexOf(" ");

                    if (fsp < 0) {
                        logger.info("Invalid format for setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType,
                                sbt);
                    } else {
                        String shortName = sbt.substring(0, fsp).trim();
                        String description = sbt.substring(fsp + 1).trim();
                        getApiPageEntry(id, line, col, shortName, description, this.buttonValue);
                    }
                } else if (this.fieldType == FieldType.Ignore) {
                    // ignore
                } else {
                    logger.info("Unhandled setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType, sb);
                }
            }
        } else if (this.parserState == ParserState.DataEntry && this.fieldType == FieldType.Button
                && "span".equals(elementName)) {
            // ignored...
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

        if (this.parserState == ParserState.DataEntry) {
            // logger.debug("Text {}:{}: {}", line, col, new String(buffer, offset, len));
            StringBuilder sb = this.value;
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

    private void getApiPageEntry(@Nullable String id2, int line, int col, String shortName, String description,
            Object value) {
        // this.taCmiSchemaHandler.thingUpdated(thing);
        if (logger.isDebugEnabled()) {
            logger.debug("Found parameter {}:{}:{} [{}] : {} \"{}\" = {}", id, line, col, this.fieldType, shortName,
                    description, value);
        }
        if (!this.seenNames.add(shortName)) {
            logger.warn("Found duplicate parameter '{}' in {}:{}:{} [{}] : {} \"{}\" = {}", shortName, id, line, col,
                    this.fieldType, shortName, description, value);
            return;
        }

        if (value instanceof String && ((String) value).contains("can_busy")) {
            return; // special state to indicate value currently not retrieveable..
        }
        ApiPageEntry.Type type;
        State state;
        String channelType;
        ChannelTypeUID ctuid;
        switch (this.fieldType) {
            case Button:
                type = Type.SwitchButton;
                state = this.buttonValue == ButtonValue.On ? OnOffType.ON : OnOffType.OFF;
                ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RW_UID;
                channelType = "Switch";
                break;
            case ReadOnly:
            case FormValue:
                String vs = (String) value;
                boolean isOn = "ON".equals(vs) || "EIN".equals(vs); // C.M.I. mixes up languages...
                if (isOn || "OFF".equals(vs) || "AUS".equals(vs)) {
                    channelType = "Switch";
                    state = isOn ? OnOffType.ON : OnOffType.OFF;
                    if (this.fieldType == FieldType.ReadOnly || this.address == null) {
                        ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RO_UID;
                        type = Type.ReadOnlySwitch;
                    } else {
                        ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RW_UID;
                        type = Type.SwitchForm;
                    }
                } else {
                    try {
                        // check if we have a numeric value (either with or without unit)
                        String[] valParts = vs.split(" ");
                        BigDecimal bd = new BigDecimal(valParts[0]);
                        if (valParts.length == 2) {
                            if ("°C".equals(valParts[1])) {
                                channelType = "Number:Temperature";
                                state = new QuantityType<>(bd, SIUnits.CELSIUS);
                            } else if ("%".equals(valParts[1])) {
                                channelType = "Number:Percent";
                                state = new QuantityType<>(bd, SmartHomeUnits.PERCENT);
                            } else if ("Imp".equals(valParts[1])) {
                                // impulses - no idea how to map this to something useful here?
                                channelType = "Number";
                                state = new DecimalType(bd);
                            } else if ("V".equals(valParts[1])) {
                                channelType = "Number:Voltage";
                                state = new QuantityType<>(bd, SmartHomeUnits.VOLT);
                            } else if ("A".equals(valParts[1])) {
                                channelType = "Number:Current";
                                state = new QuantityType<>(bd, SmartHomeUnits.AMPERE);
                            } else if ("Hz".equals(valParts[1])) {
                                channelType = "Number:Frequency";
                                state = new QuantityType<>(bd, SmartHomeUnits.HERTZ);
                            } else if ("kW".equals(valParts[1])) {
                                channelType = "Number:Power";
                                bd = bd.multiply(new BigDecimal(1000));
                                state = new QuantityType<>(bd, SmartHomeUnits.WATT);
                            } else if ("kWh".equals(valParts[1])) {
                                channelType = "Number:Power";
                                bd = bd.multiply(new BigDecimal(1000));
                                state = new QuantityType<>(bd, SmartHomeUnits.KILOWATT_HOUR);
                            } else if ("l/h".equals(valParts[1])) {
                                channelType = "Number:Volume";
                                bd = bd.divide(new BigDecimal(60));
                                state = new QuantityType<>(bd, SmartHomeUnits.LITRE_PER_MINUTE);
                            } else {
                                channelType = "Number";
                                state = new DecimalType(bd);
                                logger.info("Unhandled UoM for channel {} of type {} for '{}': {}", shortName,
                                        channelType, description, valParts[1]);
                            }
                        } else {
                            channelType = "Number";
                            state = new DecimalType(bd);
                        }
                        if (this.fieldType == FieldType.ReadOnly || this.address == null) {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
                            type = Type.ReadOnlyNumeric;
                        } else {
                            ctuid = null;
                            type = Type.NumericForm;
                        }
                    } catch (NumberFormatException nfe) {
                        // not a number...
                        channelType = "String";
                        if (this.fieldType == FieldType.ReadOnly || this.address == null) {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
                            type = Type.ReadOnlyState;
                        } else {
                            ctuid = null;
                            type = Type.StateForm;
                        }
                        state = new StringType(vs);
                    }
                }
                break;
            case Unknown:
            case Ignore:
                return;
            default:
                // should't happen but we have to add default for the compiler...
                return;
        }
        ApiPageEntry e = this.entries.get(shortName);
        if (e == null || e.type != type || !channelType.equals(e.channel.getAcceptedItemType())) {
            @Nullable
            Channel channel = this.taCmiSchemaHandler.getThing().getChannel(shortName);
            @Nullable
            ChangerX2Entry cx2e = null;
            if (this.fieldType == FieldType.FormValue) {
                try {
                    URI uri = this.taCmiSchemaHandler.buildUri("INCLUDE/changerx2.cgi?sadrx2=" + address);
                    final ChangerX2Parser pp = this.taCmiSchemaHandler.parsePage(uri, new ChangerX2Parser());
                    cx2e = pp.getParsedEntry();
                } catch (final Exception ex) {
                    logger.error("Error loading API Scheme: {} ", ex.getMessage(), ex);
                }
            }
            if (channel == null) {
                logger.info("Creating / updating channel {} of type {} for '{}'", shortName, channelType, description);
                this.configChanged = true;
                ChannelUID channelUID = new ChannelUID(this.taCmiSchemaHandler.getThing().getUID(), shortName);
                ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, channelType);
                channelBuilder.withLabel(description);
                if (ctuid != null) {
                    channelBuilder.withType(ctuid);
                } else if (cx2e != null) {
                    StateDescriptionFragmentBuilder sdb = StateDescriptionFragmentBuilder.create()
                            .withReadOnly(type.readOnly);
                    String itemType;
                    switch (cx2e.optionType) {
                        case Number:
                            itemType = "Number";
                            String min = cx2e.options.get(ChangerX2Entry.NUMBER_MIN);
                            if (StringUtils.isNotBlank(min)) {
                                sdb.withMinimum(new BigDecimal(min));
                            }
                            String max = cx2e.options.get(ChangerX2Entry.NUMBER_MAX);
                            if (StringUtils.isNotBlank(max)) {
                                sdb.withMaximum(new BigDecimal(max));
                            }
                            String step = cx2e.options.get(ChangerX2Entry.NUMBER_STEP);
                            if (StringUtils.isNotBlank(step)) {
                                sdb.withStep(new BigDecimal(step));
                            }
                            break;
                        case Select:
                            itemType = "String";
                            for (Entry<@NonNull String, @Nullable String> entry : cx2e.options.entrySet()) {
                                String val = entry.getValue();
                                if (val != null) {
                                    sdb.withOption(new StateOption(val, entry.getKey()));
                                }
                            }
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    ChannelType ct = ChannelTypeBuilder
                            .state(new ChannelTypeUID(TACmiBindingConstants.BINDING_ID, shortName), shortName, itemType)
                            .withDescription("Auto-created for " + shortName)
                            .withStateDescription(sdb.build().toStateDescription())
                            // .withCategory("CategoryName") can we do something useful ?
                            .build();
                    channelTypeProvider.addChannelType(ct);
                    channelBuilder.withType(ct.getUID());
                } else {
                    logger.warn("Error configurating channel for {}: channeltype cannot be determined!", shortName);
                }
                channel = channelBuilder.build(); // add configuration property...
            }
            this.configChanged = true;
            e = new ApiPageEntry(type, channel, address, cx2e);
            this.entries.put(shortName, e);
        }
        this.channels.add(e.channel);
        this.taCmiSchemaHandler.updateState(e.channel.getUID(), state);
    }

    protected boolean isConfigChanged() {
        return this.configChanged;
    }

    protected @NonNull List<@NonNull Channel> getChannels() {
        return channels;
    }

}