package myserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
public class MyServer {
	public static HashMap<String, String> users = new HashMap<>();

	public static void main(String[] args) {
		SpringApplication.run(MyServer.class, args);
	}
}