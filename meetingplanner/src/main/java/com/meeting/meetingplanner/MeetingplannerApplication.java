package com.meeting.meetingplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@SpringBootApplication
public class MeetingplannerApplication {

	
	
	public static void main(String[] args) {
		SpringApplication.run(MeetingplannerApplication.class, args);
	}
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/allRooms").allowedOrigins("http://localhost:4200");
				registry.addMapping("/allReservations").allowedOrigins("http://localhost:4200");
				registry.addMapping("/associations").allowedOrigins("http://localhost:4200");
			}
		};
	}
			
}
