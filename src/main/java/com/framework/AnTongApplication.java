package com.framework;

import com.framework.common.settings.EnvironmentSettings;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.framework.antong.*.mapper")
@Slf4j
public class AnTongApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AnTongApplication.class, args);
        EnvironmentSettings environmentSettings = (EnvironmentSettings)ctx.getBean("environmentSettings");

        log.info(environmentSettings.getName());
    }

}
