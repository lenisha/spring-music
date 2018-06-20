package org.cloudfoundry.samples.music.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile({"mysql-cloud", "postgres-cloud", "oracle-cloud", "sqlserver-cloud"})
public class RelationalCloudDataSourceConfig extends AbstractCloudConfig {

    private static final Logger logger = LoggerFactory.getLogger(RelationalCloudDataSourceConfig.class);


    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        logger.info("CREATING SQL DS on the Cloud");
        return connectionFactory().dataSource();
    }

}
