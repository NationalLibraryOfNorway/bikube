import { createContext, useContext, useEffect, useState } from 'react';

type Theme = 'light' | 'dark';

interface UserSettings {
    theme: Theme;
}

interface UserSettingsContextProps extends UserSettings {
    toggleTheme: () => void;
}

const defaultSettings: UserSettings = {
    theme: 'light',
};

const STORAGE_KEY = 'user-settings';

const UserSettingsContext = createContext<UserSettingsContextProps>({
    ...defaultSettings,
    toggleTheme: () => {},
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
            };
            setSettings(merged);
            applyDomTheme(merged.theme);
        } else {
            applyDomTheme(defaultSettings.theme);
        }
    }, []);

    // persist endringer
    useEffect(() => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    }, [settings]);

    // reager på tema-endringer (om de oppstår utenom toggleTheme)
    useEffect(() => {
        applyDomTheme(settings.theme);
    }, [settings.theme]);

    const applyDomTheme = (theme: Theme) => {
        const root = document.documentElement;
        root.classList.toggle('dark', theme === 'dark');
    };

    const toggleTheme = () => {
        setSettings((prev) => ({ ...prev, theme: prev.theme === 'dark' ? 'light' : 'dark' }));
    };

    return (
        <UserSettingsContext.Provider
            value={{
                ...settings,
                toggleTheme,
            }}
        >
            {children}
        </UserSettingsContext.Provider>
    );
};

export const useUserSettings = () => useContext(UserSettingsContext);
