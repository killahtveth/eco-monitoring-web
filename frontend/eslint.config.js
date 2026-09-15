import js from '@eslint/js'
import globals from 'globals'
import react from 'eslint-plugin-react'

export default [
  { ignores: ['dist'] },
  {
    files: ['**/*.{js,jsx,mjs,cjs,ts,tsx}'],
    plugins: { react },
    languageOptions: {
      parserOptions: {
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
      globals: { ...globals.browser },
    },
    settings: {
      react: {
        version: 'detect', // ESLint сам определит версию React из node_modules
      },
    },
    rules: {
      ...js.configs.recommended.rules,
      ...react.configs.recommended.rules,
      // Отключаем устаревшее правило (React 17+ не требует импорт React)
      'react/react-in-jsx-scope': 'off',
      'react/jsx-uses-react': 'off',
      // Отключаем проверку target="_blank" (или исправь код, добавив rel="noreferrer")
      'react/jsx-no-target-blank': 'off',
    },
  },
]