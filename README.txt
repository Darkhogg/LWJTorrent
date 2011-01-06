 
 es.darkhogg.bencode is a simple Java implementation of Bencode, a format
 designed for the BitTorrent .torrent files.
 
 To read a file or some other stream, simply instantiate BencodeInputStream and
 call readValue(). This will return a Value<?>, which can be casted to one of
 its subclasses or converted using the convertFromValue methods in Bencode.
 
 To do the opposite, writing data to streams, use BencodeOutputStream. You can
 write Values directly or convert other objects into values using the Bencode
 methods convertToValue.
 
 If you find any problems, bugs, etc., please use the GitHub main project
 page: https://github.com/Darkhogg/es.darkhogg.bencode/
 
 es.darkhogg.bencode  Copyright (C) 2011  Daniel Escoz
 This program comes with ABSOLUTELY NO WARRANTY.
 This is free software, and you are welcome to redistribute it
 under certain conditions. See LICENSE.txt for details.