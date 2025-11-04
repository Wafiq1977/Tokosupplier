package savora.com.savora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    public void configureResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to file system location
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Alternative mapping for uploaded images
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:uploads/products/");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}