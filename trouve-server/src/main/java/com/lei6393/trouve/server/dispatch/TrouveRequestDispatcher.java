package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Trouve 请求分发入口，使用方式：
 * <pre>
 * {@code
 * @RestController
 * public class EntranceController {
 *
 *     @RequestMapping("/**")
 *     public void entrance(HttpServletRequest request, HttpServletResponse response) {
 *         try {
 *             TrouveRequestDispatcher.entrance(request, response);
 *         } catch (Throwable e) {
 *             LOGGER.error("entrance error!", e);
 *         }
 *     }
 *
 * }
 * }
 * </pre>
 *
 * @author leiyu
 * @date 2022/5/25 22:08
 */
public class TrouveRequestDispatcher extends AbstractDispatchCenterProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveRequestDispatcher.class);

    /**
     * 入口
     *
     * @param request  源请求
     * @param response 源返回值
     * @throws Throwable
     */
    public static void entrance(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response) throws Throwable {
        try (ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response)) {

            // 构建请求匹配包
            MatchPackage matchPackage = MatchPackage.create(request);

            // 匹配实例
            Instance instance = matchInstance(matchPackage);

            // 组装请求参数
            RequestParam requestParam = assembleRequestParam(request, matchPackage, instance);

            // 执行转发，并返回结果
            ResponseParam responseParam = execute(requestParam, instance);

            // 将结果填入 http response
            flush(httpResponse, responseParam);
        }
    }

}
