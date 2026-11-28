// The login of the page. firstStep() decides from the address and the storages which way to go; createAuth() carries
// the ways out with the fetch, storages and timers it is given, keeps the access token in a variable only, and
// renews it. The page's own refresh token lives in session storage; MainUI's is only ever read.

import { base64url, challenge, newVerifier } from './pkce.js';

/** Way 3: sign in with MainUI's login of the same browser. */
export const USE_MAINUI_LOGIN = true;
export const ADMIN_ROLE = 'administrator';
export const RENEW_BEFORE_SECONDS = 60;
export const RETRY_SECONDS = 30;

const KEY_STATE = 'knx.loginState';
const KEY_VERIFIER = 'knx.loginVerifier';
const KEY_ROUTE = 'knx.loginRoute';
const KEY_REFRESH = 'knx.refreshToken';
const KEY_NO_MAINUI = 'knx.noMainUi';
const MAINUI_REFRESH = 'openhab.ui:refreshToken';

const STATE_BYTES = 16;
const DEFAULT_SECONDS = 3600;
const ANSWER_PARAMETERS = ['code', 'state', 'error', 'error_description', 'error_uri'];

export class KnxAuthError extends Error {
    /** @param {string} code signed-out or network */
    constructor(code) {
        super(code);
        this.name = 'KnxAuthError';
        this.code = code;
    }
}

function withoutAnswer(url) {
    const clean = new URL(url.href);
    for (const name of ANSWER_PARAMETERS) {
        clean.searchParams.delete(name);
    }
    return clean.href;
}

function mainUiToken(local) {
    try {
        return local.getItem(MAINUI_REFRESH);
    } catch {
        return null;
    }
}

function mainUiAllowed(session, local, useMainUi) {
    return useMainUi && session.getItem(KEY_NO_MAINUI) === null && Boolean(mainUiToken(local));
}

/**
 * Decides the first step of the login; reads the storages, changes nothing.
 *
 * @param {string} address the page's address as the browser shows it
 * @returns {{way: string, address?: string, code?: string, verifier?: string, route?: string}} way is one of
 *     "login-error" (the address holds error), "foreign" (an answer that is not this page's), "code" (way 1),
 *     "own" (way 2), "mainui" (way 3), "signed-out" (way 4); address is the address without the answer
 */
export function firstStep(address, session, local, useMainUi = USE_MAINUI_LOGIN) {
    const url = new URL(address);
    const parameters = url.searchParams;
    if (parameters.has('error')) {
        return { way: 'login-error', address: withoutAnswer(url) };
    }
    if (parameters.has('code') || parameters.has('state')) {
        const code = parameters.get('code');
        const state = parameters.get('state');
        const kept = session.getItem(KEY_STATE);
        const verifier = session.getItem(KEY_VERIFIER);
        if (code && state && kept && verifier && state === kept) {
            return { way: 'code', address: withoutAnswer(url), code, verifier, route: session.getItem(KEY_ROUTE) ?? '' };
        }
        return { way: 'foreign', address: withoutAnswer(url) };
    }
    if (session.getItem(KEY_REFRESH)) {
        return { way: 'own' };
    }
    if (mainUiAllowed(session, local, useMainUi)) {
        return { way: 'mainui' };
    }
    return { way: 'signed-out' };
}

/**
 * @param {Object} options
 * @param {Function} options.fetch
 * @param {URL} options.base the REST root, new URL('../rest/', <page address>)
 * @param {string} options.address the page's address when it was loaded
 * @param {Storage} options.session the browser's session storage
 * @param {{getItem: Function}} options.local a read-only view of local storage
 * @param {{setTimeout: Function, clearTimeout: Function}} options.timers
 * @param {() => number} options.now milliseconds, as Date.now
 * @param {(length: number) => Uint8Array} options.randomBytes
 * @param {SubtleCrypto} [options.subtle] crypto.subtle where the browser has it
 * @param {(address: string) => void} options.replaceAddress shows another address without loading it
 * @param {(messageKey: string) => void} [options.onSignedOut] the login ended on its own
 * @param {boolean} [options.useMainUi]
 */
export function createAuth(options) {
    const { fetch, session, local, timers, now, randomBytes, subtle, replaceAddress } = options;
    const onSignedOut = options.onSignedOut ?? (() => {});
    const useMainUi = options.useMainUi ?? USE_MAINUI_LOGIN;
    const loaded = new URL(options.address);
    const pageAddress = loaded.origin + loaded.pathname;
    const origin = loaded.origin;
    const tokenAddress = new URL('auth/token', options.base).href;
    const logoutAddress = new URL('auth/logout', options.base).href;

    let access = null;
    let kind = null;
    let user = null;
    let timer = null;
    let renewing = null;

    function post(url, form, token) {
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded', Accept: 'application/json' };
        if (token) {
            headers['X-OPENHAB-TOKEN'] = token;
        }
        return fetch(url, { method: 'POST', headers, body: new URLSearchParams(form).toString() });
    }

    /**
     * @returns {Promise<{result: string, answer?: Object, refreshToken?: string}>} result is ok, refused (400, 401),
     *     bad (200 without a token answer) or unreachable (no answer, another status)
     */
    async function trade(form, keepRefreshToken) {
        let response;
        try {
            response = await post(tokenAddress, form);
        } catch {
            return { result: 'unreachable' };
        }
        if (response.status === 400 || response.status === 401) {
            return { result: 'refused' };
        }
        if (response.status !== 200) {
            return { result: 'unreachable' };
        }
        let answer;
        try {
            answer = await response.json();
        } catch {
            return { result: 'bad' };
        }
        if (!answer || typeof answer.access_token !== 'string' || answer.access_token === '') {
            return { result: 'bad' };
        }
        const refreshToken = keepRefreshToken ? answer.refresh_token : undefined;
        delete answer.refresh_token;
        return { result: 'ok', answer, refreshToken };
    }

    function stopTimer() {
        if (timer !== null) {
            timers.clearTimeout(timer);
            timer = null;
        }
    }

    function startTimer(ms) {
        stopTimer();
        timer = timers.setTimeout(() => {
            timer = null;
            renew().catch(() => {});
        }, ms);
    }

    function forget() {
        stopTimer();
        access = null;
        kind = null;
        user = null;
    }

    function isAdmin(answer) {
        return Array.isArray(answer.user?.roles) && answer.user.roles.includes(ADMIN_ROLE);
    }

    function use(answer, sessionKind) {
        const seconds = Number.isFinite(answer.expires_in) && answer.expires_in > 0 ? answer.expires_in : DEFAULT_SECONDS;
        const wait = Math.max((seconds - RENEW_BEFORE_SECONDS) * 1000, seconds * 500);
        access = { token: answer.access_token, renewAt: now() + wait };
        kind = sessionKind;
        user = { name: typeof answer.user?.name === 'string' ? answer.user.name : '' };
        startTimer(wait);
    }

    async function logout(token) {
        const refreshToken = session.getItem(KEY_REFRESH);
        try {
            await post(logoutAddress, { refresh_token: refreshToken ?? '' }, token);
        } catch {
            // the key goes whatever the answer
        } finally {
            session.removeItem(KEY_REFRESH);
        }
    }

    /** A token answer of the page's own session: used, or, without the administrator role, signed out again. */
    async function useOwn(answer) {
        if (!isAdmin(answer)) {
            forget();
            await logout(answer.access_token);
            return { signedIn: false, message: 'login.needsAdmin' };
        }
        use(answer, 'own');
        return { signedIn: true, user: { ...user } };
    }

    async function mainUiWay() {
        if (!mainUiAllowed(session, local, useMainUi)) {
            return { signedIn: false };
        }
        const traded = await trade({
            grant_type: 'refresh_token',
            refresh_token: mainUiToken(local) ?? '',
            client_id: origin,
            redirect_uri: origin,
        });
        if (traded.result !== 'ok') {
            return { signedIn: false };
        }
        if (!isAdmin(traded.answer)) {
            return { signedIn: false, message: 'login.needsAdmin' };
        }
        use(traded.answer, 'mainui');
        return { signedIn: true, user: { ...user } };
    }

    async function ownWay() {
        const traded = await trade({
            grant_type: 'refresh_token',
            refresh_token: session.getItem(KEY_REFRESH) ?? '',
            client_id: pageAddress,
            redirect_uri: pageAddress,
        });
        if (traded.result === 'refused') {
            session.removeItem(KEY_REFRESH);
            return mainUiWay();
        }
        if (traded.result !== 'ok') {
            return { signedIn: false, message: 'login.unreachable', retry: true };
        }
        return useOwn(traded.answer);
    }

    function dropLoginKeys() {
        session.removeItem(KEY_STATE);
        session.removeItem(KEY_VERIFIER);
        session.removeItem(KEY_ROUTE);
    }

    async function codeWay(step) {
        const traded = await trade(
            {
                grant_type: 'authorization_code',
                code: step.code,
                client_id: pageAddress,
                redirect_uri: pageAddress,
                code_verifier: step.verifier,
            },
            true,
        );
        if (traded.result !== 'ok') {
            return { signedIn: false, message: traded.result === 'unreachable' ? 'login.unreachable' : 'login.failed' };
        }
        if (typeof traded.refreshToken !== 'string' || traded.refreshToken === '') {
            return { signedIn: false, message: 'login.failed' };
        }
        session.setItem(KEY_REFRESH, traded.refreshToken);
        const outcome = await useOwn(traded.answer);
        return outcome.signedIn && step.route ? { ...outcome, route: step.route } : outcome;
    }

    /**
     * Signs in by the first way that works.
     *
     * @returns {Promise<{signedIn: boolean, user?: {name: string}, route?: string, message?: string,
     *     retry?: boolean}>} route is the route kept before the login page; message a key of texts.js
     */
    async function start() {
        forget();
        const step = firstStep(loaded.href, session, local, useMainUi);
        if (step.address !== undefined) {
            replaceAddress(step.address);
        }
        switch (step.way) {
            case 'login-error':
                dropLoginKeys();
                return { signedIn: false };
            case 'foreign':
                dropLoginKeys();
                return { signedIn: false, message: 'login.foreign' };
            case 'code':
                dropLoginKeys();
                return codeWay(step);
            case 'own':
                return ownWay();
            case 'mainui':
                return mainUiWay();
            default:
                return { signedIn: false };
        }
    }

    function end(messageKey, quiet) {
        forget();
        if (!quiet) {
            onSignedOut(messageKey);
        }
    }

    async function renewNow(quiet) {
        const renewed = kind;
        if (renewed === null) {
            throw new KnxAuthError('signed-out');
        }
        const own = renewed === 'own';
        const refreshToken = own ? session.getItem(KEY_REFRESH) : mainUiToken(local);
        const traded = refreshToken
            ? await trade({
                  grant_type: 'refresh_token',
                  refresh_token: refreshToken,
                  client_id: own ? pageAddress : origin,
                  redirect_uri: own ? pageAddress : origin,
              })
            : { result: 'refused' };
        if (kind !== renewed) {
            throw new KnxAuthError('signed-out');
        }
        if (traded.result === 'unreachable' || traded.result === 'bad') {
            startTimer(RETRY_SECONDS * 1000);
            throw new KnxAuthError('network');
        }
        if (traded.result === 'refused') {
            if (own) {
                session.removeItem(KEY_REFRESH);
            }
            end('login.ended', quiet);
            throw new KnxAuthError('signed-out');
        }
        if (!isAdmin(traded.answer)) {
            end('login.needsAdmin', quiet);
            if (own) {
                await logout(traded.answer.access_token);
            }
            throw new KnxAuthError('signed-out');
        }
        use(traded.answer, renewed);
        return access.token;
    }

    function renewOnce(quiet) {
        if (renewing === null) {
            renewing = renewNow(quiet).finally(() => {
                renewing = null;
            });
        }
        return renewing;
    }

    /** Renews the access token now; one renewal at a time. */
    function renew() {
        return renewOnce(false);
    }

    async function currentToken(quiet) {
        if (access === null) {
            throw new KnxAuthError('signed-out');
        }
        return now() >= access.renewAt ? renewOnce(quiet) : access.token;
    }

    /** {@return a valid access token, renewed first when its time has come} */
    function token() {
        return currentToken(false);
    }

    /** The server refused a renewed token: the login has ended. */
    function lost() {
        end('login.ended', false);
    }

    /**
     * The page's own session is ended in openHAB as well; after MainUI's login only the access token is forgotten.
     * Either way way 3 is skipped in this tab from now on.
     */
    async function signOut() {
        const own = kind === 'own';
        session.setItem(KEY_NO_MAINUI, '1');
        if (own) {
            let token = null;
            try {
                token = await currentToken(true);
            } catch {
                token = null;
            }
            await logout(token);
        }
        forget();
    }

    /**
     * Keeps a new verifier, state and the route in session storage.
     *
     * @param {string} route the route to show after the login
     * @returns {Promise<string>} the address of openHAB's login page to go to
     */
    async function signIn(route) {
        const verifier = newVerifier(randomBytes);
        const state = base64url(randomBytes(STATE_BYTES));
        session.setItem(KEY_STATE, state);
        session.setItem(KEY_VERIFIER, verifier);
        session.setItem(KEY_ROUTE, typeof route === 'string' ? route : '');
        const login = new URL('../auth', pageAddress);
        login.search = new URLSearchParams({
            response_type: 'code',
            client_id: pageAddress,
            redirect_uri: pageAddress,
            scope: 'admin',
            state,
            code_challenge: await challenge(verifier, subtle),
            code_challenge_method: 'S256',
        }).toString();
        return login.href;
    }

    return {
        start,
        token,
        renew,
        lost,
        signOut,
        signIn,
        signedIn: () => access !== null,
        user: () => (user === null ? null : { ...user }),
    };
}
