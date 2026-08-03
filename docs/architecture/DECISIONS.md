# Architectural Decision Records (ADRs)

This document records the major architectural, design, and technical decisions made for YumaPlayer. 
Each record captures the context, decision, rationale, and consequences of a key architectural choice.

---

## Index of Decisions

- **[ADR-001](#adr-001-material-3-expressive-ui-foundation)** — Material 3 Expressive as the UI Foundation
- **[ADR-002](#adr-002-hct-hue-chroma-tone-color-engine)** — HCT (Hue-Chroma-Tone) Color Engine for Dynamic Theming
- **[ADR-003](#adr-003-unidirectional-data-flow-udf--stateflow)** — Unidirectional Data Flow (UDF) & StateFlow
- **[ADR-004](#adr-004-androidx-media3-as-exclusive-playback-engine)** — AndroidX Media3 as Exclusive Playback Engine
- **[ADR-005](#adr-005-jetpack-compose-only)** — Jetpack Compose Only
- **[ADR-006](#adr-006-glass--translucency-effects-restricted-to-overlays)** — Glass & Translucency Effects Restricted to Overlays
- **[ADR-007](#adr-007-pure-kotlin-domain-layer)** — Pure Kotlin Domain Layer (Zero Android Framework Imports)
- **[ADR-008](#adr-008-multi-module-clean-architecture)** — Multi-Module Clean Architecture

---

## ADR-001: Material 3 Expressive UI Foundation

- **Status:** Accepted
- **Context:** Music players require distinct visual identity, high contrast legibility, and rich motion while maintaining standard Android accessibility.
- **Decision:** Adopt Material 3 Expressive principles as the foundation for Yuma Design System (YDS 1.0). Customize shapes (squircled corners, custom radii), elevation, and expressive motion curves.
- **Consequences:**
  - *Positive:* Unique music-centric aesthetic, smooth spring animations, unified design tokens.
  - *Negative:* Most UI components should be built through Yuma UI Kit wrappers rather than consuming raw Material 3 components directly.

---

## ADR-002: HCT (Hue-Chroma-Tone) Color Engine

- **Status:** Accepted
- **Context:** Album art covers vary dramatically in color palette. RGB/HSL tinting often results in muddy UI colors or poor contrast against text.
- **Decision:** Use Google's HCT (Hue, Chroma, Tone) color model to dynamically extract dominant album colors and map them to YDS surface tokens designed to maintain accessible contrast ratios.
- **Consequences:**
  - *Positive:* Perceptually accurate color extraction, automatic high-contrast light/dark surfaces.
  - *Negative:* Requires CPU-bound palette extraction (must run off the Main thread on background dispatchers).

---

## ADR-003: Unidirectional Data Flow (UDF) & StateFlow

- **Status:** Accepted
- **Context:** Complex UI state (loading, error, queue updates, lyrics sync) can easily lead to state desynchronization and race conditions if mutated from multiple sources.
- **Decision:** Enforce UDF across all screens. ViewModels expose a single immutable `StateFlow<UiState>` and process incoming user actions via sealed intent interfaces (`UiIntent`).
- **Consequences:**
  - *Positive:* Single source of truth, predictable state transitions, seamless UI testability.
  - *Negative:* Requires creating explicit intent classes and state wrappers for every screen flow.

---

## ADR-004: AndroidX Media3 as Exclusive Playback Engine

- **Status:** Accepted
- **Context:** Background audio playback must support system media controls, notification actions, Android Auto, lockscreen controls, and ExoPlayer customization.
- **Decision:** Build playback infrastructure strictly around AndroidX Media3 (`MediaSessionService`, `ExoPlayer`, `MediaController`).
- **Consequences:**
  - *Positive:* Seamless system integrations, official Media3 cache data source support, decoupled UI from playback lifecycle.
  - *Negative:* Inter-process communication complexity between UI `MediaController` and background `MediaSession`.

---

## ADR-005: Jetpack Compose Only

- **Status:** Accepted
- **Context:** XML layout inflation adds view hierarchy overhead and complex state synchronization code.
- **Decision:** YumaPlayer UI is declarative Jetpack Compose. XML-based UI layouts are not used for new development.
- **Consequences:**
  - *Positive:* Concise layout code, reactive UI state binding, shared YDS design token system.
  - *Negative:* `AndroidView` interop is strictly restricted to specialized surface engines (e.g., video canvas playback).

---

## ADR-006: Glass & Translucency Effects Restricted to Overlays

- **Status:** Accepted
- **Context:** Overusing glassmorphism (translucency + background blur) causes high GPU render latency and poor text legibility on low-end devices.
- **Decision:** Glass effects are strictly restricted to Overlay surfaces (mini-player bar, bottom sheets, dialogs, floating headers). Main content backgrounds must use solid YDS surface tones.
- **Consequences:**
  - *Positive:* Preserves GPU performance, guarantees text legibility on main screens.
  - *Negative:* Limits design freedom for main content areas.

---

## ADR-007: Pure Kotlin Domain Layer

- **Status:** Accepted
- **Context:** Coupling business logic to Android framework APIs makes unit testing slow and breaks multiplatform / modular architecture potential.
- **Decision:** Domain modules (`:core:domain`, `:core:model`) must remain pure Kotlin modules with zero imports from `android.*` packages.
- **Consequences:**
  - *Positive:* Fast JVM unit testing (no Robolectric needed), clean separation of concerns.
  - *Negative:* Android Context utilities must be wrapped in domain interfaces implemented in `:data` or `:app`.

---

## ADR-008: Multi-Module Clean Architecture

- **Status:** Accepted
- **Context:** A monolithic application module leads to slow build times, tight coupling, and uncontrolled dependencies.
- **Decision:** Split YumaPlayer into isolated Gradle modules (`:app`, `:core`, `:feature:*`, `:lyrics:*`, `:canvas`, `:spotifycore`, `:shazamkit`) with inward dependency rules toward domain core.
- **Consequences:**
  - *Positive:* Parallel Gradle builds, strict feature isolation, modular provider system.
  - *Negative:* Requires managing build logic across multiple module definitions.
