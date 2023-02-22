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
package org.openhab.binding.digitalstrom.internal.discovery;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.handler.BridgeHandler;
import org.openhab.binding.digitalstrom.internal.handler.CircuitHandler;
import org.openhab.binding.digitalstrom.internal.handler.DeviceHandler;
import org.openhab.binding.digitalstrom.internal.handler.SceneHandler;
import org.openhab.binding.digitalstrom.internal.handler.ZoneTemperatureControlHandler;
import org.openhab.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.openhab.binding.digitalstrom.internal.providers.DsDeviceThingTypeProvider;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryServiceManager} manages the different scene and device discovery services and informs them about
 * new added or removed scenes and devices.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DiscoveryServiceManager
        implements SceneStatusListener, DeviceStatusListener, TemperatureControlStatusListener {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryServiceManager.class);

    private final Map<String, AbstractDiscoveryService> discoveryServices;
    private final Map<String, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final String bridgeUID;

    /**
     * Creates a new {@link DiscoveryServiceManager} and generates automatically all {@link SceneDiscoveryService}s and
     * {@link DeviceDiscoveryService}s for all supported {@link ThingType}s of the {@link DeviceHandler} and
     * {@link SceneHandler}.
     *
     * @param bridgeHandler (must not be null)
     */
    public DiscoveryServiceManager(BridgeHandler bridgeHandler) {
        bridgeUID = bridgeHandler.getThing().getUID().getAsString();
        discoveryServices = new HashMap<>(SceneHandler.SUPPORTED_THING_TYPES.size()
                + DeviceHandler.SUPPORTED_THING_TYPES.size() + CircuitHandler.SUPPORTED_THING_TYPES.size()
                + ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.size());
        for (ThingTypeUID type : SceneHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new SceneDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : DeviceHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new DeviceDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : CircuitHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new DeviceDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new ZoneTemperatureControlDiscoveryService(bridgeHandler, type));
        }
        bridgeHandler.registerSceneStatusListener(this);
        bridgeHandler.registerDeviceStatusListener(this);
        bridgeHandler.registerTemperatureControlStatusListener(this);
    }

    /**
     * Deactivates all {@link SceneDiscoveryService}s and {@link DeviceDiscoveryService}s of this
     * {@link DiscoveryServiceManager} and unregisters them from the given {@link BundleContext}.
     *
     * @param bundleContext (must not be null)
     */
    public void unregisterDiscoveryServices(BundleContext bundleContext) {
        if (discoveryServices != null) {
            for (AbstractDiscoveryService service : discoveryServices.values()) {
                if (service instanceof SceneDiscoveryService) {
                    SceneDiscoveryService sceneDisServ = (SceneDiscoveryService) service;
                    ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID + sceneDisServ.getID());
                    sceneDisServ.deactivate();
                    serviceReg.unregister();
                    discoveryServiceRegs.remove(bridgeUID + sceneDisServ.getID());
                }
                if (service instanceof DeviceDiscoveryService) {
                    DeviceDiscoveryService devDisServ = (DeviceDiscoveryService) service;
                    ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID + devDisServ.getID());
                    devDisServ.deactivate();
                    serviceReg.unregister();
                    discoveryServiceRegs.remove(bridgeUID + devDisServ.getID());
                }
                if (service instanceof ZoneTemperatureControlDiscoveryService) {
                    ZoneTemperatureControlDiscoveryService devDisServ = (ZoneTemperatureControlDiscoveryService) service;
                    ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID + devDisServ.getID());
                    devDisServ.deactivate();
                    serviceReg.unregister();
                    discoveryServiceRegs.remove(bridgeUID + devDisServ.getID());
                }
            }
        }
    }

    /**
     * Registers all {@link SceneDiscoveryService}s and {@link DeviceDiscoveryService}s of this
     * {@link DiscoveryServiceManager} to the given {@link BundleContext}.
     *
     * @param bundleContext (must not be null)
     */
    public void registerDiscoveryServices(BundleContext bundleContext) {
        if (discoveryServices != null) {
            for (AbstractDiscoveryService service : discoveryServices.values()) {
                if (service instanceof SceneDiscoveryService) {
                    this.discoveryServiceRegs.put(bridgeUID + ((SceneDiscoveryService) service).getID(), bundleContext
                            .registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
                }
                if (service instanceof DeviceDiscoveryService) {
                    this.discoveryServiceRegs.put(bridgeUID + ((DeviceDiscoveryService) service).getID(), bundleContext
                            .registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
                }
                if (service instanceof ZoneTemperatureControlDiscoveryService) {
                    this.discoveryServiceRegs
                            .put(bridgeUID + ((ZoneTemperatureControlDiscoveryService) service).getID(), bundleContext
                                    .registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
                }
            }
        }
    }

    @Override
    public String getSceneStatusListenerID() {
        return SceneStatusListener.SCENE_DISCOVERY;
    }

    @Override
    public void onSceneStateChanged(boolean flag) {
        // nothing to do
    }

    @Override
    public void onSceneRemoved(InternalScene scene) {
        if (discoveryServices.get(scene.getSceneType()) != null) {
            ((SceneDiscoveryService) discoveryServices.get(scene.getSceneType())).onSceneRemoved(scene);
        }
    }

    @Override
    public void onSceneAdded(InternalScene scene) {
        if (discoveryServices.get(scene.getSceneType()) != null) {
            ((SceneDiscoveryService) discoveryServices.get(scene.getSceneType())).onSceneAdded(scene);
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        // nothing to do
    }

    @Override
    public void onDeviceRemoved(GeneralDeviceInformation device) {
        if (device instanceof Device) {
            String id = ((Device) device).getHWinfo().substring(0, 2);
            if (((Device) device).isSensorDevice()) {
                id = ((Device) device).getHWinfo().replace("-", "");
            }
            if (discoveryServices.get(id) != null) {
                ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceRemoved(device);
            }
        }
        if (device instanceof Circuit) {
            if (discoveryServices.get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString()) != null) {
                ((DeviceDiscoveryService) discoveryServices
                        .get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString())).onDeviceRemoved(device);
            }
        }
    }

    @Override
    public void onDeviceAdded(GeneralDeviceInformation device) {
        try {
            if (device instanceof Device) {
                String id = ((Device) device).getHWinfo().substring(0, 2);
                if (((Device) device).isSensorDevice()) {
                    id = ((Device) device).getHWinfo();
                }
                if (discoveryServices.get(id) != null) {
                    ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceAdded(device);
                }
            }
            if (device instanceof Circuit) {
                if (discoveryServices.get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString()) != null) {
                    ((DeviceDiscoveryService) discoveryServices
                            .get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString()))
                                    .onDeviceAdded(device);
                }
            }
        } catch (RuntimeException ex) {
            logger.warn("Unable to add devices {}", device, ex);
        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig) {
        // nothing to do
    }

    @Override
    public void onSceneConfigAdded(short sceneId) {
        // nothing to do
    }

    @Override
    public String getDeviceStatusListenerID() {
        return DeviceStatusListener.DEVICE_DISCOVERY;
    }

    @Override
    public void configChanged(TemperatureControlStatus tempControlStatus) {
        // currently only this thing-type exists
        if (discoveryServices.get(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL.toString()) != null) {
            ((ZoneTemperatureControlDiscoveryService) discoveryServices
                    .get(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL.toString()))
                            .configChanged(tempControlStatus);
        }
    }

    @Override
    public void registerTemperatureSensorTransmitter(
            TemperatureControlSensorTransmitter temperatureSensorTransreciver) {
        // nothing to do
    }

    @Override
    public Integer getTemperationControlStatusListenrID() {
        return TemperatureControlStatusListener.DISCOVERY;
    }

    @Override
    public void onTargetTemperatureChanged(Float newValue) {
        // nothing to do
    }

    @Override
    public void onControlValueChanged(Integer newValue) {
        // nothing to do
    }
}
