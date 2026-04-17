# ComposeHints Project Rules

You are an expert developer working on **ComposeHints**, a Compose Multiplatform library for displaying hints and tooltips.

## Tech Stack
- **Language:** Kotlin 2.3.20+
- **Framework:** Compose Multiplatform 1.10.3+
- **Platforms:** Android, iOS, Desktop (JVM), Web (Wasm)
- **Build System:** Gradle 9.4.1 (Kotlin DSL) with `libs.versions.toml`
- **AGP:** 9.1.1+ (Note: Currently using `android.builtInKotlin=false` bypass for single-module compatibility)

## Architecture & Design Patterns

### 1. Host/Overlay Pattern
- `HintHost` **MUST** be the root wrapper for any UI that uses hints. It defines the coordinate space for overlays.
- `HintController` manages the lifecycle and queue of hints. It uses `CompositionLocal` to communicate with `HintHost`.

### 2. State Management
- Use `rememberHintController()` to create a controller.
- `HintAnchorState` is used to link a specific Composable (via `Modifier.hintAnchor`) to a `Hint`.
- All UI-related state should be wrapped in `remember` or `mutableStateOf`.

### 3. Concurrency
- `HintController.show(...)` is a **suspend** function. It resumes only after the hint (or sequence of hints) is dismissed.
- Always use a `CoroutineScope` (e.g., `rememberCoroutineScope`) to trigger hint displays from UI events.

### 4. Component Definitions
- **Hints**: Created using `rememberHint { ... }`.
- **Anchors**: Defined using `rememberHintAnchorState(hint)`.
- **Modifiers**: Use `Modifier.hintAnchor(state, shape)` to mark the target element.

## Coding Standards

### 1. Kotlin & Compose Idioms
- Favor `Stable` and `Immutable` annotations for state classes to optimize Compose recomposition.
- Use `internal` visibility for implementation details that shouldn't be part of the public API.
- Adhere to Compose `Modifier` extension patterns.

### 2. Multiplatform
- Keep core logic in `commonMain`.
- Use `expect`/`actual` only when platform-specific APIs are strictly necessary (e.g., specific input handling).
- Ensure all resources and configurations are compatible with Android, iOS, Desktop, and Wasm.

### 3. API Consistency
- Maintain binary compatibility. Use the `binary-compatibility-validator` plugin.
- Public APIs should be documented with KDoc.

## Development Workflow

### 1. Testing
- Verify changes on at least two platforms (e.g., Android and Desktop) using the `sample` app.
- Ensure `HintHost` is correctly placed in the sample app's root.

### 2. Dependencies
- Always update dependencies in `gradle/libs.versions.toml`.
- Do not add new dependencies without justifying their multiplatform support.

### 3. Documentation
- Update `README.md` and `CHANGELOG.md` for any public API changes or new features.
- Ensure examples in `README.md` are up-to-date with the current implementation.
