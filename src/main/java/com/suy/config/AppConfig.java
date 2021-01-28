package com.suy.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import({DaoConfig.class, MvcConfig.class})
@ComponentScan("com.suy")
@EnableTransactionManagement
public class AppConfig {
}
