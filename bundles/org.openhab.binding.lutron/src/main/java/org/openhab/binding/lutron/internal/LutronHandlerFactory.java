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
package org.openhab.binding.lutron.internal;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lutron.internal.discovery.LutronDeviceDiscoveryService;
import org.openhab.binding.lutron.internal.grxprg.GrafikEyeHandler;
import org.openhab.binding.lutron.internal.grxprg.PrgBridgeHandler;
import org.openhab.binding.lutron.internal.grxprg.PrgConstants;
import org.openhab.binding.lutron.internal.handler.BlindHandler;
import org.openhab.binding.lutron.internal.handler.CcoHandler;
import org.openhab.binding.lutron.internal.handler.DimmerHandler;
import org.openhab.binding.lutron.internal.handler.FanHandler;
import org.openhab.binding.lutron.internal.handler.GrafikEyeKeypadHandler;
import org.openhab.binding.lutron.internal.handler.GreenModeHandler;
import org.openhab.binding.lutron.internal.handler.IPBridgeHandler;
import org.openhab.binding.lutron.internal.handler.IntlKeypadHandler;
import org.openhab.binding.lutron.internal.handler.KeypadHandler;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.handler.MaintainedCcoHandler;
import org.openhab.binding.lutron.internal.handler.OGroupHandler;
import org.openhab.binding.lutron.internal.handler.OccupancySensorHandler;
import org.openhab.binding.lutron.internal.handler.PalladiomKeypadHandler;
import org.openhab.binding.lutron.internal.handler.PicoKeypadHandler;
import org.openhab.binding.lutron.internal.handler.PulsedCcoHandler;
import org.openhab.binding.lutron.internal.handler.QSIOHandler;
import org.openhab.binding.lutron.internal.handler.ShadeHandler;
import org.openhab.binding.lutron.internal.handler.SwitchHandler;
import org.openhab.binding.lutron.internal.handler.SysvarHandler;
import org.openhab.binding.lutron.internal.handler.TabletopKeypadHandler;
import org.openhab.binding.lutron.internal.handler.TimeclockHandler;
import org.openhab.binding.lutron.internal.handler.VcrxHandler;
import org.openhab.binding.lutron.internal.handler.VirtualKeypadHandler;
import org.openhab.binding.lutron.internal.handler.WciHandler;
import org.openhab.binding.lutron.internal.hw.HwConstants;
import org.openhab.binding.lutron.internal.hw.HwDimmerHandler;
import org.openhab.binding.lutron.internal.hw.HwSerialBridgeHandler;
import org.openhab.binding.lutron.internal.radiora.RadioRAConstants;
import org.openhab.binding.lutron.internal.radiora.handler.PhantomButtonHandler;
import org.openhab.binding.lutron.internal.radiora.handler.RS232Handler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LutronHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added bridge discovery service registration/removal
 */

@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lutron")
public class LutronHandlerFactory extends BaseThingHandlerFactory {

    // Used by LutronDeviceDiscoveryService to discover these types
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(THING_TYPE_DIMMER, THING_TYPE_SWITCH, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_KEYPAD,
                            THING_TYPE_TTKEYPAD, THING_TYPE_INTLKEYPAD, THING_TYPE_PICO, THING_TYPE_VIRTUALKEYPAD,
                            THING_TYPE_VCRX, THING_TYPE_CCO, THING_TYPE_SHADE, THING_TYPE_TIMECLOCK,
                            THING_TYPE_GREENMODE, THING_TYPE_QSIO, THING_TYPE_GRAFIKEYEKEYPAD, THING_TYPE_BLIND,
                            THING_TYPE_PALLADIOMKEYPAD, THING_TYPE_WCI, THING_TYPE_OGROUP, THING_TYPE_FAN)
                    .collect(Collectors.toSet()));

    // Used by the HwDiscoveryService
    public static final Set<ThingTypeUID> HW_DISCOVERABLE_DEVICE_TYPES_UIDS = Collections
            .unmodifiableSet(Collections.singleton(HwConstants.THING_TYPE_HWDIMMER));

    // Other types that can be initiated but not discovered
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(THING_TYPE_IPBRIDGE, THING_TYPE_LEAPBRIDGE, PrgConstants.THING_TYPE_PRGBRIDGE,
                            PrgConstants.THING_TYPE_GRAFIKEYE, RadioRAConstants.THING_TYPE_RS232,
                            RadioRAConstants.THING_TYPE_DIMMER, RadioRAConstants.THING_TYPE_SWITCH,
                            RadioRAConstants.THING_TYPE_PHANTOM, HwConstants.THING_TYPE_HWSERIALBRIDGE,
                            THING_TYPE_CCO_PULSED, THING_TYPE_CCO_MAINTAINED, THING_TYPE_SYSVAR)
                    .collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(LutronHandlerFactory.class);

    private final SerialPortManager serialPortManager;
    private final HttpClient httpClient;

    @Activate
    public LutronHandlerFactory(final @Reference SerialPortManager serialPortManager,
            @Reference HttpClientFactory httpClientFactory) {
        this.serialPortManager = serialPortManager;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)
                || DISCOVERABLE_DEVICE_TYPES_UIDS.contains(thingTypeUID)
                || HW_DISCOVERABLE_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegMap = new HashMap<>();
    // Marked as Nullable only to fix incorrect redundant null check complaints after adding null annotations

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IPBRIDGE)) {
            IPBridgeHandler bridgeHandler = new IPBridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_LEAPBRIDGE)) {
            LeapBridgeHandler bridgeHandler = new LeapBridgeHandler((Bridge) thing);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            return new DimmerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SHADE)) {
            return new ShadeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new SwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO)) {
            return new CcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO_PULSED)) {
            return new PulsedCcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO_MAINTAINED)) {
            return new MaintainedCcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OCCUPANCYSENSOR)) {
            return new OccupancySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_KEYPAD)) {
            return new KeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TTKEYPAD)) {
            return new TabletopKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_INTLKEYPAD)) {
            return new IntlKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PICO)) {
            return new PicoKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_GRAFIKEYEKEYPAD)) {
            return new GrafikEyeKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PALLADIOMKEYPAD)) {
            return new PalladiomKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VIRTUALKEYPAD)) {
            return new VirtualKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VCRX)) {
            return new VcrxHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WCI)) {
            return new WciHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TIMECLOCK)) {
            return new TimeclockHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_GREENMODE)) {
            return new GreenModeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_QSIO)) {
            return new QSIOHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BLIND)) {
            return new BlindHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SYSVAR)) {
            return new SysvarHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OGROUP)) {
            return new OGroupHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FAN)) {
            return new FanHandler(thing);
        } else if (thingTypeUID.equals(PrgConstants.THING_TYPE_PRGBRIDGE)) {
            return new PrgBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(PrgConstants.THING_TYPE_GRAFIKEYE)) {
            return new GrafikEyeHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_RS232)) {
            return new RS232Handler((Bridge) thing, serialPortManager);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_DIMMER)) {
            return new org.openhab.binding.lutron.internal.radiora.handler.DimmerHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_SWITCH)) {
            return new org.openhab.binding.lutron.internal.radiora.handler.SwitchHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_PHANTOM)) {
            return new PhantomButtonHandler(thing);
        } else if (thingTypeUID.equals(HwConstants.THING_TYPE_HWSERIALBRIDGE)) {
            return new HwSerialBridgeHandler((Bridge) thing, serialPortManager);
        } else if (thingTypeUID.equals(HwConstants.THING_TYPE_HWDIMMER)) {
            return new HwDimmerHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof IPBridgeHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegMap.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                logger.debug("Unregistering device discovery service.");
                serviceReg.unregister();
            }
        }
    }

    /**
     * Register a discovery service for an IP bridge handler.
     *
     * @param bridgeHandler IP bridge handler for which to register the discovery service
     */
    private synchronized void registerDiscoveryService(IPBridgeHandler bridgeHandler) {
        logger.debug("Registering XML device discovery service.");
        LutronDeviceDiscoveryService discoveryService = new LutronDeviceDiscoveryService(bridgeHandler, httpClient);
        bridgeHandler.setDiscoveryService(discoveryService);
        discoveryServiceRegMap.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, null));
    }
}
