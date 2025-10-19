# CQRS Event Sourcing Kafka - Guía de Ejecución

## 1. Compilar los JARs

Desde la raíz de cada microservicio, ejecuta:

```powershell
cd bank-account/account.cmd
./mvnw clean package

cd ../account.query
./mvnw clean package

cd ../account.common
./mvnw clean package

cd ../../cqrs-es/cqrs.core
./mvnw clean package
```

Esto generará los archivos `app.jar` en las carpetas `target` correspondientes.

---

## 2. Construir los contenedores Docker

Desde la raíz del proyecto, ejecuta:

```powershell
docker-compose build
```

Esto construirá las imágenes de todos los servicios definidos en `docker-compose.yml`.

---

## 3. Levantar los contenedores

Para iniciar todos los servicios:

```powershell
docker-compose up -d
```

Esto levantará los microservicios, Kafka, MongoDB, MySQL, Zookeeper, etc.

---

## 4. Verificar los logs

Para ver los logs de un servicio específico, por ejemplo el de consulta:

```powershell
docker-compose logs -f account-query
```

---

## 5. Resetear el offset del consumidor (opcional, para pruebas de reproducción de eventos)

1. Detén el servicio de consulta:
	```powershell
	docker-compose stop account-query
	```

2. Resetea el offset en Kafka:
	```powershell
	docker exec -it kafka /usr/bin/kafka-consumer-groups --bootstrap-server kafka:9092 --group bankaccConsumer --reset-offsets --to-earliest --execute --topic account-events
	```

3. Inicia el servicio de consulta:
	```powershell
	docker-compose start account-query
	```

---

## 6. Acceso a Kafdrop (opcional)

Puedes visualizar los topics y mensajes en Kafka accediendo a [http://localhost:18000](http://localhost:18000).

---

## 7. Notas

- Asegúrate de tener Docker y Maven instalados.
- Los puertos expuestos pueden cambiar según tu configuración en `docker-compose.yml`.
- Si tienes problemas con los offsets, revisa la sección de reseteo.
