package com.hwj.common.to.mq;

import lombok.Data;

/**
 * 库存向mq发送消息的to
 *
 * @author hwj
 */
@Data
public class StockLockedTo {
    private Long id;
    private StockDetailTo detailTo;
}
