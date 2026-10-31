<h1 style="color: #0946B5;">
  <img src="images/LogoRentCar.png" alt="Logo Rentcar" width="80" style="vertical-align: middle; margin-right: 15px;">Diagrama de Classes
</h1>

<h2 style="color: #0946B5;">1. Diagrama Visual</h2>

![Diagrama de Classes](images/DiagramaDeClasses.png)

<br>

<h2 style="color: #0946B5;">2. Dados e Entidades</h2>

<h3 style="color: #26D0FA;">2.1. Usuários e Autenticação</h3>

* **Usuario (`<<abstract>>`):** 
  Superclasse que centraliza a obrigatoriedade de autenticação. Todo usuário do sistema possui um `id`, `login` e `palavraPasse`.
  
* **Cliente:** 
  Herda de `Usuario`. Representa as pessoas físicas que realizam os pedidos. Armazena dados pessoais (`rg`, `cpf`, `nome`, `morada`, `profissao`).
  
* **Agente:** 
  Herda de `Usuario`. Representa as entidades corporativas. Possui um `nome` e um `tipo` (definido pelo enumerador `TipoAgente` como EMPRESA ou BANCO).

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">2.2. Entidades Relacionadas ao Cliente</h3>

* **EntidadeEmpregadora:** 
  Representa as fontes de renda do cliente. Possui forte relação de composição com o Cliente, respeitando a regra de negócio que permite o registro de até 3 empregadores com seus respectivos `rendimentoAuferido`.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">2.3. Gestão de Veículos e Pedidos</h3>

* **Automovel:** 
  Registra os dados dos veículos (`matricula`, `ano`, `marca`, `modelo`, `placa`). É associado de forma genérica à superclasse `Usuario`, pois pode ser propriedade de clientes, empresas ou bancos.
  
* **Pedido:** 
  Centraliza a intenção de aluguel do cliente. Possui uma `dataCriacao` e um `status` (controlado pelo enumerador `StatusPedido`: PENDENTE, EM_ANALISE, APROVADO, REJEITADO, CANCELADO). Um pedido reserva exatamente um automóvel.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">2.4. Contratos e Financiamentos</h3>

* **Contrato:** 
  Gerado exclusivamente a partir de um pedido aprovado por um Agente. Define a `dataInicio` e `dataFim` do aluguel.
  
* **Credito:** 
  Representa um financiamento que pode estar associado a um contrato. Este crédito é concedido obrigatoriamente por um Agente do tipo BANCO e possui um `valor` específico.