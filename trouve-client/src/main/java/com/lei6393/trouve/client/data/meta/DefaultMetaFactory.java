package com.lei6393.trouve.client.data.meta;

import com.lei6393.trouve.client.api.ExposeApiRegistry;
import com.lei6393.trouve.client.data.instance.InstanceFactory;
import com.lei6393.trouve.core.MetaConstants;
import com.lei6393.trouve.core.data.MetaMsg;
import com.lei6393.trouve.core.utils.GsonUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author leiyu
 * @date 2022/5/24 23:57
 */
public class DefaultMetaFactory implements MetaFactory, ApplicationContextAware {

    private InstanceFactory factory;

    @Override
    public MetaMsg create() {
        MetaMsg metaMsg = new MetaMsg(factory.create());
        metaMsg.addMetadataElement(
                MetaConstants.URI_INFOS,
                GsonUtil.INSTANCE.toJson(ExposeApiRegistry.getRequestInfoSet())
        );
        return metaMsg;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        factory = applicationContext.getBean(InstanceFactory.class);
    }
}
