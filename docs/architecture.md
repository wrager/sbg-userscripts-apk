# Архитектура SBG Userscripts APK

## Обзор

Android-приложение с WebView, загружающее игру SBG (`sbg-game.ru/app/`) и инжектирующее пользовательские скрипты. Один APK заменяет несколько сборок Anmiles SBG APK, добавляя менеджер скриптов, кеширование ассетов и проверку обновлений.

## Activities

| Activity | Назначение |
|---|---|
| `SplashActivity` | LAUNCHER. Список скриптов с тогглами, кнопка «Запустить» |
| `GameActivity` | WebView с игрой, инжекция скриптов, immersive mode |
| `ScriptManagerActivity` | Управление скриптами: добавить/удалить/обновить |
| `AppSettingsActivity` | Настройки приложения |

## WebView

- `SbgWebViewClient` — перехват загрузки страниц, инжекция скриптов в `onPageStarted`, перехват ресурсов для кеширования в `shouldInterceptRequest`
- JS-бриджи: `ClipboardBridge` (полифил `navigator.clipboard`), `ShareBridge` (открытие URL)
- Инжекция только на `sbg-game.ru/app*`

## Менеджер скриптов

### Модель данных

- `UserScript` — скрипт: идентификатор, заголовок (Tampermonkey-формат), URL, контент, enabled, isPreset
- `ScriptHeader` — парсинг `// ==UserScript==` блока
- `ScriptConflict` — правила конфликтов между скриптами

### Предустановленные скрипты

1. **SVP** (SBG Vanilla+) — `wrager/sbg-vanilla-plus`
2. **EUI** (Enhanced UI) — `egorantonov/sbg-enhanced`
3. **CUI** (Custom UI) — `nicko-v/sbg-cui`
4. **Anmiles script** (sbg.plus) — `anmiles/userscripts`

### Правила конфликтов

- SVP конфликтует с: EUI, CUI, Anmiles script
- EUI, CUI, Anmiles script — совместимы между собой
- Архитектура поддерживает более сложные правила в будущем

### Порядок инжекции

1. Глобальные переменные
2. Clipboard-полифил
3. Скрипты (каждый в IIFE, обёрнут в try-catch)

## Кеширование

- `shouldInterceptRequest` + дисковый кеш
- API-вызовы (`/api/*`) — не кешировать
- Статика (JS, CSS, изображения) — кешировать на диск
- Лимит кеша (50MB по умолчанию)

## Обновления

- **Скрипты**: загрузка `.meta.js`, сравнение `@version`, фоновая проверка при запуске
- **APK**: GitHub Releases API, сравнение `tag_name` с `BuildConfig.VERSION_NAME`, раз в 24 часа

## Флоу запуска

1. `SplashActivity`: список скриптов с тогглами + кнопка «Запустить»
2. Первый запуск: загрузка пресетных скриптов (прогресс-бар)
3. Последующие запуски: список сразу, обновления в фоне
4. Нажатие «Запустить» → `GameActivity`

## Структура проекта

```
app/src/main/kotlin/com/github/wrager/sbguserscripts/
├── GameActivity.kt
├── bridge/            ClipboardBridge, ShareBridge
├── cache/             AssetCache, CachePolicy
├── diagnostic/        DiagnosticCollector, BugReportLauncher
├── game/              GameVersionMonitor
├── manager/           ScriptManagerActivity, AddScriptDialog
├── script/
│   ├── injector/      ScriptInjector, InjectionResult
│   ├── model/         UserScript, ScriptHeader, ScriptConflict
│   ├── parser/        HeaderParser
│   ├── preset/        PresetScripts, ConflictRules
│   ├── storage/       ScriptStorage (interface + impl)
│   └── updater/       ScriptDownloader, ScriptUpdateChecker
├── settings/          AppSettingsActivity, AppPreferences
├── splash/            SplashActivity, ScriptListAdapter
├── updater/           ApkUpdateChecker, ApkUpdateDialog
└── webview/           SbgWebViewClient
```
