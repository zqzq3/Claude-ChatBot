package com.eyu.config;

import com.eyu.handler.MessageEventHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 帐户配置
 *
 * @author zqzq3
 * @date 2023/02/13
 */
@Slf4j
@Data
@Component
@ConfigurationProperties("account")
public class AccountConfig {
    private Long qq;
    private String password;
    private Bot qqBot;
    @Resource
    private MessageEventHandler messageEventHandler;

    @PostConstruct
    public void init() {
        FixProtocolVersion.fix();
        //qq
        //登录
        BotConfiguration.MiraiProtocol[] protocolArray = BotConfiguration.MiraiProtocol.values();
        BotConfiguration.MiraiProtocol protocol = protocolArray[2];
//        int loginCounts = 1;
//        for (BotConfiguration.MiraiProtocol protocol : miraiProtocols) {
//            try {
//                log.warn("正在尝试第 {} 次， 使用 {} 的方式进行登录", loginCounts++, protocol);
//                qqBot = BotFactory.INSTANCE.newBot(qq, password.trim(), new BotConfiguration(){{setProtocol(protocol);}});
//                qqBot.login();
//                log.info("成功登录账号为 {} 的qq, 登陆方式为 {}",qq, protocol);
//                //订阅监听事件
//                qqBot.getEventChannel().registerListenerHost(this.messageEventHandler);
//                break;
//            }catch (Exception e){
//                log.error("登陆失败，qq账号为 {}, 登陆方式为 {} ，原因：{}", qq, protocol, e.getMessage());
//                if (loginCounts > 3){
//                    log.error("经过多种登录方式仍然登陆失败，可能是密码错误或者受风控影响，请尝试修改密码、绑定手机号等方式提高qq安全系数或者待会再试试");
//                    System.exit(-1);
//                }
//            }
//        }
        int loginCounts = 1;
        for (int i = 0; i < 3; i++) {
            try {
                log.warn("正在尝试第 {} 次， 使用 {} 的方式进行登录", loginCounts++, protocol);
                // 密码登录
                // qqBot = BotFactory.INSTANCE.newBot(qq, password.trim(), new BotConfiguration(){{setProtocol(protocol);}});
                qqBot = BotFactory.INSTANCE.newBot(qq, BotAuthorization.byQRCode(), new BotConfiguration(){{setProtocol(protocol);}});
                qqBot.login();
                log.info("成功登录账号为 {} 的qq, 登陆方式为 {}",qq, protocol);
                //订阅监听事件
                qqBot.getEventChannel().registerListenerHost(this.messageEventHandler);
                break;
            }catch (Exception e){
                log.error("登陆失败，qq账号为 {}, 登陆方式为 {} ，原因：{}", qq, protocol, e.getMessage());
                if (loginCounts > 3){
                    log.error("经过多种登录方式仍然登陆失败，可能是密码错误或者受风控影响，请尝试修改密码、绑定手机号等方式提高qq安全系数或者待会再试试");
                    System.exit(-1);
                }
            }
        }
    }
}
