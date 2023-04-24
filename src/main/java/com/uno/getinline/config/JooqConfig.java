//package com.uno.getinline.config;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.jooq.ConnectionProvider;
//import org.jooq.ExecuteListenerProvider;
//import org.jooq.conf.Settings;
//import org.jooq.impl.DefaultConfiguration;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
//import org.springframework.boot.autoconfigure.jooq.JooqProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.context.properties.ConstructorBinding;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
////jooq프로퍼티를 가져와서 pretty formatting 구현
//@EnableConfigurationProperties(JooqProperties.class)
//@Configuration
//public class JooqConfig {
//
//    @Bean
//    public DefaultConfiguration jooqConfiguration(
//            JooqProperties properties,
//            CustomJooqProperties customProperties,
//            ConnectionProvider connectionProvider,
//            DataSource dataSource,
//            ObjectProvider<ExecuteListenerProvider> executeListenerProviders,
//            ObjectProvider<DefaultConfigurationCustomizer> configurationCustomizers
//    ) {
//        DefaultConfiguration configuration = new DefaultConfiguration();
//        configuration.set(properties.determineSqlDialect(dataSource));
//        configuration.set(connectionProvider);
//        configuration.set(executeListenerProviders.orderedStream().toArray(ExecuteListenerProvider[]::new));
//        //SQL 포맷팅할지 여부 하도록 -> 이미 사용중인 configuration-processor를 활용
//        configuration.setSettings(new Settings().withRenderFormatted(customProperties.isFormatSql()));
//        configurationCustomizers.orderedStream().forEach((customizer) -> customizer.customize(configuration));
//
//        return configuration;
//    }
//
//    @Getter
//    @RequiredArgsConstructor
//    @ConstructorBinding
//    @ConfigurationProperties("spring.custom-jooq")
//    public static class CustomJooqProperties {
//        /**
//         * sql pretty formatting
//         */
//        private final boolean formatSql;
//    }
//
//}