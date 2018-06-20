package org.cloudfoundry.samples.music.config.data;

import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionAzureKeyVaultProvider;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "spring.datasource.dataSourceProperties.ColumnEncryptionSetting", havingValue = "Enabled", matchIfMissing = false)
public class DataSourceBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceBeanPostProcessor.class);

    @Value("${microsoft.vault.clientId}")
    private String clientId;
    @Value("${microsoft.vault.clientSecret}")
    private String clientSecret;

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            try {
                logger.info ("initializing DataSource AlwaysEncryption Vault provider");
                SQLServerColumnEncryptionAzureKeyVaultProvider akvProvider =
                        new SQLServerColumnEncryptionAzureKeyVaultProvider(clientId, clientSecret);

                Map<String, SQLServerColumnEncryptionKeyStoreProvider> keyStoreMap = new HashMap<String, SQLServerColumnEncryptionKeyStoreProvider>();
                keyStoreMap.put(akvProvider.getName(), akvProvider);

                SQLServerConnection.registerColumnEncryptionKeyStoreProviders(keyStoreMap);

            } catch (SQLException ex) {
                logger.error(ex.getMessage());
                throw new FatalBeanException(ex.getMessage());
            }
        }
        return bean;
    }
}