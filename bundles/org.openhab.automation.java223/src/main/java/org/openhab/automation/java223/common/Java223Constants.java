/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.automation.java223.common;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * Constants.
 *
 * @author Gwendal Roulleau - initial contribution
 *
 */
@NonNullByDefault
public class Java223Constants {

    public static final String JAVA_FILE_TYPE = "java";
    public static final String JAR_FILE_TYPE = "jar";
    public static final String JAVA_223_IDENTIFIER = "java223-identifier";
    public static final String SERVICE_GETTER = "serviceGetter";
    public static final String BINDINGS = "bindings";

    // Placeholder (sort of) to express emptiness
    public static final String ANNOTATION_DEFAULT = "\u0002\u0003";

    public static final Path LIB_DIR = Path.of(OpenHAB.getConfigFolder(), "automation", "lib", JAVA_FILE_TYPE);
}
