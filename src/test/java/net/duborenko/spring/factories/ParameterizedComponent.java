package net.duborenko.spring.factories;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;

public class ParameterizedComponent {

    private final MessageSource messageSource;
    private final BeanFactory beanFactory;
    private final Param param;

    public ParameterizedComponent(MessageSource messageSource,
                                  BeanFactory beanFactory,
                                  @Parameter Param param) {
        this.messageSource = messageSource;
        this.beanFactory = beanFactory;
        this.param = param;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Param getParam() {
        return param;
    }

    @ComponentFactory
    public interface Factory {

        ParameterizedComponent parameterizedComponent(Param param);

    }
}
