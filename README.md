# RentCar

> Protótipo acadêmico para solicitar e acompanhar aluguéis de automóveis, com análise financeira por agentes e formalização de contratos.

<p align="center">
  <img src="./Codigo/frontend/src/assets/LogoRentCar.png" alt="Logo RentCar" width="220">
</p>

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?logo=postgresql&logoColor=white)](https://supabase.com/)

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Links úteis](#links-úteis)
- [Funcionalidades](#funcionalidades)
- [Aderência ao enunciado](#aderência-ao-enunciado)
- [Tecnologias e arquitetura](#tecnologias-e-arquitetura)
- [Documentação e diagramas](#documentação-e-diagramas)
- [Como executar](#como-executar)
- [Configuração](#configuração)
- [Testes](#testes)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Demonstração](#demonstração)
- [Contribuição e agradecimentos](#contribuição-e-agradecimentos)
- [Autor e licença](#autor-e-licença)

## Sobre o projeto

O RentCar é um sistema web desenvolvido para o Laboratório 02 da disciplina DIAW, no curso de Engenharia de Software da PUC Minas. Ele apoia o fluxo de aluguel de automóveis: clientes registram pedidos e acompanham seu andamento; agentes de empresas ou bancos analisam os dados financeiros e conduzem pedidos aprovados até a formalização do contrato.

O sistema é composto por uma aplicação React + Vite, uma API REST em Spring Boot e persistência PostgreSQL no Supabase. A autenticação é obrigatória para as operações protegidas, com permissões distintas para clientes, agentes e administradores.

## Links úteis

- [Repositório no GitHub](https://github.com/LukasGom3s/RentCar)
- [Documentação do projeto](./Docs/)
- [Histórias de usuário](./Docs/HistoriasDeUsuario.md)
- [Diagrama de casos de uso](./Docs/CasosDeUso.md)
- [Spring Boot](https://spring.io/projects/spring-boot) · [React](https://react.dev/) · [Vite](https://vite.dev/) · [Supabase](https://supabase.com/docs)

## Funcionalidades

- Cadastro, login e recuperação de senha de clientes.
- Consulta, atualização e exclusão do perfil do cliente, incluindo até três fontes de renda.
- Criação, consulta com filtro de status, edição e cancelamento de pedidos de aluguel.
- Fila de análise financeira para agentes, com consulta dos rendimentos e registro de parecer positivo ou negativo.
- Alteração do automóvel e do período por agentes enquanto o pedido está em análise.
- Encaminhamento de pedidos aprovados para consideração da execução do contrato.
- Formalização e consulta de contratos; agentes bancários podem registrar detalhes opcionais do crédito.
- Área administrativa para cadastrar, consultar e editar agentes de empresas e bancos.
- Frota demonstrativa criada automaticamente quando a tabela de automóveis está vazia.

## Aderência ao enunciado

Esta tabela compara o código e os modelos atualmente presentes no repositório com o enunciado do Laboratório 02. “Parcial” indica que existe um fluxo relacionado, mas o requisito não está representado integralmente.

| Requisito | Situação | Evidência / observação |
|---|---|---|
| Java 25, Maven, React + Vite, Supabase/PostgreSQL e autenticação | ✅ Implementado | As tecnologias estão configuradas nos módulos `Codigo/backend/rentcar` e `Codigo/frontend`. |
| Spring Boot 4.0.x | ⚠️ Divergente | O `pom.xml` atualmente usa Spring Boot 4.1.2, não a série 4.0.x indicada no enunciado. |
| Cadastro e autenticação de clientes e agentes | ⚠️ Parcial | As rotas de negócio são protegidas por Spring Security, mas `GET /api/automoveis` permite consulta do catálogo sem login. |
| CRUD do perfil do cliente e limite de três empregadores, cada qual com rendimento positivo | ✅ Implementado | Cadastro, RG, CPF, nome, morada, profissão e rendimentos são validados; o perfil permite consulta, atualização e exclusão. A exclusão é bloqueada se o cliente já tiver pedidos registrados. |
| Cliente criar, consultar, modificar e cancelar pedidos | ✅ Implementado | As operações são restritas ao próprio cliente e respeitam as regras de status. |
| Agente modificar e avaliar pedidos; parecer financeiro registrado | ✅ Implementado | O agente edita pedidos em análise e registra parecer positivo ou negativo, visível ao cliente. |
| Pedido aprovado encaminhado para consideração da execução e contrato consultável | ✅ Implementado | O pedido passa por `AGUARDANDO_EXECUCAO` e, após formalização, por `CONTRATO_EXECUTADO`. |
| Back-end Spring MVC, front-end web dinâmico e persistência Supabase | ✅ Implementado | A API Spring é organizada em controllers/services/repositories/models; o front-end usa React + Vite e o acesso a dados usa PostgreSQL/JPA. |
| Registrar se o automóvel pertence a cliente, empresa ou banco | ⚠️ Parcial | O catálogo guarda matrícula, ano, marca, modelo e placa, mas o modelo `Automovel` não possui proprietário. A frota atual é demonstrativa; não há cadastro administrativo de veículos. |
| Associar aluguel a contrato de crédito concedido por banco agente | ⚠️ Parcial | O contrato aceita um texto opcional de detalhes bancários, mas não existe uma entidade de crédito separada nem uma associação estruturada ao banco concedente. |
| Modelagem de casos de uso, histórias, classes e pacotes alinhada ao código | ⚠️ Parcial | Os quatro documentos estão em `Docs/`, mas os diagramas visuais de classes e pacotes ainda não refletem integralmente o modelo e os pacotes atuais. Devem ser atualizados antes da entrega final. |
| Repositório GitHub com versões dos modelos e código final | ⚠️ A confirmar | O repositório e os documentos estão presentes localmente. A publicação das alterações atuais no GitHub depende de commit e push; o estado remoto não é verificado por este README. |

**Conclusão:** os principais fluxos de autenticação, perfil de cliente, pedidos, análise financeira e contratos estão implementados. O projeto ainda não atende integralmente ao enunciado: faltam a propriedade do automóvel no modelo, a associação estruturada do crédito ao banco, a atualização dos diagramas visuais e o alinhamento da versão do Spring Boot. A apresentação final do protótipo também deve ser realizada pela equipe.

## Tecnologias e arquitetura

### Tecnologias

- **Back-end:** Java 25, Spring Boot 4.1.2, Spring Security, Spring Data JPA/Hibernate e Maven.
- **Front-end:** JavaScript, React 18, React Router e Vite 5.
- **Banco de dados:** PostgreSQL hospedado no Supabase.
- **Autenticação:** sessão HTTP com Spring Security; reCAPTCHA habilitado por padrão e configurável para testes locais.

### Organização

O front-end contém páginas, componentes reutilizáveis e serviços HTTP. Ele se comunica com a API REST e envia credenciais de sessão nas requisições. O back-end segue uma organização em camadas:

1. **Controllers** recebem requisições REST e definem os formatos de entrada e saída.
2. **Services** aplicam as regras de negócio e as transições de estado.
3. **Repositories** acessam o banco via Spring Data JPA.
4. **Models** representam clientes, agentes, automóveis, pedidos, empregadores e contratos.
5. **Configuração e segurança** definem autenticação, autorização, CORS e filtros.

O Hibernate atualiza o schema durante a inicialização (`spring.jpa.hibernate.ddl-auto=update`). O backend também verifica a restrição de status dos pedidos no startup.

## Documentação e diagramas

- [Casos de uso](./Docs/CasosDeUso.md)
- [Histórias de usuário](./Docs/HistoriasDeUsuario.md)
- [Diagrama de classes](./Docs/DiagramaDeClasses.md)
- [Diagrama de pacotes (visão lógica)](./Docs/DiagramaDePacotes.md)
- [Diagrama visual de casos de uso](./Docs/images/DiagramaCasosDeUso.png)
- [Diagrama visual de classes](./Docs/images/DiagramaDeClasses.png)
- [Diagrama visual de pacotes](./Docs/images/DiagramaDePacotes.png)

## Como executar

### Pré-requisitos

- JDK 25.
- Node.js e npm.
- Acesso a um banco PostgreSQL compatível (o projeto está configurado para Supabase).

### Instalar dependências

No PowerShell, na raiz do repositório:

```powershell
Push-Location .\Codigo\frontend
npm install
Pop-Location
```

As dependências do back-end são resolvidas pelo Maven Wrapper na primeira execução.

### Configurar e iniciar o back-end

Configure as variáveis de ambiente descritas abaixo e execute:

```powershell
Push-Location .\Codigo\backend\rentcar
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### Iniciar o front-end

Em outro terminal:

```powershell
Push-Location .\Codigo\frontend
npm run dev
```

O Vite está configurado para servir a aplicação em `http://localhost:3000`.

## Configuração

O backend importa opcionalmente `application-local.properties`, que é ignorado pelo Git. Use esse arquivo local ou variáveis de ambiente para configurar credenciais; **não versione senhas, tokens ou chaves privadas**.

| Variável | Uso |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL/Supabase. |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco. |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_ADMIN_NAME` | Credenciais e nome da conta administrativa inicial. |
| `FRONTEND_URL` | URL usada nos links de recuperação de senha enviados por e-mail. |
| `APP_CORS_ALLOWED_ORIGINS` | Origens do front-end autorizadas pelo CORS, separadas por vírgula. |
| `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Credenciais SMTP usadas pelos fluxos de e-mail. |
| `RECAPTCHA_ENABLED` | Ativa o CAPTCHA; o padrão é `true`. Use `false` somente para testes locais. |
| `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY` | Chaves do reCAPTCHA. |
| `VITE_API_URL` | URL base da API no front-end; por padrão, `http://<host-do-navegador>:8080`. Não acrescente `/api`. |

Para desenvolvimento local sem CAPTCHA, configure `RECAPTCHA_ENABLED=false` no ambiente do backend ou em `application-local.properties`. Em produção, mantenha o CAPTCHA ativo e configure as chaves apropriadas.

O envio de e-mails requer configuração SMTP. O cadastro de cliente e o fluxo de recuperação de senha dependem dessa configuração conforme o fluxo utilizado. A conta do PostgreSQL também precisa de permissões de alteração de schema, pois o Hibernate e a verificação de status dos pedidos atualizam estruturas no startup.

> **Atenção antes de publicar:** confira `application.properties` e o histórico do Git para garantir que nenhum segredo real esteja versionado. Use variáveis de ambiente ou o arquivo local ignorado e rotacione credenciais que já tenham sido expostas.

## Testes

Execute os testes do back-end:

```powershell
Push-Location .\Codigo\backend\rentcar
.\mvnw.cmd test
```

Gere o build de produção do front-end:

```powershell
Push-Location .\Codigo\frontend
npm run build
```

O front-end não possui atualmente um script de testes automatizados configurado no `package.json`.

## Estrutura do repositório

```text
.
├── Codigo/
│   ├── backend/rentcar/
│   │   ├── src/main/java/com/example/rentcar/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── exception/
│   │   │   ├── models/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   ├── src/test/java/
│   │   ├── src/main/resources/
│   │   ├── mvnw.cmd
│   │   └── pom.xml
│   └── frontend/
│       ├── public/
│       ├── src/
│       │   ├── assets/
│       │   ├── components/
│       │   ├── pages/
│       │   └── services/
│       └── package.json
└── Docs/
    ├── images/
    ├── CasosDeUso.md
    ├── DiagramaDeClasses.md
    ├── DiagramaDePacotes.md
    └── HistoriasDeUsuario.md
```

## Demonstração

Não há uma versão pública hospedada nem capturas de tela mantidas no repositório. Para experimentar o protótipo, configure o backend e o banco e inicie o front-end seguindo [Como executar](#como-executar). A logo e os diagramas disponíveis estão na estrutura do projeto.

## Contribuição e agradecimentos

Este é um projeto acadêmico. Sugestões podem ser registradas como issues ou pull requests no [repositório](https://github.com/LukasGom3s/RentCar). Agradecemos à PUC Minas e ao professor da disciplina pelas orientações e pelo enunciado que norteou o desenvolvimento.

## Autor e licença

- **Autor:** [Lucas Gomes (@LukasGom3s)](https://github.com/LukasGom3s)
- **Repositório:** [LukasGom3s/RentCar](https://github.com/LukasGom3s/RentCar)
- **Licença:** não há um arquivo `LICENSE` no repositório; os termos de uso e redistribuição ainda não foram definidos.

Projeto acadêmico desenvolvido para a PUC Minas, na disciplina DIAW.
