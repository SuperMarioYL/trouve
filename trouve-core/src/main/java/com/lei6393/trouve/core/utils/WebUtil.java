package com.lei6393.trouve.core.utils;

import com.lei6393.trouve.core.exception.TrouveException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yulei
 * @date 2022/5/23 10:37
 */
public class WebUtil {

    private static final String ENCODING_KEY = "encoding";

    /**
     * get target value from parameterMap, if not found will throw {@link IllegalArgumentException}.
     *
     * @param req {@link HttpServletRequest}
     * @param key key
     * @return value
     */
    public static String required(final HttpServletRequest req, final String key) {
        String value = req.getParameter(key);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Param '" + key + "' is required.");
        }
        String encoding = req.getParameter(ENCODING_KEY);
        return resolveValue(value, encoding);
    }


    /**
     * get target value from parameterMap, if not found will return default value.
     *
     * @param req          {@link HttpServletRequest}
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    public static String optional(final HttpServletRequest req, final String key, final String defaultValue) {
        if (!req.getParameterMap().containsKey(key) || req.getParameterMap().get(key)[0] == null) {
            return defaultValue;
        }
        String value = req.getParameter(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        String encoding = req.getParameter(ENCODING_KEY);
        return resolveValue(value, encoding);
    }


    /**
     * decode target value.
     *
     * @param value    value
     * @param encoding encode
     * @return Decoded data
     */
    private static String resolveValue(String value, String encoding) {
        if (StringUtils.isEmpty(encoding)) {
            encoding = StandardCharsets.UTF_8.name();
        }
        try {
            value = new String(value.getBytes(StandardCharsets.UTF_8), encoding);
        } catch (UnsupportedEncodingException ignore) {
        }
        return value.trim();
    }

    public static Map<String, String> parseMetadata(String metadata) throws TrouveException {

        Map<String, String> metadataMap = new HashMap<>(16);

        if (StringUtils.isBlank(metadata)) {
            return metadataMap;
        }

        try {
            Type type = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
            metadataMap = GsonUtil.INSTANCE.fromJson(metadata, type);
        } catch (Exception e) {
            String[] datas = metadata.split(",");
            if (datas.length > 0) {
                for (String data : datas) {
                    String[] kv = data.split("=");
                    if (kv.length != 2) {
                        throw new TrouveException("metadata format incorrect:" + metadata);
                    }
                    metadataMap.put(kv[0], kv[1]);
                }
            }
        }

        return metadataMap;
    }


}
