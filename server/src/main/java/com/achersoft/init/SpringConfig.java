package com.achersoft.init;

import com.achersoft.configuration.PropertiesManager;
import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.UserAuthenticationServiceImpl;
import com.achersoft.security.authenticator.Authenticator;
import com.achersoft.security.providers.SignatureServiceProvider;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.TokenAdminServiceImpl;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.user.UserService;
import com.achersoft.user.UserServiceImpl;
import com.achersoft.user.persistence.UserMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@MapperScan(basePackageClasses = { UserMapper.class,
                                   TokenAdminMapper.class
                                 })
public class SpringConfig {
    
    // <editor-fold defaultstate="collapsed" desc="REST Gateways"> 
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Services"> 
    @Bean
    public TokenAdminService tokenAdminService() {
        return new TokenAdminServiceImpl();
    }
    
    @Bean
    public UserAuthenticationService userAuthenticationService() {
        return new UserAuthenticationServiceImpl();
    }
    
    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="System"> 
    @Bean
    public DataSource dataSource() {
        DataSource datasource = new DataSource();
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:" + "mysql" + 
                 "://" + "localhost" +
                 ":" + "3306" +
                 "/" + "td_tokens");
        p.setUsername("root");
        p.setPassword("zxcxcv");
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        datasource.setPoolProperties(p);
        return datasource;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory;
    }
    
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }
    
    @Bean
    public SecretKeySpec signatureKey() throws Exception {
        byte[] key = new byte[256];
        new SecureRandom().nextBytes(key);
        return new SecretKeySpec(key, "HmacSHA256");
    }
    
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SignatureServiceProvider signatureServiceProvider(SecretKeySpec signatureKey) throws Exception {
        return new SignatureServiceProvider(signatureKey);
    }
     
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserPrincipalProvider userPrincipalProvider() {
        return new UserPrincipalProvider();
    }
    
    @Bean
    public Authenticator authenticator() {
        return new Authenticator();
    }
    
    @Bean
    public PropertiesManager propertiesManager() {
        return new PropertiesManager();
    }
    
    @Bean(name = "userMap")
    public Cache<String, String> userMap() {
        return CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="DTO Prototypes"> 
  /*  @Bean
    @Scope("prototype")
    public PositionListDTO positionListDTO() {
        return new PositionListDTO();
    }*/
    

    // </editor-fold>
}
