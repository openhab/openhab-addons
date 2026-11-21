// What a viewer set on the page and finds again on the next visit: the widths of the panes and whether Properties is
// open. It is kept as one text in the browser's store for this page, which main.js hands over; a browser that gives
// no store, or refuses to read or write it, keeps the values for this visit only.

/**
 * @param {{read: () => string|null, write: (text: string) => void}|null} [store]
 * @returns {{get: (name: string) => *, set: (name: string, value: *) => void}}
 */
export function createLayout(store) {
    let values = null;

    function load() {
        if (values !== null) {
            return values;
        }
        values = {};
        try {
            const kept = JSON.parse(store?.read() ?? 'null');
            if (kept !== null && typeof kept === 'object' && !Array.isArray(kept)) {
                values = { ...kept };
            }
        } catch {
            values = {};
        }
        return values;
    }

    return {
        get(name) {
            const all = load();
            return Object.hasOwn(all, name) ? all[name] : undefined;
        },
        set(name, value) {
            load()[name] = value;
            try {
                store?.write(JSON.stringify(values));
            } catch {
                // kept for this visit
            }
        },
    };
}
