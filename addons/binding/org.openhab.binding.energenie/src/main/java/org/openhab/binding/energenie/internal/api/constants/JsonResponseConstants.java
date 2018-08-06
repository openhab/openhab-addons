/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.constants;

/**
 * Contains all Energenie Mi|Home Response keys used in the communication with the server
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class JsonResponseConstants {

    public static final String RESPONSE_STATUS_KEY = "status";
    public static final String RESPONSE_MESSAGE_KEY = "message";
    public static final String RESPONSE_ERROR_KEY = "errors";
    public static final String TIME_KEY = "time";
    public static final String DATA_KEY = "data";

    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_NOT_FOUND = "not-found";
    public static final String RESPONSE_ACCESS_DENIED = "access-denied";
    public static final String RESPONSE_PARAMETER_ERROR = "parameter-error";
    public static final String RESPONSE_VALIDATION_ERROR = "validation-error";
    public static final String RESPONSE_MAINTENANCE = "maintenance";
    public static final String RESPONSE_INTERNAL_SERVER_ERROR = "internal-server-error";
}
