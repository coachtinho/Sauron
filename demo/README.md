# Sauron demonstration

## 1. Preparação do Sistema

Para testar a aplicação e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Compilar o Projeto

Para compilar todos os módulos do projeto, abrir uma *shell* na pasta *root* do projeto e correr:

```
$ mvn clean install -DskipTests
```

Verifique que o projeto compila na íntegra (não existem erros de compilação).

### 1.2. *Silo*

Para correr os testes é necessário que exista um servidor a correr.
Para iniciar o servidor:

```
$ cd silo-server/
$ mvn exec:java -Dinstance="1"
```

Se vir este *output* então o server foi inicializado corretamente:

```
SiloServerApp
Received 6 arguments
arg[0] = localhost
arg[1] = 2181
arg[2] = 1
arg[3] = localhost
arg[4] = 8081
arg[5] = 1
Server started
<Press enter to shutdown>
```

### 1.3. *Eye*

Para testar o cliente eye corra abra uma nova *shell* na pasta *root* do projeto.
corra:

```
$ cd eye/
```

Vamos inicializar o sevidor com 2 câmeras diferentes e com as respetivas observações.
Para isso e preciso correr:


```
$ mvn exec:java -Dexec.args="localhost 2181 Alameda -25.284736 30.621354 1" < ../demo/Data/input1.txt
```

Se vir este *output* então a primeira câmera e os seus reports foram registados corretamente:

```
EyeApp
Received 6 arguments
arg[0] = localhost
arg[1] = 2181
arg[2] = Alameda
arg[3] = -25.284736
arg[4] = 30.621354
arg[5] = 1
Connected to specified replica at localhost:8081
Camera was sucessfully registered
Sucessfully reported 9 items
Sucessfully reported 6 items
Exiting...
```

E para a segunda câmera é preciso correr:

```
$ mvn exec:java -Dexec.args="localhost 2181 Tagus -25.284736 30.621354 1" < ../demo/Data/input2.txt
```

Se vir este *output* então a segunda câmera e os seus reports foram registados corretamente:

```
EyeApp
Received 6 arguments
arg[0] = localhost
arg[1] = 2181
arg[2] = Tagus
arg[3] = -25.284736
arg[4] = 30.621354
arg[5] = 1
Connected to specified replica at localhost:8081
Camera was sucessfully registered
Sucessfully reported 3 items
Sucessfully reported 2 items
```

Depois de executar os comandos acima já temos o que é necessário para testar o sistema. 

## 2. Teste das Operações

Nesta secção vamos correr os comandos necessários para testar todas as operações. 
Cada subsecção é respetiva a cada operação presente no *silo*.

### 2.1. *cam_join*

Esta operação já foi testada na preparação do ambiente, no entanto ainda é necessário testar algumas restrições.

2.1.1. Teste das câmeras com nome duplicado e coordenadas diferentes.  
Para realizar este teste é preciso correr:

```
$ mvn exec:java -Dexec.args="localhost 2181 Tagus 1 2 1"
```

Este teste deve dar o seguinte erro:

```
Camera with this name already exists
```

2.1.2. Teste do tamanho do nome.  
Para realizar este teste é preciso correr:

```
$ mvn exec:java -Dexec.args="localhost 2181 a 1 2 1"
$ mvn exec:java -Dexec.args="localhost 2181 aadadasdadasdasdasd 1 2 1"
```

Ambos os testes devem imprimir o erro:

```
Caught exception with code INVALID_ARGUMENT and description: INVALID_ARGUMENT: Name length is illegal!
```

### 2.2. *cam_info*

Esta operação não tem nenhum comando específico associado e portanto apenas é testável com os testes de integração (Ver no fim como executar).

### 2.3. *report*

Para as próximas operações é necessário ter um cliente spotter ligado. Para isso deve executar:

```
$ cd ../spotter/
$ mvn exec:java -Dexec.args="localhost 2181 1"
```

Se vir o seguinte *output* então o cliente spotter foi lançado corretamente.

```
Received 3 arguments
arg[0] = localhost
arg[1] = 2181
arg[2] = 1
Connected to specified replica at localhost:8081
```

Esta operação já foi testada acima na preparação do ambiente, mas no entanto falta testar o sucesso do comando *zzz*.
Para isso corra: 

```
trail car 20SD30
```

O resultado desta operação deve ser:

```
car,20SD30,2020-05-02T12:15:10,Tagus,-25.284736,30.621354
car,20SD30,2020-05-02T12:15:05,Tagus,-25.284736,30.621354
```

**ATENÇÃO:** as datas não serão as mesmas, o importante é notar a diferença de 5 segundos entre a primeira e a segunda observação

### 2.4. *track*

Esta operação vai ser testada utilizando o comando *spot* com um identificador.

2.4.1. Teste com uma pessoa que não exista:

```
spot person 1
```

não deve ser imprimido nenhum output

2.4.2. Teste com uma pessoa:

```
> spot person 5638246
person,5638246,<timestamp>,Alameda,-25.284736,30.621354
```

2.4.3. Teste com um carro:

```
> spot car 20SD23
car,20SD23,<timestamp>,Tagus,-25.284736,30.621354
```

### 2.5. *trackMatch*

Esta operação vai ser testada utilizando o comando *spot* com um fragmento de identificador.

2.5.1. Teste com uma pessoa que não exista:

```
> spot person 4321*
```

não deve ser imprimido nada.

2.5.2. Testes com uma pessoa:

```
> spot person 563824* 
person,5638246,<timestamp>,Alameda,-25.284736,30.621354

> spot person *638246
person,5638246,<timestamp>,Alameda,-25.284736,30.621354

> spot person 563*246
person,5638246,<timestamp>,Alameda,-25.284736,30.621354
```

2.5.3. Testes com duas ou mais pessoas:

```
> spot person 51*
person,5111111,<timestamp>,Tagus,-25.284736,30.621354
person,5111112,<timestamp>,Alameda,-25.284736,30.621354
person,5112112,<timestamp>,Alameda,-25.284736,30.621354

> spot person *2
person,5111112,<timestamp>,Alameda,-25.284736,30.621354
person,5112112,<timestamp>,Alameda,-25.284736,30.621354

> spot person 511*112
person,5111112,<timestamp>,Alameda,-25.284736,30.621354
person,5112112,<timestamp>,Alameda,-25.284736,30.621354
```

2.5.4. Testes com um carro:

```
> spot car 20SD3*
car,20SD30,<timestamp>,Tagus,-25.284736,30.621354

> spot car *0SD30
car,20SD30,<timestamp>,Tagus,-25.284736,30.621354

> spot car 20*D30
car,20SD30,<timestamp>,Tagus,-25.284736,30.621354
```

2.5.5. Testes com dois ou mais carros:

```
> spot car 20SD*
car,20SD21,<timestamp>,Alameda,-25.284736,30.621354
car,20SD23,<timestamp>,Tagus,-25.284736,30.621354
car,20SD24,<timestamp>,Tagus,-25.284736,30.621354
car,20SD30,<timestamp>,Tagus,-25.284736,30.621354

> spot car *1
car,10SD21,<timestamp>,Alameda,-25.284736,30.621354
car,20HD21,<timestamp>,Alameda,-25.284736,30.621354
car,20SD21,<timestamp>,Alameda,-25.284736,30.621354

> spot car 20*D21
car,20HD21,<timestamp>,Alameda,-25.284736,30.621354
car,20SD21,<timestamp>,Alameda,-25.284736,30.621354

```

### 2.6. *trace*

Esta operação vai ser testada utilizando o comando *trail* com um identificador.

2.6.1. Teste com uma pessoa que nao exista:

```
> trail person 123
```

não deve ser imprimido nada na consola.

2.6.2. Teste com uma pessoa:

```
> trail person 5111111
person,5111111,<timestamp>,Tagus,-25.284736,30.621354
person,5111111,<timestamp>,Alameda,-25.284736,30.621354
person,5111111,<timestamp>,Alameda,-25.284736,30.621354
```

2.6.3. Teste com um carro que não existe:

```
> trail car 10HH20
```

não deve ser imprimido nada na consola.

2.6.4. Teste com um carro:

```
> trail car 20SD24
car,20SD24,<timestamp>,Tagus,-25.284736,30.621354
car,20SD24,<timestamp>,Alameda,-25.284736,30.621354
car,20SD24,<timestamp>,Alameda,-25.284736,30.621354
car,20SD24,<timestamp>,Alameda,-25.284736,30.621354
```

### 2.7. *Help*

Para ver todos os comandos suportados pelo spotter pode ser feito:

```
> help
```

### 2.8. *Exit*

Para sair da aplicação spotter pode ser usado o comando:

```
> exit
```

e a aplicação encerra

## 3. Testes de integração

Para correr os testes de integração que cobrem todas as operações do SILO é necessário ter o projeto e todos os módulos compilados.
Para isso deve-se abrir uma *shell* na pasta *root* do projeto e correr:

```
$ mvn clean install -DskipTests
```

Verifique que não há nenhum erro de compilação e de seguida corra:

```
cd silo-client/
mvn verify
```

Os 47 testes devem ter corrido sem erros ou falhas, testando assim por completo todas as operações do Silo.

## 4. Replicação e tolerância a faltas

Nesta secção vai ser mostrado o funcionamento do protocolo de gossip e os mecanismos de tolerância a faltas. Para lançar os servidores deve ser dado como argumento da linha de comandos o número da instância e o número total de réplicas (ver em 4.1 como o fazer) ou adicionar ao pom.xml do servidor. É necessário também ter todos os módulos compilados (ver 1.1) e o servidor de nomes (ZooKeeper) a correr. 

### 4.1 Funcionamento normal

Para este teste é necessário lançar duas réplicas, um eye e um spotter. Para lançar as réplicas devem ser corridos os seguintes comandos na pasta *silo-server* (uma réplica em cada consola):

```
$ mvn exec:java -Dinstance="1" -DreplicaCount="2"
$ mvn exec:java -Dinstance="2" -DreplicaCount="2"
```

para lançar o spotter deve ser executado o seguinte comando da pasta *spotter*:

```
$ mvn exec:java -Dinstance="1"
```

Para se ver o funcionamento do gossip a funcionar lançe um eye:

```
$ mvn exec:java -Dinstance="2"
```

Pode observar na consola da réplica 2 (nos próximos 30 segundos) a seguinte mensagem:

```
Sending gossip message to localhost:8081
```

que corresponse ao gossip do registo da câmera. Do lado da réplica 1 pode ser vista a mesagem indicativa da receção do gossip:

```
Caught gossip
```

De seguida faça um report no eye da seguinta forma:

```
> person,12345
> 
> 
```

Antes de ser feito o gossip confirme que o spotter não consegue ver o novo report (não deve ser imprimido nada na consola):

```
> spot person 12345
```

depois de ser feito o gossip (pode ser visto na consola da réplica 2), volte a fazer o comando de spot e poderá ver o report feito na outra réplica.

```
person,12345,<timestamp>,Alameda,-25.284736,30.621354
```

### 4.2 Tolerância a faltas

Neste teste é mostrado o mecanismo de tolerância a faltas num caso geral. Neste teste são precisas duas réplicas (como lançar as réplicas está descrito na secção 4.1) e um spotter a ser lançado da seguinte forma da pasta correspondente:

```
$ mvn exec:java
```

Simular falta encerrando a réplica a que se ligou.

Executar o comando:

```
> spot person 12345
```

Como a réplica não está disponível o mecanismo de tolerância a faltas troca para a outra réplica e re-executa o comando (não deve ser apresentado output):
O mecanismo de tolerância a faltas deve exprimir-se da seguinte forma:

```
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Retrying in 2 seconds...
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Retrying in 4 seconds...
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Trying to contact another replica to tolerate fault...
Trying to contact replica at localhost:<porto da nova réplica>...
Found new replica at localhost:<porto da nova réplica>
Retrying operation...
```

De seguida encerrar a última réplica e re-executar o comando do spotter:

```
> spot person 12345
```

O mecanismo de tolerância a faltas deve revelar-se do seguinte modo tendo em conta que já não existem mais réplicas disponíveis:

```
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Retrying in 2 seconds...
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Retrying in 4 seconds...
Failed to contact replica at localhost:<porto da réplica a que se ligou>
Trying to contact another replica to tolerate fault...
Error connecting to random replica:Tried all known replicas without success
```

Se depois eventualmente uma réplica voltar a estar disponível basta executar um comando qualquer e a conexão fica restabelicida.

### 4.3 Leituras coerentes e tolerância a faltas

Neste teste é mostrado o mecanismo de tolerância a faltas em funcionamento bem como a coerência das leituras nos clientes. Para este teste são precisos duas réplicas (como lançar as réplicas está descrito na secção 4.1) bem como um eye e um spotter.
Para o spotter na pasta correspondente:

```
$ mvn exec:java
```

O eye deve ligar-se à mesma instância a que se ligou o spotter:

```
$ mvn exec:java -Dinstance="<instancia do spotter>"
```

**Os próximos passos devem ser executados com rapidez e antes de ser feito o gossip do report**

no eye deve ser feito um report da seguinte forma:

```
> person,12345
>
>
```

o spotter lê esse report:

```
> spot person 12345
person,12345,<timestamp>,Alameda,-25.284736,30.621354
```

E a réplica deve ser encerrada **Antes de ser feito o gossip** (para ser simulada uma falta). Se o gossip foi feito o teste deve ser reiniciado.
De seguida o spotter deve ler o report novamente:

```
> spot person 12345
```

É possível observar o comportamento da tolerância a faltas(Spotter a mudar de instância). Pode ser observado também que apesar da réplica nova não ter conhecimento do report é mostrado ao cliente uma versão coerente da leitura:

```
person,12345,<timestamp>,Alameda,-25.284736,30.621354
```

### 4.4 Dependência Causal

Neste teste é mostrado como foi resolvido as dependências causais entre os reports e o registo da câmera. Vão ser precisos duas réplicas e um eye (Como lançar as réplicas está na seccção 4.1).

Desta vez não especifique a instância a que a câmera se deve ligar:

```
$ mvn exec:java
```

Antes de ser feito o gossip encerre a replica a que a camera se conectou.
Tente fazer um report:

```
> person,12345


```

Poderá ver o mecanismo de tolerância a faltas em funcionamento e a operação de report a ser executada mesmo sem ter sido feito (explicitamente) o gossip do registo da câmera.

## 5. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código. Foram omitidos nos testes de tolerância a faltas/replicação o exemplo para todos os comandos existentes. É importante relembrar que as operações de controlo não estão implementadas de forma distribuída.

