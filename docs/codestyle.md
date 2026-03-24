# Соглашения о стиле кодирования SBG Scout

| Что | Формат | Пример |
|---|---|---|
| Пакеты | lowercase | `com.github.wrager.sbgscout.script.model` |
| Классы/интерфейсы | PascalCase | `ScriptStorage`, `UserScript` |
| Функции/переменные | camelCase | `getEnabledScripts` |
| Константы | UPPER_SNAKE_CASE | `MAX_CACHE_SIZE` |
| XML layout файлы | snake_case | `activity_game.xml` |
| XML id | camelCase | `@+id/scriptToggle` |
| Ресурсы строк | snake_case | `@string/script_name` |

Избегать слов "util" и "manager" в названиях новых сущностей.

Не сокращать имена идентификаторов — `button`, не `btn`; `element`, не `el`; `callback`, не `cb`. Полные имена читаются без контекста.

Аббревиатуры в camelCase/PascalCase считаются одним словом (первая буква заглавная, остальные строчные): `Ui`, `Api`, `Id`, `Url`.
