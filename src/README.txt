Comandos a serem rodados no prompt para iniciar a aplicação key_value

// == Comentários do autor do projeto

// No caso de minha máquina (Caio Macedo - Autor), o projeto está no 
// armazenamento D:

Acessando a pasta do projeto:
D:
cd D:\arquivos\Documentos-HD\Scripts\Eclipse\key_value\src

// Insira corretamente a localização da pasta do projeto extraída

Compilando a classe mensagem:
javac key_value/mensagem.java

Compilando a classe cliente:
javac -cp .;lib/gson-2.9.1.jar; key_value/cliente.java

Compilando a classe servidor:
javac -cp .;lib/gson-2.9.1.jar; key_value/servidor.java

Executando a aplicação, primeiramente, inicie os 3 servidores:
java -cp .;lib/gson-2.9.1.jar; key_value/servidor

Executando a aplicação, agora inicie os 2 clientes:
java -cp .;lib/gson-2.9.1.jar; key_value/cliente

Siga as instruções para inicializar os 3 servidores
Por fim, siga as instruções para inicializar os 2 clientes