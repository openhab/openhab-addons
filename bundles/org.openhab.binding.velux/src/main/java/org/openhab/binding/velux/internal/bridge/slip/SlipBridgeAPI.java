/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.bridge.slip;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeInstance;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.GetDeviceStatus;
import org.openhab.binding.velux.internal.bridge.common.GetFirmware;
import org.openhab.binding.velux.internal.bridge.common.GetHouseStatus;
import org.openhab.binding.velux.internal.bridge.common.GetLANConfig;
import org.openhab.binding.velux.internal.bridge.common.GetProduct;
import org.openhab.binding.velux.internal.bridge.common.GetProductLimitation;
import org.openhab.binding.velux.internal.bridge.common.GetProducts;
import org.openhab.binding.velux.internal.bridge.common.GetScenes;
import org.openhab.binding.velux.internal.bridge.common.GetWLANConfig;
import org.openhab.binding.velux.internal.bridge.common.Login;
import org.openhab.binding.velux.internal.bridge.common.Logout;
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.common.RunProductDiscovery;
import org.openhab.binding.velux.internal.bridge.common.RunProductIdentification;
import org.openhab.binding.velux.internal.bridge.common.RunProductSearch;
import org.openhab.binding.velux.internal.bridge.common.RunScene;
import org.openhab.binding.velux.internal.bridge.common.SetHouseStatusMonitor;
import org.openhab.binding.velux.internal.bridge.common.SetProductLimitation;
import org.openhab.binding.velux.internal.bridge.common.SetSceneVelocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLIP-based 3rd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides the one-and-only protocol specific 1st-level communication class.
 * Additionally it provides all methods for different gateway interactions.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link SlipBridgeAPI#getDeviceStatus} for retrieving the bridge state (i.e. IDLE, BUSY, a.s.o),</LI>
 * <LI>{@link SlipBridgeAPI#getFirmware} for retrieving the firmware version of the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getHouseStatus} for retrieving the information about device state changes recognized by the
 * bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getLANConfig} for retrieving the complete LAN information of the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getProduct} for retrieving the any information about a device behind the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getProductLimitation} for retrieving the limitation information about a device behind the
 * bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getProducts} for retrieving the any information for all devices behind the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getScenes} for retrieving the any information for all scenes defined on the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#getWLANConfig} for retrieving the complete WLAN information of the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#login} for establishing a trusted connectivity by authentication,</LI>
 * <LI>{@link SlipBridgeAPI#logout} for tearing down the trusted connectivity by deauthentication,</LI>
 * <LI>{@link SlipBridgeAPI#runProductCommand} for manipulation of a device behind the bridge (i.e. instructing to
 * modify a position),</LI>
 * <LI>{@link SlipBridgeAPI#runProductDiscovery} for activation of learning mode of the bridge to discovery new
 * products,</LI>
 * <LI>{@link SlipBridgeAPI#runProductIdentification} for human-oriented identification a device behind the bridge (i.e.
 * by winking or switching on-and-off),</LI>
 * <LI>{@link SlipBridgeAPI#runProductSearch} for searching for lost products on the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#runScene} for manipulation of a set of devices behind the bridge which are tied together as
 * scene,</LI>
 * <LI>{@link SlipBridgeAPI#setHouseStatusMonitor} for activation or deactivation of the house monitoring mode to be
 * informed about device state changes recognized by the bridge,</LI>
 * <LI>{@link SlipBridgeAPI#setSceneVelocity} for changes the velocity of a scene defined on the bridge (i.e. silent or
 * fast mode).</LI>
 * </UL>
 * <P>
 * As most derived class of the several inheritance levels it defines an
 * interfacing method which returns the SLIP-protocol-specific communication for gateway interaction.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SlipBridgeAPI implements BridgeAPI {
    private final Logger logger = LoggerFactory.getLogger(SlipBridgeAPI.class);

    private static final GetDeviceStatus GETDEVICESTATUS = new SCgetDeviceStatus();
    private static final GetFirmware GETFIRMWARE = new SCgetFirmware();
    private static final GetHouseStatus GETHOUSESTATUS = new SCgetHouseStatus();
    private static final GetLANConfig GETLANCONFIG = new SCgetLANConfig();
    private static final GetProduct GETPRODUCT = new SCgetProduct();
    private static final GetProductLimitation GETPRODUCTLIMITATION = new SCgetLimitation();
    private static final GetProducts GETPRODUCTS = new SCgetProducts();
    private static final GetScenes GETSCENES = new SCgetScenes();
    private static final GetWLANConfig GETWLANCONFIG = new SCgetWLANConfig();
    private static final Login LOGIN = new SClogin();
    private static final Logout LOGOUT = new SClogout();
    private static final RunProductCommand RUNPRODUCTCOMMAND = new SCrunProductCommand();
    private static final RunProductDiscovery RUNPRODUCTDISCOVERY = new SCrunProductDiscovery();
    private static final RunProductIdentification RUNPRODUCTIDENTIFICATION = new SCrunProductIdentification();
    private static final RunProductSearch RUNPRODUCTSEARCH = new SCrunProductSearch();
    private static final RunScene RUNSCENE = new SCrunScene();
    private static final SetHouseStatusMonitor SETHOUSESTATUSMONITOR = new SCsetHouseStatusMonitor();
    private static final SetProductLimitation SETPRODUCTLIMITATION = new SCsetLimitation();
    private static final SetSceneVelocity SETSCENEVELOCITY = new SCsetSceneVelocity();

    /**
     * Constructor.
     * <P>
     * Inherits the initialization of the binding-wide instance for dealing for common information and
     * initializes the handler {@link SlipVeluxBridge#bridgeAPI}
     * to pass the interface methods.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing for common informations.
     */
    SlipBridgeAPI(VeluxBridgeInstance bridgeInstance) {
        logger.trace("SlipBridgeAPI(constructor) called.");
    }

    @Override
    public GetDeviceStatus getDeviceStatus() {
        return GETDEVICESTATUS;
    }

    @Override
    public GetFirmware getFirmware() {
        return GETFIRMWARE;
    }

    @Override
    public @Nullable GetHouseStatus getHouseStatus() {
        return GETHOUSESTATUS;
    }

    @Override
    public GetLANConfig getLANConfig() {
        return GETLANCONFIG;
    }

    @Override
    public @Nullable GetProduct getProduct() {
        return GETPRODUCT;
    }

    @Override
    public @Nullable GetProductLimitation getProductLimitation() {
        return GETPRODUCTLIMITATION;
    }

    @Override
    public @Nullable SetProductLimitation setProductLimitation() {
        return SETPRODUCTLIMITATION;
    }

    @Override
    public GetProducts getProducts() {
        return GETPRODUCTS;
    }

    @Override
    public GetScenes getScenes() {
        return GETSCENES;
    }

    @Override
    public GetWLANConfig getWLANConfig() {
        return GETWLANCONFIG;
    }

    @Override
    public Login login() {
        return LOGIN;
    }

    @Override
    public Logout logout() {
        return LOGOUT;
    }

    @Override
    public @Nullable RunProductCommand runProductCommand() {
        return RUNPRODUCTCOMMAND;
    }

    @Override
    public RunProductDiscovery runProductDiscovery() {
        return RUNPRODUCTDISCOVERY;
    }

    @Override
    public RunProductIdentification runProductIdentification() {
        return RUNPRODUCTIDENTIFICATION;
    }

    @Override
    public RunProductSearch runProductSearch() {
        return RUNPRODUCTSEARCH;
    }

    @Override
    public RunScene runScene() {
        return RUNSCENE;
    }

    @Override
    public @Nullable SetHouseStatusMonitor setHouseStatusMonitor() {
        return SETHOUSESTATUSMONITOR;
    }

    @Override
    public SetSceneVelocity setSceneVelocity() {
        return SETSCENEVELOCITY;
    }
}
