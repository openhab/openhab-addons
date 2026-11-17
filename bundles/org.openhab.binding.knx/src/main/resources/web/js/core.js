// The client of openHAB's own REST calls that the wizard uses to create what a preview's steps say and to undo it:
// a Thing, an Item (also a group) or a link, each found by ids that are checked here and put into the path encoded.
// Nothing of an answer is read; the caller gets the status. The token goes as in the API client.

/** A Thing UID the wizard makes: knx:device:<bridge id>:<id>. */
export const THING_UID = /^knx:device:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$/;
/** A channel UID: a Thing UID plus :<id>. */
export const CHANNEL_UID = /^knx:device:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$/;
export const ITEM_NAME = /^[A-Za-z_][A-Za-z0-9_]*$/;
/** The kinds of a create step, in the order a plan makes them. */
export const KINDS = ['GROUP', 'THING', 'ITEM', 'LINK'];

export class CoreError extends Error {
    /**
     * @param {number} status the HTTP status, 0 when there was no answer or no call
     * @param {string} code bad-id, bad-body, network, signed-out, or renewed (a 401, the token is renewed, the call
     *     was not sent again)
     */
    constructor(status, code) {
        super(code);
        this.name = 'CoreError';
        this.status = status;
        this.code = code;
    }
}

function matches(pattern, value) {
    return typeof value === 'string' && pattern.test(value);
}

/**
 * {@return the path below the REST root for a kind and its ids, or throws bad-id}
 *
 * @param {boolean} [create] the path of the create call (for a Thing the list, not the Thing)
 */
export function corePath(kind, ids, create = false) {
    const id = (pattern, value) => {
        if (!matches(pattern, value)) {
            throw new CoreError(0, 'bad-id');
        }
        return encodeURIComponent(value);
    };
    switch (kind) {
        case 'THING': {
            const uid = id(THING_UID, ids?.uid);
            return create ? 'things' : `things/${uid}`;
        }
        case 'GROUP':
        case 'ITEM':
            return `items/${id(ITEM_NAME, ids?.itemName)}`;
        case 'LINK':
            return `links/${id(ITEM_NAME, ids?.itemName)}/${id(CHANNEL_UID, ids?.channelUID)}`;
        default:
            throw new CoreError(0, 'bad-id');
    }
}

function authFailure(error) {
    return new CoreError(0, error?.code === 'network' ? 'network' : 'signed-out');
}

/**
 * @param {{fetch: Function, base: URL, auth: {token: Function, renew: Function, lost: Function}}} options base is
 *     the REST root, new URL('../rest/', <page address>); auth is the object createApi takes
 */
export function createCore({ fetch, base, auth }) {
    async function call(method, path, body, { renew = true } = {}) {
        const url = new URL(path, base).href;
        let token;
        try {
            token = await auth.token();
        } catch (error) {
            throw authFailure(error);
        }
        const init = {
            method,
            headers: { 'X-OPENHAB-TOKEN': token, Accept: 'application/json', 'Content-Type': 'application/json' },
        };
        if (body !== undefined) {
            init.body = body;
        }
        let response;
        try {
            response = await fetch(url, init);
        } catch {
            throw new CoreError(0, 'network');
        }
        if (response.status === 401) {
            if (!renew) {
                auth.lost();
                throw new CoreError(401, 'signed-out');
            }
            try {
                await auth.renew();
            } catch (error) {
                throw authFailure(error);
            }
            throw new CoreError(401, 'renewed');
        }
        return response.status;
    }

    function json(body) {
        if (body === null || typeof body !== 'object' || Array.isArray(body)) {
            throw new CoreError(0, 'bad-body');
        }
        return JSON.stringify(body);
    }

    return {
        /** GET: 200 when it exists, 404 when the id is free. */
        get: async (kind, ids, options) => call('GET', corePath(kind, ids), undefined, options),
        /** POST for a Thing, PUT for an Item, a group or a link. */
        create: async (kind, ids, body, options) => {
            const path = corePath(kind, ids, true);
            return call(kind === 'THING' ? 'POST' : 'PUT', path, json(body), options);
        },
        remove: async (kind, ids, options) => call('DELETE', corePath(kind, ids), undefined, options),
    };
}
