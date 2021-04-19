package com.fixiu.autoconfigure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fixiu.flyway.config.MigrationProperties;
import com.fixiu.flyway.context.ApplicationContextHelper;
import com.fixiu.flyway.parser.StandardColumnInjectionSupport;

@Configuration
@EnableConfigurationProperties(MigrationProperties.class)
public class FlywayInjectionAutoConfiguration {
	
	@Bean
	public StandardColumnInjectionSupport standardColumnInjectionSupport(MigrationProperties migrationProperties) {
		return new StandardColumnInjectionSupport(migrationProperties);
	}
	
	@Bean
	@Qualifier("flywaydbApplicationContextHelper")
	public ApplicationContextHelper applicationContextHelper() {
		return new ApplicationContextHelper();
	}

}