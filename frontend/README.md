# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

You can also install [eslint-plugin-react-x](https://npmx.dev/package/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://npmx.dev/package/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

## Future UI/UX Roadmap

### Calendar

Add a calendar as a persistent sidebar on the relevant application pages.

Tasks that have a `dueDate` must be displayed on the corresponding calendar date. The calendar should allow the user to navigate dates and quickly see tasks planned for a selected day.

### Daily Notes

Add a second sidebar containing three equal-sized note sections:

- **Я вчера** — notes for yesterday (`today - 1 day`)
- **Я сегодня** — notes for today
- **Я завтра** — notes for tomorrow (`today + 1 day`)

The sections are relative to the current calendar date, not permanent note fields.

For example, on September 2:

- Я вчера → September 1
- Я сегодня → September 2
- Я завтра → September 3

On September 3 the same stored notes automatically appear as:

- Я вчера → September 2
- Я сегодня → September 3
- Я завтра → September 4

Daily notes should therefore be stored by a specific date and user, so that their position in the three sections changes automatically as the date changes.

### Planned Authentication Improvements

Add OAuth login options for:

- Google
- Yandex

OAuth integration should be implemented after the production environment is configured, so that production callback URLs, HTTPS, user linking and JWT authentication can be designed correctly.
