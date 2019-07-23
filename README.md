### springboot邮箱发送

pom.xml 需要添加email和thymeleaf，如下：
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

本项目是忘记密码后的处理操作的demo，包含邮件的发送。

需要把application.yml的邮件账号和密码修改为自己的邮箱



