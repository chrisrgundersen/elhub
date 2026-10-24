package no.gundersen.elhub.exception

class DuplicateMeteringPointException(gsrn: String) :
    RuntimeException("Målepunkt med GSRN $gsrn finnes allerede")

class MeteringPointNotFoundException : RuntimeException {
    constructor(id: Long) : super("Fant ikke målepunkt med id $id")
    constructor(gsrn: String) : super("Fant ikke målepunkt med GSRN $gsrn")
}