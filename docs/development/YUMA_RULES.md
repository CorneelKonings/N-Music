# Yuma Rules

This document defines the mandatory engineering rules for YumaPlayer.

These rules apply to all contributions, whether written by human contributors or AI assistants.

If a rule conflicts with an implementation, the rule takes precedence.

---

## 1. Project Principles

- **No Shortcuts or Placeholders:** Never use `TODO`, `FIXME`, mock code, or temporary hacks as a final solution.
- **Single Source of Truth:** Every state or piece of data must have one unambiguous owner (StateFlow, Repository cache, or MediaSession).
- **Single Responsibility:** Classes, functions, and modules must have one clear responsibility.
- **Root Cause First:** Fix the underlying cause of an issue instead of introducing workarounds.
- **No Duplicate Implementations:** Reuse existing abstractions, helpers, and components before creating new ones.
- **Fail Gracefully:** Errors must be mapped to structured UI states (`Error`), never swallowed silently or causing unhandled crashes.

---

## 2. Architecture & Module Isolation

- **Strict Layer Inversion:** Inner layers (`:core:domain`, `:core:model`) must never import outer layers (`:data:*`, `:feature:*`, `:service:*`).
- **Feature Isolation:** Feature modules (`:feature:*`) cannot depend on each other directly. Shared code belongs in `:core:*`.
- **UI Logic Separation:** Composable functions must not contain business logic. ViewModels coordinate UI state but must not implement business rules.
- **No Direct API Access from ViewModel:** ViewModels must interact with data strictly via UseCases or Repositories.
- **No Context in ViewModels:** Android `Context` objects must never be referenced inside ViewModel instances.

---

## 3. UI & Design System (YDS)

- **YDS Is the Source of Truth:** New UI components and screens must follow Yuma Design System. Do not introduce screen-specific design patterns unless they become part of YDS.
- **Use Yuma UI Kit:** Prefer Yuma UI Kit components over raw Material 3 components. Direct Material components should only be used when no Yuma equivalent exists or within Yuma UI Kit implementations.
- **No Material Dividers:** Never use `HorizontalDivider` or `VerticalDivider`. Separate content using spacing tokens and surface color contrast.
- **No Card-in-Card Nesting:** Never nest a `Card` inside another `Card`. Use surface elevation or container fills.
- **Glass Effect Restrictions:** Translucency and blur effects are permitted ONLY for Overlay surfaces (Player bar, Bottom Sheets, Dialogs).
- **No Hardcoded UI Values:** User-facing strings must reside in `strings.xml`. Colors, dimensions, spacing, typography, and shapes must use YDS tokens whenever available.

---

## 4. Jetpack Compose & State Management

- **Reusable UI Components are Stateless:** Reusable UI components must be stateless, receiving immutable state objects and emitting intent lambdas (`onClick: () -> Unit`).
- **State Hoisting:** Components expose state and callbacks instead of owning business state.
- **Explicit Lazy Layout Keys:** Every item in `LazyColumn`, `LazyRow`, and `LazyVerticalGrid` must define an explicit unique `key`.
- **Compose Stability:** UI state models should be immutable whenever practical. Use `@Immutable` or `@Stable` where appropriate. Collect flows using `collectAsStateWithLifecycle()`.
- **No State Mutation in Composition:** Side effects and state changes inside Composable functions are strictly prohibited.

---

## 5. Concurrency & Performance

- **Explicit Thread Dispatching:** Disk/Network I/O MUST use `Dispatchers.IO`. Heavy CPU calculations/parsing MUST use `Dispatchers.Default`.
- **Zero Main-Thread Blocking:** Blocking the main thread (`runBlocking`, synchronous I/O) is strictly forbidden.
- **ViewModel Scope:** All ViewModel coroutines must be launched within `viewModelScope` and properly handle cancellation.
- **Playback Authority:** Playback control must occur exclusively through AndroidX Media3 `MediaController`.

---

## 6. Code Hygiene & Security

- **No Unsafe Casts:** Avoid unchecked casts (`as`) unless correctness is guaranteed and documented.
- **Avoid `@Suppress`:** Do not suppress compiler warnings or lint checks without a documented reason.
- **No Secrets in Code:** Hardcoded API keys, tokens, or absolute local paths are strictly prohibited. Use `local.properties` / `BuildConfig`.
- **Code Shrinking Compatibility:** Types used by serialization, reflection, or code generation must remain compatible with R8/ProGuard (e.g. `@Serializable`, `@Keep`, or explicit keep rules).

---

## 7. Documentation

- **Keep Specs Updated:** Update documentation whenever architecture, APIs, workflows, or design rules change.
- **No Hidden Conventions:** Do not introduce undocumented conventions.
- **Centralized Decisions:** Long-term project decisions belong in `docs/`, not in pull request discussions or commit messages.
