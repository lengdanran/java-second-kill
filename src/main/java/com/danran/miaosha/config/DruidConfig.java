package com.danran.miaosha.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname DruidConfig
 * @Description TODO
 * @Date 2021/6/6 15:43
 * @Created by LengDanran
 */
@Configuration
public class DruidConfig {
    /*
      从配置文件中配置的参数加入到Druid源中
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druid() {
        return new DruidDataSource();
    }
    /**
     * 配置Druid的监控，如果不配置这个类，将连接不上druid后台。http://localhost:8080/druid/
     * 配置一个管理后台的Servlet
     */
    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String,String> params = new HashMap<>();
        params.put("loginUsername","admin");//设置后台登录名
        params.put("loginPassword","admin");//密码
        params.put("allow","");//设置默认就是允许所有访问
        params.put("deny","192.168.84.129");//设置黑名单
        bean.setInitParameters(params);
        return bean;
    }

    /**
     * 配置一个web监控的filter,如果不配置这个类，在页面的Web模块功能不会开启
     */
    @Bean
    public FilterRegistrationBean webStatFilter(){
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setFilter(new WebStatFilter());
        Map<String,String> params = new HashMap<>();
        params.put("exclusions","*.js,*.css,/druid/*");//设置不拦截请求
        frb.setInitParameters(params);
        frb.setUrlPatterns(Collections.singletonList("/*"));//拦截请求
        return  frb;
    }
}
