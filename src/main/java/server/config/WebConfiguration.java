package server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import server.persistence.dao.ServerDao;
import server.persistence.dao.TransactionDao;
import server.persistence.dao.daoImpl.ServerDaoImpl;
import server.persistence.dao.daoImpl.TransactionDaoImpl;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = "server")
@PropertySource("classpath:persistence.properties")
public class WebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    private Environment env;
    private Logger logger = LoggerFactory.getLogger(WebConfiguration.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/WEB-INF/resources/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter();
        converter.setWriteAcceptCharset(false);
        converters.add(converter);
    }

    @Bean
    public ViewResolver viewResolver() {
        return new InternalResourceViewResolver("/WEB-INF/views/", ".jsp");
    }

    @Bean
    @Lazy
    public Gson gson() {
        return new GsonBuilder().disableHtmlEscaping().create();
    }

    @Bean(destroyMethod = "")
    public DataSource dataSource() {
        try {
            String dataSource = env.getProperty("dataSource");
            logger.info("Jndi data source: {}", dataSource);
            return new JndiTemplate().lookup(dataSource, DataSource.class);
        } catch (NamingException e) {
            logger.error("{}", e.getMessage());
            return null;
        }
    }

    @Bean
    @Scope(value = "singleton")
    public EntityManagerFactory emf(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPackagesToScan("server.persistence.entity");
        Properties properties = new Properties();
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        properties.put("hibernate.max_fetch_depth", env.getProperty("hibernate.max_fetch_depth"));
        properties.put("hibernate.jdbc.fetch_size", env.getProperty("hibernate.jdbc.fetch_size"));
        properties.put("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size"));
        properties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        factory.setJpaProperties(properties);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager tx(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    @Lazy
    public TransactionDao transactionDao() {
        return new TransactionDaoImpl(){};
    }

    @Bean
    @Lazy
    public ServerDao serverDao() {
        return new ServerDaoImpl(){};
    }
}
