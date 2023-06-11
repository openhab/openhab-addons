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
package org.openhab.binding.somfymylink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfyMyLinkCommandSceneList extends SomfyMyLinkCommandShadeBase {
    public SomfyMyLinkCommandSceneList(String auth) {
        super("*.*", "mylink.scene.list", auth);
    }
}
