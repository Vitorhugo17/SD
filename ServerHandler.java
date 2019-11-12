import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

import jdk.nashorn.internal.parser.DateParser;

public class ServerHandler extends Thread {
	private Socket connection;
	private BufferedReader in ;
	private PrintWriter out;
	private Hashtable<Integer, String> presencesList;
	private ArrayList<Message> messagesList;

	public ServerHandler(Socket connection, Hashtable<Integer, String> presencesList, ArrayList<Message> messagesList) {
		this.connection = connection;
		this.presencesList = presencesList;
		this.messagesList = messagesList;
		
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

			while (len != 0) {
				System.out.println(msg);
				msg = in.readLine();
				len = msg == null ? 0 : msg.trim().length();
			}
			System.out.println("");

			String txt = "";
			switch (method) {
				case "GET": 
					if (route.equals("/")) {
						txt = "{\"presences\":  [";
						Enumeration presences = presencesList.elements();
						int i = 0;
						while(presences.hasMoreElements()) {
							if (i != 0) {
								txt += ", \"" + presences.nextElement() + "\"";
							} else {
								i++;
								txt += "\"" + presences.nextElement() + "\"";
							}
						};
						txt += "], \"messages\": [";
						for (int j = 0; j < messagesList.size(); j++) {
							Message m = messagesList.get(j);
							if (j != 0) {
								txt += ", {\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							} else {
								txt += "{\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							}
						};
						txt += "]}";
						res = new String(txt);
						length = res.length();
						out.println("HTTP/1.1 200 OK");
						out.println("Access-Control-Allow-Origin: *");
						out.println("Content-type: application/json");
						out.println("Content-Length: " + length);
						out.write("\r\n");
						out.println(res);
						out.flush();
					} else {
						res = new String("404 Not Found");
						length = res.length();
						out.println("HTTP/1.1 404 Not Found");
						out.println("Access-Control-Allow-Origin: *");
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
						
						synchronized(messagesList) {
							int idUser = Integer.parseInt((String) json.get("idUser"));
							boolean exists = false;
							Enumeration presencesId = presencesList.keys();
							String username = "";
							while(presencesId.hasMoreElements()) {
								if ((int) presencesId.nextElement() == idUser) {
									exists = true;
									username = presencesList.get(idUser);
								}
							};

							if (exists) {
								String messageText = (String) json.get("message");
								long writeTime = new Date().getTime();
								Message message = new Message(username, writeTime, messageText);

								messagesList.add(message);
								
								txt = "{\"presences\":  [";
								Enumeration presences = presencesList.elements();
								int i = 0;
								while(presences.hasMoreElements()) {
									if (i != 0) {
										txt += ", \"" + presences.nextElement() + "\"";
									} else {
										i++;
										txt += "\"" + presences.nextElement() + "\"";
									}
								};
								txt += "], \"messages\": [";
								for (int j = 0; j < messagesList.size(); j++) {
									Message m = messagesList.get(j);
									if (j != 0) {
										txt += ", {\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									} else {
										txt += "{\"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage() + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									}
								};
								txt += "]}";
								res = new String(txt);
								length = res.length();
								out.println("HTTP/1.1 200 OK");
								out.println("Access-Control-Allow-Origin: *");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							} else {
								res = new String("{\"error\": \"User not register\"}");
								length = res.length();
								out.println("HTTP/1.1 400 Bad Request");
								out.println("Access-Control-Allow-Origin: *");
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
							Enumeration presences = presencesList.elements();
							while(presences.hasMoreElements()) {
								String u = (String) presences.nextElement();
								if (u.equals(username)) {
									exists = true;
								}
							};

							if (!exists) {
								int id = 0;
								if (!presencesList.isEmpty()) {
									Enumeration presencesId = presencesList.keys();
									id = (int) presencesId.nextElement();
								}
								id++;
								presencesList.put(id, username);
								txt = "{\"idUser\":  " + id + "}";
								res = new String(txt);
								length = res.length();
								out.println("HTTP/1.1 200 OK");
								out.println("Access-Control-Allow-Origin: *");
								out.println("Content-type: application/json");
								out.println("Content-Length: " + length);
								out.write("\r\n");
								out.println(res);
							} else {
								res = new String("{\"error\": \"Username already exists\"}");
								length = res.length();
								out.println("HTTP/1.1 400 Bad Request");
								out.println("Access-Control-Allow-Origin: *");
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
						out.println("Access-Control-Allow-Origin: *");
						out.println("Content-type: text/plain");
						out.println("Content-Length: " + length);
						out.write("\r\n");
						out.println(res);
						out.flush();
					}
					break;
				case "OPTIONS": 
					out.println("HTTP/1.1 200 OK");
					out.println("Access-Control-Allow-Headers: Content-Type");
					out.println("Access-Control-Allow-Origin: *");
					out.flush();					
					break;
				default:
					res = new String("404 Not Found");
					length = res.length();
					out.println("HTTP/1.1 404 Not Found");
					out.println("Access-Control-Allow-Origin: *");
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