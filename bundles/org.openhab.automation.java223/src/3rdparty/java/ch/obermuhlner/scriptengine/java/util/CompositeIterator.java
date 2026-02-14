package ch.obermuhlner.scriptengine.java.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link Iterator} that will iterate over several iterators.
 *
 * @param <T> the type of elements returned by this iterator
 */
public class CompositeIterator<T> implements Iterator<T> {
    private final Iterator<? extends T>[] iterators;
    private int iteratorIndex = 0;

    /**
     * Creates a {@link CompositeIterator} over the specified iterators.
     *
     * @param iterators the {@link Iterator}s
     */
    @SafeVarargs
    public CompositeIterator(Iterator<? extends T>... iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if (iteratorIndex >= iterators.length) {
            return false;
        }
        if (iterators[iteratorIndex].hasNext()) {
            return true;
        }
        iteratorIndex++;

        if (iteratorIndex >= iterators.length) {
            return false;
        }

        return iterators[iteratorIndex].hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return iterators[iteratorIndex].next();
    }
}
