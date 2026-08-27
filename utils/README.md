# Data generation
The script in here can be used to generate a lot of sample data. It requires node and npm to run

# Installation
```shell
npm install
```

# Execution
```shell
node ./generateData.js > ../src/main/resources/db/changelog/data.sql
node ./generateProductsData.js > ../src/main/resources/db/changelog/products.sql
```

`generateProductsData.js` derives the product catalogue from the order descriptions already in
`data.sql` (it doesn't generate new random data), so run it after `generateData.js`, whenever
`data.sql` changes.

# Notes
If you change the liquibase migration, you'll need to hack the liquibase changelog. Or drop and recreate your database