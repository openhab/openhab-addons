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
package org.openhab.binding.tacmi.internal.schema;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.attoparser.ParseException;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.binding.tacmi.internal.TACmiChannelTypeProvider;
import org.openhab.binding.tacmi.internal.schema.ApiPageEntry.Type;
import org.openhab.binding.tacmi.internal.schema.TACmiSchemaHandler.UnitAndType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiPageParser} class parses the 'API' schema page from the CMI and
 * maps it to our channels
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class ApiPageParser extends AbstractSimpleMarkupHandler {

    private final Logger logger = LoggerFactory.getLogger(ApiPageParser.class);

    static enum ParserState {
        INIT,
        DATA_ENTRY
    }

    static enum FieldType {
        UNKNOWN,
        READ_ONLY,
        FORM_VALUE,
        BUTTON,
        IGNORE
    }

    static enum ButtonValue {
        UNKNOWN,
        ON,
        OFF
    }

    private ParserState parserState = ParserState.INIT;
    private TACmiSchemaHandler taCmiSchemaHandler;
    private TACmiChannelTypeProvider channelTypeProvider;
    private boolean configChanged = false;
    private FieldType fieldType = FieldType.UNKNOWN;
    private @Nullable String id;
    private @Nullable String address;
    private @Nullable StringBuilder value;
    private ButtonValue buttonValue = ButtonValue.UNKNOWN;
    private Map<String, ApiPageEntry> entries;
    private Set<String> seenNames = new HashSet<>();
    private List<Channel> channels = new ArrayList<>();
    // Time stamp when status request was started.
    private final long statusRequestStartTS;
    private static @Nullable URI configDescriptionUriAPISchemaDefaults;
    private final Pattern timePattern = Pattern.compile("[0-9]{2}:[0-9]{2}");
    private final Pattern durationPattern = Pattern.compile("([0-9\\.]{1,4}[dhms] ?)+");

    // needed for unit rewrite. it seems OHM is not registered as symbol in the units.
    public ApiPageParser(TACmiSchemaHandler taCmiSchemaHandler, Map<String, ApiPageEntry> entries,
            TACmiChannelTypeProvider channelTypeProvider) {
        super();
        this.taCmiSchemaHandler = taCmiSchemaHandler;
        this.entries = entries;
        this.channelTypeProvider = channelTypeProvider;
        this.statusRequestStartTS = System.currentTimeMillis();
        if (configDescriptionUriAPISchemaDefaults == null) {
            try {
                configDescriptionUriAPISchemaDefaults = new URI(
                        TACmiBindingConstants.CONFIG_DESCRIPTION_API_SCHEMA_DEFAULTS);
            } catch (URISyntaxException ex) {
                logger.warn("Can't create ConfigDescription URI '{}', ConfigDescription for channels not avilable!",
                        TACmiBindingConstants.CONFIG_DESCRIPTION_API_SCHEMA_DEFAULTS);
            }
        }
    }

    @Override
    public void handleDocumentStart(final long startTimeNanos, final int line, final int col) throws ParseException {
        this.parserState = ParserState.INIT;
        this.seenNames.clear();
        this.channels.clear();
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
    public void handleStandaloneElement(final @Nullable String elementName,
            final @Nullable Map<String, String> attributes, final boolean minimized, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected StandaloneElement in {}:{}: {} [{}]", line, col, elementName, attributes);
    }

    @Override
    @NonNullByDefault({})
    public void handleOpenElement(final @Nullable String elementName, final @Nullable Map<String, String> attributes,
            final int line, final int col) throws ParseException {
        if (this.parserState == ParserState.INIT && "div".equals(elementName)) {
            this.parserState = ParserState.DATA_ENTRY;
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
            this.fieldType = FieldType.READ_ONLY;
            this.value = new StringBuilder();
            this.buttonValue = ButtonValue.UNKNOWN;
            if (classFlags != null && StringUtil.isNotBlank(classFlags)) {
                String[] classFlagList = classFlags.split("[ \n\r]");
                for (String classFlag : classFlagList) {
                    if ("changex2".equals(classFlag)) {
                        this.fieldType = FieldType.FORM_VALUE;
                    } else if ("buttonx2".equals(classFlag) || "taster".equals(classFlag)) {
                        this.fieldType = FieldType.BUTTON;
                    } else if ("visible0".equals(classFlag)) {
                        this.buttonValue = ButtonValue.OFF;
                    } else if ("visible1".equals(classFlag)) {
                        this.buttonValue = ButtonValue.ON;
                    } else if ("durchsichtig".equals(classFlag)) { // link
                        this.fieldType = FieldType.IGNORE;
                    } else if ("bord".equals(classFlag)) { // special button style - not of our interest...
                    } else {
                        logger.debug("Unhanndled class in {}:{}:{}: '{}' ", id, line, col, classFlag);
                    }
                }
            }
        } else if (this.parserState == ParserState.DATA_ENTRY && this.fieldType == FieldType.BUTTON
                && "span".equals(elementName)) {
            // ignored...
        } else {
            logger.debug("Unexpected OpenElement in {}:{}: {} [{}]", line, col, elementName, attributes);
        }
    }

    @Override
    public void handleCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        if (this.parserState == ParserState.DATA_ENTRY && "div".equals(elementName)) {
            this.parserState = ParserState.INIT;
            StringBuilder sb = this.value;
            this.value = null;
            if (sb != null) {
                while (sb.length() > 0 && sb.charAt(0) == ' ') {
                    sb = sb.delete(0, 0);
                }
                if (this.fieldType == FieldType.READ_ONLY || this.fieldType == FieldType.FORM_VALUE) {
                    int len = sb.length();
                    int lids = sb.lastIndexOf(":");
                    if (len - lids == 3) {
                        int lids2 = sb.lastIndexOf(":", lids - 1);
                        if (lids2 > 0 && (lids - lids2 >= 3 && lids - lids2 <= 7)) {
                            // the given value might be a time. validate it
                            String timeCandidate = sb.substring(lids2 + 1).trim();
                            if (timeCandidate.length() == 5 && timePattern.matcher(timeCandidate).matches()) {
                                lids = lids2;
                            }
                        }
                    }
                    int fsp = sb.indexOf(" ");
                    if (fsp < 0 || lids < 0 || fsp > lids) {
                        logger.debug("Invalid format for setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType,
                                sb);
                    } else {
                        String shortName = sb.substring(0, fsp).trim();
                        String description = sb.substring(fsp + 1, lids).trim();
                        String value = sb.substring(lids + 1).trim();
                        getApiPageEntry(id, line, col, shortName, description, value);
                    }
                } else if (this.fieldType == FieldType.BUTTON) {
                    String sbt = sb.toString().trim().replaceAll("[\r\n ]+", " ");
                    int fsp = sbt.indexOf(" ");

                    if (fsp < 0) {
                        logger.debug("Invalid format for setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType,
                                sbt);
                    } else {
                        String shortName = sbt.substring(0, fsp).trim();
                        String description = sbt.substring(fsp + 1).trim();
                        getApiPageEntry(id, line, col, shortName, description, this.buttonValue);
                    }
                } else if (this.fieldType == FieldType.IGNORE) {
                    // ignore
                } else {
                    logger.debug("Unhandled setting {}:{}:{} [{}] : {}", id, line, col, this.fieldType, sb);
                }
            }
        } else if (this.parserState == ParserState.DATA_ENTRY && this.fieldType == FieldType.BUTTON
                && "span".equals(elementName)) {
            // ignored...
        } else {
            logger.debug("Unexpected CloseElement in {}:{}: {}", line, col, elementName);
        }
    }

    @Override
    public void handleAutoCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected AutoCloseElement in {}:{}: {}", line, col, elementName);
    }

    @Override
    public void handleUnmatchedCloseElement(final @Nullable String elementName, final int line, final int col)
            throws ParseException {
        logger.debug("Unexpected UnmatchedCloseElement in {}:{}: {}", line, col, elementName);
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

        if (this.parserState == ParserState.DATA_ENTRY) {
            // we append it to our current value
            StringBuilder sb = this.value;
            if (sb != null) {
                sb.append(buffer, offset, len);
            }
        } else if (this.parserState == ParserState.INIT && ((len == 1 && buffer[offset] == '\n')
                || (len == 2 && buffer[offset] == '\r' && buffer[offset + 1] == '\n'))) {
            // single newline - ignore/drop it...
        } else {
            String msg = new String(buffer, offset, len).replace("\n", "\\n").replace("\r", "\\r");
            logger.debug("Unexpected Text {}:{}: ParserState: {} ({}) `{}`", line, col, parserState, len, msg);
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

    private void getApiPageEntry(@Nullable String id2, int line, int col, String shortName, String description,
            Object value) {
        if (logger.isTraceEnabled()) {
            logger.trace("Found parameter {}:{}:{} [{}] : {} \"{}\" = {}", id, line, col, this.fieldType, shortName,
                    description, value);
        }
        if (!this.seenNames.add(shortName)) {
            logger.warn("Found duplicate parameter '{}' in {}:{}:{} [{}] : {} \"{}\" = {}", shortName, id, line, col,
                    this.fieldType, shortName, description, value);
            return;
        }

        if (value instanceof String && ((String) value).contains("can_busy")) {
            return; // special state to indicate value currently cannot be retrieved..
        }
        ApiPageEntry.Type type;
        Unit<?> unit;
        State state;
        String channelType;
        ChannelTypeUID ctuid;
        switch (this.fieldType) {
            case BUTTON:
                type = Type.SWITCH_BUTTON;
                state = OnOffType.from(this.buttonValue == ButtonValue.ON);
                ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RW_UID;
                channelType = "Switch";
                unit = null;
                break;
            case READ_ONLY:
            case FORM_VALUE:
                String vs = (String) value;
                boolean isOn = "ON".equals(vs) || "EIN".equals(vs); // C.M.I. mixes up languages...
                if (isOn || "OFF".equals(vs) || "AUS".equals(vs)) {
                    channelType = "Switch";
                    state = OnOffType.from(isOn);
                    unit = null;
                    if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                        ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RO_UID;
                        type = Type.READ_ONLY_SWITCH;
                    } else {
                        ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RW_UID;
                        type = Type.SWITCH_FORM;
                    }
                } else {
                    try {
                        // check if we have a numeric value (either with or without unit)
                        String[] valParts = vs.split(" ");
                        // It seems for some wired cases the C.M.I. uses different decimal separators for
                        // different device types. It seems all 'new' X2-Devices use a dot as separator,
                        // for the older pre-X2 devices (i.e. the UVR 1611) we get a comma. So we
                        // we replace all ',' with '.' to check if it's a valid number...
                        String val = valParts[0].replace(',', '.');
                        float bd = Float.parseFloat(val);
                        if (valParts.length == 2) {
                            var unitStr = valParts[1];
                            var unitData = taCmiSchemaHandler.unitsCache.get(unitStr);
                            if (unitData == null) {
                                // we try to lookup the unit given by TA.
                                try {
                                    // Special rewrite for electrical resistance measurements
                                    // U+2126 is the 'real' OHM sign, but it seems to be registered as Greek Omega
                                    // (U+03A9) in the units
                                    String unitStrRepl = unitStr.replace((char) 0x2126, (char) 0x03A9);
                                    // we build a 'normalized' value for parsing in QuantityType.
                                    var qt = new QuantityType<>(val + " " + unitStrRepl, Locale.US);
                                    // Just use the unit. We need to remember the unit in the channel data because we
                                    // need to send data to the C.M.I. in the same unit
                                    unit = qt.getUnit();
                                    channelType = "Number:" + UnitUtils.getDimensionName(unit);
                                    unitData = new UnitAndType(unit, channelType);
                                } catch (IllegalArgumentException iae) {
                                    // failed to get unit...
                                    if ("Imp".equals(unitStr) || "€$".contains(unitStr)) {
                                        // special case
                                        unitData = taCmiSchemaHandler.SPECIAL_MARKER;
                                    } else {
                                        unitData = taCmiSchemaHandler.NULL_MARKER;
                                        logger.warn(
                                                "Unhandled UoM '{}' - seen on channel {} '{}'; Message from QuantityType: {}",
                                                valParts[1], shortName, description, iae.getMessage());
                                    }
                                }
                                taCmiSchemaHandler.unitsCache.put(unitStr, unitData);
                            }
                            if (unitData == taCmiSchemaHandler.NULL_MARKER) {
                                // no UoM mappable - just send value
                                channelType = "Number";
                                unit = null;
                                state = new DecimalType(bd);
                            } else if (unitData == taCmiSchemaHandler.SPECIAL_MARKER) {
                                // special handling for unknown UoM
                                if ("Imp".equals(unitStr)) { // Number of Pulses
                                    // impulses - no idea how to map this to something useful here?
                                    channelType = "Number";
                                    unit = null;
                                    state = new DecimalType(bd);
                                } else if ("€$".contains(unitStr)) { // Currency's
                                    var currency = "€".equals(valParts[1]) ? "EUR" : "USD";
                                    unit = CurrencyUnits.getInstance().getUnit(currency);
                                    if (unit == null) {
                                        logger.trace("Currency {} is unknown, falling back to DecimalType", currency);
                                        state = new DecimalType(bd);
                                        channelType = "Number:Dimensionless";
                                    } else {
                                        state = new QuantityType<>(bd, unit);
                                        channelType = "Number:" + UnitUtils.getDimensionName(unit);
                                    }
                                } else {
                                    throw new IllegalStateException("BUG: " + unitStr + " is not mapped!");
                                }
                            } else {
                                channelType = unitData.channelType();
                                unit = unitData.unit();
                                state = new QuantityType<>(bd, unit);
                            }
                        } else {
                            channelType = "Number";
                            unit = null;
                            state = new DecimalType(bd);
                        }
                        if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
                            type = Type.READ_ONLY_NUMERIC;
                        } else {
                            ctuid = null;
                            type = Type.NUMERIC_FORM;
                        }
                    } catch (NumberFormatException nfe) {
                        ctuid = null;
                        unit = null;
                        // check for time - 'Time' field
                        String[] valParts = vs.split(":");
                        if (valParts.length == 2) {
                            // convert it to zonedDateTime with today as date and the
                            // default timezone.
                            var zdt = LocalTime.parse(vs, DateTimeFormatter.ofPattern("HH:mm")).atDate(LocalDate.now())
                                    .atZone(ZoneId.systemDefault());
                            state = new DateTimeType(zdt);
                            channelType = "DateTime";
                            type = Type.NUMERIC_FORM;
                            if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                                ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_DATE_TIME_RO_UID;
                                type = Type.READ_ONLY_NUMERIC;
                            }
                        } else {
                            // durations are a set of '000d 00h 00m 00.0s` fields
                            var durMatcher = durationPattern.matcher(vs);
                            if (durMatcher.matches()) {
                                // we have a duration
                                var parts = vs.split(" ");
                                float time = 0;
                                // sum up parts to a time
                                for (var timePart : parts) {
                                    // last char is time unit, part before is time.
                                    // for seconds it could be a fraction;
                                    var pl = timePart.length();
                                    var tu = timePart.charAt(pl - 1);
                                    var tv = Float.parseFloat(timePart.substring(0, pl - 1));

                                    time += switch (tu) {
                                        case 'd' -> tv * 86400; // days - 24h*60m*60s
                                        case 'h' -> tv * 3600; // hours - 60m*60s
                                        case 'm' -> tv * 60; // minutes - 60s
                                        case 's' -> tv; // seconds - pass value
                                        default -> throw new IllegalArgumentException(
                                                "Unexpected time unit " + tu + " in " + vs);
                                    };
                                }
                                state = new QuantityType<>(time, Units.SECOND);
                                channelType = "Number:Time";
                                type = Type.TIME_PERIOD;
                                if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                                    ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
                                    type = Type.READ_ONLY_NUMERIC;
                                }
                            } else {
                                // not a number and not time or duration
                                channelType = "String";
                                state = new StringType(vs);
                                type = Type.STATE_FORM;
                                if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                                    ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
                                    type = Type.READ_ONLY_STATE;
                                }
                            }
                        }
                    }
                }
                break;
            case UNKNOWN:
            case IGNORE:
                return;
            default:
                // should't happen but we have to add default for the compiler...
                return;
        }
        ApiPageEntry e = this.entries.get(shortName);
        boolean isNewEntry;
        if (e == null || e.type != type || !channelType.equals(e.channel.getAcceptedItemType())
                || !Objects.equals(e.unit, unit)) {
            @Nullable
            Channel channel = this.taCmiSchemaHandler.getThing().getChannel(shortName);
            @Nullable
            ChangerX2Entry cx2e = null;
            if (this.fieldType == FieldType.FORM_VALUE) {
                try {
                    URI uri = this.taCmiSchemaHandler.buildUri("INCLUDE/changerx2.cgi?sadrx2=" + address);
                    final ChangerX2Parser pp = this.taCmiSchemaHandler.parsePage(uri, new ChangerX2Parser(shortName));
                    cx2e = pp.getParsedEntry();
                } catch (final ParseException | RuntimeException ex) {
                    logger.warn("Error parsing API Scheme: {} ", ex.getMessage(), ex);
                } catch (final TimeoutException | InterruptedException | ExecutionException ex) {
                    logger.warn("Error loading API Scheme: {} ", ex.getMessage());
                }
                if (cx2e == null) {
                    // switch channel to readOnly
                    this.fieldType = FieldType.READ_ONLY;
                    if (type == Type.NUMERIC_FORM || type == Type.TIME_PERIOD) {
                        if ("DateTime".equals(channelType)) {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_DATE_TIME_RO_UID;
                        } else {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
                        }
                        type = Type.READ_ONLY_NUMERIC;
                    } else {
                        ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
                        type = Type.READ_ONLY_STATE;
                    }

                }
            }
            if (e != null && !channelType.equals(e.channel.getAcceptedItemType())) {
                // channel type has changed. we have to rebuild the channel.
                this.channels.remove(channel);
                channel = null;
            }
            if (channel != null && ctuid == null && cx2e != null) {
                // custom channel type - check if it already exists and recreate when needed...
                ChannelTypeUID curCtuid = channel.getChannelTypeUID();
                if (curCtuid == null) {
                    // we have to re-create and re-register the channel uuid
                    logger.debug("Re-Registering channel type UUID for: {} ", shortName);
                    var ct = buildAndRegisterChannelType(shortName, type, cx2e);
                    var channelBuilder = ChannelBuilder.create(channel);
                    channelBuilder.withType(ct.getUID());
                    channel = channelBuilder.build(); // update channel
                } else {
                    // check if channel uuid still exists and re-carete when needed
                    ChannelType ct = channelTypeProvider.getChannelType(curCtuid, null);
                    if (ct == null) {
                        buildAndRegisterChannelType(shortName, type, cx2e);
                    }
                }
            } else if (channel == null || !Objects.equals(ctuid, channel.getChannelTypeUID())) {
                logger.debug("Creating / updating channel {} of type {} for '{}'", shortName, channelType, description);
                this.configChanged = true;
                ChannelUID channelUID = new ChannelUID(this.taCmiSchemaHandler.getThing().getUID(), shortName);
                ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, channelType);
                channelBuilder.withLabel(description);
                if (ctuid != null) {
                    channelBuilder.withType(ctuid);
                } else if (cx2e != null) {
                    ChannelType ct = buildAndRegisterChannelType(shortName, type, cx2e);

                    channelBuilder.withType(ct.getUID());
                } else {
                    logger.warn("Error configurating channel for {}: channeltype cannot be determined!", shortName);
                }
                channel = channelBuilder.build(); // add configuration property...
            }
            this.configChanged = true;
            e = new ApiPageEntry(type, channel, unit, address, cx2e, state);
            this.entries.put(shortName, e);
            isNewEntry = true;
        } else {
            isNewEntry = false;
        }
        this.channels.add(e.channel);
        // only update the state when there was no state change sent to C.M.I. after we started
        // polling the state. It might deliver the previous / old state.
        if (e.getLastCommandTS() < this.statusRequestStartTS) {
            Number updatePolicyI = (Number) e.channel.getConfiguration().get("updatePolicy");
            int updatePolicy = updatePolicyI == null ? 0 : updatePolicyI.intValue();
            switch (updatePolicy) {
                case 0: // 'default'
                default:
                    // we do 'On-Fetch' update when channel is changeable, otherwise 'On-Change'
                    switch (e.type) {
                        case NUMERIC_FORM:
                        case TIME_PERIOD:
                        case STATE_FORM:
                        case SWITCH_BUTTON:
                        case SWITCH_FORM:
                            if (isNewEntry || !state.equals(e.getLastState())) {
                                e.setLastState(state);
                                this.taCmiSchemaHandler.updateState(e.channel.getUID(), state);
                            }
                            break;
                        case READ_ONLY_NUMERIC:
                        case READ_ONLY_STATE:
                        case READ_ONLY_SWITCH:
                            e.setLastState(state);
                            this.taCmiSchemaHandler.updateState(e.channel.getUID(), state);
                            break;
                    }
                    break;
                case 1: // On-Fetch
                    e.setLastState(state);
                    this.taCmiSchemaHandler.updateState(e.channel.getUID(), state);
                    break;
                case 2: // On-Change
                    if (isNewEntry || !state.equals(e.getLastState())) {
                        e.setLastState(state);
                        this.taCmiSchemaHandler.updateState(e.channel.getUID(), state);
                    }
                    break;
            }
        }
    }

    private ChannelType buildAndRegisterChannelType(String shortName, Type type, ChangerX2Entry cx2e) {
        StateDescriptionFragmentBuilder sdb = StateDescriptionFragmentBuilder.create().withReadOnly(type.readOnly);
        String itemType;
        switch (cx2e.optionType) {
            case NUMBER:
                itemType = "Number";
                String min = cx2e.options.get(ChangerX2Entry.NUMBER_MIN);
                if (min != null && !min.trim().isEmpty()) {
                    sdb.withMinimum(new BigDecimal(min));
                }
                String max = cx2e.options.get(ChangerX2Entry.NUMBER_MAX);
                if (max != null && !max.trim().isEmpty()) {
                    sdb.withMaximum(new BigDecimal(max));
                }
                String step = cx2e.options.get(ChangerX2Entry.NUMBER_STEP);
                if (step != null && !step.trim().isEmpty()) {
                    sdb.withStep(new BigDecimal(step));
                }
                break;
            case SELECT:
                itemType = "String";
                for (Entry<String, @Nullable String> entry : cx2e.options.entrySet()) {
                    String val = entry.getValue();
                    if (val != null) {
                        sdb.withOption(new StateOption(val, entry.getKey()));
                    }
                }
                break;
            case TIME:
                if (type == Type.TIME_PERIOD) {
                    itemType = "Number";
                } else {
                    itemType = "DateTime";
                }
                break;
            default:
                throw new IllegalStateException("Unhandled OptionType: " + cx2e.optionType);
        }
        ChannelTypeBuilder<?> ctb = ChannelTypeBuilder
                .state(new ChannelTypeUID(TACmiBindingConstants.BINDING_ID, shortName), shortName, itemType)
                .withDescription("Auto-created for " + shortName).withStateDescriptionFragment(sdb.build());

        // add config description URI
        URI cdu = configDescriptionUriAPISchemaDefaults;
        if (cdu != null) {
            ctb = ctb.withConfigDescriptionURI(cdu);
        }

        ChannelType ct = ctb.build();
        channelTypeProvider.addChannelType(ct);
        return ct;
    }

    protected boolean isConfigChanged() {
        return this.configChanged;
    }

    protected List<Channel> getChannels() {
        return channels;
    }
}
