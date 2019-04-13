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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
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
    private NeeoRecipe @Nullable [] recipes;

    /**
     * Creates the recipes from the given recipes
     *
     * @param recipes the recipes
     */
    NeeoRecipes(NeeoRecipe[] recipes) {
        Objects.requireNonNull(recipes, "recipes cannot be null");
        this.recipes = recipes;
    }

    /**
     * Gets the recipes.
     *
     * @return the recipes
     */
    public NeeoRecipe[] getRecipes() {
        final NeeoRecipe[] localRecipes = recipes;
        return localRecipes == null ? new NeeoRecipe[0] : localRecipes;
    }

    /**
     * Gets the recipe by key
     *
     * @param key the key
     * @return the recipe or null if none found
     */
    @Nullable
    public NeeoRecipe getRecipe(String key) {
        if (recipes == null || StringUtils.isEmpty(key)) {
            return null;
        }

        for (NeeoRecipe recipe : getRecipes()) {
            if (StringUtils.equalsIgnoreCase(key, recipe.getKey())) {
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
        if (recipes == null || StringUtils.isEmpty(key)) {
            return null;
        }

        for (NeeoRecipe recipe : getRecipes()) {
            if (StringUtils.equalsIgnoreCase(key, recipe.getScenarioKey())
                    && StringUtils.equalsIgnoreCase(type, recipe.getType())) {
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
        if (recipes == null || StringUtils.isEmpty(name)) {
            return null;
        }

        for (NeeoRecipe recipe : getRecipes()) {
            if (StringUtils.equalsIgnoreCase(name, recipe.getName())) {
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
