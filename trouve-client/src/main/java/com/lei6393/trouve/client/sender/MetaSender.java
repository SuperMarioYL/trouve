package com.lei6393.trouve.client.sender;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.core.connection.CenterURI;
import com.lei6393.trouve.core.data.MetaMsg;
import com.lei6393.trouve.core.utils.GsonUtil;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author yulei
 * @date 2022/5/11 20:55
 */
public class MetaSender extends AbstractSender<MetaMsg> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaSender.class);

    public MetaSender(List<ServerAddressParam> addressParams) {
        super(addressParams, CenterURI.META);
    }

    @Override
    protected boolean realRegister(MetaMsg metaMsg) {
        Request request = new Request.Builder()
                .url(realUrl())
                .post(RequestBody.create(GsonUtil.INSTANCE.toJson(metaMsg),
                        MediaType.get(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)))
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected boolean realUpdate(MetaMsg metaMsg) {
        Request request = new Request.Builder()
                .url(realUrl())
                .put(RequestBody.create(GsonUtil.INSTANCE.toJson(metaMsg),
                        MediaType.get(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)))
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected boolean realRemove(MetaMsg metaMsg) {
        Request request = new Request.Builder()
                .url(realUrl())
                .delete(RequestBody.create(GsonUtil.INSTANCE.toJson(metaMsg),
                        MediaType.get(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)))
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }


}
