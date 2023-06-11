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
package org.openhab.binding.mybmw.internal.dto.remote;

/**
 * The {@link ExecutionError} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ExecutionError {
    public String title;// ": "Etwas ist schiefgelaufen",
    public String description;// ": "Die folgenden Einschränkungen verbieten die Ausführung von Remote Services: Aus
                              // Sicherheitsgründen sind Remote Services nicht verfügbar, wenn die Fahrbereitschaft
                              // eingeschaltet ist. Remote Services können nur mit einem ausreichenden Ladezustand
                              // durchgeführt werden. Die Remote Services „Verriegeln“ und „Entriegeln“ können nur
                              // ausgeführt werden, wenn die Fahrertür geschlossen und der Türstatus bekannt ist.",
    public String presentationType;// ": "PAGE",
    public int iconId;// ": 60217,
    public boolean isRetriable;// ": true,
    public String errorDetails;// ": "NACK"
}
