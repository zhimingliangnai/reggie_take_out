package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (Strings.isNotEmpty(phone)) {
            // 生成随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = " + code);

//            SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909", "15350131227", code);

            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("手机验证码短信发送失败");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        // 获取map中的数据
        String phone = String.valueOf(map.get("phone"));
        String code = String.valueOf(map.get("code"));
        // 获取session中的验证码
        String checkCode = String.valueOf(session.getAttribute(phone));

        // 判断验证码是否相同
        if (checkCode.equals(code))  {
            //构建条件构造器
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(phone != null , User::getPhone, phone);

            User user = userService.getOne(wrapper);

            // 判断用户是否存在，存在则直接登录，不存在则注册用户
            if (user == null ) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("验证码错误");
    }
}
