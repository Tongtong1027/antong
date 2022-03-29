package com.framework.antong.sty;

import com.framework.antong.sys.controller.SysUserController;
import com.framework.common.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: chenkaihang
 * date: 2022/1/25 2:09 下午
 */
@RestController
public class SynTestController {

    private static final Logger logger = LoggerFactory.getLogger(SysUserController.class);

    Map<String,Object> mutexCache = new ConcurrentHashMap<>();

    @Autowired
    private SynchronizeBykey synchronizeBykey;
    @RequestMapping(value = "/process/{orderId}")
    public Result process(@PathVariable("orderId") String orderId) {

        synchronizeBykey.exec(orderId,() -> {
            logger.info("[{}] 开始", orderId);
            sleep();
            logger.info("[{}] 结束", orderId);
        });

        return Result.ok();
    }

    private void sleep() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
