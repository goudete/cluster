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
import java.util.ArrayList;
import java.util.Random;
import java.nio.charset.Charset;

@RestController
public class UserController {
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> register(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username");
		String password = payloadObj.getString("password");
		String hashedKey = null;
		hashedKey = BCrypt.hashpw(password, BCrypt.gensalt());

		HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.set("Content-Type", "application/json");

			Connection conn = null;
		    try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
				String query = "INSERT INTO clusterDB.users (username, password)"
					+ " VALUES (?,?)";
				PreparedStatement stmt = null;
		        stmt = conn.prepareStatement(query);
		        stmt.setString(1, username);
						stmt.setString(2, hashedKey);
		        int rs = stmt.executeUpdate();

		    } catch (SQLException e ) {
					return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
		    } finally {
		    	try {
		    		if (conn != null) { conn.close(); }
		    	}catch(SQLException se) {

		    	}
		    }
			return new ResponseEntity(payloadObj.toString(), responseHeaders, HttpStatus.OK);
	 }

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ResponseEntity<String> login(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username");
		String password = payloadObj.getString("password");
		int userID = payloadObj.getInt("userID");

		HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONObject responseObject = new JSONObject();
			try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
			String query = "SELECT password FROM clusterDB.users WHERE username = " + "\'" + username + "\'";
			PreparedStatement stmt = null;
					stmt = conn.prepareStatement(query);
					ResultSet rs = stmt.executeQuery();

					while(rs.next()){
					String returnedPassword = rs.getString("password");
					if(BCrypt.checkpw(password, returnedPassword)){
						String token = generateRandomString(10);
						User user = new User(userID, username, token);

						if(MyServer.tokensArrayList.size() == 100){
							MyServer.tokensArrayList.remove(99);
							MyServer.tokenHashmap.remove(username);
							//send user back to login screen bc token has expired
						}

						MyServer.tokensArrayList.add(0, user);
						MyServer.tokenHashmap.put(username, user);

						responseObject.put("token", token);
						responseObject.put("message", "user logged in");
						return new ResponseEntity(responseObject.toString(), responseHeaders, HttpStatus.OK);
					}
					else{
						return new ResponseEntity("{\"message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
					}
				}
			} catch (SQLException e ) {
				return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
			} finally {
				try {
					if (conn != null) { conn.close(); }
				}catch(SQLException se) {
					return new ResponseEntity(se.toString(), responseHeaders,HttpStatus.BAD_REQUEST);
				}
			}
		return new ResponseEntity("{\"message\":\"dude, something went wrong\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/addLocation", method = RequestMethod.POST)
	public ResponseEntity<String> addLocation(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username");
		String name = payloadObj.getString("name");
		String address = payloadObj.getString("address");
		String lat = payloadObj.getString("lat");
		String lng = payloadObj.getString("lng");
		String type = payloadObj.getString("type");
		String token = payloadObj.getString("token");

		HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.set("Content-Type", "application/json");

		Connection conn = null;

		if (!validateToken(username, token)) {
			return new ResponseEntity("{\"message\":\"username/Bad token\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}else {

			try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
			String query = "INSERT INTO clusterDB.locations (username, name, address, lat, lng, type)"
				+ " VALUES (?,?,?,?,?,?)";
			PreparedStatement stmt = null;
					stmt = conn.prepareStatement(query);
					stmt.setString(1, username);
					stmt.setString(2, name);
					stmt.setString(3, address);
					stmt.setString(4, lat);
					stmt.setString(5, lng);
					stmt.setString(6,type);
					int rs = stmt.executeUpdate();

			} catch (SQLException e ) {
				return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
			} finally {
				try {
					if (conn != null) { conn.close(); }
				}catch(SQLException se) {

				}
			}
		return new ResponseEntity(payloadObj.toString(), responseHeaders, HttpStatus.OK);

		}
 }


	@RequestMapping(value = "/friendConnection", method = RequestMethod.POST)
	public ResponseEntity<String> friendConnection(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username");
		String token = payloadObj.getString("token");
		String requester = payloadObj.getString("username");
		String requestee = payloadObj.getString("requestee");

		HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("Content-Type", "application/json");

			if (!validateToken(username, token)) {
				return new ResponseEntity("{\"message\":\"username/Bad token\"}", responseHeaders, HttpStatus.BAD_REQUEST);
			}else {
				Connection conn = null;
					try {
					conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
					String query = "INSERT INTO clusterDB.friends (requester, requestee)"
						+ " VALUES (?,?)";
					PreparedStatement stmt = null;
							stmt = conn.prepareStatement(query);
							stmt.setString(1, requester);
							stmt.setString(2, requestee);
							int rs = stmt.executeUpdate();

					} catch (SQLException e ) {
						return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
					} finally {
						try {
							if (conn != null) { conn.close(); }
						}catch(SQLException se) {

						}
					}
				return new ResponseEntity(payloadObj.toString(), responseHeaders, HttpStatus.OK);
		}
 }


	// @RequestMapping(value = "/postFavoriteSpot", method = RequestMethod.GET)
	// public ResponseEntity<String> login(HttpServletRequest request) {
	// 	String username = request.getParameter("username");
	// 	String token = request.getParameter("token");
	//
	// 	HttpHeaders responseHeaders = new HttpHeaders();
  //   	responseHeaders.set("Content-Type", "application/json");
	//
	// 	if (!validateToken(username, token)) {
	// 		return new ResponseEntity("{\"message\":\"username/Bad token\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	// 	}else {
	//
	// 	}
	// 	User user = MyServer.tokenHashmap.get(username);
	// 	if(user.token.equals(token)){
	// 		MyServer.tokensArrayList.
	// 	}
	// 	else{
	// 		return new ResponseEntity("{\"message\":\"username/Bad token\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	//
	// 	}
	//
	// 		try {
	// 		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
	// 		String query = "SELECT password FROM clusterDB.users WHERE username = " + "\'" + username + "\'";
	// 		PreparedStatement stmt = null;
	// 				stmt = conn.prepareStatement(query);
	// 				ResultSet rs = stmt.executeQuery();
	//
	// 				while(rs.next()){
	// 				String returnedPassword = rs.getString("password");
	// 				if(BCrypt.checkpw(password, returnedPassword)){
	// 					String token = generateRandomString(10);
	// 					JSONObject responseObject = new JSONObject();
	// 					responseObject.put("token", token);
	// 					responseObject.put("message", "user logged in");
	// 					return new ResponseEntity(responseObject.toString(), responseHeaders, HttpStatus.OK);
	// 				}
	// 				else{
	// 					//return new ResponseEntity("{\"message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	// 					//obj.put("failure", "failure");
	// 					//arrayCheck.put(obj);
	// 				}
	// 			}
	// 		} catch (SQLException e ) {
	// 			return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
	// 		} finally {
	// 			try {
	// 				if (conn != null) { conn.close(); }
	// 			}catch(SQLException se) {
	// 				return new ResponseEntity(se.toString(), responseHeaders,HttpStatus.BAD_REQUEST);
	// 			}
	// 		}
	// 	return new ResponseEntity(arrayCheck.toString(), responseHeaders, HttpStatus.OK);
	// }
	//
	// @RequestMapping(value = "/connectToDB", method = RequestMethod.GET)
	// public ResponseEntity<String> connectToDB(HttpServletRequest request) {
	// 	//String nameToPull = request.getParameter("firstname");
	// 	HttpHeaders responseHeaders = new HttpHeaders();
  //   	responseHeaders.set("Content-Type", "application/json");
	// 	Connection conn = null;
	// 	JSONArray usersArray = new JSONArray();
	//     try {
	//     conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
	// 		String query = "SELECT * FROM users.users";
	// 		PreparedStatement stmt = null;
	//         stmt = conn.prepareStatement(query);
	//         //stmt.setString(1, nameToPull);
	//         ResultSet rs = stmt.executeQuery();
	//
	// 				while (rs.next()) {
	//             String username = rs.getString("username");
	//             int id = rs.getInt("id");
	// 						String password = rs.getString("password");
	// 						String locations = rs.getString("locations");
	// 						String description = rs.getString("description");
	//
	//             JSONObject obj = new JSONObject();
	//             obj.put("username", username);
	//             obj.put("id", id);
	// 						obj.put("password", password);
	// 						obj.put("locations", locations);
	// 						obj.put("description", description);
	//             usersArray.put(obj);
	//         }
	//     } catch (SQLException e ) {
	// 			return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
	//     } finally {
	//     	try {
	//     		if (conn != null) { conn.close(); }
	//     	}catch(SQLException se) {
	//
	//     	}
	//     }
	// 	return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	// }

	@RequestMapping(value = "/getMyMap", method = RequestMethod.GET)
	public ResponseEntity<String> getMyMap(@RequestBody String payload, HttpServletRequest request) {
		JSONObject usernamePayload = new JSONObject(payload);
		String username = usernamePayload.getString("username");
		HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.set("Content-Type", "application/json");
			Connection conn = null;
			JSONArray usersArray = new JSONArray();
				try {
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8", "root", "cluster");
				String query = "SELECT * FROM clusterDB.locations WHERE userID='(username)'"
					+ " VALUE (?)";
					PreparedStatement stmt = null;
						stmt = conn.prepareStatement(query);
						stmt.setString(1, username);
						ResultSet rs = stmt.executeQuery();
						while (rs.next()) {

								String coordinates = rs.getString("coordinates");
								String placeName = rs.getString("place_name");
								String description = rs.getString("description");

								JSONObject obj = new JSONObject();
								obj.put("coordinates", coordinates);
								obj.put("placeName", placeName);
								obj.put("description", description);
								usersArray.put(obj);
						}
				} catch (SQLException e ) {
					return new ResponseEntity(e.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
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

	public static String generateRandomString(int length){
		byte[] array = new byte[length];
		new Random().nextBytes(array);
		String generatedString = new String(array, Charset.forName("UTF-8"));

		return generatedString;
	}

	public boolean validateToken(String username, String token){
			User user = MyServer.tokenHashmap.get(username);
			if(user.token.equals(token)){
				return true;
			}
			else{
				return false;
			}
	}

}
