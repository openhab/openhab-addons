/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

/**
 * Interface for data objects that have an error code in their response.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public interface HasErrorResponse {
    /**
     * @return returns the object containing the error response
     */
    ErrorResponse getErrorResponse();
}
