/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.bridge.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Definition of the 3rd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides the one-and-only protocol specific 1st-level communication class.
 * Additionally it provides all methods for different gateway interactions.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link #getDeviceStatus} for retrieving the bridge state (i.e. IDLE, BUSY, a.s.o),</LI>
 * <LI>{@link #getFirmware} for retrieving the firmware version of the bridge,</LI>
 * <LI>{@link #getHouseStatus} for retrieving the information about device state changes recognized by the
 * bridge,</LI>
 * <LI>{@link #getLANConfig} for retrieving the complete LAN information of the bridge,</LI>
 * <LI>{@link #getProduct} for retrieving the any information about a device behind the bridge,</LI>
 * <LI>{@link #getProductLimitation} for retrieving the limitation information about a device behind the
 * bridge,</LI>
 * <LI>{@link #getProducts} for retrieving the any information for all devices behind the bridge,</LI>
 * <LI>{@link #getScenes} for retrieving the any information for all scenes defined on the bridge,</LI>
 * <LI>{@link #getWLANConfig} for retrieving the complete WLAN information of the bridge,</LI>
 * <LI>{@link #login} for establishing a trusted connectivity by authentication,</LI>
 * <LI>{@link #logout} for tearing down the trusted connectivity by deauthentication,</LI>
 * <LI>{@link #runProductCommand} for manipulation of a device behind the bridge (i.e. instructing to
 * modify a position),</LI>
 * <LI>{@link #runProductDiscovery} for activation of learning mode of the bridge to discovery new
 * products,</LI>
 * <LI>{@link #runProductIdentification} for human-oriented identification a device behind the bridge (i.e.
 * by winking or switching on-and-off),</LI>
 * <LI>{@link #runProductSearch} for searching for lost products on the bridge,</LI>
 * <LI>{@link #runScene} for manipulation of a set of devices behind the bridge which are tied together as scene,</LI>
 * <LI>{@link #setHouseStatusMonitor} for activation or deactivation of the house monitoring mode to be informed about
 * device state changes recognized by the bridge,</LI>
 * <LI>{@link #setSceneVelocity} for changes the velocity of a scene defined on the bridge (i.e. silent or
 * fast mode).</LI>
 * </UL>
 * <P>
 * Message semantic: Retrieval of Bridge configuration and information of devices beyond the bridge.
 * <P>
 *
 * It defines information how to send query and receive answer through the
 * VeluxBridgeProvider as described by the BridgeCommunicationProtocol.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public interface BridgeAPI {

    Login login();

    Logout logout();

    @Nullable
    SetHouseStatusMonitor setHouseStatusMonitor();

    @Nullable
    GetHouseStatus getHouseStatus();

    RunProductDiscovery runProductDiscovery();

    RunProductSearch runProductSearch();

    RunProductIdentification runProductIdentification();

    GetDeviceStatus getDeviceStatus();

    GetFirmware getFirmware();

    GetLANConfig getLANConfig();

    GetWLANConfig getWLANConfig();

    GetProducts getProducts();

    @Nullable
    GetProduct getProduct();

    @Nullable
    GetProductLimitation getProductLimitation();

    @Nullable
    SetProductLimitation setProductLimitation();

    @Nullable
    RunProductCommand runProductCommand();

    GetScenes getScenes();

    SetSceneVelocity setSceneVelocity();

    RunScene runScene();

    @Nullable
    RunReboot runReboot();
}
