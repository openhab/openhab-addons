import { defineConfig, PluginOption } from "vite";
const pkg = require('./package.json');
let isDevelopment = false;
let isProduction = false;
// https://vitejs.dev/config/
export default defineConfig(async ({ command, mode }) => {
    isDevelopment = command == "serve" || process.env.NODE_ENV === "development"
    isProduction = !isDevelopment;
    const envMode = isProduction ? 'production' : 'development';
    console.log(`Building ${envMode} Action Template Interpreter UI`);
    const baseUrl = mode === 'oh' ? '/actiontemplatehli' : undefined;
    if (baseUrl) {
        console.log(`With base url $${baseUrl}`);
    }
    const devProxyUrl = (process.env as any).OH_URL ?? "http://127.0.0.1:8080"
    if (command == "serve") {
        (process.env as any).VITE_DEV_SERVER_URL = "http://localhost:5173";
        console.log(`Proxy oh rest calls to ${devProxyUrl}`);
    }
    return {
        base: baseUrl,
        build: {
            sourcemap: isDevelopment,
            minify: isProduction,
        },
        server: {
            port: 5173,
            proxy: {
                "/rest": {
                    target: devProxyUrl,
                },
            },
        },
        clearScreen: false,
    };
});
