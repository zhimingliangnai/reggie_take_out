package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.exception.CustomException;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.pojo.*;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    public void submit(Orders orders) {
        // 获取用户id
        Long userId = BaseContext.getId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(cartWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        // 查询用户数据
        User user = userService.getById(userId);

        // 查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        if (addressBook == null)  {
            throw new CustomException("未填写地址，请填写地址后下单");
        }

        /*
            前端数据
            addressBookId: "1590970052962213889"
            payMethod: 1
            remark: ""
         */
        AtomicInteger amount = new AtomicInteger(0);
        long orderId = IdWorker.getId();
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 向订单表插入一条数据
        this.save(orders);
        // 向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车
        shoppingCartService.remove(cartWrapper);
    }
}
