package key_value;

import java.net.InetAddress;
import java.sql.Timestamp;

public class mensagem {

	//	Atributos da classe mensagem
	private String key;
	private String value;
	private String tipo;
	private String status;
	private InetAddress remIp;
	private int remPort;
	private InetAddress OrgIp;
	private int OrgPort;
	private Timestamp timeStamp;
	
//	construtor
	public mensagem(String key, String value, String tipo, 
			String status, InetAddress remIp, int remPort, 
			InetAddress OrgIp, int OrgPort, Timestamp timeStamp) {
		super();
//		Chave
		this.key = key;
//		Valor da chave
		this.value = value;
//		Tipo, exemplo: PUT, GET, REPLICATION
		this.tipo = tipo;
//		Status, exemplo: "OK", "NOT_OK"
		this.status = status;
//		IP e porta do remetente da mensagem
		this.remIp = remIp;
		this.remPort = remPort;
//		IP e porta do cliente ou servidor, que originalmente solicitou
//		o GET ou o PUT
		this.OrgIp = OrgIp;
		this.OrgPort = OrgPort;
//		Armazena o Time Stamp do solicitante ou da chave!
		this.timeStamp = timeStamp;
	}
	
//	Os tipos de mensagem são:
//		- PUT
//		- GET
//		- REPLICATION
//		- RESPONSE (PUT, GET, REPLICATION)
//	A variável "status" tem a função de retornar a resposta quando o "tipo" é "RESPONSE"
//	Caso a mensagem não seja do tipo "RESPONSE", status será N/A (Não se aplica)
//	Os tipos de STATUS são: 
//		-_OK
//		-_NOT_OK
//	Informando se o PUT ou GET foram realizados com sucesso!
	
//	Métodos Getters para os atributos da mensagem
//	Setters não são necessários, pois, caso necessite alterar um atributo, 
//	uma nova mensagem deve ser escrita!
	protected String getKey() {
		return key;
	}

	protected String getValue() {
		return value;
	}

	protected String getTipo() {
		return tipo;
	}

	protected String getStatus() {
		return status;
	}

	protected InetAddress getRemIp() {
		return remIp;
	}

	protected int getRemPort() {
		return remPort;
	}
	
	protected InetAddress getOrgIp() {
		return OrgIp;
	}

	protected int getOrgPort() {
		return OrgPort;
	}
	
	protected Timestamp getTimeStamp() {
		return timeStamp;
	}	
}