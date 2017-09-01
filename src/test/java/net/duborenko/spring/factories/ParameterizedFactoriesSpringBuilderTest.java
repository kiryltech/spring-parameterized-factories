package net.duborenko.spring.factories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParameterizedFactoriesSpringBuilderTest.SpringConfiguration.class)
public class ParameterizedFactoriesSpringBuilderTest {

    @Autowired
    private ParameterizedComponent.Factory parameterizedComponentFactory;

    @Configuration
    @ComponentScan
    @EnableParameterizedFactories
    public static class SpringConfiguration {
    }

    @Test
    public void validateFactory() {
        ParameterizedComponent parameterizedComponent = parameterizedComponentFactory.parameterizedComponent(Param.TWO);

        assertThat(parameterizedComponent).isNotNull();
        assertThat(parameterizedComponent.getBeanFactory()).isNotNull();
        assertThat(parameterizedComponent.getMessageSource()).isNotNull();
        assertThat(parameterizedComponent.getParam()).isEqualTo(Param.TWO);
    }

}