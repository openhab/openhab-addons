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
package org.openhab.binding.velux.internal.things;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Velux</B> scene representation.
 * <P>
 * Combined set of information with references towards multiple Velux product states.
 * <P>
 * Methods in handle this type of information:
 * <UL>
 * <LI>{@link #VeluxScene(String, int, boolean, VeluxProductState[])} to create a new scene.</LI>
 * <LI>{@link #VeluxScene(VeluxScene)} to duplicate a scene.</LI>
 * <LI>{@link #getName} to retrieve the name of this scene.</LI>
 * <LI>{@link #getBridgeSceneIndex()} to retrieve the index of this scene.</LI>
 * <LI>{@link #toString()} to retrieve a human-readable description of this scene.</LI>
 * </UL>
 *
 * @see VeluxProductState
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxScene {
    private final Logger logger = LoggerFactory.getLogger(VeluxScene.class);

    // Public definition

    public static final VeluxScene UNKNOWN = new VeluxScene();

    // Type definitions

    public static class SceneName {

        private static final SceneName UNKNOWN = new SceneName(VeluxBindingConstants.UNKNOWN);

        private String name;

        @Override
        public String toString() {
            return name;
        }

        public SceneName(String name) {
            this.name = name;
        }

        public boolean equals(SceneName anotherName) {
            return this.name.equals(anotherName.toString());
        }
    }

    public static class SceneBridgeIndex {

        private static final SceneBridgeIndex UNKNOWN = new SceneBridgeIndex(0);

        private int id;

        @Override
        public String toString() {
            return String.valueOf(id);
        }

        public int toInt() {
            return id;
        }

        private SceneBridgeIndex(int id) {
            this.id = id;
        }
    }

    // Class internal

    private SceneName name;
    private SceneBridgeIndex bridgeSceneIndex;
    private boolean silent;
    private VeluxProductState[] productStates;

    // Constructor

    /**
     * Constructor
     *
     * just for the dummy VeluxProduct.
     */
    private VeluxScene() {
        logger.trace("VeluxScene() created.");
        this.name = SceneName.UNKNOWN;
        this.bridgeSceneIndex = SceneBridgeIndex.UNKNOWN;
        this.silent = false;
        this.productStates = new VeluxProductState[0];
    }

    public VeluxScene(String name, int sceneBridgeIndex, boolean silentOperation, VeluxProductState[] actions) {
        this.name = new SceneName(name);
        this.bridgeSceneIndex = new SceneBridgeIndex(sceneBridgeIndex);
        this.silent = silentOperation;
        this.productStates = actions;
    }

    public VeluxScene(VeluxScene scene) {
        this.name = new SceneName(scene.name.toString());
        this.bridgeSceneIndex = new SceneBridgeIndex(scene.bridgeSceneIndex.toInt());
        this.silent = scene.silent;
        this.productStates = scene.productStates;
    }
    // Class access methods

    public SceneName getName() {
        return this.name;
    }

    public SceneBridgeIndex getBridgeSceneIndex() {
        return this.bridgeSceneIndex;
    }

    @Override
    public String toString() {
        return String.format("Scene \"%s\" (index %d) with %ssilent mode and %d actions", this.name,
                this.bridgeSceneIndex.toInt(), this.silent ? "" : "non-", this.productStates.length);
    }
}
