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
package org.openhab.binding.boschindego.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link AuthorizationListener} is used for notifying
 * {@link org.openhab.binding.boschindego.internal.handler.BoschAccountHandler}
 * when authorization state has changed and for notifying
 * {@link org.openhab.binding.boschindego.internal.handler.BoschIndegoHandler}
 * when authorization flow is completed.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface AuthorizationListener {
    /**
     * Called upon successful OAuth authorization.
     */
    void onSuccessfulAuthorization();

    /**
     * Called upon failed OAuth authorization.
     */
    void onFailedAuthorization(Throwable throwable);

    /**
     * Called upon successful completion of OAuth authorization flow.
     */
    void onAuthorizationFlowCompleted();
}
