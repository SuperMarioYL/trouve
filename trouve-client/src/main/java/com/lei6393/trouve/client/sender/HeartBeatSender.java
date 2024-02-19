package com.lei6393.trouve.client.sender;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.core.connection.CenterURI;
import com.lei6393.trouve.core.data.instance.Instance;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author yulei
 * @date 2022/5/11 20:55
 */
public class HeartBeatSender extends AbstractSender<Instance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatSender.class);

    public HeartBeatSender(List<ServerAddressParam> addressParams) {
        super(addressParams, CenterURI.INSTANCE);
    }

    @Override
    protected boolean realRegister(Instance instance) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : instance.mapping().entrySet()) {
            if (StringUtils.isNoneBlank(entry.getKey(), entry.getValue())) {
                bodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(realUrl())
                .post(bodyBuilder.build())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected boolean realUpdate(Instance instance) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : instance.mapping().entrySet()) {
            if (StringUtils.isNoneBlank(entry.getKey(), entry.getValue())) {
                bodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(realUrl())
                .put(bodyBuilder.build())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected boolean realRemove(Instance instance) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : instance.mapping().entrySet()) {
            if (StringUtils.isNoneBlank(entry.getKey(), entry.getValue())) {
                bodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(realUrl())
                .delete(bodyBuilder.build())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }


}
