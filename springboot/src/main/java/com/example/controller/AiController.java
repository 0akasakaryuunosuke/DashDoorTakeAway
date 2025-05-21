package com.example.controller;
// 建议dashscope SDK的版本 >= 2.15.0
import com.alibaba.dashscope.app.*;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;// 流式输出

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@RestController
@Component
@RequestMapping("/ai")
public class AiController {



    private final Application application;
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);
    private final String dashscopeApiKey;
    private final String dashscopeAppId;

    public AiController(Application application,
                        @Value("${dashscope.api-key}") String dashscopeApiKey,
                        @Value("${dashscope.app-id}")  String dashscopeAppId)  {

        this.dashscopeApiKey = dashscopeApiKey;
        this.dashscopeAppId  = dashscopeAppId;
        this.application = application;

/*        logger.info("DashScope API Key injected: {}", dashscopeApiKey);
        logger.info("DashScope App ID  injected: {}", dashscopeAppId);*/

    }

    @GetMapping(
            value    = "/assistant-stream",
            produces = {
                    MediaType.TEXT_EVENT_STREAM_VALUE
            }
    )
    public SseEmitter assistantStream(
            @RequestParam String question,
            @RequestHeader(value = "API-Key", required = false) String apiKey) {

        // 0L 表示永不超时，可以根据需要设置具体毫秒
        SseEmitter emitter = new SseEmitter(0L);

        // 把调用推到单独的线程，避免堵塞 Servlet 容器的 IO 线程

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                // header 里有的话优先用 header，否则用配置文件里的
                String finalApiKey = dashscopeApiKey;

                ApplicationParam param = ApplicationParam.builder()
                        .apiKey(finalApiKey)
                        .appId(dashscopeAppId)
                        .prompt(question)
                        .ragOptions(RagOptions.builder()
                                // 替换为实际指定的知识库ID，逗号隔开多个
                                .pipelineIds(Collections.singletonList("167sdbz2ql"))
                                .build())
                        .incrementalOutput(true)
                        .build();

                application.streamCall(param)
                        .blockingForEach(chunk -> {
                            emitter.send(SseEmitter.event()
                                    .data(chunk.getOutput().getText()));
                        });

                emitter.complete();
            } catch (NoApiKeyException e) {
                logger.error("API Key missing", e);
                emitter.completeWithError(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "API Key Required"));
            } catch (ApiException e) {
                // 重点修改这里
                String errorMessage = e.getMessage();
                logger.error("DashScope API 错误: {}", errorMessage, e); // 记录完整异常堆栈
                emitter.completeWithError(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "服务错误: " + (errorMessage != null ? errorMessage : "未知错误")
                ));
            } catch (Exception e) {
                logger.error("Unexpected error", e);
                emitter.completeWithError(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
            }
        });

        return emitter;
    }
}
