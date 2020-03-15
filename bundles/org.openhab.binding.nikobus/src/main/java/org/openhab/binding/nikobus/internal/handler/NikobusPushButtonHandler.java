/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.handler;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;
import static org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.AbstractUID;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikobus.internal.NikobusBindingConstants;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusPushButtonHandler} is responsible for handling Nikobus push
 * buttons.
 *
 * @author Boris Krivonog - Initial contribution
 * @author Wouter Denayer - support for module addresses as seen in Niko PC app
 * 
 */
@NonNullByDefault
public class NikobusPushButtonHandler extends NikobusBaseThingHandler {
    @NonNullByDefault
    private static class ImpactedModule {
        private final ThingUID thingUID;
        private final SwitchModuleGroup group;

        ImpactedModule(ThingUID thingUID, SwitchModuleGroup group) {
            this.thingUID = thingUID;
            this.group = group;
        }

        public ThingUID getThingUID() {
            return thingUID;
        }

        public SwitchModuleGroup getGroup() {
            return group;
        }

        @Override
        public String toString() {
            return "'" + thingUID + "'-" + group;
        }
    }

    @NonNullByDefault
    private static class ImpactedModuleUID extends AbstractUID {
        ImpactedModuleUID(String uid) {
            super(uid);
        }

        String getThingTypeId() {
            return getSegment(0);
        }

        String getThingId() {
            return getSegment(1);
        }

        SwitchModuleGroup getGroup() {
            if (getSegment(2).equals("1")) {
                return FIRST;
            }
            if (getSegment(2).equals("2")) {
                return SECOND;
            }
            throw new IllegalArgumentException("Unexpected group found " + getSegment(2));
        }

        @Override
        protected int getMinimalNumberOfSegments() {
            return 3;
        }

    }

    private static final String END_OF_TRANSMISSION = "\r#E1";
    private final Logger logger = LoggerFactory.getLogger(NikobusPushButtonHandler.class);
    private final List<ImpactedModule> impactedModules = Collections.synchronizedList(new ArrayList<>());
    private @Nullable Future<?> requestUpdateFuture;
    private Map<String, Integer> buttonIndexMap;

    public NikobusPushButtonHandler(Thing thing) {
        super(thing);

        buttonIndexMap = Stream
                .of(new Object[][] { { "A", 1 }, { "B", 3 }, { "C", 0 }, { "D", 2 }, { "1A", 5 }, { "1B", 7 },
                        { "1C", 4 }, { "1D", 6 }, { "2A", 1 }, { "2B", 3 }, { "2C", 0 }, { "2D", 2 }, })
                .collect(Collectors.toMap(p -> (String) p[0], p -> (Integer) p[1]));

    }

    @Override
    public void initialize() {
        // not calling super.initialize() here

        address = (String) getConfig().get(NikobusBindingConstants.CONFIG_ADDRESS);
        updateStatus(ThingStatus.UNKNOWN);
        String modulesConfigParameter = CONFIG_IMPACTED_MODULES;

        // the address as seen on the bus remains the default, only if it's missing we
        // look at addressPC
        if (address == null) {
            modulesConfigParameter = CONFIG_IMPACTED_MODULES_PC;

            address = parseNikobusAddress((String) getConfig().get(NikobusBindingConstants.CONFIG_ADDRESS_PC));

            if (address == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Address must be set!");
                return;
            }
        }

        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        impactedModules.clear();

        try {

            ThingUID bridgeUID = thing.getBridgeUID();
            if (bridgeUID == null) {
                throw new IllegalArgumentException("Bridge does not exist!");
            }

            String impactedModulesStringFull = (String) getConfig().get(modulesConfigParameter);
            if (impactedModulesStringFull != null) {

                String[] impactedModulesString = impactedModulesStringFull.split(",");

                for (String impactedModuleString : impactedModulesString) {
                    ImpactedModuleUID impactedModuleUID = new ImpactedModuleUID(impactedModuleString.trim());
                    ThingTypeUID thingTypeUID = new ThingTypeUID(bridgeUID.getBindingId(),
                            impactedModuleUID.getThingTypeId());
                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, impactedModuleUID.getThingId());
                    impactedModules.add(new ImpactedModule(thingUID, impactedModuleUID.getGroup()));
                }

                logger.debug("Impacted modules for {} = {}", thing.getUID(), impactedModules);
            } else {
                logger.debug("Impacted modules for {} = none specified", thing.getUID());
            }

        } catch (RuntimeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.addListener(getAddress(), this::commandReceived);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected String parseNikobusAddress(String address) {
        String addressPC = null;
        String[] addressPCParts = address.split(":"); // e.g. 28092A:1A

        // check if the addressPC is set, as well as the specific button in
        // addressButtonPC
        if (addressPCParts.length == 2) {

            Integer addressButtonPCIndex = getButtonIndex(addressPCParts[1]);

            if (addressButtonPCIndex == null) {
                logger.debug("addressButtonPCIndex = null");
                return null;
            }

            // calculate address
            int addressPCInt = Integer.parseInt(addressPCParts[0], 16);
            String addressPCString = Integer.toBinaryString(addressPCInt);

            // pad left to 22 bit
            addressPCString = String.format("%" + 22 + "s", addressPCString).replace(' ', '0');
            // pad right to 24 bit
            addressPCString = String.format("%-" + 24 + "s", addressPCString).replace(' ', '0');

            // set the specific button
            String addressButtonPCString = Integer.toBinaryString(addressButtonPCIndex);
            addressPCString = addressPCString.substring(0, addressPCString.length() - addressButtonPCString.length())
                    + addressButtonPCString;

            // reverse bits
            StringBuilder sb = new StringBuilder(addressPCString);
            addressPCString = sb.reverse().toString();

            // turn into HEX
            addressPC = String.format("%06X", Integer.parseInt(addressPCString, 2));
        } else {
            logger.debug("the address should have two parts {}", address);
        }
        return addressPC;
    }

    @Override
    public void dispose() {
        super.dispose();

        Utils.cancel(requestUpdateFuture);
        requestUpdateFuture = null;

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.removeListener(getAddress());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand '{}' '{}'", channelUID, command);

        if (!CHANNEL_BUTTON.equals(channelUID.getId())) {
            return;
        }

        // Whenever the button receives an ON command,
        // we send a simulated button press to the Nikobus.
        if (command == OnOffType.ON) {
            NikobusPcLinkHandler pcLink = getPcLink();
            if (pcLink != null) {
                pcLink.sendCommand(new NikobusCommand(getAddress() + END_OF_TRANSMISSION));
            }
        }
    }

    // called when there is a message on the Nikobus
    private void commandReceived() {

        //logger.debug("button press {}", this.address);

        // a button itself does not have an on and off state
        // so this command will be correct only half of the time...
        updateState(CHANNEL_BUTTON, OnOffType.ON);

        if (logger.isDebugEnabled()) {
            ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), CHANNEL_BUTTON);
            logger.debug("button trigger {} {}", channelUID, CommonTriggerEvents.PRESSED);
        }
        if (logger.isTraceEnabled()) {
            
            // print out the address a seen in the PC application
            int addressPCInt = Integer.parseInt(this.address, 16);
            String addressPCString = Integer.toBinaryString(addressPCInt);
            // reverse bits
            StringBuilder sb = new StringBuilder(addressPCString);
            addressPCString = sb.reverse().toString();
            // last three bits hold the specific button values (0-7)
            int lastThreeBits = Integer.parseInt(addressPCString.substring(addressPCString.length() - 3), 2);
            // transform into the letter code (A, B, 1A, ...)
            String buttonName = Utils.getKeyByValue(buttonIndexMap, lastThreeBits);
            // drop the last two bits, then set the last bit to 0 to get the base address
            String baseAddress = addressPCString.substring(0, addressPCString.length() - 3) + "0";
            int baseAddressInt = Integer.parseInt(baseAddress, 2);
            logger.trace(" button press bus {}, pc {}:{}", this.address, String.format("%06X", baseAddressInt), buttonName);

        }
        triggerChannel(CHANNEL_BUTTON, CommonTriggerEvents.PRESSED);
        
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }        

        Utils.cancel(requestUpdateFuture);
        requestUpdateFuture = scheduler.schedule(this::update, 400, TimeUnit.MILLISECONDS);
    }

    private void update() {
        for (ImpactedModule module : impactedModules) {
            NikobusModuleHandler switchModule = getModuleWithId(module.getThingUID());
            if (switchModule != null) {
                switchModule.requestStatus(module.getGroup());
            }
        }
    }

    private @Nullable NikobusModuleHandler getModuleWithId(ThingUID thingUID) {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }

        Thing thing = bridge.getThing(thingUID);
        if (thing == null) {
            return null;
        }

        ThingHandler thingHandler = thing.getHandler();
        if (thingHandler instanceof NikobusModuleHandler) {
            return (NikobusModuleHandler) thingHandler;
        }
        return null;
    }

    @Override
    protected String getAddress() {
        return "#N" + super.getAddress();
    }

    private Integer getButtonIndex(String buttonName) {
        Integer index = buttonIndexMap.get(buttonName.trim());

        //logger.trace("getButtonIndex '{}' '{}'", buttonName, index);

        if (index == null) {
            logger.debug("button index not found for button '{}'", buttonName);
            return null;
        } else {
            return index;
        }
    }
}
