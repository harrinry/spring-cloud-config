package org.springframework.cloud.config.server.environment;

import java.util.Map;

import org.springframework.cloud.config.environment.PropertySource;

public class ConfigurationManagerPropertySource extends PropertySource {

	public ConfigurationManagerPropertySource(String name, Map<String, Object> source) {
		super(name, source);
	}

}