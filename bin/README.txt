Comandos a serem rodados no prompt para iniciar a aplicação peer

D:
cd D:\arquivos\Documentos-HD\Scripts\Eclipse\key_value\src

Compilando a classe mensagem:
javac key_value/mensagem.java
Compilando a classe peer:
javac -cp .;lib/gson-2.9.1.jar; key_value/cliente.java
javac -cp .;lib/gson-2.9.1.jar; key_value/servidor.java

Executando aplicação
java -cp .;lib/gson-2.9.1.jar; key_value/cliente
java -cp .;lib/gson-2.9.1.jar; key_value/servidor

Inserir IPs e ports

