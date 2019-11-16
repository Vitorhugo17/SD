import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class ServerHandler extends Thread {
	private Socket connection;
	private BufferedReader in;
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

			String txt = "";
			switch (method) {
				case "GET": 
					if (route.equals("/")) {
						//Começo da criação do conteudo da resposta
						txt = "{\"presences\":  [";
						//Inicio da iteração sobre os elementos da lista de presenças e a inserção dos mesmos na resposta
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
						//Fim da iteração
						txt += "], \"messages\": [";
						//Inicio da iteração sobre os elementos da lista de mensagens e a inserção dos mesmos na resposta
						for (int j = 0; j < messagesList.size(); j++) {
							Message m = messagesList.get(j);
							int idUserM = 0;
							Enumeration presencesKeys = presencesList.keys();
							while(presencesKeys.hasMoreElements()) {
								int y = (int) presencesKeys.nextElement();
								if (presencesList.get(y).equals(m.getWriter())) {
									idUserM = y;
								}
							}
							if (j != 0) {
								txt += ", {\"idUser\": \"" + idUserM + "\", \"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage().replaceAll("\"", "\\\\\"") + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							} else {
								txt += "{\"idUser\": \"" + idUserM + "\", \"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage().replaceAll("\"", "\\\\\"") + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
							}
						};
						//Fim da iteração
						txt += "]}";
						//Fim da criação do conteudo
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
					//verifica qual é a rota utilizada pelo cliente
					if (route.equals("/messages")) {
						//inicio da leitura do body do pedido POST
						while (in.ready()) {
							x = in.read();
							if (x == 123) {
								start = true;
							}
							if (start) {
								char c = (char) x;
								jsonString += c;
							}
						}
						//fim da leitura
						//transformação do body num JSON Object
						json = new JSONObject(jsonString);
						
						synchronized(messagesList) {
							//inicio da verificação de se aquele id de utilizador existe na lista de presenças
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
							//fim da verificação

							if (exists) {
								//Inicio da criação do objeto Message e posterior inserção na lista de messagens
								String messageText = (String) json.get("message");
								long writeTime = new Date().getTime();
								Message message = new Message(username, writeTime, messageText);

								messagesList.add(message);
								//Fim da criação e da inserção

								//Começo da criação do conteúdo da resposta 
								txt = "{\"presences\":  [";
								//Inicio da iteração sobre os elementos da lista de presenças e a inserção dos mesmos na resposta
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
								//Fim da iteração
								txt += "], \"messages\": [";
								//Inicio da iteração sobre os elementos da lista de mensagens e a inserção dos mesmos na resposta
								for (int j = 0; j < messagesList.size(); j++) {
									Message m = messagesList.get(j);
									int idUserM = 0;
									Enumeration presencesKeys = presencesList.keys();
									while(presencesKeys.hasMoreElements()) {
										int y = (int) presencesKeys.nextElement();
										if (presencesList.get(y).equals(m.getWriter())) {
											idUserM = y;
										}
									}
									if (j != 0) {
										txt += ", {\"idUser\": \"" + idUserM + "\", \"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage().replaceAll("\"", "\\\\\"") + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									} else {
										txt += "{\"idUser\": \"" + idUserM + "\", \"writer\": \"" + m.getWriter() + "\", \"message\": \"" + m.getMessage().replaceAll("\"", "\\\\\"") + "\", \"date\": \"" + new Date(m.getWriteTime()) + "\"}";
									}
								};
								//Fim da iteração
								txt += "]}";
								//Fim da criação do conteudo
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
						//inicio da leitura do body do pedido POST
						while (in.ready()) {
							x = in.read();
							if (x == 123) {
								start = true;
							}
							if (start) {
								char c = (char) x;
								jsonString += c;
							}
						}
						//fim da leitura
						//transformação do body num JSON Object
						json = new JSONObject(jsonString);
						
						synchronized(presencesList) {
							//Inicio da verificação se o username enviado já existe na lista de presenças
							String username = (String) json.get("username");
							boolean exists = false;
							Enumeration presences = presencesList.elements();
							while(presences.hasMoreElements()) {
								String u = (String) presences.nextElement();
								if (u.equals(username)) {
									exists = true;
								}
							};
							//Fim da verificação

							if (!exists) {
								//Inicio da criação do id que este utilizador irá receber
								int id = 0;
								if (!presencesList.isEmpty()) {
									Enumeration presencesId = presencesList.keys();
									id = (int) presencesId.nextElement();
								}
								id++;
								//Fim da criação do id
								//Inserção do id e do utilizador na lista de presenças
								presencesList.put(id, username);
								//Criação da resposta ao pedido
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