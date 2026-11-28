import { test } from 'node:test';
import assert from 'node:assert/strict';

import { DETAILS_AT_ONCE, ROWS_MAX, addressOrder, clockText, createRows, dateText, parseTime, sourceNameOf } from '../web/js/rows.js';

function row(number, more = {}) {
    return {
        number,
        time: '2026-11-15T07:01:02.123456789Z',
        source: '7.7.3',
        destination: `31/7/${number % 200}`,
        type: 'WRITE',
        direction: 'IN',
        payload: '01',
        value: 'ON',
        security: 'PLAIN',
        plainOnSecuredAddress: false,
        ...more,
    };
}

function numbers(rows) {
    return rows.visible().map((entry) => entry.telegram?.number ?? entry.marker);
}

test('rows: at most 2000 rows, the oldest leave', () => {
    const rows = createRows();
    for (let n = 1; n <= 5000; n++) {
        rows.add(row(n));
    }
    assert.equal(ROWS_MAX, 2000);
    assert.equal(rows.entries().length, 2000);
    assert.equal(rows.entries()[0].telegram.number, 3001);
    assert.equal(rows.visible()[0].telegram.number, 5000);
    assert.equal(rows.visible().length, 2000);
    assert.equal(rows.held(), 2000);
});

test('rows: none and one row', () => {
    const rows = createRows();
    assert.deepEqual(rows.visible(), []);
    rows.add(row(1));
    assert.deepEqual(numbers(rows), [1]);
});

test('rows: a number already held is dropped', () => {
    const rows = createRows();
    assert.equal(rows.add(row(5)), true);
    assert.equal(rows.add(row(5, { value: 'OFF' })), false);
    assert.equal(rows.entries().length, 1);
    assert.equal(rows.entries()[0].telegram.value, 'ON');
    assert.equal(rows.add({ number: 'x' }), false);
    assert.equal(rows.add(null), false);
});

test('rows: every entry has its own key', () => {
    const rows = createRows();
    rows.add(row(1));
    rows.addLost(3);
    rows.add(row(2));
    const keys = rows.entries().map((entry) => entry.key);
    assert.equal(new Set(keys).size, 3);
});

test('rows: pause keeps collecting and counts the new rows; going on resets the count', () => {
    const rows = createRows();
    const seen = [];
    rows.subscribe((kind) => seen.push(kind));
    rows.add(row(1));
    rows.pause();
    assert.equal(rows.paused(), true);
    rows.add(row(2));
    rows.add(row(3));
    rows.add(row(3));
    assert.equal(rows.newCount(), 2);
    assert.deepEqual(numbers(rows), [3, 2, 1]);
    rows.resume();
    assert.equal(rows.paused(), false);
    assert.equal(rows.newCount(), 0);
    assert.deepEqual(seen, ['rows', 'pause', 'rows', 'rows', 'pause']);
});

test('rows: the text filter looks at source, destination and their names, without regard to case', () => {
    const rows = createRows();
    rows.add(row(1, { source: '7.7.1', destination: '31/7/1', destinationName: 'Kitchen light' }));
    rows.add(row(2, { source: '7.7.2', sourceName: 'Push button HALL', destination: '31/7/2' }));
    rows.add(row(3, { source: '7.7.250', direction: 'OUT', destination: '31/7/3' }));
    rows.add(row(4, { source: '7.7.4', destination: '31/7/40', value: 'kitchen' }));
    rows.setFilter({ text: 'KITCHEN' });
    assert.deepEqual(numbers(rows), [1]);
    rows.setFilter({ text: 'hall' });
    assert.deepEqual(numbers(rows), [2]);
    rows.setFilter({ text: 'openhab' });
    assert.deepEqual(numbers(rows), [3]);
    rows.setFilter({ text: '31/7/4' });
    assert.deepEqual(numbers(rows), [4]);
    rows.setFilter({ text: '7.7.2' });
    assert.deepEqual(numbers(rows), [3, 2]);
    rows.setFilter({ text: '  ' });
    assert.deepEqual(numbers(rows), [4, 3, 2, 1]);
    assert.deepEqual(rows.filter(), { text: '', type: '', dir: '' });
});

test('rows: the type and direction filters, alone and together, and markers stay', () => {
    const rows = createRows();
    rows.add(row(1, { type: 'READ', value: undefined, payload: undefined }));
    rows.add(row(2, { type: 'RESPONSE' }));
    rows.addLost(2);
    rows.add(row(5, { direction: 'OUT' }));
    rows.add(row(6, { type: 'READ', direction: 'OUT' }));
    rows.setFilter({ type: 'READ' });
    assert.deepEqual(numbers(rows), [6, 'lost', 1]);
    rows.setFilter({ type: '', dir: 'OUT' });
    assert.deepEqual(numbers(rows), [6, 5, 'lost']);
    rows.setFilter({ type: 'READ', dir: 'OUT' });
    assert.deepEqual(numbers(rows), [6, 'lost']);
    rows.setFilter({ type: 'RESPONSE', dir: 'IN', text: 'nothing like this' });
    assert.deepEqual(numbers(rows), ['lost']);
});

test('rows: the last telegram per address leaves out reads and rows that could not be decrypted, and survives the cap', () => {
    const rows = createRows();
    rows.add(row(1, { destination: '31/7/250', value: 'OFF' }));
    rows.add(row(2, { destination: '31/7/250', type: 'READ', value: undefined }));
    rows.add(row(3, { destination: '31/7/251', security: 'NOT_DECRYPTED', type: 'UNKNOWN', value: undefined }));
    for (let n = 10; n < 10 + ROWS_MAX; n++) {
        rows.add(row(n, { destination: '31/7/1' }));
    }
    assert.equal(rows.entries().some((entry) => entry.telegram?.number === 1), false);
    assert.equal(rows.last('31/7/250').number, 1);
    assert.equal(rows.last('31/7/251'), undefined);
    assert.equal(rows.last('31/7/1').number, 9 + ROWS_MAX);
    assert.deepEqual([...rows.lastValues().keys()].sort(), ['31/7/1', '31/7/250']);
});

test('rows: a seeded last value does not replace a newer one from the stream', () => {
    const rows = createRows();
    rows.add(row(7, { destination: '31/7/1', value: 'ON' }));
    rows.seedLast([row(3, { destination: '31/7/1', value: 'OFF' }), row(4, { destination: '31/7/2', value: '21.5' })]);
    assert.equal(rows.last('31/7/1').value, 'ON');
    assert.equal(rows.last('31/7/2').value, '21.5');
});

test('rows: Clear empties the rows and the last values; a restart of the monitor empties the rows only', () => {
    const rows = createRows();
    rows.add(row(1, { destination: '31/7/1' }));
    rows.restart();
    assert.deepEqual(numbers(rows), ['restarted']);
    assert.equal(rows.last('31/7/1').number, 1);
    assert.equal(rows.add(row(1)), true);
    rows.clear();
    assert.deepEqual(rows.visible(), []);
    assert.equal(rows.lastValues().size, 0);
});

test('rows: replace fills the rows from the start of the stream', () => {
    const rows = createRows();
    rows.add(row(99));
    rows.pause();
    rows.add(row(100));
    rows.replace([row(1), row(2), row(2), row(3, { destination: '31/7/77' })]);
    assert.deepEqual(numbers(rows), [3, 2, 1]);
    assert.equal(rows.last('31/7/77').number, 3);
    assert.equal(rows.newCount(), 0);
});

test('rows: an outcome changes its row and the row\'s last value', () => {
    const rows = createRows();
    rows.add(row(1, { direction: 'OUT', outcome: 'PENDING', destination: '31/7/1' }));
    const before = rows.entries()[0];
    const version = before.version;
    rows.setOutcome(1, 'SENT');
    assert.equal(rows.entries()[0].telegram.outcome, 'SENT');
    assert.ok(rows.entries()[0].version > version);
    assert.equal(rows.last('31/7/1').outcome, 'SENT');
    rows.setOutcome(42, 'SENT');
    assert.equal(rows.entries().length, 1);
});

test('rows: a lost marker is a row of its own', () => {
    const rows = createRows();
    rows.add(row(1));
    rows.addLost(12);
    assert.deepEqual(rows.visible()[0].marker, 'lost');
    assert.equal(rows.visible()[0].count, 12);
});

test('rows: the list of call 9 is read once, kept, read again after an upload or remove, and not kept after a failure', async () => {
    let calls = 0;
    let fail = true;
    const api = {
        async allGroupAddresses(bridge) {
            calls++;
            assert.equal(bridge, 'knx:ip:line7');
            if (fail) {
                throw new Error('network');
            }
            return [{ address: '31/7/1', name: 'Light' }];
        },
    };
    const rows = createRows({ bridge: 'knx:ip:line7', api });
    await assert.rejects(rows.groupAddresses());
    fail = false;
    const [one, two] = await Promise.all([rows.groupAddresses(), rows.groupAddresses()]);
    assert.equal(one, two);
    assert.equal(calls, 2);
    await rows.groupAddresses();
    assert.equal(calls, 2);
    rows.forgetProject();
    await rows.groupAddresses();
    assert.equal(calls, 3);
});

test('rows: the last values of the list of call 9 are taken once, when it is read, and do not come back after Clear', async () => {
    const api = {
        calls: 0,
        async allGroupAddresses() {
            api.calls++;
            return [
                { address: '31/7/1', name: 'Light', last: row(3, { destination: '31/7/1', value: 'OFF' }) },
                { address: '31/7/2', name: 'Blind' },
            ];
        },
    };
    const rows = createRows({ bridge: 'knx:ip:line7', api });
    rows.add(row(9, { destination: '31/7/5' }));
    await rows.groupAddresses();
    assert.equal(rows.last('31/7/1').value, 'OFF');
    rows.clear();
    await rows.groupAddresses();
    assert.equal(api.calls, 1);
    assert.equal(rows.lastValues().size, 0);
});

test('rows: a listener can leave, and close() removes every listener', () => {
    const rows = createRows();
    const seen = [];
    const leave = rows.subscribe(() => seen.push('a'));
    rows.subscribe(() => seen.push('b'));
    assert.equal(rows.subscribers(), 2);
    rows.add(row(1));
    leave();
    rows.add(row(2));
    rows.close();
    rows.add(row(3));
    assert.deepEqual(seen, ['a', 'b', 'b']);
    assert.equal(rows.subscribers(), 0);
});

test('rows: the time is read with up to nine digits of fraction, cut to three', () => {
    assert.equal(parseTime('2026-11-15T07:01:02.123456789Z').toISOString(), '2026-11-15T07:01:02.123Z');
    assert.equal(parseTime('2026-11-15T07:01:02.120Z').toISOString(), '2026-11-15T07:01:02.120Z');
    assert.equal(parseTime('2026-11-15T07:01:02.5Z').toISOString(), '2026-11-15T07:01:02.500Z');
    assert.equal(parseTime('2026-11-15T07:01:02Z').toISOString(), '2026-11-15T07:01:02.000Z');
    for (const bad of ['', 'yesterday', '2026-13-15T07:01:02Z', '2026-11-15T25:01:02Z', '2026-11-15T07:01:02.1234567890Z', null, 5]) {
        assert.equal(parseTime(bad), null, String(bad));
    }
});

test('rows: the clock text is HH:MM:SS.mmm of local time and the date text holds the full date', () => {
    const date = parseTime('2026-11-15T07:01:02.045Z');
    assert.match(clockText(date), /^\d\d:\d\d:\d\d\.045$/);
    assert.equal(clockText(date).slice(0, 2), String(date.getHours()).padStart(2, '0'));
    assert.match(dateText(date), /^\d{4}-\d\d-\d\d \d\d:\d\d:\d\d\.045$/);
    assert.equal(dateText(date).slice(11), clockText(date));
});

test('rows: an own row without a source name is "openHAB"', () => {
    assert.equal(sourceNameOf(row(1, { direction: 'OUT' })), 'openHAB');
    assert.equal(sourceNameOf(row(1, { direction: 'OUT', sourceName: 'Visu' })), 'Visu');
    assert.equal(sourceNameOf(row(1)), undefined);
});

test('rows: addresses sort by their raw number, as the server sorts them, three-level, two-level and free alike', () => {
    const sorted = ['31/7/10', '31/2000', '31/7/2', '65000', 'x', '31/7'].sort(addressOrder);
    assert.deepEqual(sorted, ['31/7', '65000', '31/7/2', '31/7/10', '31/2000', 'x']);
});

test('rows: the objects of every device are read once, at most four calls at a time, and again after forgetProject()', async () => {
    const devices = Array.from({ length: 11 }, (_, n) => ({ id: `d${n}`, address: `7.7.${n}` }));
    devices.push({ address: '7.7.99' });
    let open = 0;
    let most = 0;
    const api = {
        lists: 0,
        asked: [],
        async allDevices(bridge) {
            assert.equal(bridge, 'knx:ip:x');
            api.lists++;
            return devices;
        },
        async device(bridge, id) {
            api.asked.push(id);
            most = Math.max(most, ++open);
            await new Promise((resolve) => setImmediate(resolve));
            open--;
            return { id, groupObjects: [] };
        },
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    const first = rows.deviceDetails();
    assert.equal(rows.deviceDetails(), first);
    const details = await first;
    assert.deepEqual(details.problems, []);
    assert.deepEqual(details.devices.map((device) => device.id), [...devices.slice(0, 11).map((device) => device.id), '7.7.99']);
    assert.equal(most, DETAILS_AT_ONCE);
    assert.equal(DETAILS_AT_ONCE, 4);
    assert.equal(api.lists, 1);
    assert.equal(api.asked.length, 12);
    await rows.deviceDetails();
    assert.equal(api.lists, 1);
    rows.forgetProject();
    await rows.deviceDetails();
    assert.equal(api.lists, 2);
});

test('rows: a failed read of the devices\' objects is not kept', async () => {
    let fail = true;
    const api = {
        allDevices: async () => [{ id: 'd1' }],
        device: async () => {
            if (fail) {
                throw new Error('network');
            }
            return { id: 'd1', groupObjects: [] };
        },
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    const first = await rows.deviceDetails();
    assert.deepEqual(first.devices, []);
    assert.deepEqual(first.problems.map((error) => error.message), ['network']);
    fail = false;
    assert.deepEqual(await rows.deviceDetails(), { devices: [{ id: 'd1', groupObjects: [] }], problems: [], failed: [] });
});

test('rows: one device that cannot be read is left out and read again next time; the others are kept and not read twice', async () => {
    const asked = [];
    let lists = 0;
    let broken = new Set(['d2']);
    const api = {
        allDevices: async () => (lists++, [{ id: 'd1' }, { id: 'd2' }, { address: '31.1.3' }]),
        device: async (bridge, id) => {
            asked.push(id);
            if (broken.has(id)) {
                throw new Error(`no ${id}`);
            }
            return { id, groupObjects: [] };
        },
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    const first = await rows.deviceDetails();
    assert.deepEqual(first.devices.map((device) => device.id), ['d1', '31.1.3']);
    assert.deepEqual(first.problems.map((error) => error.message), ['no d2']);
    assert.deepEqual(first.failed, [{ id: 'd2' }]);
    broken = new Set();
    const second = await rows.deviceDetails();
    assert.deepEqual(second.devices.map((device) => device.id), ['d1', 'd2', '31.1.3']);
    assert.deepEqual(second.problems, []);
    assert.deepEqual(second.failed, []);
    assert.deepEqual(asked, ['d1', 'd2', '31.1.3', 'd2']);
    assert.equal(lists, 1);
    assert.equal(rows.deviceDetails(), rows.deviceDetails(), 'a whole read is kept');
    await rows.deviceDetails();
    assert.equal(asked.length, 4);
    rows.forgetProject();
    await rows.deviceDetails();
    assert.equal(asked.length, 7);
    assert.equal(lists, 2);
});

test('rows: a failed list of the devices is not kept', async () => {
    let fail = true;
    const api = {
        allDevices: async () => {
            if (fail) {
                throw new Error('network');
            }
            return [];
        },
        device: async () => ({}),
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    await assert.rejects(rows.deviceDetails());
    fail = false;
    assert.deepEqual(await rows.deviceDetails(), { devices: [], problems: [], failed: [] });
});

test('rows: a caller that has the devices already hands them over, and call 10 is not made', async () => {
    const api = {
        lists: 0,
        asked: [],
        async allDevices() {
            api.lists++;
            return [{ id: 'other' }];
        },
        async device(bridge, id) {
            api.asked.push(id);
            return { id, groupObjects: [] };
        },
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    const read = await rows.deviceDetails([{ id: 'd1', address: '31.1.1' }, { address: '31.1.2' }, null]);
    assert.equal(api.lists, 0);
    assert.deepEqual(api.asked, ['d1', '31.1.2']);
    assert.deepEqual(read.devices.map((device) => device.id), ['d1', '31.1.2']);
    // kept: another caller, with or without a list, gets the same answer without a new call
    assert.equal(await rows.deviceDetails(), read);
    assert.equal(await rows.deviceDetails([{ id: 'd9' }]), read);
    assert.deepEqual(api.asked, ['d1', '31.1.2']);
    rows.forgetProject();
    await rows.deviceDetails();
    assert.equal(api.lists, 1, 'without a list call 10 is made');
});

test('rows: the list of the devices and the building structure are read once, kept, and read again after a failure or another project', async () => {
    let fail = true;
    const reads = [];
    const api = {
        allDevices: async (bridge) => (reads.push(`devices ${bridge}`), [{ id: 'd1' }, null]),
        locations: async (bridge) => {
            reads.push(`locations ${bridge}`);
            if (fail) {
                throw new Error('network');
            }
            return [{ id: 's1', name: 'Hall' }];
        },
        device: async (bridge, id) => ({ id, groupObjects: [] }),
    };
    const rows = createRows({ bridge: 'knx:ip:x', api });
    assert.deepEqual(await rows.devices(), [{ id: 'd1' }]);
    assert.deepEqual(await rows.devices(), [{ id: 'd1' }]);
    assert.deepEqual((await rows.deviceDetails()).devices, [{ id: 'd1', groupObjects: [] }]);
    await assert.rejects(rows.locations());
    fail = false;
    assert.equal(rows.locations(), rows.locations());
    assert.deepEqual(await rows.locations(), [{ id: 's1', name: 'Hall' }]);
    assert.deepEqual(reads, ['devices knx:ip:x', 'locations knx:ip:x', 'locations knx:ip:x']);
    rows.forgetProject();
    await rows.devices();
    await rows.locations();
    assert.equal(reads.length, 5);
});
