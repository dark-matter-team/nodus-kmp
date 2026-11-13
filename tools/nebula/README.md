# Nebula background texture

`nebula.sksl` is the "Nebula" shader from the Figma mockup (Nodi Space), ported from WGSL to SkSL
with the mockup parameters fixed (seed 8, brightness 0.49, starDensity 0.11, travel 0, center 50%/50%).

The shader is static but too heavy to run every frame, so the app ships a pre-rendered texture:
`shared/src/commonMain/composeResources/drawable/nebula_stars.webp`.

To regenerate it, render `nebula.sksl` with Skia (`RuntimeEffect.makeForShader`) into a
2880×1200 surface with `iResolution = (2880, 1200)` and save it as lossless WebP.
The shader's coordinates depend only on the screen height and spread symmetrically from the
center, so `NebulaBackground` scales the texture to the screen height and centers it;
this matches the live shader for any screen up to a 2.4:1 aspect ratio.
