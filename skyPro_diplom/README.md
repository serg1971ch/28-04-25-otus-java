# Приложение по перепродаже вещей (Доска объявлений)

Приложение по перепродаже вещей (Доска объявлений)
Приложение состоит из фронтенд части, которая находится в Dockerfile. Запустить можно фронтенд с помощью установленного Docker, для этого нужно открыть командную строку (или терминал) и выполнить следующую команду:

docker run -p 3000:3000 --rm ghcr.io/bizinmitya/front-react-avito:v1.21

После выполнения команды frontend запустится на порту 3000 и можно будет зайти на него через браузер по адресу: http://localhost:3000

java -jar ads-0.0.1-SNAPSHOT.jar --spring.datasource.url=URL_OF_DATABASE --spring.datasource.username=DATABASE_USERNAME --spring.datasource.password=DATABASE_PASSWORD --server.port=NUMBER_PORT

Для размещения фотографий объявлений и фотографий пользователей приложения необходимо, чтобы в папке с файлом для запуска приложения (ads-0.0.1-SNAPSHOT.jar) распологалось две папки: user_images и ad_images.

Приложение позволяет размещать объявления с фотографиями. Делать комментарии к объявлениям.

# Стэк технологий
- Язык и окружение
  - Java 17
  - Maven
  - Spring Boot
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - REST
  - Git
  - Swagger
  - Lombok
  - Liquibase
- База данных
  - PostgreSQL
- Тестирование
  - JUnit
  - Mockito
- Прочее
  - Docker

Над приложением работали: Шлапаков Алексей, Христюк Андрей, Тюльков Станислав, Шибанов Сергей.
