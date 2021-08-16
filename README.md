# Proceso-Carga-Java--Programa-De-Prueba_V2_Servicios

Código sencillo de monitorización de un directorio y procesamiento de datos procedentes de archivos .csv para su posterior almacenamiento en una base de datos.

En este programa, no se ha utilizado Spring, si no que se ha implementado directamente con hibernate.
Este código aplica el patrón Singleton a la hora de realizar las conexiones con la base de datos y con el directorio a observar. El uso de este patrón suele estar desaconsejado, pero en este caso su implementación resulta bastante útil.
Para la lectura de los archivos csv, nos hemos ayudado de las librerias openCSV. Y para realizar los diversos logs nos hemos ayudado de apache.log4j.

No todas las librerias usadas se encuentran disponibles en la carpeta con el mismo nombre, debido al tamaño de las mismas. En caso de dudas acerca de las librerias usadas, revise las dependencias en el archivo pom.xml.
