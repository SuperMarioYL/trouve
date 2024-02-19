package com.lei6393.trouve.core.utils;

import com.google.gson.Gson;

/**
 * @author leiyu
 * @date 2022/5/19 17:26
 */
public class GsonUtil {

    public static final Gson INSTANCE= new Gson()
            .newBuilder()
            .create();
}
