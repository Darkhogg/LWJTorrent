LightWeight Java Torrent
========================

LWJTorrent (LightWeight Java Torrent) is both a BitTorrent library and a client.

 *  As a *library*, it contains classes to deal with torrent related tasks.
 *  As a *client*, it provides a lightweight, simple and silent implementation
    of the BitTorrent protocol, which can be used by other applications without
    any user interaction. **_This is currently not implemented at all_**

Library
-------

LWJTorrent includes packages that contains classes that helps with
BitTorrent-related tasks. These packages are the core of LWJTorrent, but
can be perfectly used outside this project with little effort.


### es.darkhogg.torrent.bencode

This package is completely stand-alone, something that should not be shocking,
as it once was a fully independent project called Darkhogg-Bencode. It
is just what it looks, a full Bencode library, with no dependencies
outside the JDK, which can be used completely on its own.


### es.darkhogg.torrent.data

Consists entirely of classes that models BitTorrent-related data. The most
important class here is the `TorrentMetaInfo` class, which models a meta info
file, or in more common terms, a `*.torrent` file.


### es.darkhogg.torrent.tracker

A package that deals with tracker announces. The `Tracker` class is
specifically designed as an abstract class that can only be instantiated by
passing a `TorrentMetaInfo` object. Depending on the object passed, different
implementations may be used. `TrackerRequest`s are built by the user and sent
to the `Tracker`, which then responds with a `TrackerResponse`.


### es.darkhogg.torrent.wire

Probably the most important package in the whole library. This package models
the BitTorrent protocol used to exchange data between peers. The name of the
package is chosen after the name *Peer Wire Protocol* seen in some places, as
the name *protocol* would have been confusing, as the tracker and meta info
files also define protocols. This package seems complicated at first, but is
in fact really easy to use.


### es.darkhogg.torrent.dht

An implementation of the Distributed Hash Table system used by BitTorrent for
trackerless torrents.

