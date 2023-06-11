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
import org.openhab.binding.velux.internal.bridge.common.RunReboot;
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

    private final GetDeviceStatus slipGetDeviceStatus = new SCgetDeviceStatus();
    private final GetFirmware slipGetFirmware = new SCgetFirmware();
    private final GetHouseStatus slipGetHouseStatus = new SCgetHouseStatus();
    private final GetLANConfig slipGetLanConfig = new SCgetLANConfig();
    private final GetProduct slipGetProduct = new SCgetProduct();
    private final GetProductLimitation slipGetProductLimitation = new SCgetLimitation();
    private final GetProducts slipGetProducts = new SCgetProducts();
    private final GetScenes slipGetScenes = new SCgetScenes();
    private final GetWLANConfig slipGetWLanConfig = new SCgetWLANConfig();
    private final Login slipLogin = new SClogin();
    private final Logout slipLogout = new SClogout();
    private final RunProductCommand slipRunProductCommand = new SCrunProductCommand();
    private final RunProductDiscovery slipRunProductDiscovery = new SCrunProductDiscovery();
    private final RunProductIdentification slipRunProductIdentification = new SCrunProductIdentification();
    private final RunProductSearch slipRunProductSearch = new SCrunProductSearch();
    private final RunScene slipRunScene = new SCrunScene();
    private final SetHouseStatusMonitor slipSetHouseMonitor = new SCsetHouseStatusMonitor();
    private final SetProductLimitation slipSetProductLimitation = new SCsetLimitation();
    private final SetSceneVelocity slipSetSceneVelocity = new SCsetSceneVelocity();
    private final RunReboot slipRunReboot = new SCrunReboot();
    private final GetProduct slipGetProductStatus = new SCgetProductStatus();

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
        return slipGetDeviceStatus;
    }

    @Override
    public GetFirmware getFirmware() {
        return slipGetFirmware;
    }

    @Override
    public @Nullable GetHouseStatus getHouseStatus() {
        return slipGetHouseStatus;
    }

    @Override
    public GetLANConfig getLANConfig() {
        return slipGetLanConfig;
    }

    @Override
    public @Nullable GetProduct getProduct() {
        return slipGetProduct;
    }

    @Override
    public @Nullable GetProductLimitation getProductLimitation() {
        return slipGetProductLimitation;
    }

    @Override
    public @Nullable SetProductLimitation setProductLimitation() {
        return slipSetProductLimitation;
    }

    @Override
    public GetProducts getProducts() {
        return slipGetProducts;
    }

    @Override
    public GetScenes getScenes() {
        return slipGetScenes;
    }

    @Override
    public GetWLANConfig getWLANConfig() {
        return slipGetWLanConfig;
    }

    @Override
    public Login login() {
        return slipLogin;
    }

    @Override
    public Logout logout() {
        return slipLogout;
    }

    @Override
    public @Nullable RunProductCommand runProductCommand() {
        return slipRunProductCommand;
    }

    @Override
    public RunProductDiscovery runProductDiscovery() {
        return slipRunProductDiscovery;
    }

    @Override
    public RunProductIdentification runProductIdentification() {
        return slipRunProductIdentification;
    }

    @Override
    public RunProductSearch runProductSearch() {
        return slipRunProductSearch;
    }

    @Override
    public RunScene runScene() {
        return slipRunScene;
    }

    @Override
    public @Nullable SetHouseStatusMonitor setHouseStatusMonitor() {
        return slipSetHouseMonitor;
    }

    @Override
    public SetSceneVelocity setSceneVelocity() {
        return slipSetSceneVelocity;
    }

    @Override
    public @Nullable RunReboot runReboot() {
        return slipRunReboot;
    }

    @Override
    public @Nullable GetProduct getProductStatus() {
        return slipGetProductStatus;
    }
}
