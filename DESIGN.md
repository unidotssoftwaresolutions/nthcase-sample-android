---
name: Mossgrove
description: A parts cabinet of home and desk goods. Every product is a labelled drawer and its number is its mark.
colors:
  primary: "#2E5E55"
  on-primary: "#FFFFFF"
  primary-container: "#CBE7DF"
  on-primary-container: "#0B2B25"
  secondary: "#4F615C"
  on-secondary: "#FFFFFF"
  secondary-container: "#D2E5DF"
  on-secondary-container: "#0C1F1B"
  tertiary: "#7A5A1C"
  on-tertiary: "#FFFFFF"
  tertiary-container: "#FCDFA6"
  on-tertiary-container: "#271900"
  error: "#BA1A1A"
  on-error: "#FFFFFF"
  error-container: "#FFDAD6"
  on-error-container: "#410002"
  surface: "#F7F9F7"
  on-surface: "#181C1B"
  surface-variant: "#DBE5E1"
  on-surface-variant: "#3F4946"
  outline: "#6F7976"
  outline-variant: "#BFC9C5"
  surface-container-lowest: "#FFFFFF"
  surface-container-low: "#F1F4F2"
  surface-container: "#EBEFEC"
  surface-container-high: "#E5E9E6"
  surface-container-highest: "#E0E3E1"
  inverse-surface: "#2D3130"
  inverse-on-surface: "#EFF1EF"
  inverse-primary: "#95D1C2"
  primary-dark: "#95D1C2"
  on-primary-dark: "#003730"
  primary-container-dark: "#15493F"
  on-primary-container-dark: "#B0EEDD"
  secondary-dark: "#B6CBC4"
  on-secondary-dark: "#21352F"
  secondary-container-dark: "#374B45"
  on-secondary-container-dark: "#D2E7E0"
  tertiary-dark: "#EEC06E"
  on-tertiary-dark: "#412D00"
  tertiary-container-dark: "#5D4200"
  on-tertiary-container-dark: "#FFDEA5"
  error-dark: "#FFB4AB"
  on-error-dark: "#690005"
  error-container-dark: "#93000A"
  on-error-container-dark: "#FFDAD6"
  surface-dark: "#101413"
  on-surface-dark: "#E0E3E1"
  surface-variant-dark: "#3F4946"
  on-surface-variant-dark: "#BFC9C5"
  outline-dark: "#899390"
  outline-variant-dark: "#3F4946"
  surface-container-lowest-dark: "#0B0F0E"
  surface-container-low-dark: "#181C1B"
  surface-container-dark: "#1C2120"
  surface-container-high-dark: "#272B2A"
  surface-container-highest-dark: "#313635"
  inverse-surface-dark: "#E0E3E1"
  inverse-on-surface-dark: "#2D3130"
  inverse-primary-dark: "#2E5E55"
  aisle-kitchen-tile: "#DDEBE5"
  aisle-kitchen-ink: "#2E5E55"
  aisle-desk-tile: "#E2E7EE"
  aisle-desk-ink: "#3F5466"
  aisle-garden-tile: "#DCEBD3"
  aisle-garden-ink: "#3E6A2E"
  aisle-home-tile: "#E8E4DE"
  aisle-home-ink: "#5B5148"
  aisle-kitchen-tile-dark: "#23332E"
  aisle-kitchen-ink-dark: "#95D1C2"
  aisle-desk-tile-dark: "#222A33"
  aisle-desk-ink-dark: "#B4C5D8"
  aisle-garden-tile-dark: "#263322"
  aisle-garden-ink-dark: "#A8D493"
  aisle-home-tile-dark: "#302D29"
  aisle-home-ink-dark: "#D3C7BA"
typography:
  display:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "36sp"
    fontWeight: 400
    lineHeight: "44sp"
    letterSpacing: "0"
  headline:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "28sp"
    fontWeight: 400
    lineHeight: "36sp"
    letterSpacing: "0"
  headline-small:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "24sp"
    fontWeight: 400
    lineHeight: "32sp"
    letterSpacing: "0"
  title-large:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "22sp"
    fontWeight: 400
    lineHeight: "28sp"
    letterSpacing: "0"
  title:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "16sp"
    fontWeight: 500
    lineHeight: "24sp"
    letterSpacing: "0.15sp"
  title-small:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  body-large:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    letterSpacing: "0.5sp"
  body:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    letterSpacing: "0.25sp"
  body-small:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "16sp"
    letterSpacing: "0.4sp"
  label:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  item-number:
    fontFamily: "monospace"
    fontSize: "13sp"
    fontWeight: 500
    letterSpacing: "0.5sp"
rounded:
  extra-small: "4dp"
  small: "8dp"
  medium: "12dp"
  large: "16dp"
  extra-large: "28dp"
  full: "9999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "24dp"
  xxl: "32dp"
  touch: "48dp"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label}"
    rounded: "{rounded.full}"
    height: "48dp"
  button-add-to-cart:
    backgroundColor: "{colors.tertiary}"
    textColor: "{colors.on-tertiary}"
    typography: "{typography.label}"
    rounded: "{rounded.full}"
    height: "48dp"
  button-outlined:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    typography: "{typography.label}"
    rounded: "{rounded.full}"
    height: "48dp"
  button-tonal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label}"
    rounded: "{rounded.full}"
    height: "48dp"
  text-field:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    rounded: "{rounded.extra-small}"
    height: "56dp"
  drawer-row:
    backgroundColor: "{colors.surface-container-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.medium}"
    padding: "12dp"
  label-holder:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.title}"
    rounded: "{rounded.extra-small}"
    padding: "6dp 10dp"
  item-number:
    textColor: "{colors.tertiary}"
    typography: "{typography.item-number}"
  glyph-tile:
    backgroundColor: "{colors.aisle-kitchen-tile}"
    textColor: "{colors.aisle-kitchen-ink}"
    rounded: "{rounded.small}"
    size: "56dp"
  stepper-button:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.full}"
    size: "48dp"
  cart-badge:
    backgroundColor: "{colors.tertiary}"
    textColor: "{colors.on-tertiary}"
    rounded: "{rounded.full}"
  nav-bar:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface-variant}"
    height: "80dp"
  nav-item-active:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.full}"
  chip-filter:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.small}"
    height: "32dp"
  chip-filter-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.small}"
    height: "32dp"
  dialog:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.extra-large}"
---

# Design System: Mossgrove

## Overview

**Creative North Star: "The Parts Cabinet"**

Mossgrove is a cabinet of labelled drawers, not a storefront. Each product is a drawer front: a quiet card-stock panel carrying a drawn glyph on its aisle's tint, a thin outlined label holder with the name and a monospaced number, and a price. The number ("No. 007") is the product's identity mark and appears wherever the product does: drawer row, detail page, cart line. The Lab is a second cabinet of labelled test benches, and it says plainly what it is for. The cabinet makes labels visible as a quality, which is also why every control carries a stable test id on its own accessibility node.

The palette is restrained: one cabinet-enamel green, cool card-stock neutrals, and one brass accent that marks only what is identity or live. Type is the platform's own face in its own scale; the only voice change is the system monospace for item numbers and the printed test account. Components are stock Material 3 (on Android) or the native equivalents (on iOS), shaped by one corner scale and a 4 dp grid. Depth is tonal, not shadowed. Dynamic colour is off: all five sibling apps (Android, iOS, Flutter, React Native, Ionic) and every screenshot render the same palette in light and dark.

The build refuses the default storefront: no hero banners, no photo cards, no sale badges. Products are drawings, never photographs.

**Key Characteristics:**
- Drawer fronts on `surface-container-low`, 12 dp corners, no shadow.
- The label holder: a 1 dp `outline-variant` frame, 4 dp corners, name over a brass monospace "No. 007".
- Brass (tertiary) on exactly three things: the item number, the Add to cart button, the cart badge.
- Original single-stroke art: 24-unit icons at 2-unit stroke, 48-unit product glyphs at 2.5-unit stroke, on aisle-tinted tiles.
- State is never colour alone: errors carry a warning icon, selections carry a check, a disabled stepper button loses its ring.
- System font, system monospace, system font scale; 48 dp touch targets; light and dark.

## Colors

One enamel green carries the system, cool grey-green neutrals hold the page, and a single brass is spent only on identity and the one live action.

### Primary
- **Cabinet Enamel Green** (`primary`; dark `primary-dark`): filled buttons (Sign in, Check out, Join, Open the bottom drawer), the sign-in mark, focused field outlines, the chosen gift-card border and check, settings section headers, the pinch and rotate swatches. The app's structural voice.
- **Enamel Wash** (`primary-container`): positive confirmation (the form's joined message, a labelled drop target). Always paired with a check icon or a changed word.

### Secondary
- **Lichen Grey-Green** (`secondary`, `secondary-container`): Material's tonal roles: the active navigation pill, selected filter chips, the Sign out tonal button, the gesture status strip. Never an accent.

### Tertiary
- **Cabinet Brass** (`tertiary`; dark `tertiary-dark`): the item number text, the Add to cart button fill, the cart badge. In dark mode it lifts to a pale brass so the number stays legible on near-black.
- **Label-Card Brass** (`tertiary-container`): the draggable "No. 007" label on the Gestures bench, which is an item number in physical form, framed by a 1 dp `tertiary` stroke.

### Neutral
- **Card Stock** (`surface`; dark `surface-dark`): page background and the XML window background. Slightly green-cooled white, never pure white.
- **Drawer Front** (`surface-container-low`): drawer rows, gesture pads.
- **Shelf** (`surface-container`): the navigation bar, the scroll bench's shelves.
- **Ink** (`on-surface`): names, prices, body text.
- **Pencil** (`on-surface-variant`): count lines, descriptions, help text, inactive icons, the "each" price.
- **Label Rule** (`outline-variant`): the label holder frame, list and cart-line dividers, pad borders.
- **Field Rule** (`outline`): text field and stepper outlines, the drop-target frame (2 dp).
- **Error Red** (`error`): error lines, error icons, the Reset outlined button's stroke and text. Always with the warning icon or a destructive verb.

### Aisle Tints
Each aisle owns one tile and one ink, used only together behind and on a product glyph: Kitchen (enamel green on pale green), Desk (slate blue on pale slate; Desk is slate, not a second green), Garden (leaf green on pale leaf), Home (warm umber on pale stone). Dark mode uses the `-dark` pairs. Gift-card designs borrow the Garden and Home pairs.

### Named Rules
**The Three Brass Things Rule.** Brass (`tertiary`, `tertiary-container`) is the item number, the Add to cart button and the cart badge, and nothing else. A brass element that is not an item number, the primary purchase action, or the cart count is a defect.

**The Fixed Palette Rule.** Dynamic colour and wallpaper theming are off on every platform. The hex values above are the palette in all five apps; the Compose scheme and `res/values*/colors.xml` carry the same values.

**The Never Colour Alone Rule.** Every state that is shown by colour is also shown by shape, icon or text: a warning icon on errors, a check on selected chips and chosen cards, a missing ring on a disabled stepper button, a changed word on a dropped label.

## Typography

**Display Font:** the platform system face (Roboto on Android, SF Pro on iOS)
**Body Font:** the same system face
**Label/Mono Font:** the system monospace (Droid Sans Mono / SF Mono)

**Character:** Plain and native. The only type event is the monospace item number, which reads as a stamped part number against humane sans names. No bundled fonts.

### Hierarchy
- **Display** (400, 36/44 sp): the "Mossgrove" wordmark on sign-in only.
- **Headline** (400, 28/36 sp): the "Mossgrove" title over the Shop search.
- **Headline Small** (400, 24/32 sp): the item name inside the detail page's label holder.
- **Title Large** (400, 22/28 sp): the detail price, the cart total, state-message titles. Top app bar titles use Material's own top bar style.
- **Title** (500, 16/24 sp, 0.15 sp): product names in drawer rows and cart lines, prices in rows, stepper value.
- **Title Small** (500, 14/20 sp): bench and card titles, "Quantity", XML section headers (in `primary`).
- **Body Large / Body / Body Small** (400; 16/24, 14/20, 12/16 sp): descriptions, help lines, error lines (Body Small under fields, Body on sign-in).
- **Label** (500, 14/20 sp, 0.1 sp): buttons, the count line ("40 drawers · No. 001–040"), the aisle line.
- **Item Number** (monospace 500, 13 sp, 0.5 sp): "No. 007" in brass; the test account email and password in ink.

### Named Rules
**The Stamped Number Rule.** Monospace is for identifiers only: item numbers and the printed test account credentials. Names, prices and prose stay in the system sans.

**The System Scale Rule.** Every size follows the system font scale (sp on Android, Dynamic Type on iOS). Nothing is set in fixed px/dp.

## Layout

Single column, phone-first. Screen margins are 16 dp; content on the detail page caps at 560 dp wide and the sign-in column at 480 dp, centred. Spacing sits on a 4 dp grid: 4 inside a label stack, 8 between related items and between drawer rows, 12 inside rows and between detail-page blocks, 16 for screen margins and card padding, 24 between sections (settings headers have 24 dp above), 32 around state messages and the sign-in column's top. Every interactive target is at least 48 dp (buttons are 48 dp tall; stepper buttons are 48 dp circles); XML switches rows are 56 dp, radio rows 48 dp.

The Shop's first viewport is a large title, a full-width search field, a count line, then the drawer list. Navigation is a four-tab bottom bar (Shop, Cart with badge, Lab, Settings); screens below a tab get a top app bar with Back. Cart pins its total and Check out to a tonal bottom panel. Empty and error states share one centred shape: icon (40 dp), title, one line of help, one action.

## Elevation & Depth

Flat, tonal layering. Drawer fronts and pads separate from the card-stock page by a step in the surface-container ladder (`surface` to `surface-container-low` to `surface-container`), not by shadows. The only lifted surfaces are Material's own: the cart's checkout panel (tonal elevation 2 dp), the navigation bar, dialogs, the snackbar and the date picker, all at their component defaults.

### Named Rules
**The Tonal Ladder Rule.** Separate a surface by moving one step along the surface-container ladder or by a 1 dp `outline-variant` rule. No custom shadows.

## Shapes

One corner scale, each step with a job: 4 dp for label holders, the drag label and outlined text fields (extra-small, Material's field shape); 8 dp for glyph tiles, gift-card design tiles and filter chips (small); 12 dp for drawer fronts, gesture pads and cards (medium); 16 dp reserved (large); 28 dp for dialogs and sheets (extra-large). Buttons, the stepper's circular buttons, the navigation pill and the badge are fully round, as Material ships them. Borders are thin and functional: 1 dp `outline-variant` for frames, 2 dp for the selected gift card (`primary`) and the drop target (`outline`). Glyph tiles are square; the glyph fills 62% of the tile.

The launcher mark is the system in miniature: a card-stock drawer front with 4-unit corners on enamel green, a brass label-holder outline and a rounded pull (`art/launcher.svg`).

## Components

### Buttons
Stock Material 3, full-width at 48 dp where they end a task.
- **Shape:** fully round (Material default pill).
- **Primary:** `primary` fill, `on-primary` label: Sign in, Check out, Join, error-state Try again.
- **Add to cart:** the one brass button. `tertiary` fill, `on-tertiary` label, full width, 48 dp, label carries the running total ("Add to cart · $84.00").
- **Outlined:** empty-state actions, gift-card Choose, Settings > Orders. The destructive Reset is outlined in `error` with `error` text.
- **Tonal / Text:** Sign out is tonal; dialog actions, "Use test account" and the snackbar action are text buttons.

### Chips
- **Style:** Material filter chips, 8 dp corners, outlined at rest.
- **State:** selected fills with `secondary-container` and gains a leading check icon (18 dp). The check is mandatory.

### Cards / Containers
- **Corner Style:** 12 dp.
- **Background:** `surface-container-low` for drawer fronts; Material card defaults for gift-card designs and the sign-in test-account slip (outlined).
- **Shadow Strategy:** none of its own (see Elevation & Depth).
- **Border:** none on drawer fronts; 1 dp `outline-variant` on pads; 2 dp `primary` on a chosen gift card, which also shows a check.
- **Internal Padding:** 12 dp in rows and pads, 16 dp in cards with prose.

### Inputs / Fields
- **Style:** Material outlined text fields, full width, single line, leading search icon on Shop, trailing clear or show/hide icon buttons.
- **Focus:** Material's thickened `primary` outline.
- **Error:** the field turns `error` and a separate line appears under it: a 16 dp warning icon and Body Small `error` text, on its own accessibility node so its id is reachable. Never a red outline alone.

### Navigation
- **Bottom bar:** Material navigation bar, four tabs with the Mossgrove stroked icons and labels. The active tab sits in a `secondary-container` pill. The Cart badge is brass and the tab's own accessibility label states the count.
- **Top app bar:** Material top app bar, title as a heading, stroked Back arrow.

### Quantity Stepper
Two 48 dp outlined circular icon buttons around a 40 dp-wide Title value. At the limit (1 or 9) the button disables and loses its outline ring, so the state is a shape change, not just a dimmer colour. Each button and the value carry their own ids and labels ("Decrease Stoneware Mug").

### Glyph Tile
A square on the item's aisle tile colour with 8 dp corners; the stroked glyph in the aisle ink at 62% of the tile. 56 dp in drawer rows, 48 dp in cart lines, 220 dp on the detail page.

### Label Holder (signature)
The drawer's label: a 1 dp `outline-variant` frame with 4 dp corners and 10 x 6 dp padding, holding the name (Title in rows, Headline Small on the detail page) over the brass monospace item number. It hugs its content; it never stretches to the row width. Cart lines show the name and number without the frame, because a cart line is not a drawer front.

### Drawer Row
A `surface-container-low` panel, 12 dp corners, 12 dp padding: glyph tile left, label holder in the middle, price right-aligned. The whole row is one tappable, merged accessibility node with the item's id. Rows stack with 8 dp gaps.

### State Message
Centred icon (40 dp; `error` with the warning icon for failures, `on-surface-variant` otherwise), Title Large title, Body help in `on-surface-variant`, one action: filled for errors, outlined for empty states. Merged into one node with its id; the action keeps its own.

## Do's and Don'ts

### Do:
- **Do** show the item number ("No. 007", brass monospace) on every drawer row, detail page and cart line.
- **Do** keep brass to the item number, the Add to cart button and the cart badge.
- **Do** put product art on its aisle tint as an original single-stroke drawing (icons 24 units at 2 stroke, glyphs 48 units at 2.5 stroke), tinted from the theme.
- **Do** give every state a second cue: warning icon on errors, check on selections, ring removed on a disabled stepper.
- **Do** put each test id on its own accessibility node: error lines outside the field's supporting text, the cart count spoken by the tab, merged rows with the id on the row.
- **Do** use each platform's own components (Material 3 on Android, iOS conventions on iOS) and the system face at the system font scale.
- **Do** keep 48 dp minimum targets and 16 dp screen margins.

### Don't:
- **Don't** turn on dynamic colour or wallpaper theming.
- **Don't** use brass for decoration, headers, links or secondary buttons.
- **Don't** build a storefront: no hero banners, no photo cards, no sale badges, no product photography.
- **Don't** mark state by colour alone (a red outline with no icon, a selected chip with no check).
- **Don't** add custom shadows; separate surfaces tonally.
- **Don't** set names, prices or prose in monospace.
- **Don't** wrap content in a card unless it is a real object (a drawer front, a choosable gift card, the printed test-account slip).
