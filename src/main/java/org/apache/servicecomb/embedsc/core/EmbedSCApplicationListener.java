package org.apache.servicecomb.embedsc.core;

import org.apache.servicecomb.embedsc.client.MDNSServiceRegistryClientImpl;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.function.Function;

@Configuration
@Order(-1001)
public class EmbedSCApplicationListener implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    BeanUtils.setContext(applicationContext);
    ServerRegisterUtil.init();

    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
    Function<ServiceRegistry, ServiceRegistryClient> serviceRegistryClientConstructor =
            serviceRegistry -> new MDNSServiceRegistryClientImpl(serviceRegistryConfig);
    serviceRegistryConfig.setServiceRegistryClientConstructor(serviceRegistryClientConstructor);
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {}
}
