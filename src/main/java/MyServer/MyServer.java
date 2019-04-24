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
	}
}

class User{
	int userID;
	String username;
	String token;
	User(int userID, String username, String token){
		this.userID = userID;
		this.username = username;
		this.token = token;
	}
}
