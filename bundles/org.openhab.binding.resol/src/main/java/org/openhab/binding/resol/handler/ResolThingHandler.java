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
package org.openhab.binding.resol.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.binding.resol.internal.ResolStateDescriptionOptionProvider;
import org.openhab.binding.resol.internal.providers.ResolChannelTypeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.Specification.PacketFieldSpec;
import de.resol.vbus.Specification.PacketFieldValue;
import de.resol.vbus.SpecificationFile;
import de.resol.vbus.SpecificationFile.Enum;
import de.resol.vbus.SpecificationFile.EnumVariant;
import de.resol.vbus.SpecificationFile.Language;

/**
 * The {@link ResolThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolThingHandler extends ResolBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ResolThingHandler.class);

    private ResolStateDescriptionOptionProvider stateDescriptionProvider;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_GENERAL);

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private static final SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("EEE,HH:mm");

    static {
        synchronized (DATE_FORMAT) {
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        synchronized (TIME_FORMAT) {
            TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        synchronized (WEEK_FORMAT) {
            WEEK_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    public ResolThingHandler(Thing thing, ResolStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* we ignore the commands for now on purpose */
    }

    @Override
    public void initialize() {
        ResolBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            updateStatus(bridgeHandler.getStatus());
        }
    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device.");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler(Bridge bridge) {
        ResolBridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof ResolBridgeHandler resolBridgeHandler) {
            bridgeHandler = resolBridgeHandler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }

    @Override
    protected void packetReceived(Specification spec, Language lang, Packet packet) {
        PacketFieldValue[] pfvs = spec.getPacketFieldValuesForHeaders(new Packet[] { packet });
        for (PacketFieldValue pfv : pfvs) {
            logger.trace("Id: {}, Name: {}, Raw: {}, Text: {}", pfv.getPacketFieldId(), pfv.getName(lang),
                    pfv.getRawValueDouble(), pfv.formatTextValue(null, Locale.getDefault()));

            String channelId = pfv.getName(); // use English name as channel
            channelId = channelId.replace(" [", "-");
            channelId = channelId.replace("]", "");
            channelId = channelId.replace("(", "-");
            channelId = channelId.replace(")", "");
            channelId = channelId.replace(" #", "-");
            channelId = channelId.replaceAll("[^A-Za-z0-9_-]+", "_");

            channelId = channelId.toLowerCase(Locale.ENGLISH);

            ChannelTypeUID channelTypeUID;

            if (pfv.getPacketFieldSpec().getUnit().getUnitId() >= 0) {
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                        pfv.getPacketFieldSpec().getUnit().getUnitCodeText());
            } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.DateTime) {
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "datetime");
            } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.WeekTime) {
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "weektime");
            } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Time) {
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "time");
            } else {
                /* used for enums and the numeric types without unit */
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
            }

            String acceptedItemType;

            Thing thing = getThing();
            switch (pfv.getPacketFieldSpec().getType()) {
                case DateTime:
                    acceptedItemType = "DateTime";
                    break;
                case Number:
                    acceptedItemType = ResolChannelTypeProvider.itemTypeForUnit(pfv.getPacketFieldSpec().getUnit());
                    break;
                case WeekTime:
                case Time:
                default:
                    acceptedItemType = "String";
                    break;
            }
            Channel a = thing.getChannel(channelId);

            if (a == null) {
                /* channel doesn't exit, let's create it */
                ThingBuilder thingBuilder = editThing();
                ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);

                if (pfv.getEnumVariant() != null) {
                    /* create a state option channel */
                    List<StateOption> options = new ArrayList<>();
                    PacketFieldSpec ff = pfv.getPacketFieldSpec();
                    Enum e = ff.getEnum();
                    for (long l : e.getValues()) {
                        EnumVariant v = e.getEnumVariantForValue(l);
                        options.add(new StateOption(Long.toString(l), v.getText(lang)));
                    }

                    stateDescriptionProvider.setStateOptions(channelUID, options);

                    Channel channel = ChannelBuilder.create(channelUID, "Number").withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());
                } else if ("DateTime".equals(acceptedItemType)) {
                    /* a date channel */
                    Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());

                } else if ("String".equals(acceptedItemType)) {
                    /* a string channel */
                    Channel channel = ChannelBuilder.create(channelUID, "String").withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());
                } else if (pfv.getRawValueDouble() != null) {
                    /* a number channel */
                    Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());
                } else {
                    /* a string channel */
                    Channel channel = ChannelBuilder.create(channelUID, "String").withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());
                }
                logger.debug("Creating channel: {}", channelUID);
            }

            if (pfv.getEnumVariant() != null) {
                /* update the enum / State channel */
                this.updateState(channelId, new StringType(Long.toString(pfv.getRawValueLong())));

            } else {
                switch (pfv.getPacketFieldSpec().getType()) {
                    case Number:
                        Double dd = pfv.getRawValueDouble();
                        if (dd != null) {
                            if (!isSpecialValue(dd)) {
                                /* only set the value if no error occurred */

                                String str = pfv.formatText();
                                if (str.endsWith("RH")) {
                                    /* unit %RH for relative humidity is not known in openHAB UoM, so we remove it */
                                    str = str.substring(0, str.length() - 2);
                                }
                                if (str.endsWith("Ω")) {
                                    QuantityType<?> q = new QuantityType<>(dd, Units.OHM);
                                    this.updateState(channelId, q);
                                } else {
                                    try {
                                        QuantityType<?> q = new QuantityType<>(str, Locale
                                                .getDefault()); /* vbus library returns the value in default locale */
                                        this.updateState(channelId, q);
                                    } catch (IllegalArgumentException e) {
                                        logger.debug("unit of '{}' unknown in openHAB", str);
                                        QuantityType<?> q = new QuantityType<>(dd, Units.ONE);
                                        this.updateState(channelId, q);
                                    }
                                }
                            }
                        }
                        /*
                         * else {
                         * field not available in this packet, e. g. old firmware version not (yet) transmitting it
                         * }
                         */
                        break;
                    case Time:
                        synchronized (TIME_FORMAT) {
                            this.updateState(channelId, new StringType(TIME_FORMAT.format(pfv.getRawValueDate())));
                        }
                        break;
                    case WeekTime:
                        synchronized (WEEK_FORMAT) {
                            this.updateState(channelId, new StringType(WEEK_FORMAT.format(pfv.getRawValueDate())));
                        }
                        break;
                    case DateTime:
                        synchronized (DATE_FORMAT) {
                            DateTimeType d = new DateTimeType(DATE_FORMAT.format(pfv.getRawValueDate()));
                            this.updateState(channelId, d);
                        }
                        break;
                    default:
                        Bridge b = getBridge();
                        if (b != null) {
                            ResolBridgeHandler handler = (ResolBridgeHandler) b.getHandler();
                            String value;
                            Locale loc;
                            if (handler != null) {
                                loc = handler.getLocale();
                            } else {
                                loc = Locale.getDefault();
                            }
                            value = pfv.formatTextValue(pfv.getPacketFieldSpec().getUnit(), loc);
                            try {
                                QuantityType<?> q = new QuantityType<>(value, loc);
                                this.updateState(channelId, q);
                            } catch (IllegalArgumentException e) {
                                this.updateState(channelId, new StringType(value));
                                logger.debug("unit of '{}' unknown in openHAB, using string", value);
                            }
                        }
                }
            }
        }
    }

    /* check if the given value is a special one like 888.8 or 999.9 for shortcut or open load on a sensor wire */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    private boolean isSpecialValue(Double dd) {
        if ((Math.abs(dd - 888.8) < 0.1) || (Math.abs(dd - (-888.8)) < 0.1)) {
            /* value out of range */
            return true;
        }
        if (Math.abs(dd - 999.9) < 0.1) {
            /* sensor not reachable */
            return true;
        }
        return false;
    }
}
