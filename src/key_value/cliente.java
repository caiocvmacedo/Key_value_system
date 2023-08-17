package key_value;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.Gson;

public class cliente {
	
	private static Scanner scan = new Scanner(System.in);
//	IPs do cliente e dos 3 servidores
	private static InetAddress ip;
	private static InetAddress ip1;
	private static InetAddress ip2;
	private static InetAddress ip3;
	private static InetAddress ipSort;
//	Portas do cliente e dos 3 servidores
	private static int port;
	private static int port1;
	private static int port2;
	private static int port3;
	private static int portSort;
//	Mensagem para ser transformada em Json e enviada aos servidores
	private static String mensagem;
//	Hash table que armazena o histórico de time Stamp dos GETs e PUTs que o cliente faz
	private static Hashtable <String, Timestamp> htT = new Hashtable<>();
//	Socket de receptação de mensagens dos servidores
	private static ServerSocket receive;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("\nBem-vindo ao sistema cliente servidor KV!\n");
		System.out.println("Entrando como CLIENTE\n");
		while(true) {
			System.out.println("Funcionalidades do CLIENTE:\n");
			System.out.println("Inicializar(1)// PUT(2)// GET(3)\n");
			System.out.println("Digite a opcao desejada: \n");
			int escolha = scan.nextInt();
			if (escolha == 1) {
				boolean verificar = false;
				while (verificar == false) {
//					Pedindo as informações para o usuário iniciar o cliente!
					System.out.println("Insira o seu IP:\n");
					ip = InetAddress.getByName(scan.next());
					System.out.println("Insira sua porta:\n");
					port = scan.nextInt();
					System.out.println("Insira os IPs e portas dos 3 servidores:\n");
					System.out.println("\nIP do PRIMEIRO servidor:\n");
					ip1 = InetAddress.getByName(scan.next());
					System.out.println("\nPorta do PRIMEIRO servidor:\n(Portas Default 10097, 10098, 10099)\n");
					port1 = scan.nextInt();
					System.out.println("\nIP do SEGUNDO servidor:\n");
					ip2 = InetAddress.getByName(scan.next());
					System.out.println("\nPorta do SEGUNDO servidor:\n(Portas Default 10097, 10098, 10099)\n");
					port2 = scan.nextInt();
					System.out.println("\nIP do TERCEIRO servidor:\n");
					ip3 = InetAddress.getByName(scan.next());
					System.out.println("\nPorta do TERCEIRO servidor:\n(Portas Default 10097, 10098, 10099)\n");
					port3 = scan.nextInt();
					System.out.println("\nConfirma os dados inseridos?\n");
					System.out.println("Sim(1)// nao(2)");
					int confirma = scan.nextInt();
					if (confirma == 1) {
						verificar = true;
						System.out.println("Cliente iniciado!\n");
//						Cria um socket pra recepcionar a resposta do servidor
						receive = new ServerSocket(port);
					} 
					else {
						System.out.println("Insira novamente as informacoes!\n");
					}
				}
			}
			else if (escolha == 2) {
//				Requisição PUT
				System.out.println("Digite a chave (Key), em seguida, o valor (Value):\n");
				System.out.println("Key:\n");
				String key = scan.next();
				System.out.println("Value:\n");
				String value = scan.next();
//				Sorteando server
				sortServer();
//				Criando mensagem com a Key, value e o tipo de requisição
				mensagem PUT = new mensagem(key, value, "PUT", "N/A", ip,
						port, ip, port, new Timestamp(System.currentTimeMillis()));
				envia(PUT);
//				Armazenando o Time Stamp da Key ENVIADA NO PUT
//				Este armazenamento é justamente para comparar com o Time Stamp
//				de futuras solicitações desta chave, para averiguar a consistência
//				do GET novo recebido de algum servidor
				htT.put(key, new Timestamp(System.currentTimeMillis()));
			}
			else if (escolha == 3) {
//				Requisição GET
				System.out.println("Digite a chave (Key) que deseja obter o valor (Value)!\n");
				System.out.println("Key:\n");
				String key = scan.next();
//				Sorteando server
				sortServer();
//				Criando mensagem com a Key, value e o tipo de requisição
				mensagem GET = new mensagem(key, "N/A", "GET", "N/A", ip, 
						port, ip, port, new Timestamp(System.currentTimeMillis()));
				envia(GET);
			}
			else {
				System.out.println("\nDigite uma opcao valida!\n");
			}
		}
	}
	
//	Função que atribuirá valor em ipSort e portSort, sorteando um dos 3 servers para enviar
//	as requisições desejadas
	public static void sortServer() {
		Random random = new Random();
//		Sorteia um numero
		int numero = random.nextInt(99);
		if (numero <= 33) {
			ipSort = ip1;
			portSort = port1;
		}
		else if (numero > 33 & numero <= 66) {
			ipSort = ip2;
			portSort = port2;
		}
		else {
			ipSort = ip3;
			portSort = port3;
		}
	}
	
//	Função responsável por criar a mensagem Json e acionar a Thread que enviará ao server
//	a requisição desejada
	public static void envia(mensagem msg) {
//		Transforma a classe mensagem em Json
		mensagem = new Gson().toJson(msg);
//		Abre uma Thread para enviar a requisição
		TCPClient client = new TCPClient(mensagem);
//		Dentro da Thread é aberto um socket para enviar e receber a resposta
		client.start();
	}
//	Thread responsável por realizar a conexão com o servidor, enviando requisições e recebendo respostas
	public static class TCPClient extends Thread {
//		Atributos fundamentais para o funcionamento da Thread que envia e
//		recebe mensagens dos servidores
		private static mensagem response;
		private static String mensagem;
		private Socket client;
		public TCPClient(String mensagem) {
//			Construtor para obter a mensagem
			TCPClient.mensagem = mensagem;
		}
		public void run() {
//			Começa a conexão com o servidor, através do novo socket "client"
			try {
				client = new Socket(ipSort, portSort);
				OutputStream os = client.getOutputStream();
				DataOutputStream writer = new DataOutputStream(os);
//				Envia a mensagem para o server, que irá realizar a operação desejada
				writer.writeBytes(mensagem + "\n");
				client.close();
				Socket no = receive.accept();
				InputStreamReader is = new InputStreamReader(no.getInputStream());
				BufferedReader reader = new BufferedReader(is);
//				Resposta do servidor
				response  = new Gson().fromJson(reader.readLine(), mensagem.class);
				no.close();
//				Se a resposta do servidor for uma resposta a um PUT
//				É printado na tela o status do PUT, exemplo: PUT_OK
				if (response.getTipo().equals("PUT_response")) {
					System.out.println(response.getStatus());
					System.out.println("Key:Value: {" + response.getKey() + " : " + response.getValue() + "}" 
							+ "\nRealizado no servidor: {" + response.getRemIp() + " : " + response.getRemPort()
							+ "}" + "\nTime Stamp da key no servidor: " + response.getTimeStamp());
//					Atualiza a chave quando chegar a mensagem de PUT_OK para o cliente!
					htT.put(response.getKey(), response.getTimeStamp());
				}
//				Se a resposta for uma resposta a um GET, é printado na tela
//				a chave (Key) e o valor (Value), caso seja consistente e não nula!
				else if (response.getTipo().equals("GET_response")) {
//					Comparando se a resposta do servidor é consistente
//					de acordo com o Time Stamp da resposta e o Time Stamp armazenado no 
//					histórico de buscas do cliente, caso ele já tenha buscado 
//					a mesma chave anteriormente ou realizado um PUT com esta chave
					boolean consistente;
					if (htT.get(response.getKey()) == null) {
//						Neste caso, a chave não existe no histórico do cliente ainda
						consistente = true;
					}
//					Aqui a chave existe
					else {
//						Se for nulo o time stamp da chave na mensagem de resposta, significa que
//						ela ainda não chegou no servidor solicitado
						if (response.getTimeStamp() == null) {
							consistente = false;
						}
//						Se o Time Stamp for maior, significa que a chave no servidor foi salva, ou atualizada,
//						depois da última atualização no histórico do cliente, então essa situação é consistente 
						else if(response.getTimeStamp().getTime() >= htT.get(response.getKey()).getTime()) {
							consistente = true;
						}
						else {
							consistente = false;
//							Atualizando o valor do histórico, para que caso o cliente pergunte de novo
//							sobre a mesma chave, o erro possa desaparecer, caso o servidor seja atualizado
							htT.put(response.getKey(), response.getTimeStamp());
						}
					}
//					Caso a resposta contenha um valor para a chave desejada
					if (response.getStatus().equals("GET_OK") 
							& response.getValue().equals("NULL") == false
								& consistente) {
//						Printando na tela o resultado!
						System.out.println("Request GET response: \n");
						System.out.println("Key:Value: {" + response.getKey() + " : " + response.getValue() + "}" 
								+ " obtido do servidor: {" + response.getRemIp() + " : " 
								+ response.getRemPort() + "}" + "\nTime Stamp do servidor: " 
								+ response.getTimeStamp() + "\nMeu time stamp: "
								+ new Timestamp(System.currentTimeMillis()));
//						Armazenando o Time Stamp da Key solicitada no GET
//						Este armazenamento é justamente para comparar com o Time Stamp
//						de futuras solicitações desta chave, para averiguar a consistência
//						do GET novo recebido de algum servidor
						htT.put(response.getKey(), response.getTimeStamp());
					}
//					Caso seja null o valor da chave
					else if (response.getStatus().equals("GET_OK") 
								& response.getValue().equals("NULL")
									& consistente) {
						System.out.println("Request GET response: \n");
						System.out.println(response.getKey() + " : " + response.getValue());
					}
//					Caso a resposta demonstre-se não consistente, a mensagem de erro é printada
//					na tela do cliente
					else if (response.getStatus().equals("GET_OK") 
								& consistente == false) {
//						Mensagens de erro que serão impressas ao cliente
						System.out.println("Error!\n");
						System.out.println("TRY_OTHER_SERVER_OR_LATER\n");
						System.out.println("PLEASE, WAIT 5 SECONDS!");
//						Contador de tempo, dos 5 segundos a serem esperados
//						para garantir que os servidores se atualizem antes do
//						cliente realizar a próxima Query ou PUT
						int vezes = 4;
						while (vezes != 0) {
							TCPClient.sleep(1000);
							System.out.println(vezes + " sec ...");
							vezes -=1;
						}
						TCPClient.sleep(1000);
						System.out.println("READY!");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}