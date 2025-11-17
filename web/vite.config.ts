import path from "path"
import { defineConfig } from 'vite'
import tailwindcss from "@tailwindcss/vite"
import preact from '@preact/preset-vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [preact(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: '../app/src/main/resources/static/ui',
    emptyOutDir: true,
    cssCodeSplit: false,
    manifest: false,
    sourcemap: false,
    lib: {
      entry: ['src/main.tsx'],
      name: 'index',
      formats: ['umd'],
      fileName: () => `index.js`,
    },
    rollupOptions: {
      output: {
        assetFileNames: 'styles.css',
      }
    }
  }
})
