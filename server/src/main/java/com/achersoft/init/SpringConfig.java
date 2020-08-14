package com.achersoft.init;

import com.achersoft.configuration.PropertiesManager;
import com.achersoft.email.EmailClient;
import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.UserAuthenticationServiceImpl;
import com.achersoft.security.authenticator.Authenticator;
import com.achersoft.security.providers.SignatureServiceProvider;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.CharacterServiceImpl;
import com.achersoft.tdcc.character.create.CharacterCreatorService;
import com.achersoft.tdcc.character.create.CharacterCreatorServiceImpl;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.party.PartyService;
import com.achersoft.tdcc.party.PartyServiceImpl;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import com.achersoft.tdcc.token.TokenService;
import com.achersoft.tdcc.token.TokenServiceImpl;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.TokenAdminServiceImpl;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.achersoft.tdcc.vtd.VirtualTdService;
import com.achersoft.tdcc.vtd.VirtualTdServiceImpl;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.tdcc.vtd.persistence.VtdMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "file:../app/tdcc.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:/opt/app/tdcc.properties", ignoreResourceNotFound = true)
@MapperScan(basePackageClasses = { UserMapper.class,
                                   TokenAdminMapper.class,
                                   TokenMapper.class,
                                   CharacterMapper.class,
                                   PartyMapper.class,
                                   VtdMapper.class
                                 })
public class SpringConfig {
    
    @Autowired
    private Environment env;
    
    // <editor-fold defaultstate="collapsed" desc="REST Gateways"> 
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Services">
    @Bean
    public CharacterService characterService() {
        return new CharacterServiceImpl();
    }
    
    @Bean
    public CharacterCreatorService characterCreatorService() {
        return new CharacterCreatorServiceImpl();
    }
    
    @Bean
    public PartyService partyService() {
        return new PartyServiceImpl();
    }
    
    @Bean
    public TokenService tokenService() {
        return new TokenServiceImpl();
    }
    
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
    
    @Bean
    public EmailClient emailClient() {
        return new EmailClient();
    }

    @Bean
    public VirtualTdService virtualTdService() {
        return new VirtualTdServiceImpl();
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="System"> 
    @Bean
    public DataSource dataSource() {
        DataSource datasource = new DataSource();
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:" + env.getProperty("dataSource.database") + 
                 "://" + env.getProperty("dataSource.url") +
                 ":" + env.getProperty("dataSource.port") +
                 "/" + env.getProperty("dataSource.schema"));
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setUsername(env.getProperty("dataSource.user"));
        p.setPassword(env.getProperty("dataSource.password"));
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
            .expireAfterAccess(12, TimeUnit.HOURS)
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
