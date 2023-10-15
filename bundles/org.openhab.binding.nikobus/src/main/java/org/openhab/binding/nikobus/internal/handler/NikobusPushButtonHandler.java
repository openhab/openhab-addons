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
package org.openhab.binding.nikobus.internal.handler;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;
import static org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusPushButtonHandler} is responsible for handling Nikobus push buttons.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusPushButtonHandler extends NikobusBaseThingHandler {
    private static final String END_OF_TRANSMISSION = "\r#E1";
    private final Logger logger = LoggerFactory.getLogger(NikobusPushButtonHandler.class);
    private final List<ImpactedModule> impactedModules = new CopyOnWriteArrayList<>();
    private final List<TriggerProcessor> triggerProcessors = new CopyOnWriteArrayList<>();
    private @Nullable Future<?> requestUpdateFuture;

    public NikobusPushButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        impactedModules.clear();
        triggerProcessors.clear();

        Object impactedModulesObject = getConfig().get(CONFIG_IMPACTED_MODULES);
        if (impactedModulesObject != null) {
            try {
                Bridge bridge = getBridge();
                if (bridge == null) {
                    throw new IllegalArgumentException("Bridge does not exist!");
                }

                ThingUID bridgeUID = thing.getBridgeUID();
                if (bridgeUID == null) {
                    throw new IllegalArgumentException("Unable to read BridgeUID!");
                }

                String[] impactedModulesString = impactedModulesObject.toString().split(",");
                for (String impactedModuleString : impactedModulesString) {
                    ImpactedModuleUID impactedModuleUID = new ImpactedModuleUID(impactedModuleString.trim());
                    ThingTypeUID thingTypeUID = new ThingTypeUID(bridgeUID.getBindingId(),
                            impactedModuleUID.getThingTypeId());
                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, impactedModuleUID.getThingId());

                    if (!bridge.getThings().stream().anyMatch(thing -> thing.getUID().equals(thingUID))) {
                        throw new IllegalArgumentException(
                                "Impacted module " + thingUID + " not found for '" + impactedModuleString + "'");
                    }

                    impactedModules.add(new ImpactedModule(thingUID, impactedModuleUID.getGroup()));
                }
            } catch (RuntimeException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }

            logger.debug("Impacted modules for {} = {}", thing.getUID(), impactedModules);
        }

        for (Channel channel : thing.getChannels()) {
            TriggerProcessor processor = createTriggerProcessor(channel);
            if (processor != null) {
                triggerProcessors.add(processor);
            }
        }

        logger.debug("Trigger channels for {} = {}", thing.getUID(), triggerProcessors);

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.addListener(getAddress(), this::commandReceived);
        }
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
            processImpactedModules();
        }
    }

    private void commandReceived() {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        updateState(CHANNEL_BUTTON, OnOffType.ON);

        if (!triggerProcessors.isEmpty()) {
            long currentTimeMillis = System.currentTimeMillis();
            triggerProcessors.forEach(processor -> processor.process(currentTimeMillis));
        }

        processImpactedModules();
    }

    private void processImpactedModules() {
        if (!impactedModules.isEmpty()) {
            Utils.cancel(requestUpdateFuture);
            requestUpdateFuture = scheduler.schedule(this::update, 400, TimeUnit.MILLISECONDS);
        }
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
        if (thingHandler instanceof NikobusModuleHandler nikobusModuleHandler) {
            return nikobusModuleHandler;
        }
        return null;
    }

    @Override
    protected String getAddress() {
        return "#N" + super.getAddress();
    }

    private @Nullable TriggerProcessor createTriggerProcessor(Channel channel) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID != null) {
            switch (channelTypeUID.getId()) {
                case CHANNEL_TRIGGER_FILTER:
                    return new TriggerFilter(channel);
                case CHANNEL_TRIGGER_BUTTON:
                    return new TriggerButton(channel);
            }
        }
        return null;
    }

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
            if ("1".equals(getSegment(2))) {
                return FIRST;
            }
            if ("2".equals(getSegment(2))) {
                return SECOND;
            }
            throw new IllegalArgumentException("Unexpected group found " + getSegment(2));
        }

        @Override
        protected int getMinimalNumberOfSegments() {
            return 3;
        }
    }

    private interface TriggerProcessor {
        void process(long currentTimeMillis);
    }

    private abstract class AbstractTriggerProcessor<Config> implements TriggerProcessor {
        private long lastCommandReceivedTimestamp = 0;
        protected final ChannelUID channelUID;
        protected final Config config;

        // Nikobus push button will send a new message on bus every ~50ms so
        // lets assume if we haven't received a new message in over 150ms that
        // button was released and pressed again.
        protected static final long BUTTON_RELEASED_MILIS = 150;

        protected AbstractTriggerProcessor(Class<Config> configType, Channel channel) {
            this.channelUID = channel.getUID();
            this.config = channel.getConfiguration().as(configType);
        }

        @Override
        public void process(long currentTimeMillis) {
            if (Math.abs(currentTimeMillis - lastCommandReceivedTimestamp) > BUTTON_RELEASED_MILIS) {
                reset(currentTimeMillis);
            }
            lastCommandReceivedTimestamp = currentTimeMillis;
            processNext(currentTimeMillis);
        }

        protected abstract void reset(long currentTimeMillis);

        protected abstract void processNext(long currentTimeMillis);
    }

    public static class TriggerButtonConfig {
        public int threshold = 1000;
    }

    private class TriggerButton extends AbstractTriggerProcessor<TriggerButtonConfig> {
        private long nextLongPressTimestamp = 0;
        private @Nullable Future<?> triggerShortPressFuture;

        TriggerButton(Channel channel) {
            super(TriggerButtonConfig.class, channel);
        }

        @Override
        protected void reset(long currentTimeMillis) {
            nextLongPressTimestamp = currentTimeMillis + config.threshold;
        }

        @Override
        protected void processNext(long currentTimeMillis) {
            if (currentTimeMillis < nextLongPressTimestamp) {
                Utils.cancel(triggerShortPressFuture);
                triggerShortPressFuture = scheduler.schedule(
                        () -> triggerChannel(channelUID, CommonTriggerEvents.SHORT_PRESSED), BUTTON_RELEASED_MILIS,
                        TimeUnit.MILLISECONDS);
            } else if (nextLongPressTimestamp != 0) {
                Utils.cancel(triggerShortPressFuture);
                nextLongPressTimestamp = 0;
                triggerChannel(channelUID, CommonTriggerEvents.LONG_PRESSED);
            }
        }

        @Override
        public String toString() {
            return "TriggerButton '" + channelUID + "', config: threshold = " + config.threshold;
        }
    }

    public static class TriggerFilterConfig {
        public @Nullable String command;
        public int delay = 0;
        public int period = -1;
    }

    private class TriggerFilter extends AbstractTriggerProcessor<TriggerFilterConfig> {
        private long nextTriggerTimestamp = 0;

        TriggerFilter(Channel channel) {
            super(TriggerFilterConfig.class, channel);
        }

        @Override
        protected void reset(long currentTimeMillis) {
            nextTriggerTimestamp = currentTimeMillis + config.delay;
        }

        @Override
        protected void processNext(long currentTimeMillis) {
            if (currentTimeMillis >= nextTriggerTimestamp) {
                nextTriggerTimestamp = (config.period < 0) ? Long.MAX_VALUE : currentTimeMillis + config.period;
                String command = config.command;
                if (command != null) {
                    triggerChannel(channelUID, command);
                } else {
                    triggerChannel(channelUID);
                }
            }
        }

        @Override
        public String toString() {
            return "TriggerFilter '" + channelUID + "', config: command = '" + config.command + "', delay = "
                    + config.delay + ", period = " + config.period;
        }
    }
}
