package com.framework.common.settings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * author: chenkaihang
 * date: 2020/9/10 2:17 下午
 */
@Data
@Component("environmentSettings")
@ConfigurationProperties(prefix = "environment")
public class EnvironmentSettings {

    private String name;

}
