package com.darkmatter.nodus.ui.theme

import androidx.compose.ui.graphics.Shader

/**
 * Створює шейдер з коду SkSL. Android виконує його через RuntimeShader (AGSL, Android 13+),
 * Desktop та iOS через Skia RuntimeEffect. Повертає null, якщо платформа це не підтримує.
 */
internal expect fun createRuntimeShader(sksl: String, width: Float, height: Float): Shader?

/**
 * Шейдер "Nebula" з макета Figma (зорі й туманність), перенесений з WGSL на SkSL без змін алгоритму.
 * Параметри зафіксовані як у макеті: seed 8, brightness 0.49, starDensity 0.11, travel 0, центр 50%/50%.
 * Шейдер статичний, тому кадр не анімується.
 */
internal const val NEBULA_SKSL = """
uniform float2 iResolution;

const float3 COLOR_A = float3(0.5, 0.2, 0.8);
const float3 COLOR_B = float3(0.1, 0.3, 0.9);
const float3 COLOR_C = float3(0.8, 0.1, 0.5);
const float STAR_DENSITY = 0.11;
const float BRIGHTNESS = 0.49;
const float GLOBAL_SEED = 8.0;
const float TRAVEL = 0.0;
const float2 CENTER = float2(50.0, 50.0);

float N11(float n) {
  float2 v1 = float2(fract(n * 129.3484), fract(n * 10.2347 + 1.4948));
  float2 v2 = float2(49.256, n);
  return fract(dot(v1, v2));
}

float2 N12(float n) {
  float2 v1 = float2(fract(n * 33.24102 + 1.2847), fract(n * 4.70234556 - 2.5856));
  float2 v2 = float2(39.3823 + n, n * 3.1938 + 1.4028);
  return float2(fract(dot(v1, v2)), N11(dot(v1, v2)));
}

float2 N22(float2 p) {
  return float2(N11(4.238 + p.y + p.x * 0.6274), N11(3.4148 * p.y - 8.29 * p.x + 1.39558));
}

float perlin(float2 p, float scale, float seed) {
  float2 pS = p * scale;
  float X1 = floor(p.x * scale);
  float X2 = X1 + 1.0;
  float Y1 = floor(p.y * scale);
  float Y2 = Y1 + 1.0;
  float2 v11 = float2(X1, Y1);
  float2 gpUnfaded = pS - v11;
  float xCub = pow(gpUnfaded.x, 3.0);
  float yCub = pow(gpUnfaded.y, 3.0);
  float2 gp = float2(
    (6.0 * gpUnfaded.x * gpUnfaded.x - 15.0 * gpUnfaded.x + 10.0) * xCub,
    (6.0 * gpUnfaded.y * gpUnfaded.y - 15.0 * gpUnfaded.y + 10.0) * yCub
  );
  float2 v12 = float2(X1, Y2);
  float2 v21 = float2(X2, Y1);
  float2 v22 = float2(X2, Y2);
  float2 d11 = gp - v11;
  float2 d12 = gp - v12;
  float2 d21 = gp - v21;
  float2 d22 = gp - v22;
  float fact = 1.394 + seed;
  float2 g11 = (N22(v11 * fact) - 0.5) * 2.0;
  float2 g12 = (N22(v12 * fact) - 0.5) * 2.0;
  float2 g21 = (N22(v21 * fact) - 0.5) * 2.0;
  float2 g22 = (N22(v22 * fact) - 0.5) * 2.0;
  float2 contribY1 = mix(g11, g21, gp.x);
  float2 contribY2 = mix(g12, g22, gp.x);
  float2 contrib = mix(contribY1, contribY2, gp.y);
  float value = dot(d11, contrib) + dot(d12, contrib) - dot(d21, contrib) - dot(d22, contrib);
  return mix(0.0, 1.0, value);
}

float3 starColor(float2 p, float id, float radius) {
  float2 center = N12(id) - 0.5;
  float3 color = float3(1.0, 1.0, 1.0);
  float2 v = center - p;
  float dist = length(v);
  float angle = abs(sqrt(abs(v.x * v.y))) * 5.0;
  float star = smoothstep(radius * 0.3, radius * 0.25, dist);
  float halo = smoothstep(radius * 1.2, 0.0, dist) * (0.7 + 0.5 * 0.3);
  float scint = smoothstep(1.0, 0.0, angle) * halo * (0.7 + 0.5 * 0.3);
  return star * float3(1.0, 1.0, 1.0) + (scint + halo) * color;
}

float3 layerColor(float2 uv, float layerIndex, float scale, float starDensity, float layerVisible) {
  float2 uvScaled = uv * scale;
  float2 uvCell = uvScaled * 10.0;
  float layerSeed = 2.309387 + layerIndex * 1.283374;
  float2 baseCellFloor = floor(uvCell);
  float2 baseFrac = fract(uvCell);
  float densityThreshold = 0.9999 - starDensity * 0.0999;
  float sizeGrowth = smoothstep(0.0, 0.6, layerVisible);

  float3 col = float3(0.0);
  for (float dx = -1.0; dx <= 1.0; dx += 1.0) {
    for (float dy = -1.0; dy <= 1.0; dy += 1.0) {
      float2 cellFloor = baseCellFloor + float2(dx, dy);
      float id = layerSeed * 1.4983 * cellFloor.y + 5.39283 * cellFloor.x;
      float radius = mix(0.1, 0.5, N11(id * 3.82918));
      float effectiveRadius = radius * sizeGrowth;
      float cellVisible = smoothstep(densityThreshold - 0.008, densityThreshold, N11(id * 19.10982));
      float2 gv = (baseFrac - float2(dx, dy)) * 2.0 - 1.0;
      col += starColor(gv, id, effectiveRadius) * cellVisible;
    }
  }
  return col;
}

float3 nebula(float2 uv, float scale, float seed) {
  float2 uvS = uv * scale;
  float blendAB = clamp(perlin(uvS, scale * 0.5, seed + 10.0), 0.0, 1.0);
  float blendC = clamp(perlin(uvS, scale * 0.3, seed + 20.0), 0.0, 1.0);
  float3 color = mix(mix(COLOR_A, COLOR_B, blendAB), COLOR_C, blendC);
  float valPerlin = perlin(uvS, scale, seed) + 0.6 * perlin(uvS, 5.0 * scale, seed) + 0.3 * perlin(uvS, 7.0 * scale, seed);
  return smoothstep(-1.0, 1.0, valPerlin) * color * 0.12 * (0.2 + 0.2 * length(uvS));
}

half4 main(float2 fragCoord) {
  float aspect = iResolution.x / iResolution.y;
  float nbLayers = 20.0;
  float widthMod = nbLayers * 0.5 + 0.5;
  float travel = TRAVEL * widthMod;

  float2 uv = (fragCoord / iResolution) * 2.0 - 1.0;
  uv.x *= aspect;
  uv -= float2((CENTER.x - 50.0) / 50.0 * aspect, (CENTER.y - 50.0) / 50.0);

  float3 col = float3(0.0);
  float width = nbLayers * 0.5;
  for (float i = 1.0; i < 20.0; i += 1.0) {
    float raw = width - (travel + i);
    float posI = raw - widthMod * floor(raw / widthMod);
    float scale = posI;
    float visible = clamp(2.0 - abs(posI - 2.0), 0.0, 1.0);
    float depthSlice = i + GLOBAL_SEED * 37.0;
    float phaseX = N11(depthSlice * 13.7841 + 1.234) - 0.5;
    float phaseY = N11(depthSlice * 7.2193 + 5.678) - 0.5;
    float2 uvStaggered = uv + float2(phaseX, phaseY) * 0.1;
    float layerAngle = N11(depthSlice * 5.1234 + 3.7) * 0.785;
    float cosA = cos(layerAngle);
    float sinA = sin(layerAngle);
    float2 uvRotated = float2(
      uvStaggered.x * cosA - uvStaggered.y * sinA,
      uvStaggered.x * sinA + uvStaggered.y * cosA
    );
    float3 nebulVal = nebula(uv, scale, depthSlice) * 0.7;
    col += visible * (layerColor(uvRotated, depthSlice, scale, STAR_DENSITY, visible) + nebulVal);
  }
  return half4(half3(col * BRIGHTNESS), 1.0);
}
"""
