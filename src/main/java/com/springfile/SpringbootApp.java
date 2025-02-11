package com.springfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class SpringbootApp {
	
	private static final Logger log= LoggerFactory.getLogger(SpringbootApp.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringbootApp.class,args);
	}

}
