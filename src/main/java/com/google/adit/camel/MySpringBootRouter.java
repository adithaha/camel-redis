package com.google.adit.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
	        .get("/single/{key}").to("direct:get")
	        .get("/single/{key}/{value}").to("direct:set")
	    	.get("/loop/{loop}/{key}/{value}").to("direct:loop");
	
    	from("timer:hello?period={{timer.period}}").routeId("hello").autoStartup(false)
            .log("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .setHeader("key").simple("key-${exchangeProperty.CamelTimerFiredTime.getTime}")
		    .setHeader("value").simple("value-${exchangeProperty.CamelTimerFiredTime.getTime}")
            .to("direct:set")
            .to("direct:get")
		    .log("${body}");
    	
    	from("direct:loop")
    		.setProperty("key").simple("${header.key}")
    		.setProperty("value").simple("${header.value}")
    		.loop(simple("${header.loop}"))
    			.setProperty("timestamp").simple("${date:now:yyyyMMddHHmmssSSS}")
    			.setHeader("key").simple("${exchangeProperty.key}-${exchangeProperty.timestamp}")
    			.setHeader("value").simple("${exchangeProperty.value}-${exchangeProperty.timestamp}")
    			.to("direct:set");
	
    	
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
