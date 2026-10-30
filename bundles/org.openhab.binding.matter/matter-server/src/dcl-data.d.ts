// Files of the @matter/dcl-data package, which webpack builds into matter.js (see webpack.config.js)
declare module "dcl-data/manifest.json" {
    const manifest: { builtAt: string };
    export default manifest;
}

declare module "*.jsonl" {
    const content: string;
    export default content;
}
