/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.binding.resol.internal.ResolStateDescriptionOptionProvider;
import org.openhab.binding.resol.internal.providers.ResolChannelTypeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
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

    @Nullable
    ResolBridgeHandler bridgeHandler;
    private ResolStateDescriptionOptionProvider stateDescriptionProvider;

    private SimpleDateFormat dateFormat = new SimpleDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_GENERAL);

    public ResolThingHandler(Thing thing, ResolStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.stateDescriptionProvider = stateDescriptionProvider;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* we ignore the commands for now on purpose */
    }

    @Override
    public void initialize() {
        bridgeHandler = getBridgeHandler();
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
        if (handler instanceof ResolBridgeHandler) {
            bridgeHandler = (ResolBridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }

    public void setChannelValue(String channelId, String value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Channel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
        } else if (!"String".contentEquals(Objects.requireNonNullElse(channel.getAcceptedItemType(), ""))) {
            logger.trace("Channel '{}:{}' expected to have a String type for parameters '{}'",
                    getThing().getUID().getId(), channelId, value.toString());
        } else {
            this.updateState(channelId, new StringType(value));
        }
    }

    public void setChannelValue(String channelId, long value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Channel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
        } else if (!"Number".contentEquals(Objects.requireNonNullElse(channel.getAcceptedItemType(), ""))) {
            logger.trace("Channel '{}:{}' expected to have a String type for parameters '{}'",
                    getThing().getUID().getId(), channelId, value);
        } else {
            this.updateState(channelId, new StringType(Long.toString(value)));
        }
    }

    public void setChannelValue(String channelId, Date value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Channel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
        } else if (!"DateTime".equals(channel.getAcceptedItemType())) {
            logger.trace("Channel '{}:{}' expected to have a DateTime type for parameters '{}'",
                    getThing().getUID().getId(), channelId, value.toString());
        } else {
            this.updateState(channelId, new DateTimeType(dateFormat.format(value)));
        }
    }

    public void setChannelValue(String channelId, double value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Channel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
            return;
        }

        String itmType = channel.getAcceptedItemType();
        if (itmType != null && itmType.startsWith("Number")) {
            this.updateState(channelId, new DecimalType(value));
        } else {
            logger.warn("ItemType '{}' for channel '{}' not matching parameter type double",
                    channel.getAcceptedItemType(), channelId);
        }
    }

    @Override
    public void packetReceived(Specification spec, Language lang, Packet packet) {
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
                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "DateTime");
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
                case WeekTime:
                case Number:
                    acceptedItemType = ResolChannelTypeProvider.itemTypeForUnit(pfv.getPacketFieldSpec().getUnit());
                    break;
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
                } else if (pfv.getRawValueDouble() != null) {
                    /* a number channel */
                    Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                            .withLabel(pfv.getName(lang)).build();

                    thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                    updateThing(thingBuilder.build());
                }
                logger.debug("Creating channel: {}", channelUID);
            }

            if (pfv.getEnumVariant() != null) {
                /* update the enum / State channel */
                setChannelValue(channelId, pfv.getRawValueLong()); // pfv.getEnumVariant().getText(lang));

            } else {
                switch (pfv.getPacketFieldSpec().getType()) {
                    case Number:
                        Double dd = pfv.getRawValueDouble();
                        if (dd != null) {
                            if (!isSpecialValue(dd)) {
                                /* only set the value if no error occurred */
                                setChannelValue(channelId, dd.doubleValue());
                            }
                        } else {
                            /*
                             * field not available in this packet, e. g. old firmware version
                             * not (yet) transmitting it
                             */
                        }
                        break;
                    case DateTime:
                        setChannelValue(channelId, pfv.getRawValueDate());
                        break;
                    case WeekTime:
                    case Time:
                    default:
                        Bridge b = getBridge();
                        if (b != null) {
                            setChannelValue(channelId, pfv.formatTextValue(pfv.getPacketFieldSpec().getUnit(),
                                    ((ResolBridgeHandler) b).getLocale()));
                        }
                }
            }
        }
    }

    /* check if the given value is a special one like 888.8 or 999.9 for shortcut or open load on a sensor wire */
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
