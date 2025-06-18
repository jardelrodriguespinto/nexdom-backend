# Projeto de Teste - Nexdom

Este projeto foi desenvolvido como parte de um teste técnico para a empresa Nexdom. Ele utiliza tecnologias modernas para implementar e testar funcionalidades relacionadas à gestão de produtos e movimentações de estoque.

## Tecnologias Utilizadas

- **Java**: Linguagem principal utilizada no desenvolvimento do projeto.
- **Spring Boot**: Framework para criação de aplicações Java, utilizado para configurar e gerenciar o backend.
- **SQL**: Utilizado para persistência de dados no banco de dados.
- **Maven**: Ferramenta de gerenciamento de dependências e build do projeto.
- **JUnit 5**: Framework de testes utilizado para garantir a qualidade do código.
- **Mockito**: Biblioteca para criação de mocks e simulação de dependências nos testes.

## Estrutura do Projeto

O projeto está organizado da seguinte forma:

- `src/main/java`: Contém o código-fonte principal da aplicação.
    - **Camadas principais**:
        - **Controller**: Responsável por expor as APIs REST.
        - **Service**: Contém a lógica de negócios.
        - **Repository**: Interface para acesso ao banco de dados.
        - **Model**: Classes que representam as entidades do sistema.
        - **DTO**: Objetos de transferência de dados utilizados para comunicação entre camadas.
- `src/test/java`: Contém os testes unitários.
    - **Testes principais**:
        - Testes de serviços para validação da lógica de negócios.
        - Testes de repositórios para validação de consultas ao banco de dados.

## Funcionalidades Implementadas

[//]: # ()
- **Gestão de Produtos**:
    - Cadastro e consulta de produtos.
    - Cálculo de lucro com base nas movimentações de estoque.

- **Movimentações de Estoque**:
    - Registro de entradas e saídas de produtos.
    - Cálculo de valores médios de compra e venda.

- **Cálculo de Lucro**:
    - Cálculo do lucro unitário e total com base nas movimentações de estoque.

## Testes

Os testes foram implementados para garantir a qualidade e a confiabilidade do código. As principais validações incluem:

- Cálculo correto de valores de compra, venda e lucro.
- Validação de regras de negócio, como a existência de produtos antes de realizar operações.
- Testes de integração com o banco de dados.

## Como Executar o Projeto

1. **Pré-requisitos**:
    - Java 17 ou superior.
    - Maven instalado.
    - Banco de dados configurado (ex.: MySQL, PostgreSQL).

2. **Passos**:
    - Clone o repositório.
    - Configure o arquivo `application.properties` com as credenciais do banco de dados.
    - Execute o comando `mvn spring-boot:run` para iniciar a aplicação.

3. **Executar os Testes**:
    - Utilize o comando `mvn test` para rodar os testes unitários.

## Contato

Para mais informações, entre em contato com o desenvolvedor responsável pelo projeto.## Documentação da API

Este projeto utiliza o **Swagger** para documentar e testar as APIs REST de forma interativa. O Swagger facilita a visualização das rotas disponíveis, seus parâmetros e respostas.

### Acessando a Documentação

Após iniciar a aplicação, a documentação da API estará disponível no seguinte endereço:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Configuração

O Swagger foi configurado utilizando a dependência `springdoc-openapi` no projeto. Para mais informações sobre como configurar ou personalizar a documentação, consulte a [documentação oficial do Springdoc](https://springdoc.org/).


### Acessando a Documentação no Ambiente de Produção
Além do ambiente local, a documentação da API também está disponível no ambiente de produção, onde o projeto foi implantado. Você pode acessá-la no seguinte endereço:

- **Swagger UI (Produção)**: [https://nexdom-backend-production.up.railway.app/swagger-ui/index.html](https://nexdom-backend-production.up.railway.app/swagger-ui/index.html)

### Acessando o Banco de Dados H2 no Ambiente de Produção

O projeto utiliza o banco de dados H2 em memória para o ambiente de produção. As configurações para acessar o console do H2 são as seguintes:

- **URL do H2 Console (Produção)**: [https://nexdom-backend-production.up.railway.app/h2-console/](https://nexdom-backend-production.up.railway.app/h2-console/)
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password**: *(deixe em branco)*

Certifique-se de que o console do H2 está habilitado e acessível no ambiente de produção.