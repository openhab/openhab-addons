// A view's context for the view tests: a stand-in document, fake timers and frames that count what is open, a
// confirm that records its question, and a fake stream.

import { createDocument } from './fakedom.js';
import { createSidebar } from '../web/js/sidebar.js';
import { createTimers } from './fakes.js';

/** A stream like the one of stream.js, driven by the test. */
export function fakeStream(phase = 'idle') {
    const listeners = new Set();
    const stream = {
        current: Object.freeze({ phase }),
        starts: 0,
        retries: 0,
        state: () => stream.current,
        start() {
            stream.starts++;
        },
        retry() {
            stream.retries++;
        },
        subscribe(listener) {
            listeners.add(listener);
            return () => listeners.delete(listener);
        },
        subscribers: () => listeners.size,
        set(next) {
            stream.current = Object.freeze(next);
            for (const listener of [...listeners]) {
                listener(stream.current);
            }
        },
        close() {},
    };
    return stream;
}

/**
 * @param {Object} parts what the view needs: bridge, api, rows, stream, route
 */
export function viewContext(parts) {
    const document = createDocument();
    const element = document.createElement('section');
    const clock = createTimers();
    const frames = [];
    const errors = [];
    const questions = [];
    const navigations = [];
    const replacements = [];
    const statuses = [];
    const harness = {
        document,
        element,
        clock,
        errors,
        questions,
        navigations,
        replacements,
        statuses,
        answer: true,
        /** {@return the frame callbacks asked for and not run yet} */
        pendingFrames: () => frames.length,
        /** Runs the frame callbacks, also those asked for while they run. */
        runFrames() {
            for (let turns = 0; frames.length > 0 && turns < 100; turns++) {
                frames.shift()();
            }
        },
    };
    harness.context = {
        document,
        element,
        api: parts.api ?? {},
        stream: parts.stream ?? null,
        rows: parts.rows ?? null,
        bridge: parts.bridge,
        route: parts.route ?? { view: 'monitor', bridge: parts.bridge?.uid, query: {} },
        navigate: (route) => navigations.push(route),
        replace: (route) => replacements.push(route),
        confirm: (question) => {
            questions.push(question);
            return harness.answer;
        },
        frame: (callback) => {
            frames.push(callback);
            return frames.length;
        },
        timers: clock.timers,
        clipboard: undefined,
        auth: {},
        fetch: async () => {
            throw new Error('a view of stage G2 makes no fetch of its own');
        },
        showError: (error) => errors.push(error),
        status: (status) => statuses.push(status),
        layout: parts.layout,
    };
    return harness;
}

/**
 * Makes the view one of two panels: its sidebar is the shared one, which stands outside the view's element.
 *
 * @returns {Element} the shared sidebar's node
 */
export function shareSidebar(harness) {
    const sidebar = createSidebar({ document: harness.document, rows: harness.context.rows, frame: harness.context.frame, onOpen: () => {} });
    harness.context.split = { half: 'first', open: true, toggle: null, sidebar: { face: () => sidebar.face(), release: (face) => sidebar.release(face) } };
    return sidebar.node;
}
