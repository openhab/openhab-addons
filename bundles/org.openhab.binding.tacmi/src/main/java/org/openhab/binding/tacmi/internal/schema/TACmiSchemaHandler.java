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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.config.ParseConfiguration.ElementBalancing;
import org.attoparser.config.ParseConfiguration.UniqueRootElementPresence;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.attoparser.simple.ISimpleMarkupParser;
import org.attoparser.simple.SimpleMarkupParser;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tacmi.internal.TACmiChannelTypeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TACmiSchemaHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiSchemaHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TACmiSchemaHandler.class);

    private final HttpClient httpClient;
    private final TACmiChannelTypeProvider channelTypeProvider;
    private final Map<String, ApiPageEntry> entries = new HashMap<>();
    private boolean online;
    private @Nullable String serverBase;
    private @Nullable URI schemaApiPage;
    private @Nullable String authHeader;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private final ParseConfiguration noRestrictions;

    // entry of the units lookup cache
    record UnitAndType(Unit<?> unit, String channelType) {
    }

    // this is the units lookup cache.
    protected final Map<String, UnitAndType> unitsCache = new ConcurrentHashMap<>();
    // marks an entry with known un-resolveable unit
    protected final UnitAndType NULL_MARKER = new UnitAndType(Units.ONE, "");
    // marks an entry with special handling - i.e. 'Imp'
    protected final UnitAndType SPECIAL_MARKER = new UnitAndType(Units.ONE, "s");

    public TACmiSchemaHandler(final Thing thing, final HttpClient httpClient,
            final TACmiChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.channelTypeProvider = channelTypeProvider;

        // the default configuration for the parser
        this.noRestrictions = ParseConfiguration.xmlConfiguration();
        this.noRestrictions.setElementBalancing(ElementBalancing.NO_BALANCING);
        this.noRestrictions.setNoUnmatchedCloseElementsRequired(false);
        this.noRestrictions.setUniqueAttributesInElementRequired(false);
        this.noRestrictions.setXmlWellFormedAttributeValuesRequired(false);
        this.noRestrictions.setUniqueRootElementPresence(UniqueRootElementPresence.NOT_VALIDATED);
        this.noRestrictions.getPrologParseConfiguration().setValidateProlog(false);
    }

    @Override
    public void initialize() {
        final TACmiSchemaConfiguration config = getConfigAs(TACmiSchemaConfiguration.class);

        if (config.host.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No host configured!");
            return;
        }
        if (config.username.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No username configured!");
            return;
        }
        if (config.password.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No password configured!");
            return;
        }
        this.online = false;
        updateStatus(ThingStatus.UNKNOWN);

        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((config.username + ":" + config.password).getBytes(StandardCharsets.ISO_8859_1));

        final String serverBase = "http://" + config.host + "/";
        this.serverBase = serverBase;
        this.schemaApiPage = buildUri("schematic_files/" + config.schemaId + ".cgi");

        refreshData();
        if (config.pollInterval <= 0) {
            config.pollInterval = 10;
        }
        // we want to trigger the initial refresh 'at once'
        this.scheduledFuture = scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.pollInterval,
                TimeUnit.SECONDS);
    }

    protected URI buildUri(String path) {
        return URI.create(serverBase + path);
    }

    private Request prepareRequest(final URI uri) {
        final Request req = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(10000, TimeUnit.MILLISECONDS);
        req.header(HttpHeader.ACCEPT_LANGUAGE, "en"); // we want the on/off states in english
        final String ah = this.authHeader;
        if (ah != null) {
            req.header(HttpHeader.AUTHORIZATION, ah);
        }
        return req;
    }

    protected <PP extends AbstractSimpleMarkupHandler> PP parsePage(URI uri, PP pp)
            throws ParseException, InterruptedException, TimeoutException, ExecutionException {
        final ContentResponse response = prepareRequest(uri).send();

        String responseString = null;
        String encoding = response.getEncoding();
        if (encoding == null || encoding.trim().isEmpty()) {
            // the C.M.I. dosn't sometime return a valid encoding - but it defaults to UTF-8 instead of ISO...
            responseString = new String(response.getContent(), StandardCharsets.UTF_8);
        } else {
            responseString = response.getContentAsString();
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Response body was: {} ", responseString);
        }

        final ISimpleMarkupParser parser = new SimpleMarkupParser(this.noRestrictions);
        parser.parse(responseString, pp);
        return pp;
    }

    private void refreshData() {
        URI schemaApiPage = this.schemaApiPage;
        if (schemaApiPage == null) {
            return;
        }
        try {
            final ApiPageParser pp = parsePage(schemaApiPage,
                    new ApiPageParser(this, entries, this.channelTypeProvider));

            final List<Channel> channels = pp.getChannels();
            if (pp.isConfigChanged() || channels.size() != this.getThing().getChannels().size()) {
                // we have to update our channels...
                final ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(channels);
                updateThing(thingBuilder.build());
            }
            if (!this.online) {
                updateStatus(ThingStatus.ONLINE);
                this.online = true;
            }
        } catch (final InterruptedException e) {
            // binding shutdown is in progress
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
            this.online = false;
        } catch (final ParseException | RuntimeException e) {
            logger.debug("Error parsing API Scheme: {} ", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Error: " + e.getMessage());
            this.online = false;
        } catch (final TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error: " + e.getMessage());
            this.online = false;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final ApiPageEntry e = this.entries.get(channelUID.getId());
        if (command instanceof RefreshType) {
            if (e == null) {
                // This might be a race condition between the 'initial' poll / fetch not finished yet or the channel
                // might have been deleted in between. When the initial poll is still in progress, it will send an
                // update for the channel as soon as we have the data. If the channel got deleted, there is nothing we
                // can do.
                return;
            }
            // we have our ApiPageEntry which also holds our last known state - just update it.
            updateState(channelUID, e.getLastState());
            return;
        }
        if (e == null) {
            logger.debug("Got command for unknown channel {}: {}", channelUID, command);
            return;
        }
        final Request reqUpdate;
        switch (e.type) {
            case SWITCH_BUTTON:
                reqUpdate = prepareRequest(buildUri("INCLUDE/change.cgi?changeadrx2=" + e.address + "&changetox2="
                        + (command == OnOffType.ON ? "1" : "0")));
                reqUpdate.header(HttpHeader.REFERER, this.serverBase + "schema.html"); // required...
                break;
            case SWITCH_FORM:
                ChangerX2Entry cx2e = e.changerX2Entry;
                if (cx2e != null) {
                    reqUpdate = prepareRequest(buildUri("INCLUDE/change.cgi?changeadrx2=" + cx2e.address
                            + "&changetox2=" + (command == OnOffType.ON ? "1" : "0")));
                    reqUpdate.header(HttpHeader.REFERER, this.serverBase + "schema.html"); // required...
                } else {
                    logger.debug("Got command for uninitalized channel {}: {}", channelUID, command);
                    return;
                }
                break;
            case STATE_FORM:
                ChangerX2Entry cx2sf = e.changerX2Entry;
                if (cx2sf != null) {
                    String val = cx2sf.options.get(((StringType) command).toFullString());
                    if (val != null) {
                        reqUpdate = prepareRequest(
                                buildUri("INCLUDE/change.cgi?changeadrx2=" + cx2sf.address + "&changetox2=" + val));
                        reqUpdate.header(HttpHeader.REFERER, this.serverBase + "schema.html"); // required...
                    } else {
                        logger.warn("Got unknown form command {} for channel {}; Valid commands are: {}", command,
                                channelUID, cx2sf.options.keySet());
                        return;
                    }
                } else {
                    logger.debug("Got command for uninitalized channel {}: {}", channelUID, command);
                    return;
                }
                break;
            case NUMERIC_FORM:
                ChangerX2Entry cx2en = e.changerX2Entry;
                if (cx2en != null) {
                    String val;
                    if (command instanceof QuantityType qt) {
                        float value;
                        var taUnit = e.unit;
                        if (taUnit != null) {
                            // we try to convert to the unit TA expects for this channel
                            @SuppressWarnings("unchecked")
                            @Nullable
                            QuantityType<?> qtConverted = qt.toUnit(taUnit);
                            if (qtConverted == null) {
                                logger.debug("Faild to convert unit {} to unit {} for command on channel {}",
                                        qt.getUnit(), taUnit, channelUID);
                                value = qt.floatValue();
                            } else {
                                value = qtConverted.floatValue();
                            }

                        } else {
                            // send raw value when there is no unit for this channel
                            value = qt.floatValue();
                        }
                        val = String.format(Locale.US, "%.2f", value);
                    } else if (command instanceof Number qt) {
                        val = String.format(Locale.US, "%.2f", qt.floatValue());
                    } else if (command instanceof DateTimeType dtt) {
                        // time is transferred as minutes since midnight...
                        var zdt = dtt.getZonedDateTime();
                        val = Integer.toString(zdt.getHour() * 60 + zdt.getMinute());
                    } else {
                        val = command.format("%.2f");
                    }
                    reqUpdate = prepareRequest(
                            buildUri("INCLUDE/change.cgi?changeadrx2=" + cx2en.address + "&changetox2=" + val));
                    reqUpdate.header(HttpHeader.REFERER, this.serverBase + "schema.html"); // required...
                } else {
                    logger.debug("Got command for uninitalized channel {}: {}", channelUID, command);
                    return;
                }
                break;
            case TIME_PERIOD:
                ChangerX2Entry cx2enTime = e.changerX2Entry;
                if (cx2enTime != null) {
                    long timeValMSec;
                    if (command instanceof QuantityType qt) {
                        @SuppressWarnings("unchecked")
                        QuantityType<?> seconds = qt.toUnit(MetricPrefix.MILLI(Units.SECOND));
                        if (seconds != null) {
                            timeValMSec = seconds.longValue();
                        } else {
                            // fallback - assume we have a time in milliseconds
                            timeValMSec = qt.longValue();
                        }
                    } else if (command instanceof Number qt) {
                        // fallback - assume we have a time in milliseconds
                        timeValMSec = qt.longValue();
                    } else {
                        throw new IllegalArgumentException(
                                "Command " + command + " cannot be converted to a proper Timespan!");
                    }
                    String val;
                    // TA has three different time periods. One is based on full seconds, the second on tenths of
                    // seconds and the third on minutes. We decide on the basis of the form fields provided during the
                    // ChangerX2 scan.
                    String parts = cx2enTime.options.get(ChangerX2Entry.TIME_PERIOD_PARTS);
                    if (parts == null || parts.indexOf('z') >= 0) {
                        // tenths of seconds
                        val = String.format(Locale.US, "%.1f", timeValMSec / 1000d);
                    } else if (parts.indexOf('s') >= 0) {
                        // seconds
                        val = String.format(Locale.US, "%d", timeValMSec / 1000);
                    } else {
                        // minutes
                        val = String.format(Locale.US, "%d", timeValMSec / 60000);
                    }
                    reqUpdate = prepareRequest(
                            buildUri("INCLUDE/change.cgi?changeadrx2=" + cx2enTime.address + "&changetox2=" + val));
                    reqUpdate.header(HttpHeader.REFERER, this.serverBase + "schema.html"); // required...
                } else {
                    logger.debug("Got command for uninitalized channel {}: {}", channelUID, command);
                    return;
                }
                break;
            case READ_ONLY_NUMERIC:
            case READ_ONLY_STATE:
            case READ_ONLY_SWITCH:
                logger.debug("Got command for ReadOnly channel {}: {}", channelUID, command);
                return;
            default:
                logger.debug("Got command for unhandled type {} channel {}: {}", e.type, channelUID, command);
                return;
        }
        try {
            e.setLastCommandTS(System.currentTimeMillis());
            ContentResponse res = reqUpdate.send();
            if (res.getStatus() == 200) {
                // update ok, we update the state
                e.setLastState((State) command);
                updateState(channelUID, (State) command);
            } else {
                logger.warn("Error sending update for {} = {}: {} {}", channelUID, command, res.getStatus(),
                        res.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            logger.warn("Error sending update for {} = {}: {}", channelUID, command, ex.getMessage());
        }
    }

    // make it accessible for ApiPageParser
    @Override
    protected void updateState(final ChannelUID channelUID, final State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            this.scheduledFuture = null;
        }
        super.dispose();
    }
}
