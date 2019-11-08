import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

import jdk.nashorn.internal.parser.DateParser;

public class ServerHandler extends Thread {
	private Socket connection;
	private BufferedReader in ;
	private PrintWriter out;
	private ArrayList<String> presencesList;
	private ArrayList<Message> messageList;

	public ServerHandler(Socket connection, ArrayList<String> presencesList, ArrayList<Message> messageList) {
		this.connection = connection;
		this.presencesList = presencesList;
		this.messageList = messageList;

		try {
			this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			this.out = new PrintWriter(connection.getOutputStream());
		} catch (IOException e) {
			System.out.println("Erro na execucao do servidor: " + e);
			System.exit(1);
		}
	}

	public void run() {
		try {
			String msg;
			String res;
			int len, length;
			String method = null, route = null;

			// lê a primeira linha: request-line
			msg = in.readLine();
			len = msg == null ? 0 : msg.trim().length();

			//Trata a request line
			if (len != 0) {
				StringTokenizer tokens = new StringTokenizer(msg);
				method = tokens.nextToken();
				route = tokens.nextToken();
			}

			String txt = "";
			switch (method) {
				case "GET": 
					if (route.equals("/")) {
						txt = "{\"presences\":  [";
						for (int i = 0; i < presencesList.size(); i++) {
							if (i != 0) {
								txt += ", \"" + presencesList.get(i) + "\"";
							} else {
								txt += "\"" + presencesList.get(i) + "\"";
							}
						};
						txt += "], \"messages\": [";
						for (int i = 0; i < messageList.size(); i++) {
							Message m = messageList.get(i);
							if (i != 0) {
								txt += ", {\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							} else {
								txt += "{\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							}
						};
						txt += "]}";
						res = new String(txt);
						length = res.length();
						out.println("HTTP/1.1 200 OK");
						out.println("Content-type: application/json");
						out.println("Content-Length: " + length);
						out.write("\r\n");
						out.println(res);
						out.flush();
					} else {
						res = new String("404 Not Found");
						length = res.length();
						out.println("HTTP/1.1 404 Not Found");
						out.println("Content-type: text/plain");
						out.println("Content-Length: " + length);
						out.write("\r\n");
						out.println(res);
						out.flush();
					}
					break;
				case "POST":
					int x = 0;
					String jsonString = "";
					JSONObject json = new JSONObject();
					boolean start = false;

					if (route.equals("/messages")) {
						while ((x = in.read()) != 125) {
							if (x == 123) {
								start = true;
							}
							if (start) {
								char c = (char) x;
								jsonString += c;
							}
						}
						jsonString += (char) 125;
						json = new JSONObject(jsonString);
						
						synchronized(messageList) {
							String username = (String) json.get("username");
							boolean exists = false;
							for (int i = 0; i < presencesList.size(); i++) {
								if (presencesList.get(i).equals(username)) {
									exists = true;
								}
							}

							if (exists) {
								String messageText = (String) json.get("message");
								long writeTime = new Date().getTime();
								Message message = new Message(username, writeTime, messageText);

								messageList.add(message);
								
								txt = "{\"presences\":  [";
								for (int i = 0; i < presencesList.size(); i++) {
									if (i != 0) {
										txt += ", \"" + presencesList.get(i) + "\"";
									} else {
										txt += "\"" + presencesList.get(i) + "\"";
									}
								};
								txt += "], \"messages\": [";
								for (int i = 0; i < messageList.size(); i++) {
									Message m = messageList.get(i);
									if (i != 0) {
										txt += ", {\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									} else {
										txt += "{\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									}
								};
								txt += "]}";
								res = new String(txt);
								length = res.length();
								out.println("HTTP/1.1 200 OK");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							} else {
								res = new String("{\"error\": \"User not register\"}");
								length = res.length();
								out.println("HTTP/1.1 400 Bad Request");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							}
							out.flush();
						}
					} else if (route.equals("/users")) {
						while ((x = in.read()) != 125) {
							if (x == 123) {
								start = true;
							}
							if (start) {
								char c = (char) x;
								jsonString += c;
							}
						}
						jsonString += (char) 125;
						json = new JSONObject(jsonString);
			
						synchronized(presencesList) {
							String username = (String) json.get("username");
							boolean exists = false;
							for (int i = 0; i < presencesList.size(); i++) {
								if (presencesList.get(i).equals(username)) {
									exists = true;
								}
							}

							if (!exists) {
								presencesList.add(username);
								txt = "{\"presences\":  [";
								for (int i = 0; i < presencesList.size(); i++) {
									if (i != 0) {
										txt += ", \"" + presencesList.get(i) + "\"";
									} else {
										txt += "\"" + presencesList.get(i) + "\"";
									}
								};
								txt += "], \"messages\": [";
								for (int i = 0; i < messageList.size(); i++) {
									Message m = messageList.get(i);
									if (i != 0) {
										txt += ", {\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									} else {
										txt += "{\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									}
								};
								txt += "]}";
								res = new String(txt);
								length = res.length();
								out.println("HTTP/1.1 200 OK");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							} else {
								res = new String("{\"error\": \"User already exists\"}");
								length = res.length();
								out.println("HTTP/1.1 400 Bad Request");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							}
							out.flush();
						}
					} else {
						res = new String("404 Not Found");
						length = res.length();
						out.println("HTTP/1.1 404 Not Found");
						out.println("Content-type: text/plain");
						out.println("Content-Length: " + length);
						out.write("\r\n");
						out.println(res);
						out.flush();
					}
				default:
					res = new String("404 Not Found");
					length = res.length();
					out.println("HTTP/1.1 404 Not Found");
					out.println("Content-type: text/plain");
					out.println("Content-Length: " + length);
					out.write("\r\n");
					out.println(res);
					out.flush();
					break;
			} 
			in.close();
			out.close();
			connection.close();

		} catch (IOException e) {
			System.out.println("Erro na execucao do servidor: " + e);
			System.exit(1);
		}
	}
}