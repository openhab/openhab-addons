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
package org.openhab.binding.lametrictime.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

/**
 * Integration test suit.
 *
 * @author Gregory Moyer - Initial contribution
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({ "**/*IT.class" })
@NonNullByDefault
public class AllIntegrationTestsSuite {
    // Execute all integration tests
}
