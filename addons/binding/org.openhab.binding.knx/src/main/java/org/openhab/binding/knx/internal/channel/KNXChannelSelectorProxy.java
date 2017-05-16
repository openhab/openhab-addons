/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Objects;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.internal.dpt.KNXCoreTypeMapper;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * The {@link KNXChannelSelectorProxy} class is a helper class to instantiate
 * and parameterize the {@link KNXChannelSelector} Enum
 *
 * @author Karel Goderis - Initial contribution
 */
public class KNXChannelSelectorProxy {

    public enum KNXChannelSelector {

        STATUSSWITCH(CHANNEL_STATUSSWITCH) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                return "1.001";
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, STATUS_GA)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, SWITCH_GA)), Predicates.notNull());
            }
        },
        ENERGY(CHANNEL_ENERGY) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                String unit = (String) configuration.get(UNIT);
                switch (unit) {
                    case "Wh":
                        return "13.010";
                    case "kWh":
                        return "13.013";
                }
                return null;
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, ENERGY_GA)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) {
                return Sets.newHashSet();
            }
        },
        CURRENT(CHANNEL_CURRENT) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                return "7.012";
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, CURRENT_GA)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) {
                return Sets.newHashSet();
            }
        },
        OPERATINGHOURS(CHANNEL_OPERATING_HOURS) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                return "7.001";
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, OPERATING_HOURS_GA)),
                        Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) {
                return Sets.newHashSet();
            }
        },
        DIMMER(CHANNEL_DIMMER) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                if (Objects.equals(getAddress(configuration, SWITCH_GA), groupAddress)) {
                    return "1.001";
                }
                if (Objects.equals(getAddress(configuration, STATUS_GA), groupAddress)) {
                    return "1.001";
                }
                if (Objects.equals(getAddress(configuration, POSITION_GA), groupAddress)) {
                    return "5.001";
                }
                if (Objects.equals(getAddress(configuration, POSITION_STATUS_GA), groupAddress)) {
                    return "5.001";
                }
                if (Objects.equals(getAddress(configuration, INCREASE_DECREASE_GA), groupAddress)) {
                    return "3.007";
                }
                return null;
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, STATUS_GA),
                        getAddress(configuration, POSITION_STATUS_GA)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                if (type == null) {
                    return Sets.filter(Sets.newHashSet(getAddress(configuration, SWITCH_GA),
                            getAddress(configuration, INCREASE_DECREASE_GA), getAddress(configuration, POSITION_GA)),
                            Predicates.notNull());
                } else {
                    if (type instanceof OnOffType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, SWITCH_GA)), Predicates.notNull());
                    }
                    if (type instanceof PercentType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, POSITION_GA)),
                                Predicates.notNull());
                    }
                    if (type instanceof IncreaseDecreaseType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, INCREASE_DECREASE_GA)),
                                Predicates.notNull());
                    }
                }
                return Sets.newHashSet();
            }

            @Override
            public Type convertType(KNXChannelSelectorProxy knxChannelSelectorProxy, Configuration configuration,
                    Type type) {
                if (type instanceof OnOffType) {
                    if (configuration.get(SWITCH_GA) != null) {
                        return type;
                    } else if (configuration.get(POSITION_GA) != null) {
                        return ((OnOffType) type).as(PercentType.class);
                    }
                }

                if (type instanceof PercentType) {
                    if (configuration.get(POSITION_GA) != null) {
                        return type;
                    } else if (configuration.get(SWITCH_GA) != null) {
                        return ((PercentType) type).as(OnOffType.class);
                    }
                }

                return type;
            }
        },
        ROLLERSHUTTER(CHANNEL_ROLLERSHUTTER) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                if (Objects.equals(getAddress(configuration, UP_DOWN_GA), groupAddress)) {
                    return "1.008";
                }
                if (Objects.equals(getAddress(configuration, UP_DOWN_STATUS_GA), groupAddress)) {
                    return "1.008";
                }
                if (Objects.equals(getAddress(configuration, STOP_MOVE_GA), groupAddress)) {
                    return "1.010";
                }
                if (Objects.equals(getAddress(configuration, STOP_MOVE_STATUS_GA), groupAddress)) {
                    return "1.010";
                }
                if (Objects.equals(getAddress(configuration, POSITION_GA), groupAddress)) {
                    return "5.001";
                }
                if (Objects.equals(getAddress(configuration, POSITION_STATUS_GA), groupAddress)) {
                    return "5.001";
                }
                return null;
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, UP_DOWN_STATUS_GA),
                        getAddress(configuration, STOP_MOVE_STATUS_GA), getAddress(configuration, POSITION_STATUS_GA)),
                        Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy selector, Configuration configuration,
                    Type type) throws KNXFormatException {
                if (type == null) {
                    return Sets.filter(
                            Sets.newHashSet(getAddress(configuration, UP_DOWN_GA),
                                    getAddress(configuration, STOP_MOVE_GA), getAddress(configuration, POSITION_GA)),
                            Predicates.notNull());
                } else {
                    if (type instanceof UpDownType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, UP_DOWN_GA)),
                                Predicates.notNull());
                    }
                    if (type instanceof PercentType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, POSITION_GA)),
                                Predicates.notNull());
                    }
                    if (type instanceof StopMoveType) {
                        return Sets.filter(Sets.newHashSet(getAddress(configuration, STOP_MOVE_GA)),
                                Predicates.notNull());
                    }
                }
                return Sets.newHashSet();
            }

            @Override
            public Type convertType(KNXChannelSelectorProxy knxChannelSelectorProxy, Configuration configuration,
                    Type type) {
                if (type instanceof UpDownType) {
                    if (configuration.get(UP_DOWN_GA) != null) {
                        return type;
                    } else if (configuration.get(POSITION_GA) != null) {
                        return ((UpDownType) type).as(PercentType.class);
                    }
                }

                if (type instanceof PercentType) {
                    if (configuration.get(POSITION_GA) != null) {
                        return type;
                    } else if (configuration.get(UP_DOWN_GA) != null) {
                        return ((PercentType) type).as(UpDownType.class);
                    }
                }

                return type;
            }
        },
        SETPOINT(CHANNEL_SETPOINT) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                return "9.001";
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, STATUS_GA)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, SETPOINT_GA)), Predicates.notNull());
            }
        },
        NUMBER(CHANNEL_NUMBER) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {

                // TODO : decide whether it is opportunistic to check the compatbility between the choosen DPT and
                // the supposedly Number Item linked to this channel. The alternative would be to forego this
                // check and let it up to the ThingManager to do this and log an error

                // Class<?> classType = KNXCoreTypeMapper.toTypeClass((String) configuration.get(DPT));
                // NumberItem someNumber = new NumberItem("");
                // if (!someNumber.getAcceptedDataTypes().contains(classType)) {
                // logger.warn("The DPT '{}' yields a '{}', which is not compatible with Number Items", (String)
                // configuration.get(DPT),classType.getSimpleName());
                // return null;
                // }

                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }
        },
        STRING(CHANNEL_STRING) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {

                // TODO : see NUMBER element of this Enum
                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }
        },
        CONTACT(CHANNEL_CONTACT) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {

                // TODO : see NUMBER element of this Enum
                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }
        },
        SWITCH(CHANNEL_SWITCH) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {

                // TODO : see NUMBER element of this Enum
                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }
        },
        DATETIME(CHANNEL_DATETIME) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {

                // TODO : see NUMBER element of this Enum
                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
            }
        },
        GENERIC(CHANNEL_GENERIC) {
            @Override
            public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) {
                return (String) configuration.get(DPT);
            }

            @Override
            public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                // Generic channels have a READ configuration attribute, as we can get that from the knxproj file
                if ((boolean) configuration.get(READ)) {
                    return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
                }

                return Sets.newHashSet();
            }

            @Override
            public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                // Generic channels have a WRITE configuration attribute, as we can get that from the knxproj file
                if ((boolean) configuration.get(WRITE)) {
                    return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
                }

                return Sets.newHashSet();
            }

            @Override
            public Set<GroupAddress> getTransmitAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                // Generic channels have a TRANSMIT configuration attribute, as we can get that from the knxproj file
                if ((boolean) configuration.get(TRANSMIT)) {
                    return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
                }

                return Sets.newHashSet();
            }

            @Override
            public Set<GroupAddress> getUpdateAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                    Type type) throws KNXFormatException {
                // Generic channels have a TRANSMIT configuration attribute, as we can get that from the knxproj file
                if ((boolean) configuration.get(UPDATE)) {
                    return Sets.filter(Sets.newHashSet(getAddress(configuration, GROUPADDRESS)), Predicates.notNull());
                }

                return Sets.newHashSet();
            }

            @Override
            public Type convertType(KNXChannelSelectorProxy knxChannelSelectorProxy, Configuration configuration,
                    Type type) {
                Class<? extends Type> convertedType = KNXCoreTypeMapper.toTypeClass((String) configuration.get(DPT));
                if (convertedType == type.getClass()) {
                    return type;
                } else {
                    return null;
                }
            }
        };

        private final String channelTypeID;

        private KNXChannelSelector(String channelTypeID) {
            this.channelTypeID = channelTypeID;
        }

        @Override
        public String toString() {
            return channelTypeID;
        }

        public String getChannelID() {
            return channelTypeID;
        }

        public String getDPT(GroupAddress groupAddress, KNXChannelSelectorProxy proxy, Configuration configuration,
                Type type) throws KNXFormatException {
            return null;
        }

        public Set<GroupAddress> getReadAddresses(KNXChannelSelectorProxy proxy, Configuration configuration, Type type)
                throws KNXFormatException {
            return Sets.newHashSet();
        }

        public Set<GroupAddress> getWriteAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                Type type) throws KNXFormatException {
            return Sets.newHashSet();
        }

        public Set<GroupAddress> getTransmitAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                Type type) throws KNXFormatException {
            return Sets.newHashSet();
        }

        public Set<GroupAddress> getUpdateAddresses(KNXChannelSelectorProxy proxy, Configuration configuration,
                Type type) throws KNXFormatException {
            return Sets.newHashSet();
        }

        public static KNXChannelSelector getValueSelectorFromChannelTypeId(String channelTypeID)
                throws IllegalArgumentException {

            for (KNXChannelSelector c : KNXChannelSelector.values()) {
                if (c.channelTypeID.equals(channelTypeID)) {
                    return c;
                }
            }

            throw new IllegalArgumentException(channelTypeID + " is not a valid value selector");
        }

        public Type convertType(KNXChannelSelectorProxy knxChannelSelectorProxy, Configuration configuration,
                Type type) {
            return type;
        }

        private static GroupAddress getAddress(Configuration configuration, String address) throws KNXFormatException {
            if (configuration != null && configuration.get(address) != null) {
                return new GroupAddress((String) configuration.get(address));
            }
            return null;
        }

    }

    public String getDPT(GroupAddress groupAddress, KNXChannelSelector selector, Configuration configuration, Type type)
            throws KNXFormatException {
        return selector.getDPT(groupAddress, this, configuration, type);
    }

    public Set<GroupAddress> getReadAddresses(KNXChannelSelector selector, Configuration configuration, Type type)
            throws KNXFormatException {
        return selector.getReadAddresses(this, configuration, type);
    }

    public Set<GroupAddress> getWriteAddresses(KNXChannelSelector selector, Configuration configuration, Type type)
            throws KNXFormatException {
        return selector.getWriteAddresses(this, configuration, type);
    }

    public Set<GroupAddress> getTransmitAddresses(KNXChannelSelector selector, Configuration configuration, Type type)
            throws KNXFormatException {
        return selector.getTransmitAddresses(this, configuration, type);
    }

    public Set<GroupAddress> getUpdateAddresses(KNXChannelSelector selector, Configuration configuration, Type type)
            throws KNXFormatException {
        return selector.getUpdateAddresses(this, configuration, type);
    }

    public Type convertType(KNXChannelSelector selector, Configuration configuration, Type type) {
        return selector.convertType(this, configuration, type);
    }

}
