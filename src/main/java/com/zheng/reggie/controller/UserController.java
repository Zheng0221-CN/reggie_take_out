package com.zheng.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.reggie.common.R;
import com.zheng.reggie.entity.User;
import com.zheng.reggie.service.UserService;
import com.zheng.reggie.utils.SMSUtils;
import com.zheng.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 生成验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {

        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {

            // 生成随机的 4 位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

            // 调用阿里云提供的短信服务 API 完成发送短信
            // SMSUtils.sendMessage("瑞吉外卖","", phone, code);

            // 需要将生成的验证码保存到 Session
            session.setAttribute(phone, code);

            // 将生成的验证码缓存到 redis 中，并且设置有效期为 5 分钟
            redisTemplate.opsForValue().set(phone, code, 5 , TimeUnit.MINUTES);

            return R.success("手机验证码发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        // 获取手机号
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从 Session 中获取保存的验证码
        // String codeInSession = session.getAttribute(phone).toString();

        // 从 redis 中获取保存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        // 进行验证码的比对（页面提交的验证码和 redis 中保存的验证码对比）
        if (codeInSession != null && codeInSession.equals(code)) {

            // 如果能够对比成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            // 判断当前手机号对应的用户是否为新用户
            if (user == null) {

                // 如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            // 登录成功需要将用户 id 存入 session
            session.setAttribute("user", user.getId());

            // 如果用户登录成功，则删除 redis 中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("登录失败");
    }
}
