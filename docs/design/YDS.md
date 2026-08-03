# Yuma Design System (YDS)

**Version:** 1.0
**Status:** Stable Foundation

## 1. Philosophy

Yuma is a design system centered around music as the primary content. Rather than defining the exact appearance of every screen, it establishes a consistent set of rules and constraints that guide interface design.

The interface emphasizes the atmosphere of the currently playing track while remaining lightweight, calm, and unobtrusive. The focus is placed on large shapes, natural motion, open spacing, and readable typography rather than decorative effects.

---

## 2. Visual Language & Character

Yuma aims to create a sense of:

* **Calmness:** the interface never distracts from listening to music.
* **Depth:** layers are separated through subtle tonal differences and carefully applied glass surfaces.
* **Breathing Space:** whitespace is treated as an active part of the composition.
* **Softness:** sharp corners and aggressive visual elements are avoided.
* **Physicality:** every interaction feels natural and responsive.

Yuma avoids the feeling of:

* **Visual Clutter:** only meaningful icons, controls, and details are shown.
* **Tech Demos:** excessive blur, outdated glossy effects, and oversaturated gradients.
* **Harsh Contrast:** smooth tonal transitions that remain comfortable in dark environments.
* **Noise:** unnecessary borders, outlines, and dividers.

---

## 3. Design Principles

1. **Large Elements:** Interactive components should feel substantial and comfortable to touch (recommended minimum height: **56–64dp**).
2. **Soft Geometry:** Continuous corner shapes (Squircles / Material 3 Expressive Shapes) are used throughout the interface, maintaining a unified geometric language.
3. **Color Driven:** The interface adapts to the color palette of the currently playing content rather than relying on a static theme.
4. **Low Visual Noise:** Separation is achieved through spacing and tonal surfaces instead of borders and divider lines.
5. **Comfortable Spacing:** Whitespace is considered an essential part of the composition.
6. **Motion First:** Motion and subtle deformation provide mandatory feedback for every interaction.
7. **Hierarchy Through Scale:** Visual hierarchy is established through size and scale rather than strong color contrast.
8. **Surface Hierarchy:** Content resides on solid surfaces. Glassmorphism is reserved exclusively for temporary, floating, or overlay UI.

---

## 4. Component Philosophy
* **Card:** Used to logically group settings, track details, or metadata. Keeps an opaque, tonal background for maximum contrast and readability.
* **Button & Chip:** Represents explicit actions or tags (e.g., audio codec, bitrate). Uses subtle tonal fills (`surfaceContainerHigh`) with crisp typography.
* **Overlay / Bottom Sheet:** Uses solid, high-contrast surface containers (`surfaceContainerLow` / `surfaceContainer`) with soft rounded corners. Placed over a dimmed backdrop blur (`BackdropBlur`) to create depth without sacrificing content legibility.
* **Accent Color:** Reserved exclusively for active states, toggles, and primary actions. Avoids filling large background surfaces.
* **Divider:** Not used. Layout spacing (16–24dp) and container boundaries provide natural separation.

---

## 5. Foundations (Design Tokens)
### Spacing
* Micro — 4dp
* Small — 8dp
* Medium — 16dp
* Large — 24dp
* Extra Large — 32dp

### Radius
* Small — 12dp
* Medium — 16dp (Buttons, Chips, List items)
* Large — 24dp (Dialogs, Bottom Sheets, Media Cards)
* Max — 32dp / Pill / Circle (FAB, Floating Player Bar, Switches, Icon Containers)

### Colors & Surfaces
* **Primary, Secondary, Tertiary** — Dynamic M3 Expressive colors generated from the current artwork.
* **Base Surfaces** — `surface`, `surfaceContainer`, `surfaceContainerHigh` (opaque tonal layers for sheets, cards, and dialogs).
* **Backdrop Blur** — Applied strictly to the background layer behind floating overlays/dialogs. No inner glass borders or transparent text cards.

---

## 6. Accessibility

* **Touch Targets:** Minimum interactive area is **48×48dp** (recommended **56dp+**).
* **Dynamic Type:** Full support for system font scaling.
* **Contrast:** Surface/text combinations should meet **WCAG AA** readability guidelines.
* **Reduced Motion:** When the system's reduced motion setting is enabled, heavy animations and spring effects should be disabled.

---

## 7. Layout Rules

* **Content Max Width:** Content width on tablets and TVs is limited (**840dp max**) and automatically centered.
* **Screen Margins:** 16dp on phones, 24dp on tablets and foldables.
* **Section Spacing:** Independent content blocks are separated by **24–32dp**.
* **Insets & Safe Area:** System WindowInsets are respected, including floating UI elements such as the Bottom Player.

---

## 8. Interaction Guidelines

| State         | Visual Feedback                                                                        |
| ------------- | -------------------------------------------------------------------------------------- |
| Default       | Base appearance.                                                                       |
| Pressed       | Scale down (0.96f–0.98f) with spring animation.                                        |
| Hover / Focus | Elevated surface tone (`surfaceContainerHigh`) or subtle outline.                      |
| Disabled      | 0.38f opacity and disabled interaction.                                                |
| Loading       | Component keeps its size while content is replaced by a shimmer or progress indicator. |
| Selected      | Background changes to an accent tone (`primaryContainer`).                             |
| Dragged       | Scale increases (1.04f) with subtle elevation/shadow.                                  |

---

## 9. Components Architecture & Checklist

```
Rules & Tokens (Foundations)
            ↓
Primitive Components
(YumaSurface, YumaGlassSurface, YumaButton, YumaSlider)
            ↓
Composite Components
(YumaSettingTile, YumaMediaCard, YumaNowPlayingBar)
            ↓
Screens
(Player, Settings, Library, Lyrics)
```

### Component Quality Checklist

Every new Yuma component should:

* Use Yuma design tokens (Spacing, Radius, Colors).
* Support the required interaction states (Pressed, Disabled, Focus).
* Follow the defined surface hierarchy (Surface vs Glass).
* Support dynamic color generation.
* Provide natural interaction feedback through motion.
* Adapt correctly to tablets and large screens.
* Meet accessibility requirements (touch targets, semantics, etc.).

---

## 10. Do / Don't
### ✔ DO
* Use solid tonal containers (`surfaceContainer`) inside dialogs and bottom sheets for maximum readability.
* Apply backdrop blur to the background behind floating overlays to establish visual hierarchy.
* Use uniform circular containers (`CircleShape`) for icon badges in settings.
* Maintain clear layout spacing (16–24dp) instead of divider lines.

### ✘ DON'T
* Use semi-transparent glass cards with inner borders for dense text or technical details.
* Mix more than two corner radius sizes within a single component group.
* Create interactive controls smaller than 48×48dp.
* Overuse dynamic accent colors on large surface containers.

---

## 11. Governance

Application screens should primarily be built using components from the **Yuma UI Kit**.

Direct usage of Material 3 components is allowed only:

* inside the implementation of Yuma UI Kit components; or
* when an equivalent Yuma component has not yet been implemented.

---

## 12. Implementation Strategy

1. **Phase 1:** Finalize the YDS specification (`YDS.md`).
2. **Phase 2:** Build the Yuma UI Kit by implementing primitive (Level 2) and composite (Level 3) components under `ui/component/yuma/`.
3. **Phase 3:** Gradually migrate all screens (`:feature:*` / `ui/screens`) to the Yuma UI Kit.
4. **Phase 4:** Perform visual QA, refine micro-interactions, and eliminate inconsistencies.

---

## 13. Out of Scope

YDS intentionally does **not** define:

* Business logic or application use cases.
* Navigation architecture or routing.
* ViewModel structure or state management (`UiState` / `UiIntent`).
* Data layer, repositories, caching, networking, or databases.
* Feature-specific implementations related to audio functionality.
