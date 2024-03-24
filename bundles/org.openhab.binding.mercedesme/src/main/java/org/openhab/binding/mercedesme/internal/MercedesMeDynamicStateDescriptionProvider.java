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
package org.openhab.binding.mercedesme.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Component;

/**
 * StateDescriptionProvider to change Channel State Pattern
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, MercedesMeDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class MercedesMeDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

}
