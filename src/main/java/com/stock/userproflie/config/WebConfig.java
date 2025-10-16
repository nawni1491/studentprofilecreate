package com.stock.userproflie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    	@Override
      public void addResourceHandlers(ResourceHandlerRegistry registry) {
          // 💡 FIX 3: Map the /uploads/** URL to the external file system folder using 'file:'
          // This tells Spring to look for files in the physical 'user-profile-uploads' directory.
          registry.addResourceHandler("/uploads/**")
                  .addResourceLocations("file:uploads/");
      
          // You may want to keep the default static folder serving for CSS/JS, etc.
          registry.addResourceHandler("/**")
                  .addResourceLocations("classpath:/static/");
    }
}