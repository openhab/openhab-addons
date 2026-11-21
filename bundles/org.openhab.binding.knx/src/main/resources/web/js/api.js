// The client of the add-on's REST calls 1 to 19, 21 and 22. Every call carries the access token as X-OPENHAB-TOKEN
// (behind a proxy with its own basic auth, Authorization is the proxy's) and asks for JSON. Answers are handed on
// unchanged: a field without a value is absent, not null.

import { base64 } from './pkce.js';

/** The largest limit the server takes for a page of call 9 or 10 (PAGE_MAX in KnxRestService). */
export const PAGE_MAX = 10000;
/** The project languages the server takes (call 17). */
export const LANGUAGE = /^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$/;

const TELEGRAM_PARAMETERS = ['src', 'dst', 'type', 'dir', 'dpt', 'from', 'to', 'context', 'limit', 'after', 'monitor'];

export class KnxApiError extends Error {
    /**
     * @param {number} status the HTTP status, 0 when there was no answer
     * @param {string} code the add-on's error code, or network, signed-out, bad-answer, bad-language, http-<status>
     * @param {string} [text] the server's text, when the body was the add-on's error body
     */
    constructor(status, code, text = '') {
        super(code);
        this.name = 'KnxApiError';
        this.status = status;
        this.code = code;
        this.text = text;
    }
}

function pick(values, names) {
    const picked = {};
    for (const name of names) {
        if (values && Object.hasOwn(values, name)) {
            picked[name] = values[name];
        }
    }
    return picked;
}

async function failure(response) {
    let body = null;
    try {
        body = await response.json();
    } catch {
        body = null;
    }
    if (body && typeof body.code === 'string' && typeof body.text === 'string') {
        return new KnxApiError(response.status, body.code, body.text);
    }
    return new KnxApiError(response.status, `http-${response.status}`);
}

function authFailure(error) {
    return new KnxApiError(0, error?.code === 'network' ? 'network' : 'signed-out');
}

/**
 * @param {{fetch: Function, base: URL, auth: {token: Function, renew: Function, lost: Function}}} options base is
 *     the REST root, new URL('../rest/', <page address>)
 */
export function createApi({ fetch, base, auth }) {
    const root = new URL('knx/', base);

    function address(parts, query) {
        // URL resolves "." and ".." (also as %2E) as steps of the path, so such a part would reach another call.
        if (parts.some((part) => typeof part !== 'string' || part === '' || part === '.' || part === '..')) {
            throw new KnxApiError(0, 'bad-input');
        }
        const url = new URL(parts.map((part) => encodeURIComponent(part)).join('/'), root);
        for (const [name, value] of Object.entries(query ?? {})) {
            if (value !== undefined && value !== null && value !== '') {
                url.searchParams.set(name, String(value));
            }
        }
        return url.href;
    }

    async function send(url, method, options, token) {
        const init = {
            method,
            headers: { 'X-OPENHAB-TOKEN': token, Accept: 'application/json', ...options.headers },
        };
        if (options.body !== undefined) {
            init.body = options.body;
        }
        try {
            return await fetch(url, init);
        } catch {
            throw new KnxApiError(0, 'network');
        }
    }

    async function call(method, parts, options = {}) {
        const url = address(parts, options.query);
        let token;
        try {
            token = await auth.token();
        } catch (error) {
            throw authFailure(error);
        }
        let response = await send(url, method, options, token);
        if (response.status === 401) {
            try {
                token = await auth.renew();
            } catch (error) {
                throw authFailure(error);
            }
            response = await send(url, method, options, token);
            if (response.status === 401) {
                auth.lost();
                throw new KnxApiError(401, 'signed-out');
            }
        }
        if (!response.ok) {
            throw await failure(response);
        }
        if (response.status === 204) {
            return undefined;
        }
        try {
            return await response.json();
        } catch {
            throw new KnxApiError(response.status, 'bad-answer');
        }
    }

    async function allPages(load) {
        const items = [];
        let total = Infinity;
        while (items.length < total) {
            const page = await load({ offset: items.length, limit: PAGE_MAX });
            const got = Array.isArray(page?.items) ? page.items : [];
            total = Number.isInteger(page?.total) ? page.total : 0;
            if (got.length === 0) {
                break;
            }
            for (const item of got) {
                items.push(item);
            }
        }
        return items;
    }

    function upload(parts, file, password, query) {
        const headers = { 'Content-Type': 'application/octet-stream' };
        if (typeof password === 'string' && password !== '') {
            headers['X-KNX-Password'] = base64(new TextEncoder().encode(password));
        }
        return call('PUT', parts, { body: file, headers, query });
    }

    const bridge = (b, ...more) => ['bridges', b, ...more];
    const project = (b, ...more) => bridge(b, 'project', ...more);

    const api = {
        /** 1 */
        bridges: () => call('GET', ['bridges']),
        /** 2 */
        bridge: (b) => call('GET', bridge(b)),
        /** 3 */
        monitor: (b) => call('GET', bridge(b, 'monitor')),
        /** 4: filter holds src dst type dir dpt from to context limit after monitor */
        telegrams: (b, filter) => call('GET', bridge(b, 'telegrams'), { query: pick(filter, TELEGRAM_PARAMETERS) }),
        /** 5 */
        clearTelegrams: (b) => call('DELETE', bridge(b, 'telegrams')),
        /** 6 */
        lastValues: (b) => call('GET', bridge(b, 'last-values')),
        /** Call 24: one group read or write. Only the Group Monitor's send bar calls it, after its two presses. */
        groupTelegram: (b, request) =>
            call('POST', bridge(b, 'group-telegram'), {
                body: JSON.stringify(pick(request, ['address', 'action', 'dpt', 'value'])),
                headers: { 'Content-Type': 'application/json' },
            }),
        /** 7 */
        keyring: (b) => call('GET', bridge(b, 'keyring')),
        /** 8 */
        project: (b) => call('GET', project(b)),
        /** 9, one page */
        groupAddresses: (b, page) => call('GET', project(b, 'group-addresses'), { query: pick(page, ['offset', 'limit']) }),
        /** 9, every page: the list of all group addresses */
        allGroupAddresses: (b) => allPages((page) => api.groupAddresses(b, page)),
        /** 23: one group address with usedBy, the channels and Items that hold it */
        groupAddress: (b, address) => call('GET', project(b, 'group-address'), { query: { address } }),
        /** 10, one page */
        devices: (b, page) => call('GET', project(b, 'devices'), { query: pick(page, ['offset', 'limit']) }),
        /** 10, every page: the list of all devices */
        allDevices: (b) => allPages((page) => api.devices(b, page)),
        /** 11: device is an individual address or the device's project id */
        device: (b, device) => call('GET', project(b, 'devices', device)),
        /** 12 */
        deviceChannels: (b, device) => call('GET', project(b, 'devices', device, 'channels')),
        /** 13 */
        channels: (b, groupAddresses) =>
            call('POST', project(b, 'channels'), {
                body: JSON.stringify({ groupAddresses }),
                headers: { 'Content-Type': 'application/json' },
            }),
        /** 14 */
        topology: (b) => call('GET', project(b, 'topology')),
        /** 15 */
        locations: (b) => call('GET', project(b, 'locations')),
        /** 16 */
        check: (b) => call('GET', project(b, 'check')),
        /**
         * 17. The password goes as Base64 of its UTF-8 bytes, only when one was typed; the language only when chosen,
         * and a language the server would refuse is not sent at all.
         */
        uploadProject: async (b, file, { password, language } = {}) => {
            const chosen = typeof language === 'string' ? language.trim() : '';
            if (chosen !== '' && !LANGUAGE.test(chosen)) {
                throw new KnxApiError(0, 'bad-language');
            }
            return upload(project(b), file, password, { name: file.name, language: chosen });
        },
        /** 18 */
        removeProject: (b) => call('DELETE', project(b)),
        /** 19 */
        uploadKeyring: (b, file, { password } = {}) => upload(bridge(b, 'keyring'), file, password),
        /** 21: request holds groupAddresses, functions, spaces, devices and readAtStart */
        proposal: (b, request) =>
            call('POST', project(b, 'proposal'), {
                body: JSON.stringify(request),
                headers: { 'Content-Type': 'application/json' },
            }),
        /** 22: request holds the edited things, the roomGroups to make and readAtStart */
        preview: (b, request) =>
            call('POST', project(b, 'proposal', 'preview'), {
                body: JSON.stringify(request),
                headers: { 'Content-Type': 'application/json' },
            }),
    };
    return api;
}
