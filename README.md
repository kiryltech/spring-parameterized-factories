# Overview
This library provides Spring equivalent of Guice's 
[assisted injection](https://github.com/google/guice/wiki/AssistedInject).

# Usage

Add annotation `@EnableParameterizedFactories` to configuration class:
```java
@Configuration
@ComponentScan
@EnableParameterizedFactories
public static class SpringConfiguration {
}
```

Create `Factory` interface in parameterized bean annotated with @ComponentFactory. Mark parameters 
in constructor with annotation `@Parameter`.
```java
public class ParameterizedComponent {

    ...

    public ParameterizedComponent(AnotherBean anotherBean,
                                  ...,
                                  @Parameter Param param) {
        ...
    }

    @ComponentFactory
    public interface Factory {

        ParameterizedComponent parameterizedComponent(Param param);

    }
    
}
```

Now Spring will create an instance of `Factory` with dynamic implementation of arguments injection.
