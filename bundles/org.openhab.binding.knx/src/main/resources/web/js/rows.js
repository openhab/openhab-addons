// The row model of one bridge: the telegrams the page holds, the page filter, pause, the last telegram per group
// address, and the kept list of call 9. The stream writes into it; the views read it and listen to it.

import { text } from './texts.js';

/** How many calls 11 run side by side when every device's objects are read. */
export const DETAILS_AT_ONCE = 4;

/** The rows the page holds; older ones leave. */
export const ROWS_MAX = 2000;

const TIME = /^(\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:\.(\d{1,9}))?Z$/;
const THREE_LEVEL = /^(\d{1,2})\/(\d)\/(\d{1,3})$/;
const TWO_LEVEL = /^(\d{1,2})\/(\d{1,4})$/;
const FREE = /^\d{1,5}$/;
const NO_FILTER = Object.freeze({ text: '', type: '', dir: '' });

/**
 * Reads the server's time, an instant with up to nine digits of fraction, with the page's own code.
 *
 * @returns {Date|null} null when the text is not such an instant
 */
export function parseTime(value) {
    const match = TIME.exec(typeof value === 'string' ? value : '');
    if (!match) {
        return null;
    }
    const [year, month, day, hour, minute, second] = match.slice(1, 7).map(Number);
    const millis = Number(`${match[7] ?? ''}000`.slice(0, 3));
    if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || minute > 59 || second > 59) {
        return null;
    }
    const date = new Date(Date.UTC(year, month - 1, day, hour, minute, second, millis));
    return date.getUTCDate() === day ? date : null;
}

function two(n) {
    return String(n).padStart(2, '0');
}

/** {@return HH:MM:SS.mmm of the browser's local time} */
export function clockText(date) {
    return `${two(date.getHours())}:${two(date.getMinutes())}:${two(date.getSeconds())}.${String(date.getMilliseconds()).padStart(3, '0')}`;
}

/** {@return YYYY-MM-DD HH:MM:SS.mmm of the browser's local time} */
export function dateText(date) {
    return `${date.getFullYear()}-${two(date.getMonth() + 1)}-${two(date.getDate())} ${clockText(date)}`;
}

/** {@return the name shown for the row's source: an own row without a name is openHAB} */
export function sourceNameOf(telegram) {
    return telegram.sourceName ?? (telegram.direction === 'OUT' ? text('monitor.openhab') : undefined);
}

function rawAddress(address) {
    const text = String(address);
    let match = THREE_LEVEL.exec(text);
    if (match) {
        return Number(match[1]) * 2048 + Number(match[2]) * 256 + Number(match[3]);
    }
    match = TWO_LEVEL.exec(text);
    if (match) {
        return Number(match[1]) * 2048 + Number(match[2]);
    }
    return FREE.test(text) ? Number(text) : Infinity;
}

/** Sorts group addresses by their raw number, as the server does; anything else comes last, by text. */
export function addressOrder(a, b) {
    const difference = rawAddress(a) - rawAddress(b);
    if (difference < 0 || difference > 0) {
        return difference;
    }
    return String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0;
}

function isTelegram(value) {
    return typeof value === 'object' && value !== null && Number.isSafeInteger(value.number);
}

// the server's own rule for its last values (TelegramMonitor)
function keepsLast(telegram) {
    return typeof telegram.destination === 'string' && telegram.type !== 'READ' && telegram.security !== 'NOT_DECRYPTED';
}

function lower(value) {
    return typeof value === 'string' ? value.toLowerCase() : '';
}

/**
 * @param {Object} [options]
 * @param {string} [options.bridge] the bridge's UID, for the list of call 9
 * @param [options.api] the client of api.js
 * @param {number} [options.max] the rows held, ROWS_MAX unless a test gives another
 */
export function createRows({ bridge, api, max = ROWS_MAX } = {}) {
    let entries = [];
    const byNumber = new Map();
    const last = new Map();
    const listeners = new Set();
    let key = 0;
    let paused = false;
    let fresh = 0;
    let filter = NO_FILTER;
    let addresses = null;
    let details = null;
    let deviceList = null;
    let listing = null;
    let places = null;
    let detailOf = new Map();

    /** Reads once and keeps the promise; a read that failed is made again by the next call. */
    function kept(held, set, read) {
        if (held === null) {
            const loading = Promise.resolve().then(read);
            set(loading);
            loading.catch(() => set(null, loading));
            return loading;
        }
        return held;
    }

    function devicesOf(list) {
        return (Array.isArray(list) ? list : []).filter((device) => typeof device === 'object' && device !== null);
    }

    function allDevices() {
        if (deviceList !== null) {
            return Promise.resolve(deviceList);
        }
        const read = detailOf;
        return kept(
            listing,
            (next, failed) => {
                if (next !== null || listing === failed) {
                    listing = next;
                }
            },
            async () => {
                const devices = devicesOf(await api.allDevices(bridge));
                if (read === detailOf) {
                    deviceList = devices;
                }
                return devices;
            },
        );
    }

    function notify(kind) {
        for (const listener of [...listeners]) {
            listener(kind);
        }
    }

    function push(entry) {
        entries.push(entry);
        if (entries.length > max) {
            for (const gone of entries.splice(0, entries.length - max)) {
                if (gone.telegram) {
                    byNumber.delete(gone.telegram.number);
                }
            }
        }
    }

    function empty() {
        entries = [];
        byNumber.clear();
        fresh = 0;
    }

    function take(telegram) {
        if (!isTelegram(telegram) || byNumber.has(telegram.number)) {
            return false;
        }
        const entry = { key: ++key, telegram, version: 0 };
        byNumber.set(telegram.number, entry);
        push(entry);
        if (keepsLast(telegram)) {
            last.set(telegram.destination, telegram);
        }
        if (paused) {
            fresh++;
        }
        return true;
    }

    function seed(telegrams) {
        for (const telegram of Array.isArray(telegrams) ? telegrams : []) {
            if (isTelegram(telegram) && keepsLast(telegram) && !last.has(telegram.destination)) {
                last.set(telegram.destination, telegram);
            }
        }
    }

    function marker(name, more = {}) {
        push({ key: ++key, marker: name, version: 0, ...more });
    }

    function matches(telegram) {
        if (filter.type !== '' && telegram.type !== filter.type) {
            return false;
        }
        if (filter.dir !== '' && telegram.direction !== filter.dir) {
            return false;
        }
        if (filter.text === '') {
            return true;
        }
        const names = [telegram.source, telegram.destination, sourceNameOf(telegram), telegram.destinationName];
        return names.some((name) => lower(name).includes(filter.text));
    }

    return {
        /** Adds a telegram of the stream; a number already held is dropped. {@return true when it was added} */
        add(telegram) {
            const added = take(telegram);
            if (added) {
                notify('rows');
            }
            return added;
        },
        /** Empties the rows and fills them with the telegrams of call 4, oldest first. */
        replace(telegrams) {
            empty();
            for (const telegram of Array.isArray(telegrams) ? telegrams : []) {
                take(telegram);
            }
            fresh = 0;
            notify('rows');
        },
        /** Sets the outcome of an own row whose telegram went out as PENDING. */
        setOutcome(number, outcome) {
            const entry = byNumber.get(number);
            if (!entry || entry.telegram.outcome === outcome) {
                return;
            }
            const old = entry.telegram;
            entry.telegram = { ...old, outcome };
            entry.version++;
            if (last.get(old.destination) === old) {
                last.set(old.destination, entry.telegram);
            }
            notify('rows');
        },
        /** Adds a marker row: count telegrams left the server's buffer before they could be sent. */
        addLost(count) {
            marker('lost', { count: Number(count) || 0 });
            notify('rows');
        },
        /** The monitor's numbers started again: the rows are emptied, the last values are kept. */
        restart() {
            empty();
            marker('restarted');
            notify('rows');
        },
        /** "Clear": the rows and the last values, as the server forgets both. */
        clear() {
            empty();
            last.clear();
            notify('clear');
        },
        /** {@return every entry, oldest first: {key, version, telegram} or {key, version, marker, count?}} */
        entries() {
            return entries;
        },
        /** {@return how many entries are held} */
        held() {
            return entries.length;
        },
        /** {@return the entries that pass the page filter, newest first; markers always pass} */
        visible() {
            const shown = [];
            for (let at = entries.length - 1; at >= 0; at--) {
                const entry = entries[at];
                if (!entry.telegram || matches(entry.telegram)) {
                    shown.push(entry);
                }
            }
            return shown;
        },
        /** @param {{text?: string, type?: string, dir?: string}} change the parts of the filter that change */
        setFilter(change) {
            const next = { ...filter };
            for (const name of ['type', 'dir']) {
                if (change && Object.hasOwn(change, name)) {
                    next[name] = typeof change[name] === 'string' ? change[name] : '';
                }
            }
            if (change && Object.hasOwn(change, 'text')) {
                next.text = lower(change.text).trim();
            }
            filter = Object.freeze(next);
            notify('filter');
        },
        filter() {
            return { ...filter };
        },
        pause() {
            paused = true;
            fresh = 0;
            notify('pause');
        },
        resume() {
            paused = false;
            fresh = 0;
            notify('pause');
        },
        paused() {
            return paused;
        },
        /** {@return the telegrams added since the pause} */
        newCount() {
            return fresh;
        },
        /** {@return the last telegram with a value of the group address, or undefined} */
        last(address) {
            return last.get(address);
        },
        /** {@return the map from group address to its last telegram; the caller only reads it} */
        lastValues() {
            return last;
        },
        /** Takes last values read from the server for addresses the page has none for yet. */
        seedLast(telegrams) {
            seed(telegrams);
            notify('last');
        },
        /**
         * {@return the list of call 9, every page, read once and kept until forgetProject()}; the last values it holds
         * are taken when it is read, so a kept list does not bring back what "Clear" emptied
         */
        groupAddresses() {
            if (addresses === null) {
                const loading = Promise.resolve()
                    .then(() => api.allGroupAddresses(bridge))
                    .then((list) => {
                        if (addresses === loading) {
                            seed(Array.isArray(list) ? list.map((address) => address?.last) : []);
                            notify('last');
                        }
                        return list;
                    });
                addresses = loading;
                loading.catch(() => {
                    if (addresses === loading) {
                        addresses = null;
                    }
                });
            }
            return addresses;
        },
        /**
         * Every device of call 10 with its group objects of call 11, read a few at a time and kept until
         * forgetProject(). A device whose read fails is left out, its error is in problems and its entry of the list
         * in failed; the next call reads only those again.
         *
         * @param {Array<Object>} [known] the devices of call 10 or 14, when the caller has them: call 10 is not made
         * @returns {Promise<{devices: Array<Object>, problems: Array<Error>, failed: Array<Object>}>}
         */
        deviceDetails(known) {
            if (details === null) {
                const read = detailOf;
                const loading = Promise.resolve()
                    .then(() => deviceList ?? (Array.isArray(known) ? known : allDevices()))
                    .then(async (list) => {
                        const devices = devicesOf(list);
                        if (read === detailOf) {
                            deviceList = devices;
                        }
                        const problems = [];
                        let next = 0;
                        const worker = async () => {
                            while (next < devices.length) {
                                const id = String(devices[next].id ?? devices[next].address);
                                next++;
                                if (read.has(id)) {
                                    continue;
                                }
                                try {
                                    read.set(id, await api.device(bridge, id));
                                } catch (error) {
                                    problems.push(error);
                                }
                            }
                        };
                        await Promise.all(Array.from({ length: Math.min(DETAILS_AT_ONCE, devices.length) }, worker));
                        if (problems.length > 0 && details === loading) {
                            details = null;
                        }
                        const idOf = (device) => String(device.id ?? device.address);
                        return { devices: devices.map((device) => read.get(idOf(device))).filter((device) => device !== undefined), problems, failed: devices.filter((device) => !read.has(idOf(device))) };
                    });
                details = loading;
                loading.catch(() => {
                    if (details === loading) {
                        details = null;
                    }
                });
            }
            return details;
        },
        /** {@return the list of call 10, every page, read once and kept until forgetProject()} */
        devices() {
            return allDevices();
        },
        /** {@return the building structure of call 15, read once and kept until forgetProject()} */
        locations() {
            return kept(
                places,
                (next, failed) => {
                    if (next !== null || places === failed) {
                        places = next;
                    }
                },
                () => api.locations(bridge),
            );
        },
        /** After an upload or remove of the project: the lists of the project are read again when next needed. */
        forgetProject() {
            addresses = null;
            details = null;
            deviceList = null;
            listing = null;
            places = null;
            detailOf = new Map();
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
        close() {
            listeners.clear();
        },
    };
}
