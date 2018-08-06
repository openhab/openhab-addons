/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The model represents a token score result given to the NEEO brain (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial Contribution
 * @param <T> the type that will be scored
 */
@NonNullByDefault
public class TokenScoreResult<T> {

    /** The item being scored */
    private final T item;

    /** The id of the result */
    private final int id;

    /** The score result */
    private final double score;

    /** The maximum score found */
    private final int maxScore;

    /**
     * Creates a new token score result
     *
     * @param item the item
     * @param id the id
     * @param score the score
     * @param maxScore the maximum score
     */
    public TokenScoreResult(T item, int id, double score, int maxScore) {
        Objects.requireNonNull(item, "item cannot be null");

        this.item = item;
        this.id = id;
        this.score = score;
        this.maxScore = maxScore;
    }

    /**
     * Gets the item being scored
     *
     * @return the item
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets the id of the score
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the score
     *
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * Gets the maximum score found
     *
     * @return the maximum score
     */
    public int getMaxScore() {
        return maxScore;
    }

    @Override
    public String toString() {
        return "TokenScoreResult [item=" + item + ", id=" + id + ", score=" + score + ", maxScore=" + maxScore + "]";
    }
}
