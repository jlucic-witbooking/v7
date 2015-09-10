package witbookerv7

import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource


/**
 * Created by mongoose on 4/23/14.
 */
class ExtendedPluginAwareResourceBundleMessageSource extends PluginAwareResourceBundleMessageSource {
    static transaction = false

    Map<String, String> listMessageCodes(Locale locale) {
        Properties properties = getMergedProperties(locale).properties
        Properties pluginProperties = getMergedPluginProperties(locale).properties
        return properties.plus(pluginProperties)
    }
}
