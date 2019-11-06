import java.net.*;
import java.io.*;
import java.util.*;

public class ServerHandler extends Thread {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;
	private ArrayList<Presence> presencesList;
	private ArrayList<Message> messageList;
	private Hashtable<Integer, User> userList;

	public ServerHandler(Socket connection,ArrayList<Presence> presencesList, ArrayList<Message> messageList, Hashtable<Integer, User> userList) {
		this.connection = connection;
		this.presencesList = presencesList;
		this.messageList = messageList;
		this.userList = userList;

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
			String metodo = null;

			// lê a primeira linha: request-line
			msg = in.readLine();
			len = msg == null ? 0 : msg.trim().length();

			//Trata a request line
			if (len != 0) {
				System.out.println(msg);

				StringTokenizer tokens = new StringTokenizer(msg);
				String token = tokens.nextToken();
				if (token.equals("GET")) {
					metodo = "GET";
				} else {
					metodo = "POST";
				}
			}

			//lê todas as linhas (terminadas por new line) até ler uma linha em branco; corresponde a ler todo o cabeçalho
			while (len != 0) {
				System.out.println(msg);

				//lê a linha seguinte
				msg = in.readLine();
				len = msg == null ? 0 : msg.trim().length();
			}
			switch (metodo) {
				case "GET":
					System.out.println("A ligacao que atendeu o GET foi: " + connection.getInetAddress() + " do porto " + connection.getPort());

					System.out.println("GET");
					System.out.println("HTTP/1.1 200 OK");
					out.println("HTTP/1.1 200 OK");
					res = new String("{\"msg\":  {\"Top6\": [2,3,7,10,6,23], \"n_volta\": 2, \"n_atualizacoes\": 20}}");
					length = res.length();
					out.println("Content-type: application/json");
					out.println("Content-Length: " + length);
					out.write("\r\n");
					out.println(res);
					out.flush();
					break;
				case "POST":
				default:
					out.println("HTTP/1.1 404 Not Found");
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