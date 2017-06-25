package org.openhab.binding.supla.internal.server;

import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("ALL")
public final class Token {
    private final String token;
    private final int validTimeInSeconds;
    private final LocalDateTime createDate;

    public Token(String token, int validTimeInSeconds, LocalDateTime createDate) {
        checkNotNull(token);
        checkArgument(!token.isEmpty());
        checkArgument(validTimeInSeconds > 0, validTimeInSeconds);
        this.token = token;
        this.validTimeInSeconds = validTimeInSeconds;
        this.createDate = checkNotNull(createDate);
    }

    public String getToken() {
        return token;
    }

    public int getValidTimeInSeconds() {
        return validTimeInSeconds;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public boolean isValid(LocalDateTime time) {
        return createDate.plusSeconds(validTimeInSeconds).isAfter(time);
    }

    public boolean isValid() {
        return isValid(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;

        Token token1 = (Token) o;

        if (validTimeInSeconds != token1.validTimeInSeconds) return false;
        if (!token.equals(token1.token)) return false;
        if (!createDate.equals(token1.createDate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", validTimeInSeconds=" + validTimeInSeconds +
                ", createDate=" + createDate +
                '}';
    }
}

