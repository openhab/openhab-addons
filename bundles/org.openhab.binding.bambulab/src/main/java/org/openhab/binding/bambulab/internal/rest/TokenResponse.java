package org.openhab.binding.bambulab.internal.rest;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public record TokenResponse(
        String accessToken,
        String refreshToken,
        int expiresIn,
        int refreshExpiresIn,
        String tfaKey,
        String accessMethod,
        String loginType) {
}
