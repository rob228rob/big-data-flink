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

можно зайти на http://localhost:7080 и глянуть в ui че там в топиках :)