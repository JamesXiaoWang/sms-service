package com.zhijia.src.sms.common.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class MyYmlPropertiesFactory {
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertiesFactory() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-config.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

	/*@Bean
    public PropertySourcesPlaceholderConfigurer propertiesFactory() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        MutablePropertySources sources = new MutablePropertySources();
        try {
            sources.addLast((PropertySource) loader.load("conf", new ClassPathResource("application-config.yml")));   
        } catch (IOException e) {
            e.printStackTrace();
        }
        configurer.setPropertySources(sources);
        return configurer;
    }*/
}
