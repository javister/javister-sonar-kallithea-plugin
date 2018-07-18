package ru.krista.sonar.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.config.Encryption;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import ru.krista.sonar.plugins.components.ConfigTools;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест чтения конфигурации плагина.
 */
public class ConfigToolsTest {

    private Settings settings;
    private ConfigTools config;

    @Before
    public void prepare() {
        settings = new Settings(new PropertyDefinitions(KallitheaPlugin.class), new Encryption(null)) {

            Map<String, String> params = new HashMap<>();

            @Override
            protected Optional<String> get(String s) {
                return params.entrySet().stream().filter(entry -> entry.getKey().equals(s))
                        .map(Map.Entry::getValue).findFirst();
            }

            @Override
            protected void set(String s, String s1) {
                params.put(s, s1);
            }

            @Override
            protected void remove(String s) {
                params.remove(s);
            }

            @Override
            public Map<String, String> getProperties() {
                return params;
            }
        };
        AnalysisMode analysisMode = Mockito.mock(AnalysisMode.class);
        Mockito.when(analysisMode.isIssues()).thenReturn(true);
        config = new ConfigTools(settings, analysisMode);
    }


    @Test
    public void checkPropertyTest() {
        settings.setProperty(PluginConsts.PLUGIN_ENABLED_PROP_NAME, "false");
        assertThat(config.check()).isEqualTo(false);
        settings.setProperty(PluginConsts.PLUGIN_ENABLED_PROP_NAME, "true");
        assertThat(config.check()).isEqualTo(true);

        settings.setProperty(PluginConsts.PLUGIN_PULL_REQUEST_ID_PROP_NAME, "11111");
        assertThat(config.getPullRequestId()).isEqualTo("11111");
        assertThat(config.needChangeStatus()).isEqualTo(true);
        assertThat(config.getUserName()).isEqualTo("service_sonar");
    }
}
