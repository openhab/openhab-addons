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
package org.openhab.binding.freeboxos.internal.handler;

<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link AlarmHandler} is responsible for handling everything associated to
 * any Freebox Home Alarm thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AlarmHandler extends HomeNodeHandler {

    public AlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        if (value != null) {
            switch (channelId) {
                case NODE_BATTERY:
                    return DecimalType.valueOf(value);
                case ALARM_PIN:
                    return StringType.valueOf(value);
                case ALARM_SOUND, ALARM_VOLUME:
                    return new QuantityType<>(value + " %");
                case ALARM_TIMEOUT1, ALARM_TIMEOUT2, ALARM_TIMEOUT3:
                    return new QuantityType<>(value + " s");
            }
        }
        return UnDefType.NULL;
=======
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;

=======
import java.math.BigDecimal;
import java.util.Comparator;
>>>>>>> cff27ca Saving work
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EpType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.binding.freeboxos.internal.providers.HomeChannelTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AlarmHandler} is responsible for handling everything associated to
 * any Freebox Home Alarm thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AlarmHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(AlarmHandler.class);

    private static final String CONF_UNIT = "unit";
    private static final String CONF_OUT = "output";

    public AlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        HomeNode node = getManager(HomeManager.class).getHomeNode(getClientId());
        if (node != null) {
            ThingBuilder thingBuilder = editThing();
            node.showEndpoints().stream().filter(ep -> ep.epType() == EpType.SIGNAL).forEach(endPoint -> {
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), endPoint.name());
                Configuration channelConf = new Configuration();
                channelConf.put(ClientConfiguration.ID, endPoint.id());
                String unit = endPoint.ui().unit();
                if (unit != null) {
                    channelConf.put(CONF_UNIT, unit);
                }
                node.showEndpoints().stream()
                        .filter(ep -> ep.epType() == EpType.SLOT && ep.name().equals(endPoint.name())).findFirst()
                        .ifPresent(output -> {
                            channelConf.put(CONF_OUT, output.id());
                        });
                ChannelBuilder builder = ChannelBuilder
                        .create(channelUID,
                                HomeChannelTypeProvider.getAcceptedType(endPoint.valueType(), endPoint.ui()))
                        .withConfiguration(channelConf).withLabel(endPoint.label()).withType(HomeChannelTypeProvider
                                .getChannelType(endPoint.name(), endPoint.valueType(), endPoint.ui()));
                thingBuilder.withChannel(builder.build());
            });
            updateThing(thingBuilder.build());
            properties.put(Thing.PROPERTY_VENDOR, "Free");

            node.showEndpoints().stream().filter(ep -> ep.epType() == EpType.SIGNAL).filter(ep -> ep.refresh() != 0)
                    .min(Comparator.comparing(Endpoint::refresh)).map(Endpoint::refresh).ifPresent(rate -> {
                        Configuration thingConfig = editConfiguration();
                        thingConfig.put(ApiConsumerConfiguration.REFRESH_INTERVAL, Integer.toString(rate / 1000));
                        updateConfiguration(thingConfig);
                    });
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID())).forEach(channel -> {
            Configuration config = channel.getConfiguration();
            String unit = (String) config.get("unit");
            unit = unit != null ? unit : "";
            BigDecimal id = (BigDecimal) config.get(ClientConfiguration.ID);
            State result = UnDefType.UNDEF;
            try {
                EndpointState state = getManager(HomeManager.class).getEndpointsState(getClientId(), id.intValue());
                if (state != null) {
                    switch (state.valueType()) {
                        case STRING:
                            String strValue = state.asString();
                            result = strValue != null ? new StringType(strValue) : UnDefType.NULL;
                            break;
                        case BOOL:
                            Boolean boolValue = state.asBoolean();
                            result = CoreItemFactory.SWITCH.equals(channel.getAcceptedItemType())
                                    ? OnOffType.from(boolValue)
                                    : boolValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                            break;
                        case FLOAT:
                            break;
                        case INT:
                            Integer intValue = state.asInt();
                            if (intValue != null) {
                                if ("%".equals(unit)) {
                                    result = new QuantityType<>(intValue, Units.PERCENT);
                                } else if ("sec".equals(unit)) {
                                    result = new QuantityType<>(intValue, Units.SECOND);
                                }
                            } else {
                                result = UnDefType.NULL;
                            }
                            break;
                        case UNKNOWN:
                            break;
                        case VOID:
                            break;
                        default:
                            break;
                    }
                }
            } catch (FreeboxException ignore) {
            }
            updateState(channel.getUID(), result);
        });
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null) {
            Configuration config = channel.getConfiguration();
            Object output = config.get(CONF_OUT);
            if (output == null) {
                logger.info("The channel {} is read-only. Command ignored", channelId);
                return true;
            }
            String targetType = channel.getAcceptedItemType();
            if (output instanceof BigDecimal outputId) {
                if (targetType.startsWith("Number") && command instanceof QuantityType<?> qtty) {
                    getManager(HomeManager.class).putCommand(getClientId(), outputId.intValue(), qtty.toBigDecimal());
                    return true;
                }
                if (CoreItemFactory.SWITCH.equals(targetType) && command instanceof OnOffType onoff) {
                    getManager(HomeManager.class).putCommand(getClientId(), outputId.intValue(),
                            OnOffType.ON.equals(onoff));
                    return true;
                }
            }
        }
        return super.internalHandleCommand(channelId, command);
>>>>>>> 6340384 Commiting work
    }
}
