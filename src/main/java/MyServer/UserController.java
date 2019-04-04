package myserver;

import org.springframework.web.bind.annotation.*; 
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import org.json.JSONArray;

@RestController
public class UserController {
	@RequestMapping(value = "/register", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> register(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); //Grabbing name and age parameters from URL
		String password = payloadObj.getString("password");

		/*Creating http headers object to place into response entity the server will return.
		This is what allows us to set the content-type to application/json or any other content-type
		we would want to return */
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		
		MessageDigest digest = null;
		String hashedKey = null;
		
		hashedKey = BCrypt.hashpw(password, BCrypt.gensalt());

    	if (!MyServer.users.containsKey(username)) {
			MyServer.users.put(username, hashedKey);
		}else {
			JSONObject responseObj = new JSONObject();
			responseObj.put("message", "username taken");
			return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
		}
		//Returns the response with a String, headers, and HTTP status
		JSONObject responseObj = new JSONObject();
		responseObj.put("username", username);
		responseObj.put("message", "user registered");
		return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);
	}
	@RequestMapping(value = "/login", method = RequestMethod.GET) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> login(HttpServletRequest request) {
		String username = request.getParameter("username"); //Grabbing name and age parameters from URL
		String password = request.getParameter("password");

		/*Creating http headers object to place into response entity the server will return.
		This is what allows us to set the content-type to application/json or any other content-type
		we would want to return */
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		
		MessageDigest digest = null;
		String hashedKey = null;

    	if (!MyServer.users.containsKey(username)) {
			return new ResponseEntity("{\"message\":\"username not registered\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}else {
			String storedHashedKey = MyServer.users.get(username);

			if (BCrypt.checkpw(password, storedHashedKey)) {
				return new ResponseEntity("{\"message\":\"user logged in\"}", responseHeaders, HttpStatus.OK);
			}else {
				return new ResponseEntity("{\"message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
			}
		}
	}

	@RequestMapping(value = "/connectToDB", method = RequestMethod.GET) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> connectToDB(HttpServletRequest request) {
		String nameToPull = request.getParameter("firstname");
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();
	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/classdb?useUnicode=true&characterEncoding=UTF-8", "root", "password");
			String query = "SELECT userid, firstName FROM users WHERE firstname=?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, nameToPull);
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            String name = rs.getString("firstName");
	            int userID = rs.getInt("userid");

	            JSONObject obj = new JSONObject();
	            obj.put("name", name);
	            obj.put("userID", userID);
	            usersArray.put(obj);
	        }
	    } catch (SQLException e ) {
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	public static String bytesToHex(byte[] in) {
		StringBuilder builder = new StringBuilder();
		for(byte b: in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}