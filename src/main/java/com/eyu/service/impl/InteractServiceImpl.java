package com.eyu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eyu.entity.bo.ChatBO;
import com.eyu.exception.ChatException;
import com.eyu.handler.ChatCompletionCallback;
import com.eyu.handler.RedisRateLimiter;
import com.eyu.service.InteractService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 交互服务impl
 *
 * @author zqzq3
 * @date 2022/12/10
 */
@Service
@Slf4j
public class InteractServiceImpl implements InteractService {
    @Autowired
    RedisRateLimiter rateLimiter;

    private OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(2, 120, TimeUnit.SECONDS))
            .build();

    @Override
    public CompletableFuture<String> chat(ChatBO chatBO, String systemPrompt) throws ChatException {

        String prompt = chatBO.getPrompt();

        //向gpt提问
        CompletableFuture<String> future = new CompletableFuture<>();
        ChatCompletionCallback callback = new ChatCompletionCallback() {
            @Override
            public void onCompletion(String response) {
                future.complete(response);
            }

            @Override
            public void onError(ChatException chatException) {
                future.completeExceptionally(chatException);
            }
        };
        try {
            if ("重置会话".equals(prompt)) {
                reset(callback);
            } else {
                getAnswer(prompt, callback);
            }
        } catch (InterruptedException e) {
            throw new ChatException("我麻了 稍后再试下吧");
        }
        return future;
    }

    public void getAnswer(String prompt, ChatCompletionCallback callback) throws InterruptedException {
        String content = "";
        if (client == null) {
            client = new OkHttpClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(2, 120, TimeUnit.SECONDS))
                    .build();
        }
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject obj = new JSONObject();
        obj.put("prompt", prompt + "(回答不要超过200字)");
        RequestBody body = RequestBody.create(mediaType, obj.toJSONString());
        int retryCount = 0;
        boolean success = false;
        while (!success && retryCount < 2) { // 最多重试2次
            try {
                Request request = new Request.Builder()
                        .url("http://127.0.0.1:8088/claude/chat")
                        .method("POST", body)
                        .addHeader("accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseStr = responseBody.string();
                    JSONObject jsonObject = JSONObject.parseObject(responseStr);
                    content = jsonObject.getString("claude");
                    callback.onCompletion(content);
                }
                success = true; // 成功获取到答案，退出重试
            } catch (Exception e) {
                Thread.sleep(3000);
                retryCount++;

            }
        }

        if (!success || StringUtils.isEmpty(content)){
            callback.onError(new ChatException("我无了 稍后再试下吧"));
        }
    }

    public void reset(ChatCompletionCallback callback) throws InterruptedException {
        String content = "";
        if (client == null) {
            client = new OkHttpClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(4, 120, TimeUnit.SECONDS))
                    .build();
        }
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{}");
        int retryCount = 0;
        boolean success = false;
        while (!success && retryCount < 2) { // 最多重试2次
            try {
                Request request = new Request.Builder()
                        .url("http://127.0.0.1:8088/claude/reset")
                        .method("POST", body)
                        .addHeader("accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseStr = responseBody.string();
                    JSONObject jsonObject = JSONObject.parseObject(responseStr);
                    content = jsonObject.getString("claude");
                    callback.onCompletion(content);
                }
                success = true; // 成功获取到答案，退出重试
            } catch (Exception e) {
                Thread.sleep(3000);
                retryCount++;
            }
        }

        if (!success || StringUtils.isEmpty(content)){
            callback.onError(new ChatException("我无了 稍后再试下吧"));
        }
    }

    @Override
    public void setUniquePrompt(String sessionId, String prompt){
        rateLimiter.setPrompt(sessionId, prompt);
    }

    @Override
    public String getUniquePrompt(String sessionId){
        return rateLimiter.getPrompt(sessionId);
    }

}
