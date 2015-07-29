# nmdapi-loader

Code to generate all mission data file from current NMD Sea2Data database.

The main class expects to find the property file sea2dataLoader.properties in the current working directory.

The property file should contain:

## JDBC connection properties to query data from
* jdbc.driver
* jdbc.url
* jdbc.user
* jdbc.password

## Paths to write files to
* output.path
 Path to write generated files 
* error.path
 Path to write any files that contains errors/missing data.