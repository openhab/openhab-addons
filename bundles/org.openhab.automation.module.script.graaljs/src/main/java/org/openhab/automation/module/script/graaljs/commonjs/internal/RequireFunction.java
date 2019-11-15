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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.PolyglotException;

/**
 * Simple interface describing the require function
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface RequireFunction {
  Object require(@Nullable String module) throws ScriptException, PolyglotException;
}
