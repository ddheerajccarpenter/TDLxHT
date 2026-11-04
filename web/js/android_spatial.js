/**
 * TDlxHT Android Native 3D Spatial Companion Engine
 * Powered by Three.js & WebGL2/WebGL1
 *
 * Designed specifically for hardware-accelerated Android WebView integration.
 * Features:
 *  - Local HTTPS asset loading through AndroidX WebViewAssetLoader
 *  - Monochrome lighting and materials across light and dark themes
 *  - Interactive 3D Cat Model with AnimationMixer & Procedural Cat Fallback
 *  - Touch Orbit Controls with Inertia Physics & Tactile Detent Detection
 *  - 3D Confetti Particle Explosion Engine for Task Celebrations
 *  - Concentric 3D Particle Vortex for Focus Timer Sessions
 *  - Dynamic neutral theme adjustment (black canvas <-> white canvas)
 *  - Lifecycle Power Governor: 0% CPU/GPU RAF Sleep when Inactive
 *  - Bidirectional AndroidBridge Communication
 */

import * as THREE from 'three';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';

(function initAndroidSpatialEngine() {
  const container = document.getElementById('spatial-canvas-container') || document.body;
  if (!container) return;

  // 1. Hardware & Context Verification
  const testCanvas = document.createElement('canvas');
  const gl = testCanvas.getContext('webgl2') || testCanvas.getContext('webgl');
  if (!gl) {
    if (window.AndroidBridge && window.AndroidBridge.logDiagnostics) {
      window.AndroidBridge.logDiagnostics('WebGL not available on this Android device');
    }
    if (window.AndroidBridge && window.AndroidBridge.notifyModelFallback) {
      window.AndroidBridge.notifyModelFallback('WebGL is unavailable');
    }
    return;
  }

  // 2. Scene & Full-Viewport Camera
  const scene = new THREE.Scene();
  let width = container.clientWidth || window.innerWidth || 360;
  let height = container.clientHeight || window.innerHeight || 240;

  const camera = new THREE.PerspectiveCamera(42, width / height, 0.1, 100);
  camera.position.set(0, 0.4, 4.8);

  const renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance'
  });

  // Clamp DPI to max 2.0 to conserve battery and GPU fill-rate on high-density Android displays
  const clampedDpi = Math.min(window.devicePixelRatio || 1, 2.0);
  renderer.setSize(width, height);
  renderer.setPixelRatio(clampedDpi);
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.3;
  renderer.setClearColor(0x000000, 0); // Transparent background

  container.innerHTML = '';
  container.appendChild(renderer.domElement);
  renderer.domElement.style.width = '100%';
  renderer.domElement.style.height = '100%';
  renderer.domElement.style.display = 'block';

  // 3. Dynamic Lighting Rig
  const ambientLight = new THREE.AmbientLight(0xD8D8D8, 1.5);
  scene.add(ambientLight);

  const keyLight = new THREE.DirectionalLight(0xFFFFFF, 2.4);
  keyLight.position.set(4, 6, 6);
  scene.add(keyLight);

  const fillLight = new THREE.DirectionalLight(0x777777, 1.2);
  fillLight.position.set(-5, -2, 4);
  scene.add(fillLight);

  const rimLight = new THREE.DirectionalLight(0xE6E6E6, 1.5);
  rimLight.position.set(0, -4, -3);
  scene.add(rimLight);

  // Master Root Group
  const masterGroup = new THREE.Group();
  scene.add(masterGroup);

  // Model Anchor Group
  const modelGroup = new THREE.Group();
  masterGroup.add(modelGroup);

  let animationMixer = null;
  let idleAction = null;
  let cheerAction = null;
  let isReducedMotion = false;
  let isDarkTheme = true;
  let loadedCatModel = null;
  let proceduralCatMaterial = null;
  const clock = new THREE.Clock();

  // 4. Procedural Stylized Fallback Geometry (immediate display)
  const placeholderModel = createProceduralCat();
  modelGroup.add(placeholderModel);

  // 5. Load External Cat GLTF Model from local asset
  const gltfLoader = new GLTFLoader();
  const modelPath = 'cat.glb';

  gltfLoader.load(
    modelPath,
    (gltf) => {
      // Remove placeholder once external model is loaded
      modelGroup.remove(placeholderModel);
      disposeHierarchy(placeholderModel);

      const catModel = gltf.scene;
      const box = new THREE.Box3().setFromObject(catModel);
      const size = new THREE.Vector3();
      const center = new THREE.Vector3();
      box.getSize(size);
      box.getCenter(center);

      // Center model
      catModel.position.x = -center.x;
      catModel.position.y = -center.y - 0.1;
      catModel.position.z = -center.z;

      // Scale to fit card viewport
      const maxDim = Math.max(size.x, size.y, size.z) || 1;
      const targetScale = 2.4 / maxDim;
      catModel.scale.set(targetScale, targetScale, targetScale);

      catModel.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true;
          child.receiveShadow = true;
          if (child.material) {
            const materials = Array.isArray(child.material) ? child.material : [child.material];
            materials.forEach((material) => {
              if (material.color) {
                const neutral = (material.color.r + material.color.g + material.color.b) / 3;
                material.color.setRGB(neutral, neutral, neutral);
              }
              material.map = null;
              material.emissiveMap = null;
              if (material.emissive) material.emissive.setRGB(0, 0, 0);
              material.vertexColors = false;
              material.roughness = 0.4;
              material.metalness = 0.12;
              material.envMapIntensity = 0.8;
              material.needsUpdate = true;
            });
          }
        }
      });

      if (gltf.animations && gltf.animations.length > 0) {
        animationMixer = new THREE.AnimationMixer(catModel);
        const idleClip = gltf.animations.find(a => a.name.toLowerCase().includes('idle')) || gltf.animations[0];
        if (idleClip) {
          idleAction = animationMixer.clipAction(idleClip);
          idleAction.setEffectiveTimeScale(0.9);
          if (!isReducedMotion) idleAction.play();
        }
        const cheerClip = gltf.animations.find(a => a.name.toLowerCase().includes('jump') || a.name.toLowerCase().includes('cheer'));
        if (cheerClip) {
          cheerAction = animationMixer.clipAction(cheerClip);
          cheerAction.setLoop(THREE.LoopOnce);
        }
      }

      modelGroup.add(catModel);
      loadedCatModel = catModel;
      applyCompanionPalette();
      if (window.AndroidBridge && window.AndroidBridge.logDiagnostics) {
        window.AndroidBridge.logDiagnostics('3D Cat model loaded successfully in Android WebGL');
      }
      if (window.AndroidBridge && window.AndroidBridge.notifyModelLoaded) {
        window.AndroidBridge.notifyModelLoaded();
      }
    },
    undefined,
    (err) => {
      if (window.AndroidBridge && window.AndroidBridge.logDiagnostics) {
        window.AndroidBridge.logDiagnostics('Loaded procedural fallback model: ' + err);
      }
      if (window.AndroidBridge && window.AndroidBridge.notifyModelFallback) {
        window.AndroidBridge.notifyModelFallback('The 3D model could not be loaded');
      }
    }
  );

  // 6. Confetti Particle System (Celebrations)
  const confettiCount = 120;
  const confettiGeometry = new THREE.BufferGeometry();
  const confettiPositions = new Float32Array(confettiCount * 3);
  const confettiVelocities = new Float32Array(confettiCount * 3);
  const confettiColors = new Float32Array(confettiCount * 3);
  const confettiAlphas = new Float32Array(confettiCount);

  // Celebration particles remain neutral so the companion follows the app palette.
  const palette = [
    new THREE.Color(0xFFFFFF),
    new THREE.Color(0xD8D8D8),
    new THREE.Color(0xA8A8A8)
  ];

  for (let i = 0; i < confettiCount; i++) {
    confettiPositions[i * 3] = 0;
    confettiPositions[i * 3 + 1] = -50; // Hidden initially
    confettiPositions[i * 3 + 2] = 0;

    confettiVelocities[i * 3] = 0;
    confettiVelocities[i * 3 + 1] = 0;
    confettiVelocities[i * 3 + 2] = 0;

    const c = palette[i % palette.length];
    confettiColors[i * 3] = c.r;
    confettiColors[i * 3 + 1] = c.g;
    confettiColors[i * 3 + 2] = c.b;

    confettiAlphas[i] = 0;
  }

  confettiGeometry.setAttribute('position', new THREE.BufferAttribute(confettiPositions, 3));
  confettiGeometry.setAttribute('color', new THREE.BufferAttribute(confettiColors, 3));

  const confettiMaterial = new THREE.PointsMaterial({
    size: 0.08,
    vertexColors: true,
    transparent: true,
    opacity: 0,
    blending: THREE.AdditiveBlending
  });

  const confettiPoints = new THREE.Points(confettiGeometry, confettiMaterial);
  scene.add(confettiPoints);

  let isConfettiActive = false;
  let confettiLife = 0;

  function burstConfetti() {
    if (isReducedMotion) return;
    isConfettiActive = true;
    confettiLife = 1.0;
    confettiMaterial.opacity = 1.0;

    const pos = confettiGeometry.attributes.position.array;
    for (let i = 0; i < confettiCount; i++) {
      pos[i * 3] = (Math.random() - 0.5) * 0.4;
      pos[i * 3 + 1] = -0.2 + (Math.random() - 0.5) * 0.4;
      pos[i * 3 + 2] = (Math.random() - 0.5) * 0.4;

      const angle = Math.random() * Math.PI * 2;
      const speed = 1.8 + Math.random() * 2.5;
      const upSpeed = 2.2 + Math.random() * 2.8;

      confettiVelocities[i * 3] = Math.cos(angle) * speed;
      confettiVelocities[i * 3 + 1] = upSpeed;
      confettiVelocities[i * 3 + 2] = Math.sin(angle) * speed;
    }
    confettiGeometry.attributes.position.needsUpdate = true;

    // Trigger bounce animation on model
    if (cheerAction) {
      cheerAction.reset();
      cheerAction.play();
    } else {
      modelBounceAnim = 1.0;
    }
  }

  let modelBounceAnim = 0;

  // 7. Focus Session Breathing Orbit & Vortex
  let isFocusActive = false;
  let focusProgress = 0;
  const focusVortexCount = 200;
  const vortexGeometry = new THREE.BufferGeometry();
  const vortexPositions = new Float32Array(focusVortexCount * 3);
  const vortexRadii = new Float32Array(focusVortexCount);
  const vortexAngles = new Float32Array(focusVortexCount);
  const vortexHeights = new Float32Array(focusVortexCount);

  for (let i = 0; i < focusVortexCount; i++) {
    vortexRadii[i] = 0.8 + Math.random() * 1.4;
    vortexAngles[i] = Math.random() * Math.PI * 2;
    vortexHeights[i] = (Math.random() - 0.5) * 2.0;

    vortexPositions[i * 3] = Math.cos(vortexAngles[i]) * vortexRadii[i];
    vortexPositions[i * 3 + 1] = vortexHeights[i];
    vortexPositions[i * 3 + 2] = Math.sin(vortexAngles[i]) * vortexRadii[i];
  }

  vortexGeometry.setAttribute('position', new THREE.BufferAttribute(vortexPositions, 3));
  const vortexMaterial = new THREE.PointsMaterial({
    size: 0.05,
    color: 0xB8B8B8,
    transparent: true,
    opacity: 0,
    blending: THREE.AdditiveBlending
  });
  const vortexPoints = new THREE.Points(vortexGeometry, vortexMaterial);
  scene.add(vortexPoints);

  // 8. Touch & Gesture Disambiguation with Inertia
  let isPointerDown = false;
  let startX = 0;
  let startY = 0;
  let lastX = 0;
  let lastY = 0;
  let targetRotationY = 0;
  let targetRotationX = 0;
  let currentRotationY = 0;
  let currentRotationX = 0;
  let velocityY = 0;
  let velocityX = 0;
  let downTimestamp = 0;
  let lastTapTimestamp = 0;

  function onPointerDown(e) {
    isPointerDown = true;
    const clientX = e.clientX || (e.touches && e.touches[0].clientX) || 0;
    const clientY = e.clientY || (e.touches && e.touches[0].clientY) || 0;
    startX = clientX;
    startY = clientY;
    lastX = clientX;
    lastY = clientY;
    downTimestamp = Date.now();
    velocityY = 0;
    velocityX = 0;
  }

  function onPointerMove(e) {
    if (!isPointerDown) return;
    const clientX = e.clientX || (e.touches && e.touches[0].clientX) || 0;
    const clientY = e.clientY || (e.touches && e.touches[0].clientY) || 0;
    const dx = clientX - lastX;
    const dy = clientY - lastY;

    targetRotationY += dx * 0.012;
    targetRotationX = Math.max(-0.4, Math.min(0.4, targetRotationX + dy * 0.008));

    velocityY = dx * 0.012;
    velocityX = dy * 0.008;

    lastX = clientX;
    lastY = clientY;
  }

  function onPointerUp(e) {
    if (!isPointerDown) return;
    isPointerDown = false;
    const duration = Date.now() - downTimestamp;
    const clientX = e.clientX || (e.changedTouches && e.changedTouches[0]?.clientX) || lastX;
    const clientY = e.clientY || (e.changedTouches && e.changedTouches[0]?.clientY) || lastY;
    const dist = Math.hypot(clientX - startX, clientY - startY);

    if (dist < 8 && duration < 300) {
      // Tap detected
      const now = Date.now();
      if (now - lastTapTimestamp < 350) {
        // Double tap: celebration backflip
        burstConfetti();
        if (window.AndroidBridge && window.AndroidBridge.notifyModelLongPressed) {
          window.AndroidBridge.notifyModelLongPressed('cat');
        }
      } else {
        // Single tap: tactile bounce + haptic tick
        modelBounceAnim = isReducedMotion ? 0 : 1.0;
        if (window.AndroidBridge && window.AndroidBridge.notifyModelTapped) {
          window.AndroidBridge.notifyModelTapped('cat');
        }
      }
      lastTapTimestamp = now;
    }
  }

  const dom = renderer.domElement;
  dom.addEventListener('pointerdown', onPointerDown, { passive: true });
  window.addEventListener('pointermove', onPointerMove, { passive: true });
  window.addEventListener('pointerup', onPointerUp, { passive: true });
  window.addEventListener('pointercancel', onPointerUp, { passive: true });

  // 9. Lifecycle Power Governor & RAF Loop
  let isRenderPaused = false;
  let rafId = null;

  function renderLoop() {
    if (isRenderPaused) return;
    rafId = requestAnimationFrame(renderLoop);

    const delta = clock.getDelta();

    // Model Animation Mixer
    if (animationMixer) {
      animationMixer.update(delta);
    }

    // Inertia rotation damping
    if (!isPointerDown) {
      targetRotationY += isReducedMotion ? 0 : velocityY;
      velocityY *= isReducedMotion ? 0 : 0.92;
      velocityX *= isReducedMotion ? 0 : 0.92;
    }

    if (isReducedMotion) {
      currentRotationY = targetRotationY;
      currentRotationX = targetRotationX;
    } else {
      currentRotationY += (targetRotationY - currentRotationY) * 0.12;
      currentRotationX += (targetRotationX - currentRotationX) * 0.12;
    }
    modelGroup.rotation.y = currentRotationY;
    modelGroup.rotation.x = currentRotationX;

    // Bounce animation
    if (!isReducedMotion && modelBounceAnim > 0) {
      modelGroup.position.y = Math.sin(modelBounceAnim * Math.PI) * 0.28;
      modelBounceAnim = Math.max(0, modelBounceAnim - delta * 3.5);
    }

    // Confetti Physics Update
    if (isConfettiActive) {
      confettiLife -= delta * 0.55;
      confettiMaterial.opacity = Math.max(0, confettiLife);

      const pos = confettiGeometry.attributes.position.array;
      for (let i = 0; i < confettiCount; i++) {
        pos[i * 3] += confettiVelocities[i * 3] * delta;
        pos[i * 3 + 1] += confettiVelocities[i * 3 + 1] * delta;
        pos[i * 3 + 2] += confettiVelocities[i * 3 + 2] * delta;

        // Gravity
        confettiVelocities[i * 3 + 1] -= 5.5 * delta;
      }
      confettiGeometry.attributes.position.needsUpdate = true;

      if (confettiLife <= 0) {
        isConfettiActive = false;
        confettiMaterial.opacity = 0;
      }
    }

    // Focus Vortex Update
    if (isFocusActive) {
      vortexMaterial.opacity = isReducedMotion ? 0.55 : Math.min(0.85, vortexMaterial.opacity + delta * 1.5);
      const vpos = vortexGeometry.attributes.position.array;
      const speed = 0.8 + (1.0 - focusProgress) * 1.5;

      if (!isReducedMotion) {
        for (let i = 0; i < focusVortexCount; i++) {
          vortexAngles[i] += delta * speed * (0.6 + (i % 5) * 0.15);
          vpos[i * 3] = Math.cos(vortexAngles[i]) * vortexRadii[i];
          vpos[i * 3 + 2] = Math.sin(vortexAngles[i]) * vortexRadii[i];
        }
        vortexGeometry.attributes.position.needsUpdate = true;
      }
    } else if (vortexMaterial.opacity > 0) {
      vortexMaterial.opacity = isReducedMotion ? 0 : Math.max(0, vortexMaterial.opacity - delta * 2.0);
    }

    renderer.render(scene, camera);
  }

  // Start initial loop
  rafId = requestAnimationFrame(renderLoop);

  // Resize handler
  function onResize() {
    width = container.clientWidth || window.innerWidth || 360;
    height = container.clientHeight || window.innerHeight || 240;
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
  }
  window.addEventListener('resize', onResize);

  // 10. Global Window Hooks for Android Kotlin Bridge
  window.triggerCelebration = function(priorityLevel, streakCount) {
    burstConfetti();
  };

  window.setFocusSession = function(active, remainingSec, totalSec) {
    isFocusActive = !!active;
    focusProgress = (totalSec > 0) ? (remainingSec / totalSec) : 1.0;
  };

  window.setThemeMode = function(isDark) {
    isDarkTheme = !!isDark;
    if (isDark) {
      ambientLight.color.setHex(0xD8D8D8);
      keyLight.color.setHex(0xFFFFFF);
      fillLight.color.setHex(0x777777);
      rimLight.color.setHex(0xE6E6E6);
    } else {
      ambientLight.color.setHex(0xFFFFFF);
      keyLight.color.setHex(0xFFFFFF);
      fillLight.color.setHex(0x999999);
      rimLight.color.setHex(0xD0D0D0);
    }
    applyCompanionPalette();
  };

  window.setReducedMotion = function(reduced) {
    isReducedMotion = !!reduced;
    if (idleAction) {
      if (isReducedMotion) idleAction.stop();
      else if (!idleAction.isRunning()) idleAction.play();
    }
    if (isReducedMotion) {
      modelBounceAnim = 0;
      confettiLife = 0;
      isConfettiActive = false;
      confettiMaterial.opacity = 0;
    }
  };

  window.setRenderPaused = function(paused) {
    const shouldPause = !!paused;
    if (shouldPause === isRenderPaused) return;
    isRenderPaused = shouldPause;
    if (!isRenderPaused) {
      clock.getDelta(); // flush delta
      rafId = requestAnimationFrame(renderLoop);
    } else if (rafId) {
      cancelAnimationFrame(rafId);
      rafId = null;
    }
  };

  // Procedural stylized cat helper
  function createProceduralCat() {
    const group = new THREE.Group();
    proceduralCatMaterial = new THREE.MeshPhysicalMaterial({
      color: 0xBDBDBD,
      metalness: 0.12,
      roughness: 0.38,
      clearcoat: 0.35
    });
    const faceMaterial = new THREE.MeshStandardMaterial({ color: 0x292929, roughness: 0.42 });

    const bodyGeo = new THREE.CylinderGeometry(0.35, 0.45, 0.9, 16);
    const body = new THREE.Mesh(bodyGeo, proceduralCatMaterial);
    group.add(body);

    const headGeo = new THREE.SphereGeometry(0.38, 16, 16);
    const head = new THREE.Mesh(headGeo, proceduralCatMaterial);
    head.position.y = 0.65;
    group.add(head);

    const earGeo = new THREE.ConeGeometry(0.12, 0.25, 8);
    const leftEar = new THREE.Mesh(earGeo, proceduralCatMaterial);
    leftEar.position.set(-0.2, 1.0, 0);
    leftEar.rotation.z = 0.25;
    group.add(leftEar);

    const rightEar = new THREE.Mesh(earGeo, proceduralCatMaterial);
    rightEar.position.set(0.2, 1.0, 0);
    rightEar.rotation.z = -0.25;
    group.add(rightEar);

    const eyeGeo = new THREE.SphereGeometry(0.045, 10, 8);
    [-0.14, 0.14].forEach((x) => {
      const eye = new THREE.Mesh(eyeGeo, faceMaterial);
      eye.position.set(x, 0.69, 0.34);
      group.add(eye);
    });

    const muzzleGeo = new THREE.SphereGeometry(0.085, 10, 8);
    [-0.075, 0.075].forEach((x) => {
      const muzzle = new THREE.Mesh(muzzleGeo, proceduralCatMaterial);
      muzzle.scale.set(1.1, 0.72, 0.65);
      muzzle.position.set(x, 0.53, 0.34);
      group.add(muzzle);
    });

    const nose = new THREE.Mesh(new THREE.SphereGeometry(0.035, 8, 8), faceMaterial);
    nose.scale.set(1.25, 0.85, 0.6);
    nose.position.set(0, 0.56, 0.4);
    group.add(nose);

    const tailCurve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(0.28, -0.05, 0),
      new THREE.Vector3(0.56, 0.02, 0),
      new THREE.Vector3(0.7, 0.28, 0),
      new THREE.Vector3(0.57, 0.48, 0)
    ]);
    group.add(new THREE.Mesh(new THREE.TubeGeometry(tailCurve, 18, 0.065, 8, false), proceduralCatMaterial));

    return group;
  }

  function applyCompanionPalette() {
    if (proceduralCatMaterial) {
      proceduralCatMaterial.color.setHex(isDarkTheme ? 0xBDBDBD : 0x5A5A5A);
      proceduralCatMaterial.needsUpdate = true;
    }
    if (!loadedCatModel) return;

    // The bundled GLB uses a white texture. Since textures are removed to keep
    // the companion monochrome, give its model a theme-aware neutral base so
    // it remains visible on both the black and white canvases.
    const modelTone = isDarkTheme ? 0xC5C5C5 : 0x555555;
    loadedCatModel.traverse((child) => {
      if (!child.isMesh || !child.material) return;
      const materials = Array.isArray(child.material) ? child.material : [child.material];
      materials.forEach((material) => {
        if (material.color) material.color.setHex(modelTone);
        material.map = null;
        material.alphaMap = null;
        material.emissiveMap = null;
        if (material.emissive) material.emissive.setRGB(0, 0, 0);
        material.opacity = 1;
        material.transparent = false;
        material.depthWrite = true;
        material.vertexColors = false;
        if ('transmission' in material) material.transmission = 0;
        material.needsUpdate = true;
      });
    });
  }

  function disposeHierarchy(obj) {
    obj.traverse((child) => {
      if (child.geometry) child.geometry.dispose();
      if (child.material) {
        if (Array.isArray(child.material)) child.material.forEach(m => m.dispose());
        else child.material.dispose();
      }
    });
  }
})();

