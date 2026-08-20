# Yuma Design System (YDS)

**Version:** 2.1  
**Status:** Stable Foundation  

---

## 1. Philosophy

Yuma is a design system centered around music as the primary content. It establishes a strict set of constraints and rules to produce a lightweight, deep, and aesthetically cohesive interface.

The interface avoids generic, monolithic Material 3 templates with flat, continuous lists. Yuma's visual identity is built on segmented glass geometry, accented matte icon badges, breathable whitespace, and tactile micro-interactions.

---

## 2. Visual Language & Character

* **Calmness:** The interface never overwhelms the user or distracts from the listening experience.
* **Depth & Geometry:** Depth is established via subtle translucent surfaces (`glassBackground`) and a 1dp outline (`glassBorder`).
* **Soft Geometry:** Composite corner radii (22dp on outer group corners, 5dp on inner joints) visually unify distinct rows into a cohesive group.
* **Low Visual Noise:** Total elimination of divider lines (`Dividers`) in settings lists. Separation is achieved strictly through inter-row gaps (`SegmentGap = 1.5dp`) and structural padding. Dividers remain permissible within standard M3 dialogs and selection bottom sheets.
* **Physicality:** Every interactive element delivers tactile press feedback via a spring-animated scale down to `0.96f`.

---

## 3. Design Principles

1. **Hierarchy Through Scale:**
   * **Preference Rows:** Fixed minimum height of **72dp**.
   * **Primary Actions:** Recommended height of **56dp+**.
   * **Standard Interactive Controls:** Minimum touch target of **48×48dp**.
   * **Icon Badges:** Container size of **32dp / 46dp**.
2. **Segmented Glass Structure:** Settings rows and integration cards are composed as independent segmented glass buttons, grouped into functional blocks separated by `SegmentGap`.
3. **Surface Hierarchy (Strict Separation of Surfaces):**
   * **Solid Surfaces:** Opaque tonal containers (`surface`, `surfaceContainer`) for main content surfaces where translucency is unnecessary.
   * **Static Translucent Glass:** Translucent fill (`glassBackground`) + 1dp outline (`glassBorder`) **strictly without backdrop blur**. Applied to segmented settings lists and cards. Modal bottom sheets and selection dialogs utilize opaque solid surfaces (`surfaceContainerHigh`).
   * **Backdrop Blur Glass:** Background blur + tonal overlay. Applied **exclusively** to floating overlays (Bottom Player Bar, floating FAB, modal backdrops).
4. **Color Driven:** Dynamic Material 3 Expressive palette generation extracted from the current track's album art for accents, switch thumbs, and active indicators.
5. **No Divider Lines:** Divider lines are omitted in preference lists. Group boundaries are defined solely by card geometry and borders.

---

## 4. Foundations (Design Tokens)

All dimensions, paddings, and radii must be referenced directly from design tokens (`SettingsDimensions` / `YumaSpacing` / `YumaRadius`). Arbitrary hardcoded values are prohibited.

### Spacing
* **`SegmentGap`:** `1.5dp` (inter-row gap within a segmented group)
* **`Section`:** `12dp` (spacing between independent preference groups, `SectionSpacing`)
* **`Medium`:** `16dp` (screen and card horizontal padding, `ScreenHorizontalPadding`)
* **`Large`:** `24dp` (bottom screen padding, `ScreenBottomPadding`)

### Radius
* **`SegmentInner`:** `5dp` (inner joint corners between grouped rows)
* **`Small`:** `12dp`
* **`Medium`:** `16dp` (buttons, action chips)
* **`SegmentOuter`:** `22dp` (outer corners of first/last group rows and cards)
* **`SheetList`:** `24dp` (corner radius of inner sheet lists, `BottomSheetListCornerRadius`)
* **`Sheet`:** `28dp` (modal bottom sheets, `BottomSheetCornerRadius`)
* **`Max / Pill`:** `32dp` / `CircleShape` (FAB, toggle badges, circular indicators)

### Colors & Surfaces
* **`glassBackground`:** Static translucent matte fill for dark and light themes (no runtime blur shaders).
* **`glassBorder`:** 1dp subtle translucent outline for crisp edge definition.
* **`primaryContainer` / `primary`:** Dynamic accent color for active toggle pills, badges, and switch thumbs.

---

## 5. Settings & Preferences (Pattern: Glass Segmented Rows)

### 5.1 Preference Group Anatomy

Each row within a group is an **independent glass card** (`Modifier.yumaGlassCard`), rather than an item inside a single shared container.


┌─────────────────────────────────────────────────────────┐  ◄── Top Corners: 22dp (SegmentOuter)
│  [Badge]  Title & Description                 [Control] │
└─────────────────────────────────────────────────────────┘  ◄── Bottom Corners: 5dp (SegmentInner)
▲
Gap: 1.5dp (SegmentGap, No Divider)
▼
┌─────────────────────────────────────────────────────────┐  ◄── All Corners: 5dp (SegmentInner)
│  [Badge]  Title & Description                 [Control] │
└─────────────────────────────────────────────────────────┘  ◄── All Corners: 5dp (SegmentInner)
▲
Gap: 1.5dp (SegmentGap, No Divider)
▼
┌─────────────────────────────────────────────────────────┐  ◄── Top Corners: 5dp (SegmentInner)
│  [Badge]  Title & Description                 [Chevron] │
└─────────────────────────────────────────────────────────┘  ◄── Bottom Corners: 22dp (SegmentOuter)


* **Corner Radii by Position (`PreferenceGroupPosition`):**
  * **`Single`:** 22dp on all four corners.
  * **`First`:** Top corners 22dp, bottom corners 5dp.
  * **`Middle`:** All corners 5dp.
  * **`Last`:** Top corners 5dp, bottom corners 22dp.
* **Icon Badges:** `46dp` container (`SegmentedIconBoxSize`) with custom squircle/petal geometry and monochrome or accent fill.
* **Typography:**
  * Title: `titleMedium` Bold (W700), color `onSurface`.
  * Description: `bodyMedium`, color `onSurfaceVariant`, single-line truncated with ellipsis.
* **Navigation Items:** Display a trailing chevron `R.drawable.ic_arrow_right` with `RowChevronAlpha` opacity.

---

### 5.2 Provider Chip (Segmented Toggle)

Interactive music provider toggle (YouTube Music / Spotify):
* Implemented as a `yumaGlassCard` segmented card.
* Features a track with a sliding 36dp container (`primary`) that smoothly animates on provider change.
* Active label renders in `onPrimary`, inactive in `onSurfaceVariant`.

---

### 5.3 Dropdowns & Selection Dialogs

Dropdowns, context dialogs, and single-choice selectors follow **Material 3 structural patterns** (aligned with the main settings screen): grouped M3-styled buttons organized into clean blocks.

Key differences from vanilla M3 (ArchiveTune):
* Individual translucent fill + border per item instead of a single solid container.
* Custom geometric icon containers (squircles / petals).
* The chevron `R.drawable.ic_arrow_right` is reserved strictly for screen navigation rows.

---

## 6. Interaction Guidelines

| State | Visual Feedback |
| :--- | :--- |
| **Default** | Translucent `glassBackground` fill + 1dp `glassBorder`. |
| **Pressed** | Scale down to `0.96f` with `spring(stiffness = Spring.StiffnessMedium)`. |
| **Active / Selected** | Active indicator filled with `primary`, text colored `onPrimary`. |
| **Disabled** | Row opacity set to `0.5f`, touch handling blocked. |

---

## 7. Component Checklist



Design Tokens (SettingsDimensions, YumaColors)
↓
Primitive Modifiers
(Modifier.yumaGlassCard, Modifier.yumaClickable)
↓
Composite Components
(PreferenceGroup, PreferenceEntry, SwitchPreference, SegmentedPreference, ListPreference, EditTextPreference, SliderPreference, NumberPickerPreference)
↓
Screens
(SettingsScreen, AccountSettings, AppearanceSettings, etc.)


---

## 8. Do / Don't

### ✔ DO
* Use `Modifier.yumaGlassCard()` with `glassBackground` fill and 1dp `glassBorder` for all preference rows.
* Use the `SegmentGap` token (1.5dp) to separate grouped rows instead of `HorizontalDivider`.
* Apply segmented corner radii (22dp outer, 5dp inner) to unify items within a group.
* Use custom squircle/petal badge shapes for icons.
* Follow M3 conventions for dialogs and selection bottom sheets (opaque solid surfaces, permissible dividers).

### ✘ DON'T
* Wrap an entire preference group into a single opaque container without individual segmented rows.
* Apply heavy runtime blur shaders (`BackdropBlur`) to list items or preference rows (blur is strictly reserved for floating overlays).
* Introduce arbitrary corner radii or paddings outside the defined YDS tokens.
* Use generic circular solid chips from default Material 3.
