import { defineConfig, PluginOption } from "vite";
const pkg = require('./package.json');
let isDevelopment = false;
let isProduction = false;
// https://vitejs.dev/config/
export default defineConfig(async ({ command, mode }) => {
    isDevelopment = command == "serve" || process.env.NODE_ENV === "development"
    isProduction = !isDevelopment;
    const envMode = isProduction ? 'production' : 'development';
    if (command == "serve") {
        (process.env as any).VITE_DEV_SERVER_URL = "http://localhost:5173";
    }
    console.log(`Building ${envMode} Action Template Interpreter UI`);
    const baseUrl = mode === 'oh' ? '/actiontemplatehli' : undefined;
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
                    target: "http://192.168.1.200:8080",
                },
            },
        },
        clearScreen: false,
    };
});
