package ru.krista.sonar.plugins;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import ru.krista.sonar.plugins.components.ConfigTools;
import ru.krista.sonar.plugins.components.PluginCommentBuilder;
import ru.krista.sonar.plugins.components.PullRequestHandler;
import ru.krista.sonar.plugins.model.PrintedComments;


/**
 * Сам плагин (указывается в pom.xml).
 */
@Properties({
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.BOOLEAN,
                key = PluginConsts.PLUGIN_ENABLED_PROP_NAME,
                name = "Enabled",
                defaultValue = "true"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_REPO_PREFIX_PROP_NAME,
                name = "Kallithea base URL",
                defaultValue = "http://example.com"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_KALLITHEA_API_URL_PROP_NAME,
                name = "Kallithea-Api base URL"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_KALLITHEA_USER_API_KEY_NAME,
                name = "Kallithea-Api user api-key"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_COMMENT_USER_NAME_PROP_NAME,
                name = "Kallithea base user",
                defaultValue = "service_sonar"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.BOOLEAN,
                key = PluginConsts.PLUGIN_CLEAN_PREV_COMMENTS_PROP_NAME,
                name = "Clean previous user comment",
                defaultValue = "true"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.BOOLEAN,
                key = PluginConsts.PLUGIN_NEED_CHANGE_STATUS_PROP_NAME,
                name = "Change pull-request status",
                defaultValue = "true"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.BOOLEAN,
                key = PluginConsts.PLUGIN_ONLY_NEW_ISSUE_PROP_NAME,
                name = "Only new issue",
                defaultValue = "true"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.BOOLEAN,
                key = PluginConsts.PLUGIN_SEND_STATUS_MAIL_NAME,
                name = "Send status mail",
                defaultValue = "true"
        ),
        @Property(
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_PULL_REQUEST_ID_PROP_NAME,
                global = false,
                name = "Pull-request id"
        ),
        @Property(
                type = PropertyType.STRING,
                key = PluginConsts.PLUGIN_REPO_PATH_PROP_NAME,
                global = false,
                name = "Pull-request target repository"
        ),
        @Property(
                category = PluginConsts.PLUGIN_CATEGORY,
                type = PropertyType.INTEGER,
                key = PluginConsts.PLUGIN_CONNECTION_TIMEOUT_PROP_NAME,
                name = "Connection timeout (ms)",
                defaultValue = "600000"
        )
})
public class KallitheaPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(
                KallitheaPluginPostJob.class,
                PullRequestHandler.class,
                ConfigTools.class,
                PluginCommentBuilder.class,
                PropertyDefinition.builder(PluginConsts.PLUGIN_ISSUE_THRESHOLD_PROP_NAME)
                        .category(PluginConsts.PLUGIN_CATEGORY)
                        .type(PropertyType.SINGLE_SELECT_LIST)
                        .name("Issue severity threshold")
                        .options(PluginConsts.severityListWithNone())
                        .defaultValue(PluginConsts.SEVERITY_NONE)
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),
                PropertyDefinition.builder(PluginConsts.PLUGIN_STATUS_THRESHOLD_PROP_NAME)
                        .category(PluginConsts.PLUGIN_CATEGORY)
                        .type(PropertyType.SINGLE_SELECT_LIST)
                        .name("Status severity threshold")
                        .options(PluginConsts.severityListWithNone())
                        .defaultValue(PluginConsts.SEVERITY_NONE)
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),
                PropertyDefinition.builder(PluginConsts.PLUGIN_PRINTED_COMMENTS_PROP_NAME)
                        .category(PluginConsts.PLUGIN_CATEGORY)
                        .type(PropertyType.SINGLE_SELECT_LIST)
                        .name("Printed comments")
                        .options(PluginConsts.PRINTED_COMMENTS_LIST)
                        .defaultValue(PrintedComments.ALL.name())
                        .onQualifiers(Qualifiers.PROJECT)
                        .build()
        );
    }
}
