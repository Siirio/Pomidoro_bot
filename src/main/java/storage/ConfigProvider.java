package storage;

/**
 * @deprecated Use {@link config.Config} and {@link config.ConfigReader} instead.
 */
@Deprecated
public interface ConfigProvider {
    String getDbUrl();
    String getDbUser();
    String getDbPassword();
}