# Mossgrove for Android: an NthCase sample app

Mossgrove is a small, fictional shop for home and desk goods. It is built to be tested with
[NthCase](https://nthcase.com), the AI testing platform that records and replays tests on Android, iOS and the web.
Clone it, build it, and run your first NthCase test on it in minutes.

It is one of five identical Mossgrove apps. The same screens, test ids, texts and scenarios are built in Kotlin +
Jetpack Compose (this one), SwiftUI, Flutter, React Native and Ionic, all from one spec: [SPEC.md](SPEC.md),
**spec 1.2**. Every verb NthCase supports has a place to land, and so do the hard cases real apps have: a slow screen,
a failed load with retry, a web view, a system permission prompt, a modal, a screen with no test ids at all, (new
in 1.1) the **Lobby**: a screen of many scrollers with a touch log that says if anything was touched by accident, and
(new in 1.2) a quick sheet whose lines scroll inside it and the **Feed**, a list that never ends.

- No network, no analytics, no ads, no tracking, no third-party SDKs. Everything is seeded inside the app, so every run
  is the same.
- One screen, Settings, is built with classic **XML Views and a Fragment**, so both Android UI toolkits are covered.
- The Lobby is built twice: **Lobby** in Compose, and **Lobby (Views)** in Android Views (RecyclerView, ViewPager2, a
  nested RecyclerView and a view that scrolls both ways), because the two toolkits decide differently which parent
  takes a drag. Its first scrollable is the promo pager; with **Show the pager** off it is the **Top 10** row, a row of
  tiles that nothing vertical encloses.

| Sign in | Shop | Item | Cart |
| --- | --- | --- | --- |
| <img src="docs/screenshots/light/01-sign-in.png" width="180" alt="Sign in"> | <img src="docs/screenshots/light/03-shop.png" width="180" alt="Shop"> | <img src="docs/screenshots/light/06-item.png" width="180" alt="Item"> | <img src="docs/screenshots/light/08-cart.png" width="180" alt="Cart"> |
| Modal | Lab | Form errors | Form done |
| <img src="docs/screenshots/light/09-cart-dialog.png" width="180" alt="Modal"> | <img src="docs/screenshots/light/11-lab.png" width="180" alt="Lab"> | <img src="docs/screenshots/light/12-form-errors.png" width="180" alt="Form errors"> | <img src="docs/screenshots/light/13-form-done.png" width="180" alt="Form done"> |
| Gestures | Delivery slots | Error + retry | No test ids |
| <img src="docs/screenshots/light/14-gestures.png" width="180" alt="Gestures"> | <img src="docs/screenshots/light/17-slow-done.png" width="180" alt="Delivery slots"> | <img src="docs/screenshots/light/20-orders-error.png" width="180" alt="Error + retry"> | <img src="docs/screenshots/light/21-gift-cards.png" width="180" alt="No test ids"> |
| Settings (XML) | XML dialog | Empty search | Deep link, unknown id |
| <img src="docs/screenshots/light/22-settings.png" width="180" alt="Settings (XML)"> | <img src="docs/screenshots/light/23-settings-dialog.png" width="180" alt="XML dialog"> | <img src="docs/screenshots/light/05-shop-no-match.png" width="180" alt="Empty search"> | <img src="docs/screenshots/light/24-deep-link-unknown.png" width="180" alt="Deep link, unknown id"> |
| Lobby | Lobby, the board | Lobby, quick sheet | Lobby (Views) |
| <img src="docs/screenshots/light/25-lobby.png" width="180" alt="Lobby"> | <img src="docs/screenshots/light/26-lobby-board.png" width="180" alt="Lobby, the board"> | <img src="docs/screenshots/light/27-lobby-sheet.png" width="180" alt="Lobby, quick sheet"> | <img src="docs/screenshots/light/28-lobby-views.png" width="180" alt="Lobby (Views)"> |
| Lobby, pager off | Lobby (Views), pager off | Lobby, pager off (dark) | Lobby (Views), pager off (dark) |
| <img src="docs/screenshots/light/30-lobby-no-pager.png" width="180" alt="Lobby, pager off"> | <img src="docs/screenshots/light/31-lobby-views-no-pager.png" width="180" alt="Lobby (Views), pager off"> | <img src="docs/screenshots/dark/30-lobby-no-pager.png" width="180" alt="Lobby, pager off, dark"> | <img src="docs/screenshots/dark/31-lobby-views-no-pager.png" width="180" alt="Lobby (Views), pager off, dark"> |
| Feed | Feed (dark) | Lobby, quick sheet (dark) | Lab (dark) |
| <img src="docs/screenshots/light/32-feed.png" width="180" alt="Feed"> | <img src="docs/screenshots/dark/32-feed.png" width="180" alt="Feed, dark"> | <img src="docs/screenshots/dark/27-lobby-sheet.png" width="180" alt="Lobby, quick sheet, dark"> | <img src="docs/screenshots/dark/11-lab.png" width="180" alt="Lab, dark"> |

All 32 screens, light and dark, are in [docs/screenshots](docs/screenshots). They are drawn on the JVM by the tests
(Robolectric), not on a device.

## Test account

| | |
| --- | --- |
| Email | `tester@mossgrove.test` |
| Password | `Mossgrove123` |
| Gift-card code | `MOSS-0025` |
| Deep link | `nthcase-sample://item/{1–40}` |
| Package | `com.nthcase.sample` |

The account is printed on the sign-in screen too, with a **Use test account** button.

## Build

You need JDK 17 or newer (21 is what CI uses) and the Android SDK (`ANDROID_HOME`, or `sdk.dir` in
`local.properties`). The Gradle wrapper fetches everything else.

```bash
./gradlew assembleDebug
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`. It is debug-signed, about 17 MB (an unminified debug
build). Every [release](../../releases) also has a ready-built one.

```bash
./gradlew testDebugUnitTest
```

This runs the unit tests and the Compose UI tests on the JVM with Robolectric: no emulator. They check every spec id
on its screen, in the accessibility tree Appium reads. They play the scenarios' flows, and draw the screenshots.
`./gradlew updateScreenshots` copies the pictures into `docs/screenshots`. The instrumented tests in `app/src/androidTest`
are for an emulator or device of your own: a smoke test, and spec 1.2's sheet and Feed on a real window manager and a
real input pipeline (`SheetAndFeedTest`). `./gradlew connectedDebugAndroidTest` runs them on **every** device adb lists,
so with more than one attached, build the two APKs (`./gradlew assembleDebug assembleDebugAndroidTest`), install them on
the one you mean with `adb -s <serial> install -r`, and run
`adb -s <serial> shell am instrument -w -r com.nthcase.sample.test/androidx.test.runner.AndroidJUnitRunner`.

| | |
| --- | --- |
| Language and UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose; Settings in XML Views + Fragment (Material Components); Lobby (Views) in Android Views (RecyclerView, ViewPager2, Material Components) |
| SDK | minSdk 26 (Android 8.0), targetSdk and compileSdk 37 (Android 17) |
| Build | Gradle 9.8.2 (wrapper), Android Gradle Plugin 9.5, Kotlin 2.4, version catalog in `gradle/libs.versions.toml` |
| Tests | JUnit 4, Robolectric 4.17, Compose UI test |
| Permissions | `POST_NOTIFICATIONS` only. No `INTERNET`: the app cannot reach the network. |

## Test it with NthCase

1. Install the APK on an emulator or a test phone: `adb install app/build/outputs/apk/debug/app-debug.apk`.
2. Connect the device to NthCase with the agent: `npx nthcase-agent@latest`. The NthCase docs, under *Your
   devices*, walk through it. Or use a cloud device.
3. In NthCase, add an Android app with the package `com.nthcase.sample`, and upload the APK as its build.
4. Create a scenario from one of the files in [nthcase/scenarios](nthcase/scenarios): its numbered lines are the
   steps, and each `Expect:` line is that step's check. Or record one: **Scenarios → New scenario → Record**.
5. Compile it, and run it.

With NthCase's MCP server, ask your assistant to "create a scenario from nthcase/scenarios/03-cart-and-order.md for
Mossgrove and run it on my Android phone".

## What it covers

| NthCase verb | Where | Scenario |
| --- | --- | --- |
| launch, tap, type | sign in, search, every button | 01, 02 |
| scroll to an element, swipe | the 40-item list; the form; the gestures screen | 02, 04, 05 |
| doubletap, longpress, pinch, dragdrop | Lab › Gestures | 05 |
| slide, select | Lab › Join the club (slider, dropdown, date picker) | 04 |
| rotate, lock, unlock, home, back | any screen | 06 |
| a web view | Lab › Care guide (a bundled page) | 07 |
| waitFor, an error with retry, empty states | Lab › Delivery slots, Lab › Orders, Settings › Testing | 08 |
| a system permission prompt, a modal, XML Views | Settings | 09 |
| a screen with no test ids | Lab › Gift cards | 10 |
| extract, startif / endif | the cart total; sign in only if asked | 03, 06 |
| deep link | `nthcase-sample://item/7` | 11 |
| scroll to an element, inside a screen of many scrollers | Lab › Lobby: the page, a sideways tile row, a board that scrolls both ways | 12, 13, 14 |
| swipe a pager; longpress, doubletap on tiles and cells | Lab › Lobby | 13, 14, 15 |
| a control that appears late (waitFor), and a touch log that catches a scroll acting as a touch | Lab › Lobby, Lab › Lobby (Views) | 16, 17, 18 |
| the same, with the pager off, so the first scrollable is the Top 10 row of tiles | Lab › Lobby, Lab › Lobby (Views) | 19, 20 |
| scroll inside a sheet, its lines past the sheet's foot (1.2) | Lab › Lobby: a tile's long press, then More like this | — |
| scroll on a list that never ends, its spinner row 1 s between loads (1.2) | Lab › Feed | — |

The full table, with every check type and the known gaps, is in [SPEC.md §8](SPEC.md#8-verb-coverage).

## Dependencies

Every runtime dependency is published by Google, as part of Android Jetpack (AndroidX) or Material Components:
Core KTX, Activity Compose, Navigation Compose, Lifecycle, Fragment (KTX and Compose), AppCompat, RecyclerView and
ViewPager2 (named for Lobby (Views); Material already brought the same versions), Material Components, and the
Compose BOM (UI, graphics, Material 3, tooling preview). Test and build tools that never ship in the app: JUnit 4,
Robolectric, AndroidX Test, Compose UI test, Gradle and the Android Gradle Plugin.

## Test ids

Every control has a stable test id from one list shared by all five apps: [nthcase/test-ids.json](nthcase/test-ids.json).
In Compose they are `Modifier.testTag` with `testTagsAsResourceId`, so Appium sees `resource-id="shop_search"`. On
the XML Settings screen they are `android:id`, so Appium sees `resource-id="com.nthcase.sample:id/settings_reset"`.
The care-guide page's elements have the same HTML `id` and `data-testid`. The Gift cards screen has none, on purpose.
Lobby (Views) uses the Lobby's ids as view ids, declared in `res/values/lobby_ids.xml` (made from the JSON; a test
checks it), so Appium sees `resource-id="com.nthcase.sample:id/lobby_pager"` there and `resource-id="lobby_pager"` in
the Compose Lobby.

## More like this and the Feed (spec 1.2)

The Lobby's quick sheet (a long press on a tile or a banner) has **More like this** under its four buttons: 20 lines,
"Like Bedside Lamp · 1 of 20" and on, in the sheet's own scroller. The sheet's content is half the window high and the
sheet opens whole at that height and never grows, so the lines run past its foot and come only by scrolling inside it;
the Lobby under it does not move. A tap on a line is logged as a tap on a sheet button is, and closes the sheet.
Lab › **Feed** is a list of rows, "Feed row 1" and on: 20 at first, and each time the third row from the end of what
has come shows, a spinner row after the last row for 1 s, then 20 more. It never ends.

| Part | Lobby (Compose) and Feed | Lobby (Views) |
| --- | --- | --- |
| More like this (`lobby_sheet_more`) | a `LazyColumn` under the buttons in the `ModalBottomSheet` (`skipPartiallyExpanded`, content half the window high) | a `RecyclerView` under the buttons in the `BottomSheetDialog` (content half the window high, expanded) |
| Feed (`feed_list`) | a `LazyColumn`; a load starts from the rows its layout shows | — (the Feed is built once, in Compose) |

## Layout

```
app/src/main/java/com/nthcase/sample/   MainActivity, TestIds, data/ (Feed.kt, 1.2), ui/ (Compose screens, FeedScreen 1.2),
                                        settings/ (the Fragment),
                                        lobby/ (Lobby (Views): the Fragment, the two-way BoardView, GlyphDrawable)
app/src/main/res/layout/                fragment_settings.xml, dialog_reset.xml: the XML screen; fragment_lobby.xml
app/src/main/assets/web/                care-guide.html, the web view's page
app/src/test/                           JVM tests: data, spec ids, screens, flows, deep link, screenshots, the Lobby,
                                        LobbySwipeTest (UiScrollable's swipe injected through both Lobbies), FeedTest (1.2)
app/src/androidTest/                    instrumented tests: a smoke test, SheetAndFeedTest (1.2)
art/                                    the original icons, product drawings and launcher mark (SVG)
nthcase/                                test-ids.json and the scenarios
docs/screenshots/                       every screen, light and dark
```

## CI

Every run is one job on `ubuntu-latest`: the unit tests and the Robolectric UI tests on the JVM, and the APKs, in 1 to
5 minutes, then "Every check", which lists each check with its outcome. Android has no device suite in CI: the
instrumented tests in `app/src/androidTest` run on an emulator or a phone of your own (see Build). It runs on a push to `main`, on a pull
request, on "Run workflow", at 03:17 UTC on the 1st of each month, and before every release: a `v*` tag publishes only when it is
green. A push that changes only `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `DESIGN.md`, `LICENSE`, `NOTICE` or
`docs/screenshots/` starts nothing. To run it by hand, open Actions → build → Run workflow on `main`, or run
`gh workflow run build.yml --ref main`. The job is in [.github/workflows/ci.yml](.github/workflows/ci.yml).

## Licence

Apache-2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE). Mossgrove, its items and its drawings are fictional and were
made for these apps. "NthCase" is a trademark of Unidots Software Solutions LLC.
