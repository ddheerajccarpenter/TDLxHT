import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  root: 'web',
  build: {
    outDir: '../dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'web/index.html'),
        auth: resolve(__dirname, 'web/auth/index.html'),
        callback: resolve(__dirname, 'web/auth/callback/index.html')
      }
    }
  }
});
