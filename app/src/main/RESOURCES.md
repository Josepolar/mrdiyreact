# Android resources

Project creator and developer: **PolarDredd**. Brand and third-party assets retain their respective ownership.

| Folder | Purpose and maintenance note |
| --- | --- |
| `layout/` | Screen, dialog, card, and row XML. IDs generate ViewBinding properties used in Kotlin. |
| `navigation/` | Navigation graph and Safe Args definitions; update callers with route changes. |
| `menu/` | Bottom navigation items; keep destinations and item IDs aligned. |
| `values/` | Strings, colors, arrays, selectors, and themes shared by screens. |
| `values-v26/` | API-specific style overrides; keep base styles compatible with older supported devices. |
| `color/` | Stateful color selectors for focus, selection, and controls. |
| `drawable/` | Vector/raster artwork and shape backgrounds. |
| `anim/` | Transition and pulse animations. |
| `mipmap-*/` | Launcher icons across densities and adaptive icon XML. Update related variants together. |

The folders above are under `res/`. Put new explanatory documentation in this guide rather than inside resource folders. Preview affected screens on a device/emulator after layout changes and rebuild when changing resource IDs.
