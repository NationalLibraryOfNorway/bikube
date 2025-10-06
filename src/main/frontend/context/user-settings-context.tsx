import { createContext, useContext, useEffect, useState } from 'react';
import { i18n } from '@vaadin/hilla-react-i18n';

type Theme = 'light' | 'dark';
type Language = 'nb' | 'se' | 'nn'; // nb = Norsk bokmål, se = Nordsamisk

interface UserSettings {
    theme: Theme;
    language: Language;
}

interface UserSettingsContextProps extends UserSettings {
    toggleTheme: () => void;
    setLanguage: (lang: Language) => void;
}

const defaultSettings: UserSettings = {
    theme: 'light',
    language: 'nb',
};

const STORAGE_KEY = 'user-settings';

const UserSettingsContext = createContext<UserSettingsContextProps>({
    ...defaultSettings,
    toggleTheme: () => {},
    setLanguage: () => {},
});

export const UserSettingsProvider = ({ children }: { children: React.ReactNode }) => {
    const [settings, setSettings] = useState<UserSettings>(defaultSettings);

    // hydrate fra storage
    useEffect(() => {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
            const parsed = JSON.parse(stored) as Partial<UserSettings>;
            const merged: UserSettings = {
                ...defaultSettings,
                ...parsed,
                // fallback hvis gammel storage mangler language
                language: (parsed.language as Language) ?? defaultSettings.language,
            };
            setSettings(merged);
            applyDomTheme(merged.theme);
            applyLanguage(merged.language);
        } else {
            applyDomTheme(defaultSettings.theme);
            applyLanguage(defaultSettings.language);
        }
    }, []);

    // persist endringer
    useEffect(() => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    }, [settings]);

    // reager på språk-endringer (om de oppstår utenom hydrering)
    useEffect(() => {
        applyLanguage(settings.language);
    }, [settings.language]);

    // reager på tema-endringer (om de oppstår utenom toggleTheme)
    useEffect(() => {
        applyDomTheme(settings.theme);
    }, [settings.theme]);

    const applyDomTheme = (theme: Theme) => {
        const root = document.documentElement;
        root.classList.toggle('dark', theme === 'dark');
    };

    const applyLanguage = (lang: Language) => {
        document.documentElement.lang = lang;
        // Hilla i18n forventer BCP-47-kode; 'se' for nordsamisk
        i18n.configure({ language: lang });
    };

    const toggleTheme = () => {
        setSettings((prev) => ({ ...prev, theme: prev.theme === 'dark' ? 'light' : 'dark' }));
    };

    const setLanguage = (lang: Language) => {
        setSettings((prev) => ({ ...prev, language: lang }));
    };

    return (
        <UserSettingsContext.Provider
            value={{
                ...settings,
                toggleTheme,
                setLanguage,
            }}
        >
            {children}
        </UserSettingsContext.Provider>
    );
};

export const useUserSettings = () => useContext(UserSettingsContext);
