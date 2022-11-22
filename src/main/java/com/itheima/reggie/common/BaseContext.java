package com.itheima.reggie.common;

/**
 * ThreadLocal工具类，用于存储登录用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置用户id
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }


    /**
     * 获取用户id
     * @return
     */
    public static Long getId() {
        return threadLocal.get();
    }
}
