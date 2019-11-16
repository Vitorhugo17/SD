import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    static int DEFAULT_PORT = 8081;
    
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		//variável para guardar a lista de presenças que contém um id (inteiro) e o username (String)
		Hashtable<Integer, String> presencesList = new Hashtable<Integer, String>();
		//variável para guardar a lista de mensagem que contém um objecto do tipo Message
		ArrayList<Message> messagesList = new ArrayList<Message>();

		ServerSocket server = null;
		try {
			//iniciar o servidor abrindo a porta 8081
			server = new ServerSocket(port);
			System.out.println("Servidor a' espera de ligacoes na porta " + port);
		} catch (IOException e) {
			System.out.println("Erro ao criar o ServerSocket...");
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				//receber o pedido de coneção do cliente e criar uma thread para tratar desse pedido
				Socket connection = server.accept();
				
				ServerHandler handler = new ServerHandler(connection, presencesList, messagesList);
				handler.start();
			} catch (IOException e) {
				System.out.println("Erro na execucao do servidor: " + e);
				System.exit(1);
			}
		}
	}
}