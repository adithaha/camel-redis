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
    	rest("/redis/")
	        .get("/{key}").to("direct:get")
	        .get("/{key}/{value}").to("direct:set");
    	
    	from("timer:hello?period={{timer.period}}").routeId("hello")
            .log("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .setHeader("key").simple("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
		    .setHeader("value").simple("value-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .to("direct:set")
            .to("direct:get")
		    .log("${body}");
    	
    	from("direct:get")
	    	.setHeader(RedisConstants.COMMAND).simple("GET")
		    .setHeader(RedisConstants.KEY).simple("${header.key}")
		    .to("spring-redis://{{redis.host}}");
    	
    	from("direct:set")
	    	.setHeader(RedisConstants.COMMAND).simple("SET")
		    .setHeader(RedisConstants.KEY).simple("${header.key}")
		    .setHeader(RedisConstants.VALUE).simple("${header.value}")
            .to("spring-redis://{{redis.host}}");
	
        
//        from("spring-redis://localhost:6379?command=SUBSCRIBE&channels=testChannel")
//        	.log("${body}");
            
    }

}
