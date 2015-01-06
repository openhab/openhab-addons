/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.discovery.GroupAddressDiscoveryService;
import org.openhab.binding.knx.discovery.IndividualAddressDiscoveryService;
import org.openhab.binding.knx.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;
import org.openhab.binding.knx.handler.SerialBridgeThingHandler;
import org.openhab.binding.knx.handler.physical.DimmerThingHandler;
import org.openhab.binding.knx.handler.physical.EnergySwitchThingHandler;
import org.openhab.binding.knx.handler.physical.GenericThingHandler;
import org.openhab.binding.knx.handler.physical.GroupAddressThingHandler;
import org.openhab.binding.knx.handler.physical.RollerShutterSwitchThingHandler;
import org.openhab.binding.knx.handler.physical.RollerShutterThingHandler;
import org.openhab.binding.knx.handler.physical.SwitchThingHandler;
import org.openhab.binding.knx.handler.physical.ThermostatThingHandler;
import org.openhab.binding.knx.handler.virtual.VirtualSwitchThingHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Lists;

/**
 * The {@link KNXHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KNXHandlerFactory extends BaseThingHandlerFactory implements AutoUpdateBindingConfigProvider {

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            KNXBindingConstants.THING_TYPE_GROUPADDRESS, KNXBindingConstants.THING_TYPE_GENERIC,
            KNXBindingConstants.THING_TYPE_SWITCH, KNXBindingConstants.THING_TYPE_THERMOSTAT,
            KNXBindingConstants.THING_TYPE_ENERGY_SWITCH, KNXBindingConstants.THING_TYPE_DIMMER,
            KNXBindingConstants.THING_TYPE_ROLLERSHUTTER, KNXBindingConstants.THING_TYPE_IP_BRIDGE,
            KNXBindingConstants.THING_TYPE_SERIAL_BRIDGE, KNXBindingConstants.THING_TYPE_ROLLERSHUTTERSWITCH,
            KNXBindingConstants.THING_TYPE_VIRTUALSWITCH);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    protected ItemRegistry itemRegistry;
    protected ItemChannelLinkRegistry itemChannelLinkRegistry;

    protected void setItemRegistry(ItemRegistry registry) {
        itemRegistry = registry;
    }

    protected void unsetItemRegistry(ItemRegistry registry) {
        itemRegistry = null;
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry registry) {
        itemChannelLinkRegistry = registry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry registry) {
        itemChannelLinkRegistry = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (KNXBindingConstants.THING_TYPE_IP_BRIDGE.equals(thingTypeUID)) {
            ThingUID IPBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, IPBridgeUID, null);
        }
        if (KNXBindingConstants.THING_TYPE_SERIAL_BRIDGE.equals(thingTypeUID)) {
            ThingUID serialBridgeUID = getSerialBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, serialBridgeUID, null);
        }
        if (KNXBindingConstants.THING_TYPE_GROUPADDRESS.equals(thingTypeUID)) {
            ThingUID gaUID = getGAThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, gaUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_GENERIC.equals(thingTypeUID)) {
            ThingUID gaUID = getGenericThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, gaUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_SWITCH.equals(thingTypeUID)) {
            ThingUID switchUID = getSwitchThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, switchUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            ThingUID thermostatUID = getThermostatThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, thermostatUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_ENERGY_SWITCH.equals(thingTypeUID)) {
            ThingUID switchUID = getSwitchThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, switchUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_DIMMER.equals(thingTypeUID)) {
            ThingUID dimmerUID = getDimmerThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dimmerUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_ROLLERSHUTTER.equals(thingTypeUID)) {
            ThingUID rollerShutterUID = getRollerShutterThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, rollerShutterUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_ROLLERSHUTTERSWITCH.equals(thingTypeUID)) {
            ThingUID rollerShutterUID = getRollerShutterThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, rollerShutterUID, bridgeUID);
        }
        if (KNXBindingConstants.THING_TYPE_VIRTUALSWITCH.equals(thingTypeUID)) {
            ThingUID switchUID = getVirtualSwitchThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, switchUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the KNX binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_IP_BRIDGE)) {
            IPBridgeThingHandler handler = new IPBridgeThingHandler(thing, itemChannelLinkRegistry);
            registerGroupAddressDiscoveryService(handler);
            registerIndividualAddressDiscoveryService(handler);
            return handler;
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_SERIAL_BRIDGE)) {
            SerialBridgeThingHandler handler = new SerialBridgeThingHandler(thing, itemChannelLinkRegistry);
            registerGroupAddressDiscoveryService(handler);
            registerIndividualAddressDiscoveryService(handler);
            return handler;
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_GROUPADDRESS)) {
            return new GroupAddressThingHandler(thing, itemChannelLinkRegistry, itemRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_GENERIC)) {
            return new GenericThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_SWITCH)) {
            return new SwitchThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_THERMOSTAT)) {
            return new ThermostatThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_ENERGY_SWITCH)) {
            return new EnergySwitchThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_DIMMER)) {
            return new DimmerThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_ROLLERSHUTTER)) {
            return new RollerShutterThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_ROLLERSHUTTERSWITCH)) {
            return new RollerShutterSwitchThingHandler(thing, itemChannelLinkRegistry);
        } else if (thing.getThingTypeUID().equals(KNXBindingConstants.THING_TYPE_VIRTUALSWITCH)) {
            return new VirtualSwitchThingHandler(thing, itemChannelLinkRegistry);
        } else {
            return null;
        }
    }

    private ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String ipAddress = (String) configuration.get(IPBridgeThingHandler.IP_ADDRESS);
            thingUID = new ThingUID(thingTypeUID, ipAddress);
        }
        return thingUID;
    }

    private ThingUID getSerialBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialPort = (String) configuration.get(SerialBridgeThingHandler.SERIAL_PORT);
            thingUID = new ThingUID(thingTypeUID, serialPort);
        }
        return thingUID;
    }

    private ThingUID getGAThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String address = ((String) configuration.get(GroupAddressThingHandler.GROUP_ADDRESS)).replace("/", "_");

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;
    }

    private ThingUID getGenericThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String address = ((String) configuration.get(PhysicalActorThingHandler.ADDRESS)).replace(".", "_");

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;
    }

    private ThingUID getSwitchThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {

        String address = ((String) configuration.get(SwitchThingHandler.SWITCH_GA));

        if (address == null) {
            address = ((String) configuration.get(SwitchThingHandler.STATUS_GA));
        }

        if (address != null) {
            address = address.replace("/", "_");
        }

        if (thingUID == null && address != null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;
    }

    private ThingUID getThermostatThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {

        String address = ((String) configuration.get(ThermostatThingHandler.SETPOINT_GA));

        if (address == null) {
            address = ((String) configuration.get(ThermostatThingHandler.STATUS_GA));
        }

        if (address != null) {
            address = address.replace("/", "_");
        }

        if (thingUID == null && address != null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;
    }

    private ThingUID getDimmerThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {

        String address = ((String) configuration.get(DimmerThingHandler.SWITCH_GA));

        if (address == null) {
            address = ((String) configuration.get(DimmerThingHandler.INCREASE_DECREASE_GA));
        }

        if (address == null) {
            address = ((String) configuration.get(DimmerThingHandler.POSITION_GA));
        }

        if (address == null) {
            address = ((String) configuration.get(DimmerThingHandler.DIM_VALUE_GA));
        }

        if (address != null) {
            address = address.replace("/", "_");
        }

        if (thingUID == null && address != null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;

    }

    private ThingUID getVirtualSwitchThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {

        String address = ((String) configuration.get(VirtualSwitchThingHandler.ADDRESS));

        if (address != null) {
            address = address.replace(".", "_");
        }

        if (thingUID == null && address != null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;

    }

    private ThingUID getRollerShutterThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {

        String address = ((String) configuration.get(RollerShutterThingHandler.UP_DOWN_GA));

        if (address == null) {
            address = ((String) configuration.get(RollerShutterThingHandler.STOP_MOVE_GA));
        }

        if (address == null) {
            address = ((String) configuration.get(RollerShutterThingHandler.POSITION_GA));
        }

        if (address != null) {
            address = address.replace("/", "_");
        }

        if (thingUID == null && address != null) {
            thingUID = new ThingUID(thingTypeUID, address, bridgeUID.getId());
        }
        return thingUID;
    }

    private synchronized void registerGroupAddressDiscoveryService(KNXBridgeBaseThingHandler bridgeHandler) {
        GroupAddressDiscoveryService discoveryService = new GroupAddressDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void registerIndividualAddressDiscoveryService(KNXBridgeBaseThingHandler bridgeHandler) {
        IndividualAddressDiscoveryService discoveryService = new IndividualAddressDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    public Boolean autoUpdate(String itemName) {
        // The principle we maintain is that is up to KNX devices to emit the actual state of a variable, rather
        // than us auto-updating the channel. Most KNX devices have an Communication Object for both writing/updating a
        // variable, and next to that another Communication Object to read out the state, or the device (T)ransmits the
        // actual state after an update. In other words, implementing classes can either do nothing and wait for a
        // (T)ransmit, or implement an explicit read operation to read out the actual value from the KNX device

        if (itemChannelLinkRegistry != null) {
            Set<ChannelUID> boundChannels = itemChannelLinkRegistry.getBoundChannels(itemName);
            for (ChannelUID channelUID : boundChannels) {
                if (channelUID.getBindingId().equals(KNXBindingConstants.BINDING_ID)) {
                    return false;
                }
            }
        }

        return null;
    }

}
