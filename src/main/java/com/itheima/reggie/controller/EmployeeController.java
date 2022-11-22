package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    /**
     * 登录功能
     *
     * @param request
     * @param employee
     * @return
     */

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1、根据页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = service.getOne(wrapper);

        // 3、如果没有查询到结果，则返回登录失败
        if (null == emp) {
            return R.error("登陆失败");
        }

        // 4、判断页面提交的密码是否跟emp的密码相同，不同则返回登陆失败
        if (!password.equals(emp.getPassword())) {
            return R.error("登陆失败");
        }


        // 5、查看员工状态是否可用
        if (emp.getStatus() == 0) {
            return R.error("账号以禁用");
        }
        // 6、登录成功，将用户id存入Session中并返回结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    /*
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>();
        wrapper.eq(Employee::getUsername,employee.getUsername());
        wrapper.eq(Employee::getPassword,password);

        Employee emp = service.getOne(wrapper);
        R<Employee> res = new R<>();

        if (null != emp) {
            res.setCode(1);
            res.setData(emp);
            res.setMsg("登录成功");
            HttpSession session = request.getSession();
            session.setAttribute("用户",emp);

            log.info("用户" + emp.getId()+ "." + emp.getName() + ":用户登录成功");
        } else {
            res.setCode(0);
            res.setMsg("登录失败，用户名或密码错误，请重新输入。");
            log.info("登录失败");
        }

        return res;

    }
    */

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1、清除session
        request.getSession().removeAttribute("employee");

        // 2、返回结果
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping()
    public R<String> addEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

//        Long empID = (Long) request.getSession().getAttribute("employee");
//        BaseContext.setCurrentId(empID);

        service.save(employee);
        return R.success("添加成功");
    }

    /**
     * 员工分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {}, name = {}", page, pageSize, name);

        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 构建条件构造器
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();
        wrapper.like(StringUtils.isNotEmpty(name) ,Employee::getName,name);

        service.page(pageInfo,wrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改员工信息(与修改员工状态调用相同)
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("进行员工修改");
        // 设置update_time、update_user;
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));

//        Long empID = (Long) request.getSession().getAttribute("employee");
//        BaseContext.setCurrentId(empID);
        service.updateById(employee);

        return R.success("修改成功");

    }

    /**
     * 回显员工数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> queryEmployeeById(@PathVariable String id) {
        Employee employee = service.getById(Long.parseLong(id));
        if (employee != null)
            return R.success(employee);

        return R.error("未找到该员工");
    }

}
