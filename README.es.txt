Darkhogg Bencode es una implementación simple en Java de Bencode, un
formato originalmente diseñado para los ficheros .torrent de BitTorrent.

Para leer valores bencode de un fichero o cualquier otro stream, simplemente
instancia BencodeInputStream y llama readValue(). Esto devolverá un Value<?>,
que puede ser convertido directamente a una de sus subclases o convertido a un
objeto estándar de la API de Java usando convertFromValue de la clase Bencode.

Para realizar la acción contraria, escribir valores a un stream, utiliza la
clase BencodeOutputStream. Puedes escribir Values directamente o convertir
otros objetos usando los métodos convertToValue de Bencode.

Ante cualquier problema, bug, etc., contactar con el autor usando la p�gina
del proyecto en GitHub: https://github.com/Darkhogg/Darkhogg-Bencode

Darkhogg Bencode  Copyright (C) 2011  Daniel Escoz
This library comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions. See LICENSE.txt for details.