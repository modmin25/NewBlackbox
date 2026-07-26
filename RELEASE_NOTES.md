# BlackBox Pro v5.0.0 Release Notes

## What's New

### Rebranding
- App renamed from **NewBlackbox** to **BlackBox Pro**
- New modern dark-themed adaptive icon
- Redesigned splash screen with premium dark aesthetic

### UI/UX Overhaul
- **Complete Dark Theme**: Premium dark mode with deep navy/charcoal color palette
- **Modern Color System**: Material Design 3 inspired accent colors (E94560 accent, 1A1A2E primary)
- **Dark Status & Navigation Bars**: Fully immersive dark interface
- **New Icon Design**: Hexagonal shield icon representing security and virtualization

### Multi-Language Support
- **Arabic (AR)**: Full RTL language support
- **English (EN)**: Default language
- **Spanish (ES)**: Complete translation
- **French (FR)**: Complete translation
- **Chinese Simplified (zh-CN)**: Updated translations
- **Chinese Traditional (zh-TW)**: Updated translations
- **Language Switcher**: Settings-based language selection with system default option

### Settings Enhancements
- Language selector in Settings
- Dark theme toggle (persistent)
- About section with version info
- Improved preference layout with categories

### Code Quality
- Added `LanguageHelper` utility for language management
- Improved error handling in Application class
- Updated `App.kt` for language context initialization

### CI/CD
- New `build-apk.yml` GitHub Actions workflow
- Automated debug and release APK builds
- Artifact upload for easy download

### Configuration
- Application ID changed to `com.blackbox.pro.app`
- Version bump to 5.0.0 (code 500)
- Root project name changed to `BlackBoxPro`

## Fixed
- Dark theme color inconsistencies
- Location icon visibility in dark mode
- Various string references updated for branding consistency
