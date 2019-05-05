package myserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.ArrayList;

@SpringBootApplication
public class MyServer {
	public static HashMap<String, User> tokenHashmap = new HashMap<>();
	public static ArrayList<User> tokensArrayList = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(MyServer.class, args);

		for(int i = 0; i < tokensArrayList.size(); i++){
			System.out.println(tokensArrayList.get(i));
		}
	}
}

class User{
	String username;
	String token;
	User(String username, String token){
		this.username = username;
		this.token = token;
	}
}
