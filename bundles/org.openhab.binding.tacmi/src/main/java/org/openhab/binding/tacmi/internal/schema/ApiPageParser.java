/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.attoparser.ParseException;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.binding.tacmi.internal.TACmiChannelTypeProvider;
import org.openhab.binding.tacmi.internal.schema.ApiPageEntry.Type;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
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
                    int lids = sb.lastIndexOf(":");
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
        State state;
        String channelType;
        ChannelTypeUID ctuid;
        switch (this.fieldType) {
            case BUTTON:
                type = Type.SWITCH_BUTTON;
                state = this.buttonValue == ButtonValue.ON ? OnOffType.ON : OnOffType.OFF;
                ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_SWITCH_RW_UID;
                channelType = "Switch";
                break;
            case READ_ONLY:
            case FORM_VALUE:
                String vs = (String) value;
                boolean isOn = "ON".equals(vs) || "EIN".equals(vs); // C.M.I. mixes up languages...
                if (isOn || "OFF".equals(vs) || "AUS".equals(vs)) {
                    channelType = "Switch";
                    state = isOn ? OnOffType.ON : OnOffType.OFF;
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
                        BigDecimal bd = new BigDecimal(val);
                        if (valParts.length == 2) {
                            if ("°C".equals(valParts[1])) {
                                channelType = "Number:Temperature";
                                state = new QuantityType<>(bd, SIUnits.CELSIUS);
                            } else if ("%".equals(valParts[1])) {
                                // channelType = "Number:Percent"; Number:Percent is currently not handled...
                                channelType = "Number:Dimensionless";
                                state = new QuantityType<>(bd, Units.PERCENT);
                            } else if ("Imp".equals(valParts[1])) {
                                // impulses - no idea how to map this to something useful here?
                                channelType = "Number";
                                state = new DecimalType(bd);
                            } else if ("V".equals(valParts[1])) {
                                channelType = "Number:Voltage";
                                state = new QuantityType<>(bd, Units.VOLT);
                            } else if ("A".equals(valParts[1])) {
                                channelType = "Number:Current";
                                state = new QuantityType<>(bd, Units.AMPERE);
                            } else if ("Hz".equals(valParts[1])) {
                                channelType = "Number:Frequency";
                                state = new QuantityType<>(bd, Units.HERTZ);
                            } else if ("kW".equals(valParts[1])) {
                                channelType = "Number:Power";
                                bd = bd.multiply(new BigDecimal(1000));
                                state = new QuantityType<>(bd, Units.WATT);
                            } else if ("kWh".equals(valParts[1])) {
                                channelType = "Number:Power";
                                bd = bd.multiply(new BigDecimal(1000));
                                state = new QuantityType<>(bd, Units.KILOWATT_HOUR);
                            } else if ("l/h".equals(valParts[1])) {
                                channelType = "Number:Volume";
                                bd = bd.divide(new BigDecimal(60));
                                state = new QuantityType<>(bd, Units.LITRE_PER_MINUTE);
                            } else {
                                channelType = "Number";
                                state = new DecimalType(bd);
                                logger.debug("Unhandled UoM for channel {} of type {} for '{}': {}", shortName,
                                        channelType, description, valParts[1]);
                            }
                        } else {
                            channelType = "Number";
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
                        // not a number...
                        channelType = "String";
                        if (this.fieldType == FieldType.READ_ONLY || this.address == null) {
                            ctuid = TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
                            type = Type.READ_ONLY_STATE;
                        } else {
                            ctuid = null;
                            type = Type.STATE_FORM;
                        }
                        state = new StringType(vs);
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
        if (e == null || e.type != type || !channelType.equals(e.channel.getAcceptedItemType())) {
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
            }
            if (channel == null || !Objects.equals(ctuid, channel.getChannelTypeUID())) {
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
            } else if (ctuid == null && cx2e != null) {
                // custom channel type - check if it already exists and recreate when needed...
                ChannelTypeUID curCtuid = channel.getChannelTypeUID();
                if (curCtuid != null) {
                    ChannelType ct = channelTypeProvider.getChannelType(curCtuid, null);
                    if (ct == null) {
                        buildAndRegisterChannelType(shortName, type, cx2e);
                    }
                }
            }
            this.configChanged = true;
            e = new ApiPageEntry(type, channel, address, cx2e, state);
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
            default:
                throw new IllegalStateException();
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
