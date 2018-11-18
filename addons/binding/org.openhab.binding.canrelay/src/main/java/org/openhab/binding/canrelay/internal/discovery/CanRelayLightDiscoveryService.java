/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.discovery;

import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.canrelay.internal.protocol.CanRelayAccess;
import org.openhab.binding.canrelay.internal.protocol.Floor;
import org.openhab.binding.canrelay.internal.protocol.LightState;
import org.openhab.binding.canrelay.internal.runtime.Runtime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CanRelayLightDiscoveryService} auto discovers new lights for a CanRelay on CANBUS.
 *
 * @author Lubos Housa - Initial Contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.canrelay")
public class CanRelayLightDiscoveryService extends AbstractDiscoveryService {
    // 0 means disabled, never force kill it, underlying logic is using timeout itself and more fine grained and most
    // likely in background anyway
    private static final int DISCOVER_TIMEOUT_SECONDS = 0;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_LIGHT);
    private static final Logger logger = LoggerFactory.getLogger(CanRelayLightDiscoveryService.class);

    // OSGI components
    private CanRelayAccess canRelayAccess;
    private Runtime runtime;

    public CanRelayLightDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    private void scan() {
        logger.debug("Started CanRelay CANBUS light discovery scan.");

        // simply find out the state of all lights from canAccess. Mind that the below method would get blocked for some
        // time if the bridge is not yet initialized
        Collection<LightState> lightStates = canRelayAccess.detectLightStates();

        // this call has to be done after calling detect light states to assure consistency (if this discovery is called
        // before bridge one, the bridge is not yet known
        if (runtime.getBridgeUID() == null) {
            logger.debug(
                    "bridgeUID not known. Most likely means we have no bridge, so skipping, no lights would be detected.");
            return;
        }

        for (LightState lightState : lightStates) {
            String nodeString = nodeAsString(lightState.getNodeID());
            logger.debug("Detected new light '{}' in CanRelay on floor {}", nodeString,
                    Floor.getFloorFromNodeID(lightState.getNodeID()));

            ThingUID lightUID = new ThingUID(THING_TYPE_LIGHT, nodeString);
            thingDiscovered(DiscoveryResultBuilder.create(lightUID).withBridge(runtime.getBridgeUID())
                    .withLabel("@text/thing-type.canrelay.light.label")
                    .withProperty(CONFIG_NODEID, lightState.getNodeID()).withRepresentationProperty(CONFIG_NODEID)
                    .withProperty(CONFIG_INITIALSTATE, lightState.getState() == OnOffType.ON).build());
        }

        logger.debug("Finished CanRelay CANBUS light discovery scan");
    }

    @Override
    protected void startScan() {
        // run the scan in background thread since it very likely would get blocked, depending on whether this discovery
        // is called before the bridge discovery. So let other discovery services do their job (e.g. bridge service
        // would first need to detect the bridge since this discovery need the bridge's device to be inititiated
        // alternative was to only instantiate this discovery in handler factory when a bridge is being added, but then
        // this discovery won't be started for the initial scan and the user would need to trigger scan again.
        // Disadvantage being that this in effect waits for the user to add the discovered bridge in order for this
        // light discovery to do anything useful, that's why this discovery has disabled default timeout and only
        // internally blocks for the device for a minute or so. If that expires too, user can still invoke new scan for
        // this binding to find the remaining lights
        scheduler.execute(() -> {
            scan();
        });
    }

    @Reference
    public void setCanRelayAccess(CanRelayAccess canRelayAccess) {
        this.canRelayAccess = canRelayAccess;
    }

    public void unsetCanRelayAccess(CanRelayAccess canRelayAccess) {
        this.canRelayAccess = null;
    }

    @Reference
    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    public void unsetRuntime(Runtime runtime) {
        this.runtime = null;
    }

    @Reference
    public void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
