package org.springframework.cloud.config.server.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amica.acm.configuration.component.ComponentConfiguration;
import com.amica.acm.configuration.component.EnhancedConfiguration;
import com.amica.acm.configuration.component.multienv.MultiEnvironmentComponentConfigurationManager;
import com.google.common.collect.Lists;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.core.Ordered;

public class MultiEnvironmentConfigurationManagerEnvironmentRepository implements EnvironmentRepository, Ordered {

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public Environment findOne(String application, String profile, String label) {
		Environment environment = new Environment(application, profile);

		ComponentConfiguration componentConfiguration = MultiEnvironmentComponentConfigurationManager
				.getComponentConfiguration("SpringCloudConfigServer", profile);

		Map<String, Object> newComponentConfiguration = new HashMap<String, Object>();

		List<String> configNamesToOmit = Arrays.asList("HealthEndpoints", "RequiredAWFDatabases", "EdgeAPIs",
				"EBillModificationSourceTranslations", "Manifests", "Notification", "Components", "RequiredWSClients",
				"WebApplicationComponents", "ESignSigningFrameURLs", "System_Defaults", "EDBPaymentUserMapping",
				"SecureResources", "RequiredApplicationURLs", "RequiredFileShareEntries", "WebServiceComponents",
				"ChasePayFrameURLs", "RequiredJMXComponents", "Class_Reg", "HealthEndpoints", "JMX", "WSConfigFiles",
				"RequiredAWFQueues", "SpectrumClient", "Environment", "ZuulRoutes", "Legacy", "junk");

		Set<String> configurationNames = componentConfiguration.getConfigurationNames();

		EnhancedConfiguration environmentConfiguration = componentConfiguration.getConfiguration("Environment");
		EnhancedConfiguration serverConfiguration = componentConfiguration.getConfiguration("Servers");

		for (String configurationName : configurationNames) {
			List<String> configurationKeys = Lists.newArrayList();

			if (configNamesToOmit.contains(configurationName)) {
				continue;
			}
			EnhancedConfiguration configuration = componentConfiguration.getConfiguration(configurationName);
			Iterator<String> keyIterator = configuration.getKeys();
			String key = "";
			List<SpringProperty> properties = new ArrayList<SpringProperty>();
			while (keyIterator.hasNext()) {
				key = keyIterator.next();
				if (!configurationName.equalsIgnoreCase("server")
						&& !configurationName.equalsIgnoreCase("environment")) {
					if (environmentConfiguration.containsKey(key) || serverConfiguration.containsKey(key)) {
						continue;
					}
				}

				SpringProperty property = new SpringProperty();
				property.setPrefix(configuration.getConfigurationDescriptor().getName());
				property.setKey(key);
				List<Object> value = configuration.getList(key);
				if (value.size() > 1) {
					property.setValue(value);
				} else {
					property.setValue(value.get(0));
				}
				properties.add(property);
			}
			for (SpringProperty property : properties) {
				configurationKeys.add(property.getKey());
				newComponentConfiguration.put(property.getPrefix() + "." + property.getKey(), property.getValue());
			}
			newComponentConfiguration.put(
					configuration.getConfigurationDescriptor().getName() + "." + "configurationKeys",
					configurationKeys);
		}

		ConfigurationManagerPropertySource configurationManagerPropertySource = new ConfigurationManagerPropertySource(
				application, newComponentConfiguration);

		environment.add(configurationManagerPropertySource);
		return environment;
	}

	static class SpringProperty {

		private String prefix;

		private String key;

		private Object value;

		public SpringProperty() {
		}

		public SpringProperty(String prefix, String key, Object value) {
			this.prefix = prefix;
			this.key = key;
			this.value = value;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

	}

}