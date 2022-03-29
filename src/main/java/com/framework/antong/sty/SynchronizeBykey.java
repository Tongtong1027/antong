package com.framework.antong.sty;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * author: chenkaihang
 * date: 2022/1/25 3:25 下午
 */
@Component
public class SynchronizeBykey {

    Map<String, ReentrantLock> mutexReentrantLockCache = new ConcurrentHashMap<>();

    Map<String,Object> mutexSynCache = new ConcurrentHashMap<>();


    public void exec(String key, Runnable statement){
        /** 不可取
         * 原因：由于tomcat支持的并发数默认只支持200，所以当超过200个请求的时候，tomcat会先放200个请求进来
         * 这200个请求会先在synchronized代码块前获取到同一把锁，然后在一个线程执行remove操作后，锁被释放
         * tomcat开始放第201个请求进来，但这时候锁已经被释放，于是创建了另一把锁，导致线程安全问题
        Object mutex4Key = mutexSynCache.computeIfAbsent(key, k -> new Object());
        synchronized (mutex4Key) {
            if(mutex4Key == null) {
                System.out.println(mutex4Key);
            }
            try {
                statement.run();
            }finally {
                mutexSynCache.remove(key);
            }
        }*/

        /** 不可取
         * 原因：极端情况下，mutex4Key.getQueueLength()获取到争抢当前Lock的线程数量为0，mutexReentrantLockCache.remove(key);进行锁的移除，假设是1号锁
         * 在进行移除操作的同时，这个时候又有一条线程获取到将要被移除的锁，既1号锁，
         * 这时候remove操作完成，1号锁从缓存中删除，后续线程进来，mutexReentrantLockCache.computeIfAbsent(key, k-> new ReentrantLock());拿到的锁，假设是2号锁，不是同一把锁。造成并行的情况

         ReentrantLock mutex4Key = mutexReentrantLockCache.computeIfAbsent(key, k-> new ReentrantLock());
         mutex4Key.lock();
         try {
         statement.run();
         } finally {
         if (mutex4Key.getQueueLength() == 0) {

         mutexReentrantLockCache.remove(key);
         }
         mutex4Key.unlock();
         }*/

        /**
         * 正解，将加锁操作进行double-check
         */
        ReentrantLock mutex4Key = null;
        ReentrantLock mutexInCache = null;

        while (mutexInCache == null || mutex4Key != mutexInCache) {
            if (mutex4Key != null) {
                mutex4Key.unlock();
            }
            mutex4Key = mutexReentrantLockCache.computeIfAbsent(key, k-> new ReentrantLock());
            // 如果刚好拿到被remove的锁，这里lock()是wait状态，等上一条线程remove操作和unlock()后再次获取的锁是null，所以会重新创建锁，保证线程安全
            mutex4Key.lock();
            mutexInCache = mutexReentrantLockCache.get(key);
        }

        try {
            statement.run();
        } finally {
            // ReentrantLock.getQueueLength()获取到争抢当前Lock的线程数量
            if (mutex4Key.getQueueLength() == 0) {
                mutexReentrantLockCache.remove(key);
            }
            mutex4Key.unlock();
        }
    }
}
