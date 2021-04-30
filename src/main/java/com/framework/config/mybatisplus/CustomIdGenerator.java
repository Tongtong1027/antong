package com.framework.config.mybatisplus;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * author: chenkaihang
 * date: 2020/9/3 6:25 下午
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {
    @Override
    public Long nextId(Object entity) {

        //可以将当前传入的class全类名来作为bizKey,或者提取参数来生成bizKey进行分布式Id调用生成.
        String bizKey = entity.getClass().getName();
        //根据bizKey调用分布式ID生成
        Long id = Long.valueOf(getId());
        //返回生成的id值即可.
        return id;
    }
    private AtomicLong seq = new AtomicLong();
    // 由于java Long最大19位，oracle正常显示是15位，因此这里获取的ID使用15位
    // 最大序列数(max_seq = 3)
    private int maxSeq = 3;

    public long getAtomicLong() {
        long atomic = this.seq.incrementAndGet();
        if (String.valueOf(atomic).length() > this.maxSeq) {
            //logger.info("超过最大的位数：{} reset from 0 start", maxSeq);
            this.seq.set(0L);
            return getAtomicLong();
        }
        return atomic;
    }

    public String getId() {
        SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss");
        String prefix = f.format(new Date());
        long suffix = getAtomicLong();
        // 服务ID，分布式部署时候发挥作用(server_id = 0)
//		return prefix + 0 +
//		       String.format(new StringBuilder("%0").append(this.maxSeq).append("d").toString(),
//						     new Object[] { Long.valueOf(suffix) });
        return prefix + String.format(new StringBuilder("%0").append(this.maxSeq).append("d").toString(),
                new Object[]{Long.valueOf(suffix)});
    }
}
