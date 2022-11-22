package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.pojo.Orders;
import org.springframework.transaction.annotation.Transactional;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    void submit(Orders orders);
}
