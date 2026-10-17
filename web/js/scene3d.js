/**
 * TDlxHT Immersive Whole-Page 3D Spatial Experience (Three.js + GSAP + GLTFLoader)
 *
 * Full-Viewport Fixed Spatial Canvas covering the entire web companion journey.
 * Strict Two-Color Palette: Cyprus (#004643) & Sand (#F0EDE5)
 *
 * Features:
 *  1. Interactive 3D Cat Model loaded via GLTFLoader with smooth idle animation rig
 *  2. Whole-Page Bidirectional Scroll-Linked Rotation:
 *     - Scrolling down rotates the cat downward/clockwise continuously
 *     - Scrolling up rotates the cat upward/counter-clockwise in reverse
 *  3. Cursor & Parallax Micro-Tracking across the entire page
 *  4. Floating Glass Productivity Geometry (Task Card, Streak Matrix, Multi-Session Habit Pillars)
 *  5. Drifting Focus Particle Field in Sand (#F0EDE5)
 *  6. Bulletproof Fallback: Procedural stylized feline geometry if network/asset delay occurs
 *  7. Zero Scroll-Hijacking • 60-120fps GPU Adaptive • Clean Resource Disposal
 */

import * as THREE from 'three';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { gsap } from 'gsap';

export function init3DScene() {
  const container = document.getElementById('tdlxht-3d-viewport');
  if (!container) return;

  // 1. WebGL Support & Context Availability Check
  const testCanvas = document.createElement('canvas');
  const gl = testCanvas.getContext('webgl2') || testCanvas.getContext('webgl');
  if (!gl) {
    console.info('TDlxHT: WebGL not available on this device. Displaying standard layout.');
    container.classList.add('fallback-mode');
    return;
  }

  // Reduced-motion user preference check
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // 2. Scene & Full-Viewport Camera
  const scene = new THREE.Scene();

  let width = window.innerWidth;
  let height = window.innerHeight;

  const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 100);
  camera.position.set(0, 0, 8.2);

  const renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance'
  });

  renderer.setSize(width, height);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.25;

  container.innerHTML = '';
  container.appendChild(renderer.domElement);
  renderer.domElement.style.width = '100vw';
  renderer.domElement.style.height = '100vh';
  renderer.domElement.style.display = 'block';

  // 3. Strict Brand Lighting (Cyprus #004643 & Sand #F0EDE5)
  const ambientLight = new THREE.AmbientLight(0xF0EDE5, 1.4);
  scene.add(ambientLight);

  const keyLight = new THREE.DirectionalLight(0xF0EDE5, 2.8);
  keyLight.position.set(6, 8, 8);
  scene.add(keyLight);

  const fillLight = new THREE.DirectionalLight(0x004643, 2.2);
  fillLight.position.set(-7, -4, 5);
  scene.add(fillLight);

  const rimLight = new THREE.DirectionalLight(0xF0EDE5, 1.4);
  rimLight.position.set(0, -6, -4);
  scene.add(rimLight);

  // 4. Strict Liquid-Glass Materials
  const sandGlassMaterial = new THREE.MeshPhysicalMaterial({
    color: 0xF0EDE5,
    metalness: 0.08,
    roughness: 0.14,
    transmission: 0.82,
    transparent: true,
    opacity: 0.88,
    ior: 1.48,
    reflectivity: 0.5
  });

  const cyprusGlassMaterial = new THREE.MeshPhysicalMaterial({
    color: 0x004643,
    metalness: 0.15,
    roughness: 0.12,
    transmission: 0.78,
    transparent: true,
    opacity: 0.85,
    ior: 1.48,
    reflectivity: 0.6
  });

  const sandEdgeMaterial = new THREE.LineBasicMaterial({
    color: 0xF0EDE5,
    transparent: true,
    opacity: 0.55
  });

  const cyprusEdgeMaterial = new THREE.LineBasicMaterial({
    color: 0x004643,
    transparent: true,
    opacity: 0.45
  });

  // Master root group containing all spatial elements
  const masterGroup = new THREE.Group();
  scene.add(masterGroup);

  // ==========================================
  // ELEMENT 1: Interactive Cat 3D Model Rig
  // ==========================================
  const catGroup = new THREE.Group();
  masterGroup.add(catGroup);

  // Default cat initial placement (flanking hero headline)
  catGroup.position.set(2.4, 0.3, 0);

  let animationMixer = null;
  const clock = new THREE.Clock();

  // Create procedural stylized low-poly cat companion as immediate placeholder / fallback
  const placeholderCat = createProceduralCat();
  catGroup.add(placeholderCat);

  // Load the external Cat 3D GLB model
  const gltfLoader = new GLTFLoader();
  const modelUrl = '/models/cat.glb';

  gltfLoader.load(
    modelUrl,
    (gltf) => {
      // Remove placeholder once external model is successfully loaded
      catGroup.remove(placeholderCat);
      disposeHierarchy(placeholderCat);

      const catModel = gltf.scene;

      // Compute bounding box and normalize center/scale
      const box = new THREE.Box3().setFromObject(catModel);
      const size = new THREE.Vector3();
      const center = new THREE.Vector3();
      box.getSize(size);
      box.getCenter(center);

      // Center model at origin of catGroup
      catModel.position.x = -center.x;
      catModel.position.y = -center.y;
      catModel.position.z = -center.z;

      // Fit to proportional viewport size (~2.4 units height/width)
      const maxDim = Math.max(size.x, size.y, size.z) || 1;
      const targetScale = 2.4 / maxDim;
      catModel.scale.set(targetScale, targetScale, targetScale);

      // Enhance materials for ACES tone mapping & Cyprus/Sand specular lighting
      catModel.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true;
          child.receiveShadow = true;
          if (child.material) {
            child.material.roughness = 0.35;
            child.material.metalness = 0.12;
            child.material.envMapIntensity = 1.1;
          }
        }
      });

      // Bind AnimationMixer if animations are included
      if (gltf.animations && gltf.animations.length > 0) {
        animationMixer = new THREE.AnimationMixer(catModel);
        // Look for 'idle' animation or default to first animation clip
        const idleClip = gltf.animations.find(a => a.name.toLowerCase().includes('idle')) || gltf.animations[0];
        if (idleClip) {
          const action = animationMixer.clipAction(idleClip);
          action.setEffectiveTimeScale(0.85); // Gentle breathing/idle speed
          action.play();
        }
      }

      catGroup.add(catModel);
    },
    (xhr) => {
      // Progress feedback (passive)
      if (xhr.lengthComputable) {
        const percent = Math.round((xhr.loaded / xhr.total) * 100);
        if (percent === 100) console.info('TDlxHT: 3D Cat Model loaded successfully.');
      }
    },
    (error) => {
      console.warn('TDlxHT: Note on loading external model, utilizing procedural companion model.', error);
      // Placeholder remains active as graceful fallback
    }
  );

  /**
   * Constructs a stylized 3D low-poly cat model using Cyprus & Sand glass materials
   */
  function createProceduralCat() {
    const rig = new THREE.Group();

    // Torus pedestal ring
    const pedestalGeo = new THREE.CylinderGeometry(0.9, 1.0, 0.12, 32);
    const pedestal = new THREE.Mesh(pedestalGeo, cyprusGlassMaterial);
    pedestal.position.set(0, -0.9, 0);
    pedestal.add(new THREE.LineSegments(new THREE.EdgesGeometry(pedestalGeo), sandEdgeMaterial));
    rig.add(pedestal);

    // Body (Haunches & sitting torso)
    const torsoGeo = new THREE.SphereGeometry(0.68, 24, 24);
    const torso = new THREE.Mesh(torsoGeo, sandGlassMaterial);
    torso.scale.set(0.9, 1.25, 0.85);
    torso.position.set(0, -0.15, 0);
    torso.add(new THREE.LineSegments(new THREE.EdgesGeometry(torsoGeo), cyprusEdgeMaterial));
    rig.add(torso);

    // Cat Chest Emblem
    const emblemGeo = new THREE.BoxGeometry(0.35, 0.45, 0.12);
    const emblem = new THREE.Mesh(emblemGeo, cyprusGlassMaterial);
    emblem.position.set(0, -0.1, 0.52);
    emblem.add(new THREE.LineSegments(new THREE.EdgesGeometry(emblemGeo), sandEdgeMaterial));
    rig.add(emblem);

    // Head
    const headGeo = new THREE.SphereGeometry(0.50, 24, 24);
    const head = new THREE.Mesh(headGeo, sandGlassMaterial);
    head.scale.set(1.05, 0.95, 0.95);
    head.position.set(0, 0.82, 0.15);
    head.add(new THREE.LineSegments(new THREE.EdgesGeometry(headGeo), cyprusEdgeMaterial));
    rig.add(head);

    // Left Ear
    const earGeo = new THREE.ConeGeometry(0.20, 0.42, 4);
    const leftEar = new THREE.Mesh(earGeo, cyprusGlassMaterial);
    leftEar.position.set(-0.32, 1.28, 0.12);
    leftEar.rotation.set(-0.15, 0.2, 0.35);
    leftEar.add(new THREE.LineSegments(new THREE.EdgesGeometry(earGeo), sandEdgeMaterial));
    rig.add(leftEar);

    // Right Ear
    const rightEar = new THREE.Mesh(earGeo, cyprusGlassMaterial);
    rightEar.position.set(0.32, 1.28, 0.12);
    rightEar.rotation.set(-0.15, -0.2, -0.35);
    rightEar.add(new THREE.LineSegments(new THREE.EdgesGeometry(earGeo), sandEdgeMaterial));
    rig.add(rightEar);

    // Eyes
    const eyeGeo = new THREE.SphereGeometry(0.08, 16, 16);
    const leftEye = new THREE.Mesh(eyeGeo, cyprusGlassMaterial);
    leftEye.position.set(-0.18, 0.88, 0.58);
    rig.add(leftEye);

    const rightEye = new THREE.Mesh(eyeGeo, cyprusGlassMaterial);
    rightEye.position.set(0.18, 0.88, 0.58);
    rig.add(rightEye);

    // Whiskers (Subtle Sand line segments)
    const whiskerPoints = [
      new THREE.Vector3(-0.15, 0.76, 0.6), new THREE.Vector3(-0.55, 0.82, 0.55),
      new THREE.Vector3(-0.15, 0.72, 0.6), new THREE.Vector3(-0.58, 0.70, 0.55),
      new THREE.Vector3(0.15, 0.76, 0.6), new THREE.Vector3(0.55, 0.82, 0.55),
      new THREE.Vector3(0.15, 0.72, 0.6), new THREE.Vector3(0.58, 0.70, 0.55)
    ];
    const whiskerGeo = new THREE.BufferGeometry().setFromPoints(whiskerPoints);
    const whiskers = new THREE.LineSegments(whiskerGeo, sandEdgeMaterial);
    rig.add(whiskers);

    // Tail (Curved Torus segment)
    const tailGeo = new THREE.TorusGeometry(0.48, 0.08, 16, 32, Math.PI * 0.9);
    const tail = new THREE.Mesh(tailGeo, sandGlassMaterial);
    tail.position.set(0.35, -0.55, -0.35);
    tail.rotation.set(0.3, 0.8, -0.4);
    tail.add(new THREE.LineSegments(new THREE.EdgesGeometry(tailGeo), cyprusEdgeMaterial));
    rig.add(tail);

    rig.scale.set(1.15, 1.15, 1.15);
    return rig;
  }

  // ==========================================
  // ELEMENT 2: Floating Dimensional Task Card (Left Wing)
  // ==========================================
  const taskGroup = new THREE.Group();
  const cardGeo = new THREE.BoxGeometry(2.6, 1.5, 0.08);
  const taskCard = new THREE.Mesh(cardGeo, sandGlassMaterial);
  const cardEdges = new THREE.LineSegments(new THREE.EdgesGeometry(cardGeo), cyprusEdgeMaterial);
  taskCard.add(cardEdges);
  taskGroup.add(taskCard);

  // Completed checkmark badge
  const badgeGeo = new THREE.CylinderGeometry(0.18, 0.18, 0.05, 32);
  const badgeMesh = new THREE.Mesh(badgeGeo, cyprusGlassMaterial);
  badgeMesh.rotation.x = Math.PI / 2;
  badgeMesh.position.set(0.95, 0.45, 0.06);
  badgeMesh.add(new THREE.LineSegments(new THREE.EdgesGeometry(badgeGeo), sandEdgeMaterial));
  taskGroup.add(badgeMesh);

  // Subtask progress rail
  const railGeo = new THREE.BoxGeometry(1.9, 0.06, 0.03);
  const railMesh = new THREE.Mesh(railGeo, cyprusGlassMaterial);
  railMesh.position.set(-0.15, -0.35, 0.06);
  taskGroup.add(railMesh);

  const fillGeo = new THREE.BoxGeometry(1.2, 0.07, 0.04);
  const fillMesh = new THREE.Mesh(fillGeo, sandGlassMaterial);
  fillMesh.position.set(-0.50, -0.35, 0.07);
  taskGroup.add(fillMesh);

  taskGroup.position.set(-3.6, 1.1, -0.4);
  taskGroup.rotation.set(-0.06, 0.25, -0.04);
  masterGroup.add(taskGroup);

  // ==========================================
  // ELEMENT 3: Focus Torus Ring
  // ==========================================
  const timerGroup = new THREE.Group();
  const torusGeo = new THREE.TorusGeometry(1.2, 0.05, 24, 72);
  const torusMesh = new THREE.Mesh(torusGeo, sandGlassMaterial);
  timerGroup.add(torusMesh);

  const tickGeo = new THREE.SphereGeometry(0.10, 16, 16);
  const tickMesh = new THREE.Mesh(tickGeo, cyprusGlassMaterial);
  tickMesh.position.set(1.2, 0, 0);
  tickMesh.add(new THREE.LineSegments(new THREE.EdgesGeometry(tickGeo), sandEdgeMaterial));
  timerGroup.add(tickMesh);

  timerGroup.position.set(-3.8, -1.8, 0.2);
  timerGroup.rotation.set(0.85, 0.4, 0.15);
  masterGroup.add(timerGroup);

  // ==========================================
  // ELEMENT 4: Multi-Session Habit Capsule Pillars (2-Session Target)
  // ==========================================
  const habitGroup = new THREE.Group();
  const pill1Geo = new THREE.CapsuleGeometry(0.24, 0.70, 8, 20);
  const pill1Mesh = new THREE.Mesh(pill1Geo, cyprusGlassMaterial);
  pill1Mesh.add(new THREE.LineSegments(new THREE.EdgesGeometry(pill1Geo), sandEdgeMaterial));
  habitGroup.add(pill1Mesh);

  const pill2Geo = new THREE.CapsuleGeometry(0.24, 0.70, 8, 20);
  const pill2Mesh = new THREE.Mesh(pill2Geo, sandGlassMaterial);
  pill2Mesh.add(new THREE.LineSegments(new THREE.EdgesGeometry(pill2Geo), cyprusEdgeMaterial));
  pill2Mesh.position.set(0.62, 0, 0);
  habitGroup.add(pill2Mesh);

  habitGroup.position.set(3.8, 1.6, 0.4);
  habitGroup.rotation.set(0.12, -0.28, 0.1);
  masterGroup.add(habitGroup);

  // ==========================================
  // ELEMENT 5: Streak Matrix Plane
  // ==========================================
  const gridGroup = new THREE.Group();
  const cellGeo = new THREE.BoxGeometry(0.22, 0.22, 0.03);
  for (let r = 0; r < 4; r++) {
    for (let c = 0; c < 7; c++) {
      const isCompleted = (r * 7 + c) % 3 !== 0;
      const cellMat = isCompleted ? cyprusGlassMaterial : sandGlassMaterial;
      const cellMesh = new THREE.Mesh(cellGeo, cellMat);
      cellMesh.position.set((c - 3) * 0.30, -(r - 1.5) * 0.30, 0);
      gridGroup.add(cellMesh);
    }
  }
  gridGroup.position.set(3.6, -1.9, -0.6);
  gridGroup.rotation.set(-1.0, 0.25, -0.15);
  masterGroup.add(gridGroup);

  // ==========================================
  // ELEMENT 6: Drifting Ambient Focus Particles
  // ==========================================
  const particleCount = 140;
  const particleGeo = new THREE.BufferGeometry();
  const particlePositions = new Float32Array(particleCount * 3);

  for (let i = 0; i < particleCount * 3; i += 3) {
    particlePositions[i] = (Math.random() - 0.5) * 20;
    particlePositions[i + 1] = (Math.random() - 0.5) * 14;
    particlePositions[i + 2] = (Math.random() - 0.5) * 10;
  }

  particleGeo.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3));
  const particleMat = new THREE.PointsMaterial({
    color: 0xF0EDE5,
    size: 0.07,
    transparent: true,
    opacity: 0.7,
    blending: THREE.AdditiveBlending
  });
  const particles = new THREE.Points(particleGeo, particleMat);
  scene.add(particles);

  // ==========================================
  // 7. Whole-Page Scroll & Pointer Motion Tracking
  // ==========================================
  let targetScrollFraction = 0;
  let currentScrollFraction = 0;

  let targetPointerX = 0;
  let targetPointerY = 0;
  let currentPointerX = 0;
  let currentPointerY = 0;

  let currentCatRotY = 0;
  let currentCatRotX = 0;

  // Calculates scroll progress across the entirety of the webpage
  const handleScroll = () => {
    const docHeight = Math.max(
      document.documentElement.scrollHeight - window.innerHeight,
      1
    );
    const scrollY = window.scrollY || window.pageYOffset || 0;
    targetScrollFraction = Math.min(Math.max(scrollY / docHeight, 0), 1);
  };

  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();

  // Pointer movement tracking across the whole window
  const handlePointerMove = (e) => {
    const x = (e.clientX / window.innerWidth) * 2 - 1;
    const y = -((e.clientY / window.innerHeight) * 2 - 1);
    targetPointerX = THREE.MathUtils.clamp(x * 0.7, -0.7, 0.7);
    targetPointerY = THREE.MathUtils.clamp(y * 0.45, -0.45, 0.45);
  };

  window.addEventListener('pointermove', handlePointerMove, { passive: true });

  // Floating ambient micro-oscillations via GSAP
  if (!prefersReducedMotion) {
    gsap.to(taskGroup.position, {
      y: '+=0.20',
      duration: 3.6,
      yoyo: true,
      repeat: -1,
      ease: 'sine.inOut'
    });

    gsap.to(habitGroup.position, {
      y: '-=0.22',
      duration: 4.0,
      yoyo: true,
      repeat: -1,
      ease: 'sine.inOut',
      delay: 0.3
    });

    gsap.to(timerGroup.rotation, {
      z: Math.PI * 2,
      duration: 28,
      repeat: -1,
      ease: 'none'
    });
  }

  // ==========================================
  // 8. Responsive Viewport Resize Handler
  // ==========================================
  const handleResize = () => {
    width = window.innerWidth;
    height = window.innerHeight;
    if (width === 0 || height === 0) return;

    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));

    // Dynamic scale and positioning for mobile vs. desktop
    if (width < 768) {
      catGroup.scale.set(0.72, 0.72, 0.72);
      taskGroup.scale.set(0.55, 0.55, 0.55);
      taskGroup.position.set(-1.6, 2.4, -1.2);
      habitGroup.scale.set(0.55, 0.55, 0.55);
      habitGroup.position.set(1.6, -2.5, -0.8);
      gridGroup.position.set(1.2, -3.2, -1.4);
    } else {
      catGroup.scale.set(1.0, 1.0, 1.0);
      taskGroup.scale.set(1, 1, 1);
      taskGroup.position.set(-3.6, 1.1, -0.4);
      habitGroup.scale.set(1, 1, 1);
      habitGroup.position.set(3.8, 1.6, 0.4);
      gridGroup.position.set(3.6, -1.9, -0.6);
    }
  };

  window.addEventListener('resize', handleResize, { passive: true });
  handleResize();

  // ==========================================
  // 9. Adaptive Whole-Page Animation Loop
  // ==========================================
  let animationFrameId = null;
  let isPageVisible = true;

  document.addEventListener('visibilitychange', () => {
    isPageVisible = !document.hidden;
  });

  function render() {
    animationFrameId = requestAnimationFrame(render);

    if (!isPageVisible) return;

    const delta = clock.getDelta();
    if (animationMixer) {
      animationMixer.update(delta);
    }

    if (!prefersReducedMotion) {
      // Smooth interpolation for whole-page scroll & pointer
      currentScrollFraction += (targetScrollFraction - currentScrollFraction) * 0.055;
      currentPointerX += (targetPointerX - currentPointerX) * 0.045;
      currentPointerY += (targetPointerY - currentPointerY) * 0.045;

      // Camera subtle parallax
      camera.position.x = currentPointerX * 0.4;
      camera.position.y = currentPointerY * 0.3;
      camera.lookAt(0, 0, 0);

      // BIDIRECTIONAL SCROLL-LINKED CAT ROTATION ACROSS THE WHOLE PAGE:
      // Scrolling DOWN rotates the cat downward / clockwise
      // Scrolling UP rotates the cat upward / counter-clockwise in reverse!
      // Two full rotations (Math.PI * 4) as user traverses from top to bottom
      const targetCatY = currentScrollFraction * Math.PI * 4 + currentPointerX * 0.6;
      const targetCatX = (currentScrollFraction - 0.2) * 0.5 + currentPointerY * 0.35;

      currentCatRotY += (targetCatY - currentCatRotY) * 0.08;
      currentCatRotX += (targetCatX - currentCatRotX) * 0.08;

      catGroup.rotation.y = currentCatRotY;
      catGroup.rotation.x = currentCatRotX;
      catGroup.rotation.z = Math.sin(currentScrollFraction * Math.PI * 2) * 0.12;

      // Dynamic whole-page position traversal for the cat:
      // - Hero (top, fraction 0): positioned to the right of the headline
      // - Features/Tools (fraction 0.3 - 0.6): gently tracks across center-right
      // - Download/Deep Links (bottom, fraction 0.8 - 1.0): rests prominently alongside download cards
      if (width >= 768) {
        const posX = 2.4 - Math.sin(currentScrollFraction * Math.PI) * 1.8;
        const posY = 0.3 - currentScrollFraction * 0.6 + Math.sin(currentScrollFraction * Math.PI * 3) * 0.2;
        catGroup.position.x += (posX - catGroup.position.x) * 0.05;
        catGroup.position.y += (posY - catGroup.position.y) * 0.05;
      }

      // Master spatial group subtle reactive orientation
      masterGroup.rotation.y = currentScrollFraction * 0.2 + currentPointerX * 0.08;
      masterGroup.position.z = -currentScrollFraction * 1.2;

      // Focus particles orbital drift
      particles.rotation.y += 0.0006;
      particles.rotation.x += 0.0003;
    }

    renderer.render(scene, camera);
  }

  render();

  // ==========================================
  // 10. Deep Resource Disposal on Unload
  // ==========================================
  function disposeHierarchy(obj) {
    if (!obj) return;
    obj.traverse((child) => {
      if (child.isMesh) {
        if (child.geometry) child.geometry.dispose();
        if (child.material) {
          if (Array.isArray(child.material)) {
            child.material.forEach(m => m.dispose());
          } else {
            child.material.dispose();
          }
        }
      }
    });
  }

  const cleanup = () => {
    if (animationFrameId) cancelAnimationFrame(animationFrameId);
    window.removeEventListener('scroll', handleScroll);
    window.removeEventListener('pointermove', handlePointerMove);
    window.removeEventListener('resize', handleResize);
    if (animationMixer) animationMixer.stopAllAction();
    disposeHierarchy(scene);
    renderer.dispose();
    if (typeof renderer.forceContextLoss === 'function') {
      renderer.forceContextLoss();
    }
  };

  window.addEventListener('beforeunload', cleanup, { once: true });
  window.addEventListener('pagehide', cleanup, { once: true });
}
