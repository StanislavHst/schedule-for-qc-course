import '@testing-library/jest-dom/extend-expect';

const nativeConsoleError = console.error.bind(console);

console.error = (...args) => {
    const text = args.map((a) => (typeof a === 'string' ? a : '')).join(' ');
    if (text.includes('Some problems with i18next')) {
        nativeConsoleError(...args);
        return;
    }
    throw new Error(text || 'console.error was called');
};