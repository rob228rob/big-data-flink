## how to
- запускаем все сервисы
```plantuml
docker-compose up -d
```

сразу должны все записи от продюсера улететь в топик\
затем 

```plantuml
mvn clean package
```

копируем SHADE.jar и идем на http://localhost:8081 в UI, добавляем таску и сразу сабмитим, корневой класс com.mai.flink.ConsumerFlinkJob

готово

можно зайти на http://localhost:7080 и глянуть в ui че там в топиках :)\
при нормальной работе в *-resp топик перекладываются те же json или при ошибка - exception + stack trace

## Otherwise

Или можно попробовать экспериментальную фичу - сабмит из контейнера:
```plantuml
docker compose up -d flink-job-submitter
```

При условии готовности сервисов и корректной сборки запустится та же джоба внутри докер-сети