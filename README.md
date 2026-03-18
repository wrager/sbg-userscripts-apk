# SBG Userscripts

Android-приложение для игры [SBG](https://sbg-game.ru/) с встроенным менеджером юзерскриптов.

## Возможности

- **WebView** с игрой SBG — полноэкранный immersive-режим, геолокация, JS console → Logcat
- **Менеджер скриптов** — парсинг Tampermonkey-заголовков, хранение, загрузка и проверка обновлений
- **Инжекция скриптов** — безопасная обёртка в IIFE, поддержка `@run-at` (document-start / document-end / document-idle)
- **Предустановленные скрипты** — SVP, EUI, CUI, Anmiles script с автозагрузкой при первом запуске
- **Обнаружение конфликтов** — предупреждение при включении несовместимых скриптов
- **JS-бриджи** — полифил `navigator.clipboard`, открытие URL через share

## Требования

- Android 7.0+ (API 24)
- JDK 17, Android SDK 35 (для сборки)

## Установка

Скачать последний APK из [Releases](https://github.com/wrager/sbg-userscripts-apk/releases).

## Сборка

```bash
./gradlew assembleDebug
```

## Проверка

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```

## Документация

- [Архитектура](docs/architecture.md)
- [Принципы разработки](docs/dev-principles.md)
- [Стиль кода](docs/codestyle.md)

## Лицензия

[MIT](LICENSE)
