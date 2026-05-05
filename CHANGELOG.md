# Changelog

## [3.0.1](https://github.com/vitoksmile/ComposeHints/releases/tag/3.0.1)

*   Fixed mask misalignment and "stuck" overlay state when scrolling or right after initial layout ([#6](https://github.com/vitoksmile/ComposeHints/issues/6)).
*   Ensured mask holes persist during the overlay exit animation for a smoother visual transition.

## [3.0.0](https://github.com/vitoksmile/ComposeHints/releases/tag/3.0.0)

*   **Advanced Positioning**: Added `HintAlignment` (Top, Bottom, Start, End, Center, Overlap) and `contentOffset` for precise control.
*   **Smart Positioning**: Hints now automatically adjust to stay within screen bounds, with built-in RTL awareness.
*   **Multiple Anchors**: Support for linking a single hint to multiple anchors.
*   **Enhanced Sequence Control**: Added `next()` and `previous()` methods to `HintController`, along with `currentStepIndex`, `totalStepsCount`, `hasNext`, and `hasPrevious` properties.
*   **Auto-Advance**: Support for automatic progression to the next hint via configurable timers.
*   **Hole Padding**: Added support for padding around the highlighted anchor "hole".

## [2.2.1](https://github.com/vitoksmile/ComposeHints/releases/tag/2.2.1)

*  Fixed missing anchor highlight (regression in 2.2.0)
*  Added support for Compose screenshot tests

## [2.2.0](https://github.com/vitoksmile/ComposeHints/releases/tag/2.2.0)

*  Kotlin 2.3.20 & Compose Multiplatform 1.10.3
*  AGP 9.1.1 & Gradle 9.4.1
*  Fixed `IndexOutOfBoundsException` ([#4](https://github.com/vitoksmile/ComposeHints/issues/4))

## [2.1.0](https://github.com/vitoksmile/ComposeHints/releases/tag/2.1.0)

*  Added `Properties` to customize the behavior of a Hint: `dismissOnBackPress`, `dismissOnClickOutside`

## [2.0.1](https://github.com/vitoksmile/ComposeHints/releases/tag/2.0.1)

*  Fixed BackHandler

## [2.0.0](https://github.com/vitoksmile/ComposeHints/releases/tag/2.0.0)

*  HintHost as root Composable to define a place where hints are shown

## [1.1.1](https://github.com/vitoksmile/ComposeHints/releases/tag/1.1.1)

* Animations: anchor size

## [1.1.0](https://github.com/vitoksmile/ComposeHints/releases/tag/1.1.0)

* Animations: overlay, hint

## [1.0.2](https://github.com/vitoksmile/ComposeHints/releases/tag/1.0.2)

* Fix: Use positionInRoot() for accurate hint positioning
* Compose Multiplatform 1.8.0

## [1.0.1](https://github.com/vitoksmile/ComposeHints/releases/tag/1.0.1)

* Android SDK 36

## [1.0.0](https://github.com/vitoksmile/ComposeHints/releases/tag/1.0.0)

* Initial release