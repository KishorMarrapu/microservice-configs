//package com.OTRAS.DemoProject.Config;
// 
//import org.springframework.context.annotation.Configuration;
//
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// 
//@Configuration
//
//public class FileResourceConfig implements WebMvcConfigurer {
// 
//    
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//
//        registry.addResourceHandler("/food/**")
//                .addResourceLocations("file:///C://WZGImages/");
//    }
//
//}
//
// 

package com.OTRAS.DemoProject.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/OTRAS_Demo/**")
                .addResourceLocations("file:/opt/tomcat/uploads/");
    }
}


