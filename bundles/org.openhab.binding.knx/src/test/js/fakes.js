// Stand-ins for the browser's fetch, storages and timers, for the page's tests. No network, no real waiting.

/** A storage like sessionStorage that records every key written or removed. */
export function createStorage(initial = {}) {
    const values = new Map(Object.entries(initial));
    const written = [];
    const removed = [];
    return {
        written,
        removed,
        values,
        getItem(key) {
            return values.has(key) ? values.get(key) : null;
        },
        setItem(key, value) {
            written.push(key);
            values.set(key, String(value));
        },
        removeItem(key) {
            removed.push(key);
            values.delete(key);
        },
        clear() {
            throw new Error('the page never clears a storage');
        },
        /** {@return every key the page wrote or removed} */
        touched() {
            return [...written, ...removed];
        },
    };
}

/** A read-only storage like the view of localStorage that main.js hands over; every write throws. */
export function createReadOnlyStorage(initial = {}) {
    const values = new Map(Object.entries(initial));
    const reads = [];
    const refuse = (what) => () => {
        throw new Error(`the page must not ${what} local storage`);
    };
    return {
        reads,
        getItem(key) {
            reads.push(key);
            return values.has(key) ? values.get(key) : null;
        },
        setItem: refuse('write'),
        removeItem: refuse('remove from'),
        clear: refuse('clear'),
    };
}

/** {@return a Response with a JSON body} */
export function json(status, body) {
    return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
}

/** {@return a Response without a body} */
export function empty(status) {
    return new Response(null, { status });
}

/**
 * A fetch that records every call and answers from the handler.
 *
 * @param {(call: {url: URL, method: string, headers: Object, body: *, init: Object}) => Response|Promise<Response>}
 *     handler; it may throw to stand for a network failure
 */
export function createFetch(handler) {
    const calls = [];
    const fetch = async (input, init = {}) => {
        const call = {
            url: new URL(String(input)),
            method: init.method ?? 'GET',
            headers: { ...(init.headers ?? {}) },
            body: init.body,
            init,
        };
        calls.push(call);
        return handler(call);
    };
    fetch.calls = calls;
    return fetch;
}

/** {@return a TypeError as the browser's fetch throws it when the network fails} */
export function networkFailure() {
    return new TypeError('Failed to fetch');
}

/** Lets every pending promise run. */
export async function settle() {
    for (let i = 0; i < 20; i++) {
        await new Promise((resolve) => setImmediate(resolve));
    }
}

/** Timers and a clock under the test's control. */
export function createTimers(start = 1_000_000) {
    let now = start;
    let nextId = 1;
    const pending = new Map();
    const timers = {
        setTimeout(callback, ms = 0) {
            const id = nextId++;
            pending.set(id, { callback, at: now + Math.max(0, ms), ms, repeat: false });
            return id;
        },
        clearTimeout(id) {
            pending.delete(id);
        },
        setInterval(callback, ms = 0) {
            const id = nextId++;
            pending.set(id, { callback, at: now + Math.max(1, ms), ms: Math.max(1, ms), repeat: true });
            return id;
        },
        clearInterval(id) {
            pending.delete(id);
        },
    };
    return {
        timers,
        now: () => now,
        /** {@return the delays of the timers that have not run, in ms from when they were set} */
        delays: () => [...pending.values()].map((timer) => timer.ms),
        count: () => pending.size,
        /** Moves the clock without running a timer, as a sleeping laptop does. */
        jump(ms) {
            now += ms;
        },
        /** Moves the clock on and runs every timer that falls due, letting promises settle after each. */
        async advance(ms) {
            const end = now + ms;
            for (;;) {
                let due = null;
                for (const [id, timer] of pending) {
                    if (timer.at <= end && (due === null || timer.at < due[1].at)) {
                        due = [id, timer];
                    }
                }
                if (due === null) {
                    break;
                }
                const [id, timer] = due;
                now = timer.at;
                if (timer.repeat) {
                    timer.at += timer.ms;
                } else {
                    pending.delete(id);
                }
                timer.callback();
                await settle();
            }
            now = end;
            await settle();
        },
    };
}

/**
 * The body of an event stream the test writes to; it ends with an error when the request's signal aborts, as a
 * browser's fetch body does.
 *
 * @param {AbortSignal} [signal]
 */
export function createEventBody(signal) {
    const encoder = new TextEncoder();
    let controller = null;
    let done = false;
    const stream = new ReadableStream({
        start(c) {
            controller = c;
        },
    });
    const finish = (how) => {
        if (!done) {
            done = true;
            how();
        }
    };
    signal?.addEventListener('abort', () => finish(() => controller.error(new DOMException('The operation was aborted.', 'AbortError'))));
    return {
        response: new Response(stream, { status: 200, headers: { 'Content-Type': 'text/event-stream' } }),
        /** Writes one event with its data as JSON. */
        send(event, data) {
            if (!done) {
                controller.enqueue(encoder.encode(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`));
            }
        },
        /** Writes text as it is. */
        raw(text) {
            if (!done) {
                controller.enqueue(encoder.encode(text));
            }
        },
        close() {
            finish(() => controller.close());
        },
        aborted: () => signal?.aborted === true,
    };
}
