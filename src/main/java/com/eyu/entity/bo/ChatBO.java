package com.eyu.entity.bo;

import lombok.Data;

/**
 * 聊天BO
 *
 * @author zqzq3
 * @date 2023/02/01
 */
@Data
public class ChatBO {
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 问题
     */
    private String prompt;
}
