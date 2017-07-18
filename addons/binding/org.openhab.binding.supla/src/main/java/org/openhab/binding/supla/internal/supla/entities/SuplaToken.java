package org.openhab.binding.supla.internal.supla.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class SuplaToken {
    private final String accessToken;
    private final int expiresIn;
    private final String tokenType;
    private final String scope;
    private final String refreshToken;

    public SuplaToken(String accessToken, int expiresIn, String tokenType, String scope, String refreshToken) {
        this.accessToken = requireNonNull(accessToken);
        checkArgument(expiresIn >= 1, "expires_in need to be grater than 0! Has " + expiresIn);
        this.expiresIn = expiresIn;
        this.tokenType = requireNonNull(tokenType);
        this.scope = requireNonNull(scope);
        this.refreshToken = requireNonNull(refreshToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaToken)) return false;

        SuplaToken that = (SuplaToken) o;

        if (expiresIn != that.expiresIn) return false;
        if (!accessToken.equals(that.accessToken)) return false;
        if (!tokenType.equals(that.tokenType)) return false;
        if (!scope.equals(that.scope)) return false;
        return refreshToken.equals(that.refreshToken);
    }

    @Override
    public int hashCode() {
        return accessToken.hashCode();
    }

    @Override
    public String toString() {
        return "SuplaToken{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", scope='" + scope + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
