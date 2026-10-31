# Diagrama de Classes - Sistema de Aluguel de Carros

## 1. Diagrama Visual

![Diagrama de Classes](images/DiagramaDeClasses.png)

## 2. Dados e Entidades

### 2.1. Usuários e Autenticação
* **Usuario (`<<abstract>>`):** Superclasse que centraliza a obrigatoriedade de autenticação. Todo usuário do sistema possui um `id`, `login` e `palavraPasse`.
* **Cliente:** Herda de `Usuario`. Representa as pessoas físicas que realizam os pedidos. Armazena dados pessoais (`rg`, `cpf`, `nome`, `morada`, `profissao`).
* **Agente:** Herda de `Usuario`. Representa as entidades corporativas. Possui um `nome` e um `tipo` (definido pelo enumerador `TipoAgente` como EMPRESA ou BANCO).

### 2.2. Entidades Relacionadas ao Cliente
* **EntidadeEmpregadora:** Representa as fontes de renda do cliente. Possui forte relação de composição com o Cliente, respeitando a regra de negócio que permite o registro de até 3 empregadores com seus respectivos `rendimentoAuferido`.

### 2.3. Gestão de Veículos e Pedidos
* **Automovel:** Registra os dados dos veículos (`matricula`, `ano`, `marca`, `modelo`, `placa`). É associado de forma genérica à superclasse `Usuario`, pois pode ser propriedade de clientes, empresas ou bancos.
* **Pedido:** Centraliza a intenção de aluguel do cliente. Possui uma `dataCriacao` e um `status` (controlado pelo enumerador `StatusPedido`: PENDENTE, EM_ANALISE, APROVADO, REJEITADO, CANCELADO). Um pedido reserva exatamente um automóvel.

### 2.4. Contratos e Financiamentos
* **Contrato:** Gerado exclusivamente a partir de um pedido aprovado por um Agente. Define a `dataInicio` e `dataFim` do aluguel.
* **Credito:** Representa um financiamento que pode estar associado a um contrato. Este crédito é concedido obrigatoriamente por um Agente do tipo BANCO e possui um `valor` específico.