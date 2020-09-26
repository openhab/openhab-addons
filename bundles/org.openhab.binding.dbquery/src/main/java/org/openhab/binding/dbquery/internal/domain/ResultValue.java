package org.openhab.binding.dbquery.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ResultValue {
    private final boolean correct;
    private final @Nullable Object result;

    private ResultValue(boolean correct, @Nullable Object result) {
        this.correct = correct;
        this.result = result;
    }

    public static ResultValue of(@Nullable Object result) {
        return new ResultValue(true, result);
    }

    public static ResultValue incorrect() {
        return new ResultValue(false, null);
    }

    public boolean isCorrect() {
        return correct;
    }

    public @Nullable Object getResult() {
        return result;
    }
}
