---
name: Campus Thrift Exchange
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#3d4a42'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#6d7a72'
  outline-variant: '#bccac0'
  surface-tint: '#006c4a'
  primary: '#006948'
  on-primary: '#ffffff'
  primary-container: '#00855d'
  on-primary-container: '#f5fff7'
  inverse-primary: '#68dba9'
  secondary: '#9d4300'
  on-secondary: '#ffffff'
  secondary-container: '#fd761a'
  on-secondary-container: '#5c2400'
  tertiary: '#006194'
  on-tertiary: '#ffffff'
  tertiary-container: '#007bb9'
  on-tertiary-container: '#fdfcff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#85f8c4'
  primary-fixed-dim: '#68dba9'
  on-primary-fixed: '#002114'
  on-primary-fixed-variant: '#005137'
  secondary-fixed: '#ffdbca'
  secondary-fixed-dim: '#ffb690'
  on-secondary-fixed: '#341100'
  on-secondary-fixed-variant: '#783200'
  tertiary-fixed: '#cce5ff'
  tertiary-fixed-dim: '#93ccff'
  on-tertiary-fixed: '#001d31'
  on-tertiary-fixed-variant: '#004b73'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '800'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.02em
  price-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '800'
    lineHeight: 24px
    letterSpacing: -0.01em
  price-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '800'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  gutter-desktop: 1.25rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

The design system establishes a high-trust, vibrant, and resource-conscious marketplace tailored specifically for university campus communities. It merges modern digital product clarity with the energetic, community-centric atmosphere of peer-to-peer student exchanges.

### Aesthetic Direction: Modern Community Utility
The aesthetic balances two core visual vectors:
1. **Institutional Trust & Credibility:** High-legibility typography, structured information density, and unambiguous verification markers (student ID checks, campus badges).
2. **Youthful Thrift Vitality:** Inviting rounded forms, tactile touch targets, dynamic deal highlights ("Pass gấp"), and fresh sustainable tones.

### Core Tenets
- **Clarity Over Clutter:** Secondary item metadata (e.g., campus dorm zone, course codes, item condition) is prioritized over ornamental decorations.
- **Micro-Trust Anchors:** Verified university status badges, transaction history counts, and physical campus pickup points are visibly integrated into every listing surface.
- **Brisk, Actionable Micro-Moments:** Fast visual triage via purposeful color coding (urgent liquidation vs. standard thrift).

## Colors

The palette communicates vitality, affordability, and academic trust while keeping background interference near zero to highlight user-generated item imagery.

### Role Allocations
- **Primary (`#059669` / Deep Mint & Emerald):** Used for primary CTAs ("Mua ngay", "Nhắn tin"), verified seller badges, active navigation indicators, and standard listing prices. Conveys sustainable second-hand circulation and freshness.
- **Secondary (`#F97316` / Warm Signal Coral):** Reserved strictly for urgent liquidation chips ("Pass gấp"), countdowns, flash campus bundle tags, and price drops.
- **Tertiary (`#0284C7` / Campus Verification Blue):** Dedicated exclusively to student identity validation, official university dorm tags, and fraud prevention trust markers.
- **Neutral Surface Palette:**
  - `Canvas / Background`: `#F8FAFC` (slate-50) creates subtle separation against clean white cards.
  - `Card / Modal Surface`: `#FFFFFF` (pure white) with crisp neutral borders.
  - `Subtle Divider / Field Fill`: `#F1F5F9` (slate-100) provides low-contrast tactile framing.
- **Text & Contrast:**
  - `Primary Text`: `#0F172A` (deep slate navy) guarantees WCAG AAA legibility for price numbers and titles.
  - `Secondary Text`: `#64748B` (slate-500) for timestamps, dorm distances, and specs.

## Typography

The design system relies entirely on **Plus Jakarta Sans** across all hierarchies. Its open geometric counters convey modern youthfulness without compromising the rigorous legibility required for dense cataloging, numeric currency pricing (VND), and campus address specs.

### Hierarchy & Style Rules
- **Vietnamese Diacritics:** Line heights are calibrated with ample breathing room (`1.4`–`1.5` baseline) to prevent overlapping stacked diacritics in common university terms (e.g., *Đại học*, *Ký túc xá*, *Thanh lý*).
- **Price Numerals:** Price tokens (`price-lg`, `price-md`) leverage tabular numeric alignments to ensure clean comparative scanning across product grid columns.
- **Micro-Labels:** Badges and pill chips use `label-sm` in all-caps or capitalized title styling with slight letter spacing (`0.02em`) for immediate recognition at rapid scroll speeds.

## Layout & Spacing

A mobile-first fluid layout drives the experience, optimized for single-handed usage on mobile devices while extending gracefully into tablet split-views.

### Grid & Layout Principles
- **Mobile Feed (2-Column Asymmetric Fluid Grid):**
  - Item feeds default to a 2-column layout with a fixed `0.75rem` (`12px`) gutter.
  - Outer screen padding maintains a strict `1rem` (`16px`) safe boundary.
  - Vertical scroll feeds preserve a continuous rhythm where listing card gap equals gutter width (`0.75rem`).
- **Dense Transactional Shells:** Detail views, messaging threads, and campus filters apply modular bottom-sheet patterns anchored above the mobile system gesture bar.
- **Desktop/Tablet Reflow:** For tablet and desktop viewports, the grid expands to 3 or 4 columns respectively, clamping max content container width to `1120px` with centered auto-margins.

## Elevation & Depth

Visual hierarchy relies on soft, atmospheric diffusion rather than heavy physical drops. This keeps product photography crisp while giving interactive layers clean dimensional separation.

### Surface Elevation System
- **Level 0 (Flat / Canvas):** Neutral `#F8FAFC` base surface for global views.
- **Level 1 (Card / Resting Surface):** `#FFFFFF` surfaces paired with a gossamer border (`1px solid #E2E8F0`) and an ambient, low-density shadow:
  - `box-shadow: 0 2px 8px -2px rgba(15, 23, 42, 0.04), 0 1px 3px -1px rgba(15, 23, 42, 0.02);`
- **Level 2 (Active Cards / Popovers):** Used on tap states, pinned search bars, and floating campus quick-filters:
  - `box-shadow: 0 10px 20px -3px rgba(15, 23, 42, 0.06), 0 4px 6px -2px rgba(15, 23, 42, 0.03);`
- **Level 3 (Modal Bottom Sheets & Sticky Bottom Action Bars):** Elevates transactional elements above the listing scroll view with an ambient, tinted veil:
  - `box-shadow: 0 -4px 24px 0 rgba(15, 23, 42, 0.08);`
  - Backdrops utilize `backdrop-filter: blur(8px);` with `rgba(15, 23, 42, 0.35)` scrim.

## Shapes

The design system adopts a **Rounded (Level 2)** geometry to reinforce friendliness, safety, and modern mobile ergonomics. 

### Corner Radius Mapping
- **Default Elements (`0.5rem` / `8px`):** Input fields, inline tags, metadata pills, check controls.
- **Cards & Visual Containers (`1rem` / `16px`):** Item cards, profile overview blocks, notification modules.
- **Primary Panels & Bottom Sheets (`1.5rem` / `24px`):** Bottom-sheet tops, hero promotional banners, search trigger capsules.
- **Pills / Full Rounding (`9999px`):** Floating action buttons (e.g., "Đăng tin +"), verification micro-badges, and filter chips.

## Components

### 1. Buttons
- **Primary Action ("Mua ngay", "Đăng tin"):** Emerald green (`#059669`) background, white text, semi-bold `label-lg`, `0.75rem` vertical padding, rounded-lg (`1rem`). On active tap: slight scale down (`scale(0.98)`).
- **Urgent Action ("Pass gấp", "Deal sốc"):** Coral gradient or solid `#F97316`, white text, high contrast.
- **Secondary / Ghost ("Nhắn tin", "Trả giá"):** White background, `1.5px` border with `#E2E8F0`, deep slate text (`#0F172A`).
- **Floating Action Button (Post Item):** Pill-shaped, fixed bottom-right (`bottom: 5rem`, `right: 1rem`), vibrant primary emerald with a subtle drop shadow (`Level 2`).

### 2. Marketplace Cards
- **Product Listing Card:** White surface, rounded-lg (`1rem`), overflow hidden. Image container features a `4:5` or `1:1` aspect ratio with an overlay for urgency ("Pass gấp" pill in secondary color) and location ("KTX Khu B"). Body area holds title (`body-md`, 2-line clamp), tabular price (`price-md` in `#059669`), and a student verification indicator.
- **Seller Mini Card:** Compact horizontal row featuring a circular avatar, full student name, university badge (`#0284C7`), and star/transaction count.

### 3. Verification & Trust Badges
- **Campus Verified Badge:** Pill component with `#E0F2FE` background, `#0284C7` text, micro shield-check icon, and typography `label-sm` (e.g., "ĐH Bách Khoa • Đã xác thực SV").
- **Condition Tag:** Low-saturation slate tint (`#F1F5F9`), slate text (`#475569`), indicating item status (e.g., "Mới 99%", "Hết bảo hành").

### 4. Input Fields & Search
- **Campus Search Bar:** Rounded-xl capsule container, `#F1F5F9` background, search icon prefixed, placeholder styled in `#94A3B8` ("Tìm giáo trình, tủ lạnh mini, xe máy..."). Focus state transitions border to `#059669` with pure white background.
- **Price Range Inputs:** Dual-box inline form with dynamic "đ" suffix anchored on the right.

### 5. Chips & Filter Pills
- **Filter Chips:** Pill-shaped, height `36px`, horizontal scroll rail. Inactive state: `#F1F5F9` background, `#475569` text. Active state: `#ECFDF5` background, `#059669` text, `1px solid #059669`.

### 6. Campus Selection Drawer
- Special bottom-sheet component enabling quick switching between specific student zones (e.g., "Làng Đại học Thủ Đức", "Cụm Bách - Kinh - Xây"). Includes quick distance badges based on dorm proximities.