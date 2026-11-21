// The one telegram stream of a bridge: call 4 fills the rows, then call 20 reads on from its last number. The stream
// writes into the row model, opens again after a break with a growing wait, and follows the end codes of the server.
// fetch, the timers and the clock come in as arguments; the browser's EventSource is never used (it cannot send the
// token header).

import { createSseParser } from './sse.js';
import { errorText, text } from './texts.js';

/** The rows call 4 asks for at the start: what the row model holds. */
export const START_ROWS = 2000;
/** The waits after a break, the last one repeated. */
export const BREAK_WAITS = Object.freeze([1000, 2000, 5000, 10000, 30000]);
/** The waits after too-many-streams, the last one repeated; a place frees itself within about 30 s. */
export const BUSY_WAITS = Object.freeze([10000, 20000, 30000]);
/** No event for this long is a break; the server sends alive every 10 s. */
export const SILENCE_MILLIS = 25000;

const STOP_CODES = new Set(['monitor-off', 'unknown-bridge', 'bad-input', 'signed-out', 'http-403', 'http-404']);
const WAIT_TEXTS = new Set(['too-many-streams', 'bridge-gone', 'slow-client']);

/** {@return the state as a text for the header; empty before the stream starts} */
export function stateText(state) {
    switch (state?.phase) {
        case 'connecting':
            return text('stream.connecting');
        case 'live':
            return text('stream.live');
        case 'waiting':
            return WAIT_TEXTS.has(state.code) ? errorText(state.code) : text('stream.retrying', Math.round((state.wait ?? 0) / 1000));
        case 'stopped':
            return text('stream.stopped', errorText(state.code));
        default:
            return '';
    }
}

function highest(numbers, start) {
    let top = start;
    for (const number of numbers) {
        if (Number.isSafeInteger(number) && number > top) {
            top = number;
        }
    }
    return top;
}

/**
 * @param {Object} options
 * @param {string} options.bridge the bridge's UID
 * @param options.rows the row model of rows.js
 * @param options.api the client of api.js (call 4)
 * @param options.auth the login of auth.js: token(), renew(), lost()
 * @param {Function} options.fetch
 * @param {URL} options.base the REST root
 * @param options.timers setTimeout and clearTimeout
 * @param {() => number} [options.now]
 * @returns the stream: start(), retry(), close(), state(), subscribe(listener) → leave()
 */
export function createStream({ bridge, rows, api, auth, fetch, base, timers, now = () => 0 }) {
    const listeners = new Set();
    let state = Object.freeze({ phase: 'idle' });
    let closed = false;
    let run = 0;
    let monitorId = null;
    let lastNumber = 0;
    let breaks = 0;
    let busy = 0;
    let waitTimer = null;
    let silenceTimer = null;
    let controller = null;
    let aliveAt = null;

    function setState(next) {
        state = Object.freeze(next);
        for (const listener of [...listeners]) {
            listener(state);
        }
    }

    function clearSilence() {
        if (silenceTimer !== null) {
            timers.clearTimeout(silenceTimer);
            silenceTimer = null;
        }
    }

    function clearTimers() {
        if (waitTimer !== null) {
            timers.clearTimeout(waitTimer);
            waitTimer = null;
        }
        clearSilence();
    }

    function abort() {
        const current = controller;
        controller = null;
        current?.abort();
    }

    function halt() {
        run++;
        clearTimers();
        abort();
    }

    function stop(code) {
        halt();
        setState({ phase: 'stopped', code });
    }

    function schedule(wait, code) {
        halt();
        const mine = run;
        setState(code === undefined ? { phase: 'waiting', wait } : { phase: 'waiting', code, wait });
        waitTimer = timers.setTimeout(() => {
            waitTimer = null;
            if (mine === run) {
                connect();
            }
        }, wait);
    }

    function broke(code) {
        const wait = BREAK_WAITS[Math.min(breaks, BREAK_WAITS.length - 1)];
        breaks++;
        schedule(wait, code);
    }

    function failed(code) {
        if (STOP_CODES.has(code)) {
            stop(code);
        } else {
            broke(code);
        }
    }

    function ended(code) {
        if (code === 'too-many-streams') {
            const wait = BUSY_WAITS[Math.min(busy, BUSY_WAITS.length - 1)];
            busy++;
            schedule(wait, code);
        } else if (code === 'monitor-off' || code === 'unknown-bridge' || code === 'bad-input') {
            stop(code);
        } else {
            broke(code);
        }
    }

    function address() {
        if (typeof bridge !== 'string' || bridge === '' || bridge === '.' || bridge === '..') {
            return null;
        }
        const url = new URL(`knx/bridges/${encodeURIComponent(bridge)}/telegrams/stream`, base);
        url.searchParams.set('after', String(lastNumber));
        url.searchParams.set('monitor', monitorId);
        return url.href;
    }

    /** {@return the event's code when it ends the stream, 'open' for open, otherwise null} */
    function handle(event) {
        let data;
        try {
            data = JSON.parse(event.data);
        } catch {
            return null;
        }
        if (data === null || typeof data !== 'object') {
            return null;
        }
        switch (event.event) {
            case 'open':
                if (typeof data.monitorId === 'string') {
                    monitorId = data.monitorId;
                }
                if (data.reset === true) {
                    rows.restart();
                    lastNumber = 0;
                }
                breaks = 0;
                busy = 0;
                setState({ phase: 'live' });
                return 'open';
            case 'telegram':
                rows.add(data);
                lastNumber = highest([data.number], lastNumber);
                return null;
            case 'outcome':
                if (Number.isSafeInteger(data.number) && typeof data.outcome === 'string') {
                    rows.setOutcome(data.number, data.outcome);
                }
                return null;
            case 'lost':
                rows.addLost(data.count);
                return null;
            case 'alive':
                aliveAt = now();
                return null;
            case 'end':
                return typeof data.code === 'string' && data.code !== '' ? data.code : 'internal';
            default:
                return null;
        }
    }

    function armSilence(mine, onSilence = abort) {
        clearSilence();
        silenceTimer = timers.setTimeout(() => {
            silenceTimer = null;
            if (mine === run) {
                onSilence();
            }
        }, SILENCE_MILLIS);
    }

    async function request(token, mine) {
        const url = address();
        if (url === null) {
            stop('bad-input');
            return null;
        }
        const own = new AbortController();
        controller = own;
        // a fetch that never gets its headers may never settle, even after the abort
        armSilence(mine, () => broke());
        let response;
        try {
            response = await fetch(url, {
                method: 'GET',
                headers: { 'X-OPENHAB-TOKEN': token, Accept: 'text/event-stream' },
                signal: own.signal,
            });
        } catch {
            if (mine === run) {
                broke('network');
            }
            return null;
        }
        return mine === run ? response : null;
    }

    async function tokenOf(get, mine) {
        try {
            return await get();
        } catch (error) {
            if (mine === run) {
                failed(error?.code === 'network' ? 'network' : 'signed-out');
            }
            return null;
        }
    }

    async function read(response, mine) {
        const body = response.body;
        if (!body || typeof body.getReader !== 'function') {
            broke('bad-answer');
            return;
        }
        const reader = body.getReader();
        let endCode = null;
        const parser = createSseParser((event) => {
            if (mine !== run || endCode !== null) {
                return;
            }
            armSilence(mine);
            const outcome = handle(event);
            if (outcome !== null && outcome !== 'open') {
                endCode = outcome;
            }
        });
        armSilence(mine);
        try {
            for (;;) {
                const { done, value } = await reader.read();
                if (mine !== run) {
                    return;
                }
                if (done) {
                    break;
                }
                parser.push(value);
                if (endCode !== null) {
                    break;
                }
            }
        } catch {
            if (mine !== run) {
                return;
            }
        }
        parser.end();
        if (endCode !== null) {
            reader.cancel().catch(() => {});
            ended(endCode);
        } else {
            broke();
        }
    }

    async function open(mine) {
        let token = await tokenOf(() => auth.token(), mine);
        if (token === null || mine !== run) {
            return;
        }
        let response = await request(token, mine);
        if (response === null) {
            return;
        }
        if (response.status === 401) {
            token = await tokenOf(() => auth.renew(), mine);
            if (token === null || mine !== run) {
                return;
            }
            response = await request(token, mine);
            if (response === null) {
                return;
            }
            if (response.status === 401) {
                stop('signed-out');
                auth.lost();
                return;
            }
        }
        if (!response.ok) {
            failed(`http-${response.status}`);
            return;
        }
        await read(response, mine);
    }

    async function connect() {
        const mine = ++run;
        setState({ phase: 'connecting' });
        if (monitorId === null) {
            let page;
            try {
                page = await api.telegrams(bridge, { limit: START_ROWS });
            } catch (error) {
                if (mine === run) {
                    failed(typeof error?.code === 'string' ? error.code : 'internal');
                }
                return;
            }
            if (mine !== run) {
                return;
            }
            if (typeof page?.monitorId !== 'string' || !Number.isSafeInteger(page.lastNumber)) {
                broke('bad-answer');
                return;
            }
            rows.replace(page.telegrams);
            monitorId = page.monitorId;
            lastNumber = page.lastNumber;
        }
        await open(mine);
    }

    return {
        /** Opens the stream when it is not open yet; a view calls it when it needs live rows. */
        start() {
            if (!closed && state.phase === 'idle') {
                connect();
            }
        },
        /** After a stop: starts again from call 4. */
        retry() {
            if (!closed && state.phase === 'stopped') {
                breaks = 0;
                busy = 0;
                monitorId = null;
                connect();
            }
        },
        /** Ends the stream for good: the request is aborted, every timer stopped, every listener dropped. */
        close() {
            closed = true;
            halt();
            listeners.clear();
            state = Object.freeze({ phase: 'closed' });
        },
        /** @returns {{phase: string, code?: string, wait?: number}} phase is idle, connecting, live, waiting, stopped or closed */
        state() {
            return state;
        },
        /** {@return the clock's time of the last alive event, or null} */
        aliveAt() {
            return aliveAt;
        },
        /** @returns {() => void} the call that removes the listener again */
        subscribe(listener) {
            listeners.add(listener);
            return () => {
                listeners.delete(listener);
            };
        },
        subscribers() {
            return listeners.size;
        },
    };
}
