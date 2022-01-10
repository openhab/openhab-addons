/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing Neeo Recipes (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRecipes {

    /** The recipes. */
    private NeeoRecipe[] recipes;

    /**
     * Creates the recipes from the given recipes
     *
     * @param recipes the recipes
     */
    NeeoRecipes(NeeoRecipe[] recipes) {
        this.recipes = recipes;
    }

    /**
     * Gets the recipes.
     *
     * @return the recipes
     */
    public NeeoRecipe[] getRecipes() {
        return recipes;
    }

    /**
     * Gets the recipe by key
     *
     * @param key the key
     * @return the recipe or null if none found
     */
    @Nullable
    public NeeoRecipe getRecipe(String key) {
        if (key.isEmpty()) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
            if (key.equalsIgnoreCase(recipe.getKey())) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Gets the recipe by a scenario key and recipe type
     *
     * @param key the key
     * @param type the recipe type
     * @return the recipe or null if none found
     */
    @Nullable
    public NeeoRecipe getRecipeByScenarioKey(String key, String type) {
        if (key.isEmpty()) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
            if (key.equalsIgnoreCase(recipe.getScenarioKey()) && type.equalsIgnoreCase(recipe.getType())) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Gets the recipe by name
     *
     * @param name the recipe name
     * @return the recipe or null if none found
     */
    @Nullable
    public NeeoRecipe getRecipeByName(String name) {
        if (name.isEmpty()) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
            if (name.equalsIgnoreCase(recipe.getName())) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NeeoRecipes [recipes=" + Arrays.toString(recipes) + "]";
    }
}
