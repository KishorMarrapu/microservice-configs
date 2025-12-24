////package com.OTRAS.DemoProject.Config;
////
////import java.util.Arrays;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            @Override
////            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/**")
////                      //  .allowedOriginPatterns("http://localhost:5173") 
////                .allowedOrigins(
////                        "http://localhost:5171",
////                        "http://localhost:5172",
////                        "http://localhost:5173",
////                        "http://localhost:5174",
////                        "http://localhost:5175"
////                    )
////                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
////                        .allowedHeaders("*")
////                        .allowCredentials(true);
////            }
////        };
////    }
////}
//
//
//
////package com.OTRAS.DemoProject.Config;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            @Override
////            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/**")
////                        .allowedOrigins(
////                                "http://localhost:5171",
////                                "http://localhost:5172",
////                                "http://localhost:5173",
////                                "http://localhost:5174",
////                                "http://localhost:5175",
////
////                                // ⭐ ADD YOUR WIFI IPv4 FRONTEND
////                                "http://10.10.5.163:5173"
////                        )
////                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
////                        .allowedHeaders("*")
////                        .allowCredentials(true);
////            }
////        };
////    }
////}
//
//
//
////package com.OTRAS.DemoProject.Config;
//// 
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//// 
////@Configuration
////public class CorsConfig {
//// 
////    @Bean
////    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            @Override
////            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/**")
////                        .allowedOrigins(
////                                "http://localhost:5171",
////                                "http://localhost:5172",
////                                "http://localhost:5173",
////                                "http://localhost:5174",
////                                "http://localhost:5175",
////                                
////                                "https://10.102.150.196:3000/",
////                                "https://10.102.150.196:3001/",
////                                "https://10.102.150.196:3002/"
////                                
//// 
////                            
////                        )
////                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
////                        .allowedHeaders("*")
////                        .allowCredentials(true);
////            }
////        };
////    }
////}
// 
////package com.OTRAS.DemoProject.Config;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            @Override
////            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/**")
////                        .allowedOriginPatterns(
////                                "http://localhost:5171",
////                                "http://localhost:5172",
////                                "http://localhost:5173",
////                                "http://localhost:5174",
////                                "http://localhost:5175",
////                                "https://*"
////                        )
////                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
////                        .allowedHeaders("*")
////                        .allowCredentials(true);
////            }
////        };
////    }
////}
//package com.OTRAS.DemoProject.Config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOriginPatterns(
//                                // Local development ports
//                                "http://localhost:5171",
//                                "http://localhost:5172",
//                                "http://localhost:5173",
//                                "http://localhost:5174",
//                                "http://localhost:5175",
//                                
//                                
//
//                                // Cloudflare tunnels
////                                "https://qualifying-profession-cookies-trusts.trycloudflare.com", //user
////                                "https://marketplace-substantial-appliance-detailed.trycloudflare.com", // Admin
////                                "https://hughes-their-limits-subscribe.trycloudflare.com",             // Exam
//                                
//                                "https://front-end-admin-q8ja.vercel.app/",
//                                "https://front-end-exam-phi.vercel.app/",
//                                "https://front-end-user-mg6q.vercel.app/"
//                        )
//                        .allowedMethods("*")
//                        .allowedHeaders("*")
//                        .allowCredentials(true);
//            }
//        };
//    }
//}
//
