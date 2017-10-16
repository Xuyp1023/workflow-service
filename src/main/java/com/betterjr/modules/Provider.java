package com.betterjr.modules;

import java.net.URL;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

public class Provider {

    public static void main(final String[] args) throws Exception {
        Provider.class.getClassLoader();
        final URL url = ClassLoader.getSystemResource("log4j.properties");
        System.out.println(url.toString());
        Log4jConfigurer.initLogging(url.getFile());
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "spring-context-workflow-dubbo-provider.xml" });
        context.start();

        System.in.read();
        context.close();
        System.exit(0);
    }

}
