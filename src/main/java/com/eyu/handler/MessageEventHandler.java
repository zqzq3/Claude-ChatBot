package com.eyu.handler;

import com.eyu.entity.bo.ChatBO;
import com.eyu.exception.ChatException;
import com.eyu.service.InteractService;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MessageTooLargeException;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * 事件处理
 *
 * @author zqzq3
 * @date 2023/2/1
 */
@Component
public class MessageEventHandler implements ListenerHost {
    @Resource
    private InteractService interactService;

    private static final String RESET_WORD = "重置会话";

    private static final String RESET_ALL_WORD = "RESET ALL";

    /**
     * 监听消息并把ChatGPT的回答发送到对应qq/群
     * 注：如果是在群聊则需@
     *
     * @param event 事件 ps:此处是MessageEvent 故所有的消息事件都会被监听
     */
    @EventHandler
    public void onMessage(@NotNull MessageEvent event){
        boolean flag = decide(event.getMessage().contentToString());
        if(flag){
            return;
        }
        ChatBO chatBO = new ChatBO();
        chatBO.setSessionId(String.valueOf(event.getSubject().getId()));
        if (event.getBot().getGroups().contains(event.getSubject().getId())) {
            //如果是在群聊
            if (event.getMessage().contains(new At(event.getBot().getId()))) {
                chatBO.setSessionId(String.valueOf(event.getSender().getId()));
                //存在@机器人的消息就向ChatGPT提问
                //去除@再提问
                String prompt = event.getMessage().contentToString().replace("@" + event.getBot().getId(), "").trim();
                response(event, chatBO, prompt);
            }
        } else {
            //不是在群聊 则直接回复
            String prompt = event.getMessage().contentToString().trim();
            response(event, chatBO, prompt);
        }
    }

    private boolean decide(String str) {
        //此处可以加屏蔽字 来屏蔽一些不想回复的信息
        return false;
    }

    private void response(@NotNull MessageEvent event, ChatBO chatBO, String prompt) {

        CompletableFuture<String> future;
        try {
            chatBO.setPrompt(prompt);

            future = interactService.chat(chatBO, "");

            // 处理获取到的结果
            future.thenAccept(response -> {
                // 处理获取到的结果
                try {
                    MessageChain messages = new MessageChainBuilder()
                            .append(new QuoteReply(event.getMessage()))
                            .append(response)
                            .build();
                    event.getSubject().sendMessage(messages);
                }catch (MessageTooLargeException e){
                    //信息太大，无法引用，采用直接回复
                    event.getSubject().sendMessage(response);
                }
            });

            // 处理异常
            future.exceptionally(e -> {
                // 处理异常
                MessageChain messages = new MessageChainBuilder()
                        .append(new QuoteReply(event.getMessage()))
                        .append(e.getMessage())
                        .build();
                event.getSubject().sendMessage(messages);
                return null;
            });

        }catch (ChatException e){
            MessageChain messages = new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(e.getMessage())
                    .build();
            event.getSubject().sendMessage(messages);
        }
    }

    public String getImageId(Contact contact, String urlLink) throws IOException {
        URL url = new URL(urlLink);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream is = url.openStream()) {
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        byte[] imageData = baos.toByteArray();
        ExternalResource resource;
        resource = ExternalResource.create(imageData);
        contact.uploadImage(resource);
        String result = resource.calculateResourceId();
        resource.close();
        return result;
    }
}