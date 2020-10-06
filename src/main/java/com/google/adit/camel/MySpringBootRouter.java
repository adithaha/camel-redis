package com.google.adit.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("timer:hello?period={{timer.period}}").routeId("hello")
            .transform().method("myBean", "saySomething")
            .log("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .setHeader(RedisConstants.COMMAND).simple("SET")
            .setHeader(RedisConstants.KEY).simple("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .setHeader(RedisConstants.VALUE).simple("value-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .to("spring-redis://{{redis.host}}")
		    .setHeader(RedisConstants.COMMAND).simple("GET")
		    .setHeader(RedisConstants.KEY).simple("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
		    .to("spring-redis://{{redis.host}}")
		    .log("${body}");
        
        from("spring-redis://localhost:6379?command=SUBSCRIBE&channels=testChannel")
        	.log("${body}");
            
    }

}
