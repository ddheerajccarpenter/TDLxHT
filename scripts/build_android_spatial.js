import { build } from 'vite';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';
import { copyFileSync, mkdirSync, existsSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function bundleAndroidSpatial() {
  const assetsDir = resolve(__dirname, '../app/src/main/assets/three3d');
  if (!existsSync(assetsDir)) {
    mkdirSync(assetsDir, { recursive: true });
  }

  console.log('📦 Bundling Three.js Android Spatial Companion...');

  await build({
    configFile: false,
    build: {
      lib: {
        entry: resolve(__dirname, '../web/js/android_spatial.js'),
        name: 'AndroidSpatial',
        formats: ['iife'],
        fileName: () => 'bundle.js'
      },
      outDir: assetsDir,
      emptyOutDir: false,
      minify: 'esbuild'
    }
  });

  // Copy cat.glb to assets/three3d/
  const catModelSrc = resolve(__dirname, '../web/models/cat.glb');
  const catModelDest = resolve(assetsDir, 'cat.glb');
  if (existsSync(catModelSrc)) {
    copyFileSync(catModelSrc, catModelDest);
    console.log('✅ Copied cat.glb to assets/three3d/');
  } else {
    console.warn('⚠️ cat.glb not found at ' + catModelSrc);
  }

  console.log('🎉 Android Spatial 3D bundle built successfully in app/src/main/assets/three3d/');
}

bundleAndroidSpatial().catch(err => {
  console.error('❌ Build failed:', err);
  process.exit(1);
});

