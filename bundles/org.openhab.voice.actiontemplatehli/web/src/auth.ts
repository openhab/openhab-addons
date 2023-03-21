import axios from "axios";
export class OHAuthHelper {
    accessToken = "";
    currentTokenExpireTime?: number;
    refreshAccessTokenTimeoutRef: any;
    refreshOnVisibilityChangeFn: (() => void) | null = null;
    constructor(private options: { useCookie?: boolean, path?: string, setup?: boolean, ohUrl?: () => Promise<string> } = {}) {
    }
    getRedirectUri() {
        return window.location.origin + (this.options.path ?? '');
    }
    async authorize() {
        const PkceChallenge = await import('pkce-challenge');
        const pkceChallenge = PkceChallenge.default()
        const authState = (this.options.setup ? 'setup-' : '') + generateUUID();
        sessionStorage.setItem('openhab.ui:codeVerifier', pkceChallenge.code_verifier)
        sessionStorage.setItem('openhab.ui:authState', authState)
        var redirectUri = this.getRedirectUri();
        (window.location as any) = `${await this.getUrl()}/auth?` + urlEncodeObject({
            response_type: "code",
            client_id: redirectUri,
            redirect_uri: redirectUri,
            scope: "admin",
            code_challenge_method: "S256",
            code_challenge: pkceChallenge.code_challenge,
            state: authState,
        });
    }
    /**
     * 
     * @param {(err: Error|null, { access_token: string, refresh_token: string }) => void} callback 
     * @param {boolean} noRefresh 
     */
    async refreshAccessToken(callback: (err: Error | null, result: { access_token: string, refresh_token: string | null } | null) => void, refreshNow = true) {
        try {
            const refreshToken = this.getRefreshToken();
            if (!refreshToken) {
                throw new Error("Missing refresh token");
            }
            if (this.currentTokenExpireTime == null || refreshNow) {
                var redirectUri = this.getRedirectUri();
                const payload = urlEncodeObject({
                    grant_type: "refresh_token",
                    client_id: redirectUri,
                    redirect_uri: redirectUri,
                    refresh_token: refreshToken,
                });
                const resp = await axios.post(`${await this.getUrl()}/rest/auth/token`, payload, {
                    headers: {
                        "content-type": "application/x-www-form-urlencoded",
                        accept: "application/json",
                    },
                });
                this.accessToken = resp.data.access_token;
                this.setRefreshToken(resp.data.refresh_token);
                this.currentTokenExpireTime = new Date().getTime() + resp.data.expires_in * 950;
            }
            const result = { access_token: this.getAccessToken(), refresh_token: this.getRefreshToken() };
            if (callback) {
                if (this.currentTokenExpireTime == null || isNaN(this.currentTokenExpireTime)) {
                    throw new Error("Missing token expire time");
                }
                callback(null, result);
                if (this.refreshAccessTokenTimeoutRef) {
                    clearTimeout(this.refreshAccessTokenTimeoutRef);
                }
                const newRefreshFn = () => this.refreshAccessToken(callback);
                this.refreshAccessTokenTimeoutRef = setTimeout(
                    newRefreshFn,
                    this.currentTokenExpireTime - new Date().getTime(),
                );
                if (this.refreshOnVisibilityChangeFn) {
                    document.removeEventListener("visibilitychange", this.refreshOnVisibilityChangeFn);
                }
                this.refreshOnVisibilityChangeFn = () => {
                    if (!document.hidden && this.currentTokenExpireTime && this.currentTokenExpireTime < new Date().getTime()) {
                        console.debug('Refreshing expired token')
                        this.refreshAccessToken(callback);
                    }
                };
                document.addEventListener("visibilitychange", this.refreshOnVisibilityChangeFn);
            }
            return result;
        } catch (error) {
            console.error(error);
            if (callback) {
                callback(error as Error, null);
            }
        }
        return null;
    }
    hasAccessToken() {
        return !!this.getAccessToken().length;
    }
    getAccessToken() {
        return this.accessToken;
    }
    clearAccessToken() {
        this.accessToken = "";
    }
    getRefreshToken() {
        return localStorage.getItem("openhab.ui:refreshToken") || null;
    }
    setRefreshToken(refreshToken: string) {
        localStorage.setItem("openhab.ui:refreshToken", refreshToken);
    }
    async tryExchangeAuthorizationCode() {
        const { code, state } = getQueryParams();
        if (code && state) {
            const authState = sessionStorage.getItem('openhab.ui:authState')
            sessionStorage.removeItem('openhab.ui:authState');
            if (authState !== state) {
                throw new Error('Invalid state');
            }
            if (window.history) {
                window.history.replaceState(null, window.document.title, window.location.href.replace('?code=' + code, '').replace('&state=' + authState, ''))
            }
            const codeVerifier = sessionStorage.getItem('openhab.ui:codeVerifier');
            if (!codeVerifier) {
                throw new Error('Missing code verifier.');
            }
            sessionStorage.removeItem('openhab.ui:codeVerifier');
            var redirectUri = this.getRedirectUri();
            const payload = urlEncodeObject({
                'grant_type': 'authorization_code',
                'client_id': redirectUri,
                'redirect_uri': redirectUri,
                'code': code,
                'code_verifier': codeVerifier
            });
            this.clearAccessToken();
            const resp = await axios.post(`${await this.getUrl()}/rest/auth/token?useCookie=${this.options.useCookie ?? false}`, payload, {
                headers: {
                    "content-type": "application/x-www-form-urlencoded",
                    accept: "application/json",
                },
            });
            this.accessToken = resp.data.access_token;
            this.setRefreshToken(resp.data.refresh_token);
            this.currentTokenExpireTime = new Date().getTime() + resp.data.expires_in * 950;
            return true;
        }
        return false;
    }
    private async getUrl() {
        return (this.options.ohUrl?.() ?? Promise.resolve(''))
    }
}
function getQueryParams() {
    const query = window.location.search.substring(1);
    return query.split('&').reduce((params, paramText) => {
        var pair = paramText.split('=');
        params[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        return params;
    }, {} as { [key: string]: string });
}
function urlEncodeObject(payloadObj: { [key: string]: string }) {
    return Object.entries(payloadObj).reduce(
        (text, [key, value]) => {
            if (text.length)
                text = `${text}&`;
            text = `${text}${encodeURIComponent(key)}=${encodeURIComponent(
                value
            )}`;
            return text;
        },
        ""
    );
}
function generateUUID(mask?: string) {
    const uuidMask = mask ? mask : 'xxxxxxxxxx';
    let
        d = new Date().getTime(),
        d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
    return uuidMask.replace(/[xy]/g, c => {
        let r = Math.random() * 16;
        if (d > 0) {
            r = (d + r) % 16 | 0;
            d = Math.floor(d / 16);
        } else {
            r = (d2 + r) % 16 | 0;
            d2 = Math.floor(d2 / 16);
        }
        return (c == 'x' ? r : (r & 0x7 | 0x8)).toString(16);
    });
};