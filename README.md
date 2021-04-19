## What's this?
> An extension package of flyway, which supports dynamic column values during data migration

## How to use it?

1. Maven dependence

   ```xml
   <dependency>
   	<groupId>com.fixiu</groupId>
   	<artifactId>flyway-extends</artifactId>
   	<version>0.0.1-RELEASE</version>
   </dependency>
   ```

2. Configure example

   ```yml
   migration:
     column-injection:
       snowflake-id-column:
         add-if-missing: true
         column-index: 0
         column-name: id
         column-type: long
         inject-class: com.fixiu.flyway.injection.SnowflakeIdInjector
   #      inject-value: 1245678987654321
         operations-support:
         - INSERT
         tables:
         - test_1
         - test_2
         - test_3
       date-column:
         add-if-missing: true
         column-name: date_column
         column-type: date
         inject-class: com.fixiu.flyway.injection.CurrentDateInjector
         operations-support:
         - INSERT
         - UPDATE
         tables:
         - test_1
         - test_2
         - test_3
       time-column:
         add-if-missing: true
         column-name: time_column
         column-type: time
         inject-class: com.fixiu.flyway.injection.CurrentTimeInjector
         operations-support:
         - INSERT
         tables:
         - test_1
         - test_2
         - test_3
       date-time-column:
         add-if-missing: true
         column-name: datetime_column
         column-type: timestamp
         inject-class: com.fixiu.flyway.injection.CurrentDatetimeInjector
         operations-support:
         - INSERT
         - UPDATE
         tables:
         - test_1
         - test_2
         - test_3
       normal-column:
         add-if-missing: false
         column-name: name_column
         column-type: string
         inject-value: 'Text here'
         operations-support:
         - INSERT
         - UPDATE
         tables:
         - test_2
         - test_3
   ```

3. Explain

   | Property           | Meaning                                                      | Values                                                       | Type                                                         |
   | ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
   | add-if-missing     | If there is missing the column, add it                       | true, false                                                  | Boolean                                                      |
   | column-index       | The position of a column in SQL, starting with 0, default -1 | -                                                            | Integer                                                      |
   | column-name        | The name of the column in SQL                                | -                                                            | String                                                       |
   | column-type        | Type of column in SQL                                        | string<br />long<br />double<br />date<br />time<br />timestamp<br />hex<br />null | com.fixiu.flyway.config.MigrationProperties.ColumnValueType  |
   | inject-class       | Value injection class, extends from com.fixiu.flyway.injection.AbstractValueInjector | com.fixiu.flyway.injection.SnowflakeIdInjector<br />com.fixiu.flyway.injection.CurrentDateInjector<br />com.fixiu.flyway.injection.CurrentTimeInjector<br />com.fixiu.flyway.injection.CurrentDatetimeInjector | Subclass of com.fixiu.flyway.injection.AbstractValueInjector |
   | inject-value       | Injection value                                              | -                                                            | *                                                            |
   | operations-support | Supported operation types                                    | INSERT<br />UPDATE                                           | List\<com.fixiu.flyway.config.MigrationProperties.OperationstType\> |
   | tables             | Supported table names                                        | -                                                            | List\<String\>                                               |

4. Extends

   You can extends `com.fixiu.flyway.sql.JsqlParserSupport` And Implement the supported methods,  using “jsqlparser” to realize the transformation of  “SQL statement”

