# Diagrama de Pacotes (Visão Lógica) - Sistema de Aluguel de Carros

## 1. Diagrama Visual

![Diagrama de Pacotes](images/DiagramaDePacotes.png)

## 2. Camadas e Componentes

### 2.1. Frontend
* **React + Vite:** Responsável pela interface do usuário. Comunica-se com o back-end exclusivamente via requisições HTTP (REST API). Garante uma navegação fluida e dinâmica, construindo dinamicamente as páginas web.

### 2.2. Backend (Spring Boot 4.0.x)
Todo o back-end está contido no pacote base `com.example.rentcar` e é subdividido nas seguintes camadas lógicas:

* **config (`<<Configurações>>`):** Classes de configuração geral do projeto.
* **security (`<<Autenticação>>`):** Intercepta as requisições para garantir o cumprimento do requisito obrigatório de autenticação. Nenhuma rota de negócios pode ser acessada sem um login válido.
* **controllers (`<<REST API>>`):** Recebe as requisições HTTP do front-end, delega o processamento para a camada de serviços e retorna as respostas padronizadas.
* **services (`<<Regras de Negócio>>`):** Contém as regras de negócio, como a validação do limite de rendimentos de um cliente, processamento de análise financeira pelos agentes e geração de contratos.
* **repositories (`<<Acesso ao banco de dados>>`):** Interfaces Spring Data responsáveis pela persistência de dados. Fazem a ponte entre a aplicação Java e o banco de dados.
* **models (`<<Entidades>>`):** Classes de domínio (Cliente, Agente, Pedido, Automovel, etc.) que são mapeadas diretamente para as tabelas do banco de dados relacional.
* **dto (`<<Objetos de Dados>>`):** Objetos de Transferência de Dados. Usados para formatar e restringir os dados que entram e saem pelas APIs (ex: não expor senhas ao listar usuários).

### 2.3. Base de Dados
* **Supabase (PostgreSQL):** Banco de dados relacional hospedado na nuvem que armazena todas as informações persistentes do sistema. O acesso é feito através dos `repositories` do Spring Boot utilizando JDBC/JPA.