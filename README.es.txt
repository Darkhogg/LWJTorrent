 
 es.darkhogg.bencode es una implementaci�n simple en Java de Bencode, un
 formato originalmente dise�ado para los ficheros .torrent de BitTorrent.
 
 Para leer valores bencode de un fichero o cualquier otro stream, simplemente
 instancia BencodeInputStream y llama readValue(). Esto devolver� un Value<?>,
 que puede ser convertido directamente a una de sus subclases o convertido a un
 objeto est�ndar de la API de Java usando convertFromValue de la clase Bencode.
 
 Para realizar la acci�n contraria, escribir valores a un stream, utiliza la
 clase BencodeOutputStream. Puedes escribir Values directamente o convertir
 otros objetos usando los m�todos convertToValue de Bencode.
 
 Ante cualquier problema, bug, etc., contactar con el autor usando la p�gina
 del proyecto en GitHub: https://github.com/Darkhogg/Darkhogg-Bencode
 
 es.darkhogg.bencode  Copyright (C) 2011  Daniel Escoz
 Esta libreria viene sin ABSOLUTAMENTE NINGUNA GARANT�A.
 Esto es software libre, y tienes todo el derecho a modificar y/o redistribuirlo
 bajo ciertas condiciones. V�ase LICENSE.txt para m�s detalles (en ingl�s).