package savora.com.savora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.base:uploads}")
    private String uploadBase;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Normalize base path and ensure trailing slash for resource locations
    String base = uploadBase.endsWith("/") ? uploadBase : uploadBase + "/";

    // Map /uploads/** to file system location (optional)
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + base);

    // Mapping for uploaded product images. This exposes files stored in
    // the local folder <uploadBase>/products/ under the URL path /images/products/**
    registry.addResourceHandler("/images/products/**")
        .addResourceLocations("file:" + base + "products/");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}