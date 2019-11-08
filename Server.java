import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    static int DEFAULT_PORT = 8081;
    
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		ArrayList<String> presencesList = new ArrayList<String>();
		ArrayList<Message> messageList = new ArrayList<Message>();

		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("Servidor a' espera de ligacoes na porta " + port);
		} catch (IOException e) {
			System.out.println("Erro ao criar o ServerSocket...");
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				Socket connection = server.accept();
				
				ServerHandler handler = new ServerHandler(connection, presencesList, messageList);
				handler.start();
			} catch (IOException e) {
				System.out.println("Erro na execucao do servidor: " + e);
				System.exit(1);
			}
		}
	}
}