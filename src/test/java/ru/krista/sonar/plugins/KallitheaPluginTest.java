package ru.krista.sonar.plugins;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты класса плагина.
 */
public class KallitheaPluginTest {

    @Test
    public void pluginTest() {
        Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarLint(Version.parse("6.1")));
        new KallitheaPlugin().define(context);
        assertThat(context.getExtensions().size()).isGreaterThan(1);
    }
}
