# Принципы разработки SBG Scout

## Common principles

1. **FOLLOW** SOLID, KISS, YAGNI.
2. **OBSERVE** Low coupling & High cohesion.
3. **PREFER** explicit over implicit.

## Type Safety

- **PREFER** sealed classes и enum для конечных множеств состояний.
- **AVOID** `!!` (non-null assertion) — использовать `?.let {}`, `requireNotNull()` или `checkNotNull()` с пояснением.
- **AVOID** `as` (unsafe cast) — использовать `as?` с обработкой null.

## Lint

Disabling Errors:

1. **FORBIDDEN** to disable errors for entire file.
2. **FORBIDDEN** to disable all errors at once (`@Suppress` без аргументов).
3. **ALLOWED** to disable only specific rules for specific lines/blocks.
4. **MUST** explain reason in comment.

## Tests

1. Покрывать тестами любой новый функционал и любые изменения существующего.
2. Стремиться к покрытию тестами всех веток кода.
3. Unit-тесты — в `app/src/test/`, инструментальные — в `app/src/androidTest/`.

## Readability

1. **CHOOSE** readability over micro-optimizations.
2. **USE** clear names without non-obvious abbreviations.
