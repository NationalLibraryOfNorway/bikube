import path from "path";
import tailwindcss from "@tailwindcss/vite";
import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

const customConfig: UserConfigFn = (env) => ({
  plugins: [
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src/main/frontend"),
    },
  },
});

export default overrideVaadinConfig(customConfig);
