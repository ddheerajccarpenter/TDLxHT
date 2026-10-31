import React from "react";
import { Classic } from "loading-dev";

/**
 * Classic Loading Indicator from loading-dev
 * Dark mode: color="#ffffff" size={48}
 * Light mode: color="#000000" size={48}
 */
export function ClassicDemo({ isDark = true }: { isDark?: boolean }) {
  return <Classic color={isDark ? "#ffffff" : "#000000"} size={48} />;
}

export function ClassicDarkMode() {
  return <Classic color="#ffffff" size={48} />;
}

export function ClassicLightMode() {
  return <Classic color="#000000" size={48} />;
}
