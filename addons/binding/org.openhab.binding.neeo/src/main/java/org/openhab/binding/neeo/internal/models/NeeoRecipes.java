/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * The model representing Neeo Recipes (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRecipes {

    /** The recipes. */
    private final NeeoRecipe[] recipes;

    /**
     * Instantiates a new neeo recipes.
     *
     * @param receipes the receipes
     */
    public NeeoRecipes(NeeoRecipe[] receipes) {
        this.recipes = receipes;
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
    public NeeoRecipe getRecipe(String key) {
        if (recipes == null || StringUtils.isEmpty(key)) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
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
    public NeeoRecipe getRecipeByScenarioKey(String key, String type) {
        if (recipes == null || StringUtils.isEmpty(key)) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
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
    public NeeoRecipe getRecipeByName(String name) {
        if (recipes == null || StringUtils.isEmpty(name)) {
            return null;
        }

        for (NeeoRecipe recipe : recipes) {
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
