# Ejemplo: API REST de clima (Cliente-Servidor)

Este ejemplo aplica el **patrón cliente-servidor**: el servidor expone servicios (API de clima) y el cliente los consume (app o navegador).

## Estructura

- **servidor/** — Punto de entrada, controladores, servicio y DTOs del API.
- **cliente/** — Cliente HTTP que consume el API y muestra resultados.

## Configuración

- `config.properties` o constantes con `URL_BASE` del servidor (ej. `http://localhost:8080`) para que el cliente sepa a dónde conectarse. Puede ir en la raíz del proyecto o en `cliente/`.

## Cómo comprobar que está bien aplicado

- El servidor puede ejecutarse solo y responder a `curl` o Postman.
- El cliente no contiene lógica de negocio del clima; solo consume la API.
- Si cambias el puerto o la URL del servidor, solo se toca la configuración del cliente, no su lógica.
