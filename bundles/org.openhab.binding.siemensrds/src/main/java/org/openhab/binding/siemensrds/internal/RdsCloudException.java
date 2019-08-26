/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

/**
 * Custom Cloud Server communication exception
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class RdsCloudException extends RuntimeException {

    private static final long serialVersionUID = -7048044632627280917L;

    public RdsCloudException(String message)
    {
      super(message);
    }

}
