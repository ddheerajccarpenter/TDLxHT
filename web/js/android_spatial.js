/**
 * TDlxHT Android Native 3D Spatial Companion Engine
 * Powered by Three.js & WebGL2/WebGL1
 *
 * Designed specifically for hardware-accelerated Android WebView integration.
 * Features:
 *  - 100% Offline Asset Loading (file:///android_asset/three3d/)
 *  - ACES Filmic Tone Mapping with Cyprus (#004643), Sand (#F0EDE5) & Obsidian (#0D0F11)
 *  - Interactive 3D Cat Model with AnimationMixer & Procedural Glass Fallback
 *  - Touch Orbit Controls with Inertia Physics & Tactile Detent Detection
 *  - 3D Confetti Particle Explosion Engine for Task Celebrations
 *  - Concentric 3D Particle Vortex for Focus Timer Sessions
 *  - Dynamic Theme Mode Interpolation (Dark Obsidian <-> Warm Sand)
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
  const ambientLight = new THREE.AmbientLight(0xF0EDE5, 1.6);
  scene.add(ambientLight);

  const keyLight = new THREE.DirectionalLight(0xF0EDE5, 2.6);
  keyLight.position.set(4, 6, 6);
  scene.add(keyLight);

  const fillLight = new THREE.DirectionalLight(0x004643, 2.0);
  fillLight.position.set(-5, -2, 4);
  scene.add(fillLight);

  const rimLight = new THREE.DirectionalLight(0xD4E815, 1.8); // Lime accent highlight
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
            child.material.roughness = 0.32;
            child.material.metalness = 0.15;
            child.material.envMapIntensity = 1.2;
          }
        }
      });

      if (gltf.animations && gltf.animations.length > 0) {
        animationMixer = new THREE.AnimationMixer(catModel);
        const idleClip = gltf.animations.find(a => a.name.toLowerCase().includes('idle')) || gltf.animations[0];
        if (idleClip) {
          idleAction = animationMixer.clipAction(idleClip);
          idleAction.setEffectiveTimeScale(0.9);
          idleAction.play();
        }
        const cheerClip = gltf.animations.find(a => a.name.toLowerCase().includes('jump') || a.name.toLowerCase().includes('cheer'));
        if (cheerClip) {
          cheerAction = animationMixer.clipAction(cheerClip);
          cheerAction.setLoop(THREE.LoopOnce);
        }
      }

      modelGroup.add(catModel);
      if (window.AndroidBridge && window.AndroidBridge.logDiagnostics) {
        window.AndroidBridge.logDiagnostics('3D Cat model loaded successfully in Android WebGL');
      }
    },
    undefined,
    (err) => {
      if (window.AndroidBridge && window.AndroidBridge.logDiagnostics) {
        window.AndroidBridge.logDiagnostics('Loaded procedural fallback model: ' + err);
      }
    }
  );

  // 6. Confetti Particle System (Celebrations)
  const confettiCount = 350;
  const confettiGeometry = new THREE.BufferGeometry();
  const confettiPositions = new Float32Array(confettiCount * 3);
  const confettiVelocities = new Float32Array(confettiCount * 3);
  const confettiColors = new Float32Array(confettiCount * 3);
  const confettiAlphas = new Float32Array(confettiCount);

  // Color palette: Lime (#D4E815), Amber (#F59E0B), Sand (#F0EDE5), Indigo (#6366F1)
  const palette = [
    new THREE.Color(0xD4E815),
    new THREE.Color(0xF59E0B),
    new THREE.Color(0xF0EDE5),
    new THREE.Color(0x6366F1),
    new THREE.Color(0x10B981)
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
    color: 0x6366F1,
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
        modelBounceAnim = 1.0;
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
  dom.addEventListener('touchstart', onPointerDown, { passive: true });
  window.addEventListener('touchmove', onPointerMove, { passive: true });
  window.addEventListener('touchend', onPointerUp, { passive: true });

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
      targetRotationY += velocityY;
      velocityY *= 0.92; // Friction damping
      velocityX *= 0.92;
    }

    currentRotationY += (targetRotationY - currentRotationY) * 0.12;
    currentRotationX += (targetRotationX - currentRotationX) * 0.12;
    modelGroup.rotation.y = currentRotationY;
    modelGroup.rotation.x = currentRotationX;

    // Idle slow breathing rotation when not dragged
    if (!isPointerDown && Math.abs(velocityY) < 0.001) {
      targetRotationY += 0.003;
    }

    // Bounce animation
    if (modelBounceAnim > 0) {
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
      vortexMaterial.opacity = Math.min(0.85, vortexMaterial.opacity + delta * 1.5);
      const vpos = vortexGeometry.attributes.position.array;
      const speed = 0.8 + (1.0 - focusProgress) * 1.5;

      for (let i = 0; i < focusVortexCount; i++) {
        vortexAngles[i] += delta * speed * (0.6 + (i % 5) * 0.15);
        vpos[i * 3] = Math.cos(vortexAngles[i]) * vortexRadii[i];
        vpos[i * 3 + 2] = Math.sin(vortexAngles[i]) * vortexRadii[i];
      }
      vortexGeometry.attributes.position.needsUpdate = true;
    } else if (vortexMaterial.opacity > 0) {
      vortexMaterial.opacity = Math.max(0, vortexMaterial.opacity - delta * 2.0);
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
    if (isDark) {
      ambientLight.color.setHex(0xF0EDE5);
      keyLight.color.setHex(0xF0EDE5);
      fillLight.color.setHex(0x004643);
      rimLight.color.setHex(0xD4E815);
    } else {
      ambientLight.color.setHex(0x2A2E35);
      keyLight.color.setHex(0x004643);
      fillLight.color.setHex(0xE0533C);
      rimLight.color.setHex(0x6366F1);
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
    const glassMat = new THREE.MeshPhysicalMaterial({
      color: 0xF0EDE5,
      metalness: 0.1,
      roughness: 0.15,
      transmission: 0.8,
      transparent: true,
      opacity: 0.85
    });

    const bodyGeo = new THREE.CylinderGeometry(0.35, 0.45, 0.9, 16);
    const body = new THREE.Mesh(bodyGeo, glassMat);
    group.add(body);

    const headGeo = new THREE.SphereGeometry(0.38, 16, 16);
    const head = new THREE.Mesh(headGeo, glassMat);
    head.position.y = 0.65;
    group.add(head);

    const earGeo = new THREE.ConeGeometry(0.12, 0.25, 8);
    const leftEar = new THREE.Mesh(earGeo, glassMat);
    leftEar.position.set(-0.2, 1.0, 0);
    leftEar.rotation.z = 0.25;
    group.add(leftEar);

    const rightEar = new THREE.Mesh(earGeo, glassMat);
    rightEar.position.set(0.2, 1.0, 0);
    rightEar.rotation.z = -0.25;
    group.add(rightEar);

    return group;
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

