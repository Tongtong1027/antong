package com.framework.antong.sty;

import com.framework.common.utils.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author: chenkaihang
 * date: 2022/2/18 5:55 下午
 */
@RestController
public class NotifyTestController {

    private Object obj = new Object();

    @RequestMapping(value = "/produce/{id}")
    public Result produce(@PathVariable String id) throws InterruptedException {
        synchronized (obj) {
            System.out.println(obj);
            System.out.println(id + "进入");
            obj.wait();
            System.out.println(id + "被唤醒");
        }
        return Result.ok();
    }

    @RequestMapping(value = "/notify")
    public Result cmd_notify() {
        synchronized (obj) {
            obj.notify();
        }
        return Result.ok();
    }

    @RequestMapping(value = "/notifyAll")
    public Result cmd_notifyAll() {
        synchronized (obj) {
            obj.notifyAll();
            return Result.ok();
        }
    }

}
