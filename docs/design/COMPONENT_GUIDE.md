# Yuma UI Kit Component Guide

This guide defines the standard workflow and technical requirements for building, documenting, and testing UI components in the Yuma Design System (YDS 1.0).

---

## 1. Component Lifecycle & Creation Workflow

When creating a new UI Kit component (located under `ui/component/yuma/`), follow this 6-step workflow:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ 1. Use Tokens   ├────►│ 2. Add Motion   ├────►│3. Accessibility │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                         │
┌─────────────────┐     ┌─────────────────┐              │
│ 6. Documentation│◄────┤  5. Add Sample  │◄─────────────┘
└─────────────────┘     └─────────────────┘
```

---

## 2. Component Design Principles

### 2.1 Pure Statelessness
All Yuma UI components must be 100% stateless:
- **No Internal Business State:** Accept immutable state models or primitives (`title: String`, `isSelected: Boolean`).
- **Callback Hoisting:** Emit user interactions via lambda parameters (`onClick: () -> Unit`).
- **No Repositories or ViewModels:** Components must never reference ViewModels, UseCases, or data sources.

### 2.2 Token Usage Only
- **Colors:** Use YDS color tokens (`YumaTheme.colorScheme.*`). Never hardcode hex colors (`0xFF...`).
- **Typography:** Use YDS typography tokens (`YumaTheme.typography.*`). Never use raw `TextStyle(...)` overrides.
- **Shapes & Radius:** Use YDS shape tokens (`YumaTheme.shapes.*`).
- **Spacing:** Use YDS spacing scale (`YumaSpacing.*`).

---

## 3. Step-by-Step Implementation Guide

### Step 1: Define Stateless Interface & Parameters
```kotlin
@Composable
fun YumaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    // Component rendering
}
```

### Step 2: Integrate YDS Motion & Touch Feedback
Apply tactile feedback and smooth spring animations for touch states:
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "scale"
)
```

### Step 3: Enforce Accessibility
- High-contrast touch target: Minimum **48x48dp** (recommended **56dp** for core audio actions).
- Semantics: Provide clear `contentDescription` for non-text components or icon buttons.
```kotlin
Modifier
    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    .semantics { role = Role.Button }
```

### Step 4: Add Multipreview Declarations
Provide Compose `@Preview` annotations for Light/Dark themes and Font scaling:
```kotlin
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun YumaButtonPreview() {
    YumaTheme {
        YumaButton(
            text = "Play Song",
            onClick = {}
        )
    }
}
```

### Step 5: Create Interactive Component Sample
Place usage examples in component documentation or sample code blocks showing state binding.

---

## 4. Anti-Patterns Checklist

- ❌ Hardcoding `Color(0xFF121212)` or `dp` values outside `YumaSpacing`.
- ❌ Using raw `MaterialTheme` or `Button` directly inside feature screens instead of Yuma UI Kit wrappers.
- ❌ Putting ViewModel calls, Room DB operations, or Coroutine launches inside Composables.
- ❌ Missing `key` in `LazyColumn`/`LazyRow` items inside list components.
