-- Execute no PostgreSQL conectado ao banco postgres como um usuário administrador.
CREATE ROLE mercado WITH LOGIN PASSWORD 'mercado_local';
CREATE DATABASE mercado_express OWNER mercado;
GRANT ALL ON SCHEMA public TO mercado;
