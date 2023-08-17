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
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.Hashtable;

public class servidor {
//	Variáveis declaradas

//	Variável Scanner, escaneia o teclado do usuário
	private static Scanner scan = new Scanner(System.in);
//	IP do servidor e do Líder
	private static InetAddress ip;
	private static InetAddress ipLider;
//	Portas Default, 10097, 10098, 10099
	private static int port;
	private static int port1;
	private static int port2;
	private static int portLider;
//	Sockets do servidor: Socket de atendimento e socket para iniciar conexão,
//	respectivamente
	private static ServerSocket serverSocket;
	private static Socket no;
//	Hash table que irá armazenar as chaves e valores de PUTs dos Clientes!
	private static Hashtable <String, String> htV = new Hashtable<>();
//	Hash table que armazena o time Stamp das chaves inseridas nos PUTs
	private static Hashtable <String, Timestamp> htT = new Hashtable<>();

	public static void main(String[] args) throws Exception {
		
		System.out.println("\nBem-vindo ao sistema cliente servidor KV!\n");
		System.out.println("Entrando como SERVIDOR\n");
		boolean verificar = false;
		while (verificar == false) {
//			Exigindo ao usuário para que insira as informações necessárias
//			para iniciar o servidor em questão
//			Como Default o IP de todos os servidores são o mesmo, 127.0.0.1, pois os 3 servidores
//			estão sendo rodados na MESMA MAQUINA! variando entre eles apenas as PORTAS!
			System.out.println("Para iniciar, insira seu IP:\n(Default: 127.0.0.1, local host)");
			ip = InetAddress.getByName(scan.next());
			System.out.println("Insira sua porta:\n(Portas Default: 10097, 10098, 10099)\n");
			port = scan.nextInt();
			System.out.println("Insira o IP do lider:\n");
			ipLider = InetAddress.getByName(scan.next());
			System.out.println("Insira a porta do lider:\n");
			portLider = scan.nextInt();
			System.out.println("\nConfirma os dados inseridos?\n");
			System.out.println("Sim(1)// nao(2)");
			int confirma = scan.nextInt();
			if (confirma == 1) {
				verificar = true;
//				Após informar a porta e o IP, no caso tratando-se de um funcionamento 
//				local host, as portas dos outros dois servidores são definidas de
//				maneira Default
				if (port == 10097) {
					port1 = 10098;
					port2 = 10099;
				}
				else if (port == 10098) {
					port1 = 10097;
					port2 = 10099;
				}
				else {
					port1 = 10097;
					port2 = 10098;
				}
				System.out.println("Servidor iniciado!\n");
			} 
			else {
				System.out.println("Insira novamente as informacoes!\n");
			}
		}
//		Criando socket de servidor
		serverSocket = new ServerSocket(port);
//		Criando laço para aguardar a conexão ser aceita e atender o solicitante!
		while (true) {
			no = serverSocket.accept();
//			Abrindo a Thread de atendimento, denonimada TCPServer
			TCPServer servidor = new TCPServer();
			servidor.start();
		}
	}
//	Thread responsável pelo recebimento de mensagens do cliente	
	public static class TCPServer extends Thread {
		
		public void run() {
			try {
//				Iniciando o socket a partir do socket 'no' aceito para comunicação TCP
//				com o cliente solicitante!
				InputStreamReader is = new InputStreamReader(no.getInputStream());
				BufferedReader reader = new BufferedReader(is);
//				Traduzindo a mensagem de String Json para classe mensagem
				mensagem msg = new Gson().fromJson(reader.readLine(), mensagem.class);
//				Inicialmente o servidor para a comunicação com o cliente para se comunicar com o Líder, 
//				caso este seja o líder, ele irá realizar as ações do líder, como enviar REPLICATIONs
				no.close();
//				Caso o server receba uma solicitação do tipo "PUT" e NÃO seja um líder
//				ele encaminha essa solicitação ao líder
				if (msg.getTipo().equals("PUT") & msg.getStatus().equals("N/A") & port != portLider) {
					System.out.println("Encaminhando PUT ao Lider\n");
					System.out.println("Key: " + msg.getKey() + " Value: " + msg.getValue());
//					Estabelecendo comunicação com o Líder
					try (Socket lider = new Socket(ipLider, portLider)) {
						String encaminha = new Gson().toJson(new mensagem(
								msg.getKey(), msg.getValue(), "PUT", "N/A", 
								ip, port, msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp()));
						OutputStream osL = lider.getOutputStream();
						DataOutputStream writerL = new DataOutputStream(osL);
						writerL.writeBytes(encaminha + "\n");
//						Fechando a comunicação com o Líder
						lider.close();
					}
				}
//				Caso um PUT seja recebido e a porta deste servidor seja a porta do líder
				else if (msg.getTipo().equals("PUT") & msg.getStatus().equals("N/A") & port == portLider) {
					System.out.println("Cliente: " + "[" + msg.getOrgIp() + " : " 
							+ msg.getOrgPort() + "]\n");
					System.out.println("Requested: PUT Key: " + msg.getKey() 
							+ " Value: " + msg.getValue() + "\n");
//					Sendo o líder, é adicionado na Hash table local a Key e o Value da solicitação PUT
//					com o time stamp adicionado em uma Hash table diferente
					htV.put(msg.getKey(), msg.getValue());
					htT.put(msg.getKey(), msg.getTimeStamp());
//					Realizando a comunicação com os outros servidores para 
//					atualizar as Hashtable locais de cada server
//					SERVER 1:
					try (Socket server1 = new Socket(ip, port1)) {
						OutputStream osS1 = server1.getOutputStream();
						DataOutputStream writerS1 = new DataOutputStream(osS1);
						String msgS1 = new Gson().toJson(new mensagem(msg.getKey(), 
								msg.getValue(), "REPLICATION", "N/A", ip, port, 
								msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp()));
						writerS1.writeBytes(msgS1 + "\n");
						server1.close();
					} catch (JsonSyntaxException e) {
						e.printStackTrace();
					}	
				}
//				Caso o servidor receba uma mensagem do tipo "REPLICATION"
//				ele irá inserir a informação em sua hash table local e em seguida
//				responderá com REPLICATION OK pro Líder continuar sua rotina
				else if (msg.getTipo().equals("REPLICATION") 
							&  msg.getRemPort() == portLider) {
					htV.put(msg.getKey(), msg.getValue());
					htT.put(msg.getKey(), msg.getTimeStamp());
					System.out.println("REPLICATION Key: " + msg.getKey() 
							+ " Value: " + msg.getValue() + "\nTime stamp: " + msg.getTimeStamp() + "\n");
//					Respondendo ao líder que a Key e Value foram adicionados neste servidor
					try (Socket lider = new Socket(ipLider, portLider)) {
						String respondeLider = new Gson().toJson(new mensagem(
								msg.getKey(),msg.getValue(), "REPLICATION", "REPLICATION_OK", 
								ip, port, msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp()));
						OutputStream osL = lider.getOutputStream();
						DataOutputStream writerL = new DataOutputStream(osL);
						writerL.writeBytes(respondeLider + "\n");
						lider.close();
					}
				}
//				Resposta dos servidores em relação a solicitação de REPLICATION
				else if (msg.getTipo().equals("REPLICATION") & msg.getStatus().equals("N/A") == false) {
//					Caso o primeiro servidor responda com um OK para a replicação, o Líder replica para o 
//					servidor 2
					if (msg.getRemPort() == port1 & msg.getStatus().equals("REPLICATION_OK")) {
						System.out.println("RECEIVED REPLICATION_OK FROM SERVER 1");
//						Sleep de 5 seg para enviar a replicação ao servidor 2
//						Isso serve para SIMULAR "atrasos" de envio de informação
//						na hora da replicação, para podermos "forçar" o erro de consistência!
//						Este erro entrega a resposta "TRY_OTHER_SERVER_OR_LATER"
						TCPServer.sleep(5000); // 5 seg de delay
//						REPLICANDO PARA O SERVER 2:
						try (Socket server2 = new Socket(ip, port2)) {
							OutputStream osS2 = server2.getOutputStream();
							DataOutputStream writerS2 = new DataOutputStream(osS2);
							String msgS2 = new Gson().toJson(new mensagem(msg.getKey(), 
									msg.getValue(), "REPLICATION", "N/A", ip, port, 
									msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp()));
							writerS2.writeBytes(msgS2 + "\n");
							server2.close();
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						}
					}
//					Resposta do server 2
					else if (msg.getRemPort() == port2 & msg.getStatus().equals("REPLICATION_OK")) {
						System.out.println("RECEIVED REPLICATION_OK FROM SERVER 2");
//						Caso o líder tenha recebido OK do servidor 2, e consequentemente de ambos, para
//						as replicações, a mensagem de PUT_OK é enviada DIRETAMENTE ao cliente requisitante!
						System.out.println("Enviando PUT_OK ao Cliente: " + "[" + msg.getOrgIp() 
								+ " : " + msg.getOrgPort() + "]" + "\nKey: " + msg.getKey() 
									+ "\nTime Stamp do servidor: " 
										+ new Timestamp(System.currentTimeMillis()) + "\n");
						try (Socket clientR = new Socket(msg.getOrgIp(), msg.getOrgPort())) {
							OutputStream osR = clientR.getOutputStream();
							DataOutputStream writerR = new DataOutputStream(osR);
							String msgS1 = new Gson().toJson(new mensagem(msg.getKey(), 
									msg.getValue(), "PUT_response", "PUT_OK", ip, port, 
									msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp()));
							writerR.writeBytes(msgS1 + "\n");
							clientR.close();
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						}
					}
//					Caso não seja recebido o OK de ambos os servers, é enviado a mensagem
//					PUT_NOT_OK, está condição não é esperada neste EP, porém, serve para
//					exemplificar o que poderia ser respondido caso algum REPLICATION fosse 
//					perdido ou então não fosse um "REPLICATION_OK"
					else {
						try (Socket serverR = new Socket(msg.getOrgIp(), msg.getOrgPort())) {
							OutputStream osR = serverR.getOutputStream();
							DataOutputStream writerR = new DataOutputStream(osR);
							mensagem R = new mensagem(msg.getKey(), 
									msg.getValue(), "PUT_response", "PUT_NOT_OK", ip, port, 
									msg.getOrgIp(), msg.getOrgPort(), msg.getTimeStamp());
							String msgS1 = new Gson().toJson(R);
							writerR.writeBytes(msgS1 + "\n");
							serverR.close();
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						}
					}
				}	
//				Caso o servidor receba um "GET", independe de ser Líder ou não, o servidor precisa
//				responder o cliente solicitante com o valor para a chave desejada
				else if (msg.getTipo().equals("GET")) {
					System.out.println("Cliente: " + "[" + msg.getOrgIp() 
						+ " : " + msg.getOrgPort() + "]" + " GET REQUEST"
							+ "\nTime Stamp do cliente: " + msg.getTimeStamp() + "\n");
//					O servidor verifica na sua hash table (htV) se possui valor 
//					diferente de nulo
					String Value = htV.get(msg.getKey());
//					Caso o valor da chave seja nulo
					if (Value == null) {
//						Abrindo novamente uma conexão com o cliente para respondê-lo
						try (Socket clientR = new Socket(msg.getOrgIp(), msg.getOrgPort())) {
							OutputStream osR = clientR.getOutputStream();
							DataOutputStream writerR = new DataOutputStream(osR);
							String resposta = new Gson().toJson(new mensagem(
									msg.getKey(), "NULL", "GET_response", "GET_OK", ip, port, 
									msg.getOrgIp(), msg.getOrgPort(), null));
							writerR.writeBytes(resposta + "\n");
							clientR.close();
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						}
					}
//					Caso o valor da chave pelo qual o cliente buscou não seja nulo
//					a solicitação eh respondida com o valor da Key e, adicionalmente,
//					o Time Stamp da chave também é enviado ao cliente, para que ele
//					possa verificar a consistência da informação!
					else {
						System.out.println("GET_OK, respondendo o cliente, Value: '" + Value + "'\n");
						System.out.println("Time stamp da key: " + htT.get(msg.getKey()) + "\n");
						System.out.println("Time stamp do servidor: " 
									+ new Timestamp(System.currentTimeMillis()) + "\n");
//						Abrindo novamente uma conexão com o cliente para respondê-lo
						try (Socket clientR = new Socket(msg.getOrgIp(), msg.getOrgPort())) {
							OutputStream osR = clientR.getOutputStream();
							DataOutputStream writerR = new DataOutputStream(osR);
							String resposta = new Gson().toJson(new mensagem(
									msg.getKey(), Value, "GET_response", "GET_OK", 
									ip, port, msg.getOrgIp(), msg.getOrgPort(), htT.get(msg.getKey())));
							writerR.writeBytes(resposta + "\n");
							clientR.close();
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						}
						System.out.println("GET respondido!\n");
					}
				}
			} catch (IOException e) {
					e.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}