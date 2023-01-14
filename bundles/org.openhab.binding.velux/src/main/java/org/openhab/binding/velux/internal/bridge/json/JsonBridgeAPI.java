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
package org.openhab.binding.velux.internal.bridge.json;

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
 * JSON-based 3rd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides the one-and-only protocol specific 1st-level communication class.
 * Additionally it provides all methods for different gateway interactions.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link JsonBridgeAPI#getDeviceStatus} for retrieving the bridge state (i.e. IDLE, BUSY, a.s.o),</LI>
 * <LI>{@link JsonBridgeAPI#getFirmware} for retrieving the firmware version of the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getHouseStatus} for retrieving the information about device state changes recognized by the
 * bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getLANConfig} for retrieving the complete LAN information of the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getProduct} for retrieving the any information about a device behind the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getProductLimitation} for retrieving the limitation information about a device behind the
 * bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getProducts} for retrieving the any information for all devices behind the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getScenes} for retrieving the any information for all scenes defined on the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#getWLANConfig} for retrieving the complete WLAN information of the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#login} for establishing a trusted connectivity by authentication,</LI>
 * <LI>{@link JsonBridgeAPI#logout} for tearing down the trusted connectivity by deauthentication,</LI>
 * <LI>{@link JsonBridgeAPI#runProductCommand} for manipulation of a device behind the bridge (i.e. instructing to
 * modify a position),</LI>
 * <LI>{@link JsonBridgeAPI#runProductDiscovery} for activation of learning mode of the bridge to discovery new
 * products,</LI>
 * <LI>{@link JsonBridgeAPI#runProductIdentification} for human-oriented identification a device behind the bridge (i.e.
 * by winking or switching on-and-off),</LI>
 * <LI>{@link JsonBridgeAPI#runProductSearch} for searching for lost products on the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#runScene} for manipulation of a set of devices behind the bridge which are tied together as
 * scene,</LI>
 * <LI>{@link JsonBridgeAPI#setHouseStatusMonitor} for activation or deactivation of the house monitoring mode to be
 * informed about device state changes recognized by the bridge,</LI>
 * <LI>{@link JsonBridgeAPI#setSceneVelocity} for changes the velocity of a scene defined on the bridge (i.e. silent or
 * fast mode).</LI>
 * </UL>
 * <P>
 * As most derived class of the several inheritance levels it defines an
 * interfacing method which returns the JSON-protocol-specific communication for gateway interaction.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class JsonBridgeAPI implements BridgeAPI {
    private final Logger logger = LoggerFactory.getLogger(JsonBridgeAPI.class);

    private final GetDeviceStatus jsonGetDeviceStatus = new JCgetDeviceStatus();
    private final GetFirmware jsonGetFirmware = new JCgetFirmware();
    private final GetLANConfig jsonGetLanConfig = new JCgetLANConfig();
    private final GetProducts jsonGetProducts = new JCgetProducts();
    private final GetScenes jsonGetScenes = new JCgetScenes();
    private final GetWLANConfig jsonGetWLanConfig = new JCgetWLANConfig();
    private final Login jsonLogin = new JClogin();
    private final Logout jsonLogout = new JClogout();
    private final RunProductDiscovery jsonRunProductDiscovery = new JCrunProductDiscovery();
    private final RunProductIdentification jsonRunProductIdentification = new JCrunProductIdentification();
    private final RunProductSearch jsonRunProductSearch = new JCrunProductSearch();
    private final RunScene jsonRunScene = new JCrunScene();
    private final SetSceneVelocity jsonSetSceneVelocity = new JCsetSceneVelocity();

    /**
     * Constructor.
     * <P>
     * Inherits the initialization of the binding-wide instance for dealing for common information and
     * initializes the handler {@link org.openhab.binding.velux.internal.bridge.json.JsonVeluxBridge#bridgeAPI
     * JsonVeluxBridge.bridgeAPI}
     * to pass the interface methods.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing for common informations.
     */
    JsonBridgeAPI(VeluxBridgeInstance bridgeInstance) {
        logger.trace("JsonBridgeAPI(constructor) called.");
    }

    @Override
    public GetDeviceStatus getDeviceStatus() {
        return jsonGetDeviceStatus;
    }

    @Override
    public GetFirmware getFirmware() {
        return jsonGetFirmware;
    }

    @Override
    public @Nullable GetHouseStatus getHouseStatus() {
        return null;
    }

    @Override
    public GetLANConfig getLANConfig() {
        return jsonGetLanConfig;
    }

    @Override
    public @Nullable GetProduct getProduct() {
        return null;
    }

    @Override
    public @Nullable GetProductLimitation getProductLimitation() {
        return null;
    }

    @Override
    public @Nullable SetProductLimitation setProductLimitation() {
        return null;
    }

    @Override
    public GetProducts getProducts() {
        return jsonGetProducts;
    }

    @Override
    public GetScenes getScenes() {
        return jsonGetScenes;
    }

    @Override
    public GetWLANConfig getWLANConfig() {
        return jsonGetWLanConfig;
    }

    @Override
    public Login login() {
        return jsonLogin;
    }

    @Override
    public Logout logout() {
        return jsonLogout;
    }

    @Override
    public @Nullable RunProductCommand runProductCommand() {
        return null;
    }

    @Override
    public RunProductDiscovery runProductDiscovery() {
        return jsonRunProductDiscovery;
    }

    @Override
    public RunProductIdentification runProductIdentification() {
        return jsonRunProductIdentification;
    }

    @Override
    public RunProductSearch runProductSearch() {
        return jsonRunProductSearch;
    }

    @Override
    public RunScene runScene() {
        return jsonRunScene;
    }

    @Override
    public @Nullable SetHouseStatusMonitor setHouseStatusMonitor() {
        return null;
    }

    @Override
    public SetSceneVelocity setSceneVelocity() {
        return jsonSetSceneVelocity;
    }

    @Override
    public @Nullable RunReboot runReboot() {
        return null;
    }

    @Override
    public @Nullable GetProduct getProductStatus() {
        return null;
    }
}
