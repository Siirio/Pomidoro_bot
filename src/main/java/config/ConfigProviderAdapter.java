package config;

import storage.ConfigProvider;

/**
 * Адаптер для использования Config в качестве ConfigProvider.
 * Обеспечивает обратную совместимость со старым кодом.
 */
public class ConfigProviderAdapter implements ConfigProvider {
    private final Config config;
    
    public ConfigProviderAdapter(Config config) {
        this.config = config;
    }
    
    @Override
    public String getDbUrl() {
        return config.dbUrl();
    }
    
    @Override
    public String getDbUser() {
        return config.dbUser();
    }
    
    @Override
    public String getDbPassword() {
        return config.dbPassword();
    }
}