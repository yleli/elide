## Validation for Dynamic Config

Validate the config files in local before deployment.

Sample command:
```
Usage: java -cp <Jar File Name> com.yahoo.elide.contrib.dynamicconfighelpers.validator.DynamicConfigValidator <Path for Model Configs Directory>
```
Expected Model Configs Directory Structure:
```
├── MODEL_CONFIG_DIR/
│   ├── tables
│   │   ├── table1.hjson
│   │   ├── table2.hjson
│   │   ├── ...
│   │   ├── tableN.hjson
│   ├── security.hjson (optional)
│   ├── variables.hjson (optional)
```
