package org.chenjingbin.springboot.email.demo.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.chenjingbin.springboot.email.demo.common.AESUtil;
import org.chenjingbin.springboot.email.demo.service.EmailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class UpdatePasswordHtmlController {
    private Map<String,Boolean> map = new HashMap<String,Boolean>();

    @Autowired
    private EmailService emailService;
    /**
     * 点击修改密码邮件中的链接，跳转到本方法，
     * 1（签名校验和时间判断）
     * 2 对tid解密得到userid，隐藏到页面中去对后面的操作
     * @param tid userid加密后的字符串
     * @param currentTime 生成邮件的时间
     * @param sign 签名
     * @return
     */
    @GetMapping("/update/password")
    public ModelAndView updatePassword(String tid, String currentTime, String sign, HttpServletRequest request){
        //先对sign进行校验
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currentTime",currentTime);
        jsonObject.put("tid",tid);
        if(!sign.equals(AESUtil.encrypt(jsonObject.toJSONString(),"gdqt"))){
            return new ModelAndView("error").addObject("message","sign无效");
        }
        //有效时间为12小时
        long exipre = 12*60*60*1000;
        if(System.currentTimeMillis()-Long.valueOf(currentTime)>exipre){
            return new ModelAndView("error").addObject("message","超过12小时，修改无效");
        }
        try{
           Long user_id = Long.valueOf(AESUtil.decrypt(tid,"gdqt"));
           String uuid = UUID.randomUUID().toString();
           request.getSession().setAttribute("uuid",uuid);
           return new ModelAndView("updatePassword").addObject("userId",user_id).addObject("uuid",uuid);
        }catch (Exception e){
            return new ModelAndView("error").addObject("message","修改账号无效");
        }
    }

    /**
     * 忘记密码页面，输入邮箱号后执行的操作。
     * 1校验邮箱是否为空，
     * 2发送请求到qtsso做校验
     * 3对qtsso返回的数据判断操作是否成功
     * @param email 邮箱号
     * @return
     */
    @PostMapping("/forgetSubmit")
    @ResponseBody
    public String forgetSubmit(String email){
        if(StringUtils.isNotBlank(email)){
            /** TODO 根据邮箱查询用户的操作，此处需要通过别的系统获取用户信息**/
//            if (user.size() != 1) {
//                return "该邮箱没有注册";
//            }
            //用户id加密
            String idEncrypt = AESUtil.encrypt(  "100010", "gdqt");
            Map map = new HashMap<String, String>();
            //发送邮件操作
            try {
                /**给篡改后解密会出现javax.crypto.BadPaddingException异常*/
                long currentTime = System.currentTimeMillis();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("currentTime",currentTime+"");
                jsonObject.put("tid",idEncrypt);
                String sign = AESUtil.encrypt(jsonObject.toJSONString(),"gdqt");
                map.put("path", "http://127.0.0.1:7878/update/password?tid=" + idEncrypt+"&currentTime="+currentTime+"&sign="+sign);
                emailService.sendTemplateHtmlEmail(email, "修改密码", "register.html", map);
                return "操作成功";
            }catch (Exception e){
            }
        }
        return "操作失败";
    }
    /**
     * 跳转到页面
     */
    @GetMapping("/register")
    public ModelAndView register(){
        return new ModelAndView("register");
    }
    /**
     * 跳转到页面
     */
    @GetMapping("/forget")
    public String forget(){
        return "forget";
    }

    /**
     * updatePassword.html页面，用户修改密码点击提交后的处理，结合updatePassword()方法随机的uuid做校验，先qtsso发送真正的修改密码请求。
     * 1判断uuid 是否存在与session一致
     * 2使用restTemple转发请求，判断最后执行是否成功
     * @param updatePasswordBody
     * @param request
     * @return
     */
    @PostMapping("/updatePasswordSubmit")
    public String updatePasswordSubmit(UpdatePasswordBody updatePasswordBody, HttpServletRequest request){

        Object object = request.getSession().getAttribute("uuid");
        if( object == null || !(object instanceof String)){
            return "error";
        }
        PasswordForm passwordForm = new PasswordForm();
        BeanUtils.copyProperties(updatePasswordBody,passwordForm);
        RestTemplate restTemplate = new RestTemplate();
        /** TODO 先qtsso发送真正的修改密码请求*/
        //R result = restTemplate.postForObject(ssoUrlHead+"sso/user/forgetPassword",passwordForm, R.class);
//        if((int)result.get("code") != 200){
//            return "error";
//        }
        return "success";
    }
    @Data
    @ToString
    static class UpdatePasswordBody implements Serializable{
        /**
         * 用户id
         */
        @NotNull(message = "id不能为空")
        private Long id;
        /**
         * 原密码
         */
        @NotNull(message = "不能为空")
        private String password;
        /**
         * 随机uuid
         */
        @NotNull(message = "不能为空")
        private String uuid;
        /**
         * 新密码
         */
        @NotNull(message = "新密码不能为空")
        private String newPassword;
    }
    @Data
    @ToString
    static class PasswordForm implements Serializable {
        /**
         * 用户id
         */
        private Long id;
        /**
         * 原密码
         */
        private String password;
        /**
         * 新密码
         */
        @NotNull(message = "新密码不能为空")
        private String newPassword;

    }
}
