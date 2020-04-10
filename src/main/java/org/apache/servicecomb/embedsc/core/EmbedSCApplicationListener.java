package org.apache.servicecomb.embedsc.core;

import org.apache.servicecomb.embedsc.client.MDNSServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import org.springframework.core.Ordered;

public class EmbedSCApplicationListener
    implements ApplicationListener<ApplicationEvent>, Ordered, ApplicationContextAware {
  private Class<?> initEventClass = ContextRefreshedEvent.class;

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
    serviceRegistryConfig.setServiceRegistryClientConstructor(serviceRegistry -> new MDNSServiceRegistryClientImpl(serviceRegistryConfig));
  }

  @Override
  public int getOrder() {
    // should run before CseApplicationListener, CseApplicationListener wit order number:-1000
    return -1001;
  }

  public void setInitEventClass(Class<?> initEventClass) {
    this.initEventClass = initEventClass;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {}
}
