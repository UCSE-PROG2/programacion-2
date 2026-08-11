# --- Etapa 1: build -----------------------------------------------------
# Imagen con el toolchain completo de Go, solo para compilar. No forma parte
# de la imagen final (ver Clase 1 -- "un binario de go build es
# autocontenido": no necesita la JVM ni nada instalado aparte para correr).
FROM golang:1.25 AS builder

WORKDIR /app

# Se copian go.mod/go.sum primero y se descargan dependencias ANTES de copiar
# el resto del código: mientras el código fuente cambie pero las dependencias
# no, Docker reusa esta capa de cache y no vuelve a descargar nada.
COPY go.mod go.sum ./
RUN go mod download

COPY . .

# CGO_ENABLED=0 genera un binario 100% estático (sin depender de libc del
# sistema) -- necesario para que corra en la imagen "scratch"/alpine mínima
# de la etapa 2.
RUN CGO_ENABLED=0 GOOS=linux go build -o /biblioteca-api ./cmd/api

# --- Etapa 2: imagen final ------------------------------------------------
# alpine es una imagen mínima (~5MB base) -- acá no hace falta ni el
# compilador de Go ni el código fuente, solo el binario ya compilado.
FROM alpine:3.20

WORKDIR /app
COPY --from=builder /biblioteca-api .

EXPOSE 8080

CMD ["./biblioteca-api"]
