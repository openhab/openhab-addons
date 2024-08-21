/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.bridge;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.common.Login;
import org.openhab.binding.velux.internal.bridge.common.Logout;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * It provides methods for pre- and post-communication
 * as well as a common method for the real communication.
 * The following class access methods exist:
 * <UL>
 * <LI>{@link VeluxBridge#bridgeLogin} for pre-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeLogout} for post-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeCommunicate} as method for the common communication.</LI>
 * </UL>
 * <P>
 * Each protocol-specific implementation provides a publicly visible
 * set of supported protocols as variable {@link #supportedProtocols}.
 * As root of several inheritance levels it predefines an
 * interfacing method {@link VeluxBridge#bridgeAPI} which
 * has to be implemented by any kind of protocol-specific
 * communication returning the appropriate base (1st) level
 * communication method as well as any other gateway
 * interaction with {@link #bridgeDirectCommunicate}.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class VeluxBridge {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridge.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private final String emptyAuthenticationToken = "";

    // Type definitions, variables

    /**
     * Support protocols for the concrete implementation.
     * <P>
     * For protocol-specific implementations this value has to be adapted along the inheritance i.e.
     * with the protocol-specific class values.
     */
    public Set<String> supportedProtocols = Collections.emptySet();

    /** BridgeCommunicationProtocol authentication token for Velux Bridge. */
    protected String authenticationToken = emptyAuthenticationToken;

    /**
     * Handler to access global bridge instance methods
     *
     */
    protected VeluxBridgeHandler bridgeInstance;

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Constructor.
     * <P>
     * Initializes the binding-wide instance for dealing with common informations and
     * the Velux bridge connectivity settings by preparing the configuration settings with help
     * by VeluxBridgeConfiguration.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing for common informations
     *            like existing actuators and predefined scenes.
     */
    public VeluxBridge(VeluxBridgeHandler bridgeInstance) {
        logger.trace("VeluxBridge(constructor,bridgeInstance={}) called.", bridgeInstance);
        this.bridgeInstance = bridgeInstance;
        logger.trace("VeluxBridge(constructor) done.");
    }

    // Destructor methods

    /**
     * Destructor.
     * <P>
     * Deinitializes the binding-wide instance.
     *
     */
    public void shutdown() {
        logger.trace("shutdown() called.");
    }

    // Class access methods

    /**
     * Determines whether the binding is already authenticated against the bridge so that
     * any other communication can occur without an additional care about authentication.
     * <P>
     * This method automatically decides on availability of the stored authentication
     * information {@link VeluxBridge#authenticationToken} whether a (re-)authentication is possible.
     *
     * @return true if the bridge is authenticated; false otherwise.
     */
    private boolean isAuthenticated() {
        boolean success = (authenticationToken.length() > 0);
        logger.trace("isAuthenticated() returns {}.", success);
        return success;
    }

    /**
     * Declares the binding as unauthenticated against the bridge so that the next
     * communication will take care about (re-)authentication.
     */
    protected void resetAuthentication() {
        logger.trace("resetAuthentication() called.");
        authenticationToken = emptyAuthenticationToken;
        return;
    }

    /**
     * Prepare an authorization request and communicate it with the <b>Velux</b> veluxBridge.
     * If login is successful, the returned authorization token will be stored within this class
     * for any further communication via {@link#bridgeCommunicate} up
     * to an authorization with method {@link VeluxBridge#bridgeLogout}.
     *
     * @return true if the login was successful, and false otherwise.
     */
    public synchronized boolean bridgeLogin() {
        logger.trace("bridgeLogin() called.");

        Login bcp = bridgeAPI().login();
        bcp.setPassword(bridgeInstance.veluxBridgeConfiguration().password);
        if (bridgeCommunicate(bcp, false)) {
            logger.trace("bridgeLogin(): communication succeeded.");
            if (bcp.isCommunicationSuccessful()) {
                logger.trace("bridgeLogin(): storing authentication token for further access.");
                authenticationToken = bcp.getAuthToken();
                return true;
            }
        }
        return false;
    }

    /**
     * Prepare an authenticated deauthorization request and communicate it with the <b>Velux</b> veluxBridge.
     * The authorization token stored in this class will be destroyed, so that the
     * next communication has to start with {@link VeluxBridge#bridgeLogin}.
     *
     * @return true if the logout was successful, and false otherwise.
     */
    public synchronized boolean bridgeLogout() {
        logger.trace("bridgeLogout() called: emptying authentication token.");
        authenticationToken = "";

        Logout bcp = bridgeAPI().logout();
        if (bridgeCommunicate(bcp, false)) {
            logger.trace("bridgeLogout(): communication succeeded.");
            if (bcp.isCommunicationSuccessful()) {
                logger.trace("bridgeLogout(): logout successful.");
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the Basic I/O interface {@link VeluxBridge} and parameters
     * passed as arguments (see below) and provided by VeluxBridgeConfiguration.
     *
     * @param communication the intended communication,
     *            that is request and response interactions as well as appropriate URL definition.
     * @param useAuthentication whether to use authenticated communication.
     * @return true if communication was successful, and false otherwise.
     */
    private synchronized boolean bridgeCommunicate(BridgeCommunicationProtocol communication,
            boolean useAuthentication) {
        logger.trace("bridgeCommunicate({},{}authenticated) called.", communication.name(),
                useAuthentication ? "" : "un");

        if (!isAuthenticated()) {
            if (useAuthentication) {
                logger.trace("bridgeCommunicate(): no auth token available, aborting.");
                return false;
            } else {
                logger.trace("bridgeCommunicate(): no auth token available, continuing.");
            }
        }
        return bridgeDirectCommunicate(communication, useAuthentication);
    }

    /**
     * Initializes a client/server communication towards <b>Velux</b> Bridge
     * based on the Basic I/O interface {@link VeluxBridge} and parameters
     * passed as arguments (see below) and provided by VeluxBridgeConfiguration.
     * This method automatically decides to invoke a login communication before the
     * intended request if there has not been an authentication before.
     *
     * @param communication the intended communication, that is request and response interactions as well as appropriate
     *            URL definition.
     * @return true if communication was successful, and false otherwise.
     */
    public synchronized boolean bridgeCommunicate(BridgeCommunicationProtocol communication) {
        logger.trace("bridgeCommunicate({}) called.", communication.name());
        if (!isAuthenticated()) {
            bridgeLogin();
        }
        return bridgeCommunicate(communication, true);
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last communication.
     * <P>
     * If possible, it should be overwritten by protocol specific implementation.
     * </P>
     *
     * @return timestamp (default zero).
     */
    public long lastCommunication() {
        logger.trace("lastCommunication() returns zero.");
        return 0L;
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last successful communication.
     * <P>
     * If possible, it should be overwritten by protocol specific implementation.
     * </P>
     *
     * @return timestamp (default zero).
     */
    public long lastSuccessfulCommunication() {
        logger.trace("lastSuccessfulCommunication() returns zero.");
        return 0L;
    }

    /**
     * Provides information about the base-level communication method and
     * any kind of available gateway interaction.
     * <P>
     * For protocol-specific implementations this method has to be overwritten along the inheritance i.e.
     * with the protocol-specific class implementations.
     *
     * @return bridgeAPI of type {@link org.openhab.binding.velux.internal.bridge.common.BridgeAPI BridgeAPI}.
     */
    public abstract BridgeAPI bridgeAPI();

    /**
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the protocol-specific implementations with common parameters
     * passed as arguments (see below) and provided by VeluxBridgeConfiguration.
     * <P>
     * For protocol-specific implementations this method has to be overwritten along the inheritance i.e.
     * with the protocol-specific class implementations.
     *
     * @param communication Structure of interface type {@link BridgeCommunicationProtocol} describing the
     *            intended communication.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>success</b> of type boolean which signals the success of the communication.
     */
    protected abstract boolean bridgeDirectCommunicate(BridgeCommunicationProtocol communication,
            boolean useAuthentication);

    /**
     * Check is the last communication was a good one
     *
     * @return true if the last communication was a good one
     */
    public boolean lastCommunicationOk() {
        return lastCommunication() != 0 && lastSuccessfulCommunication() == lastCommunication();
    }
}
