package no.gundersen.elhub.exception

class DuplicateMeteringPointException(gsrn: String) :
    RuntimeException("Målepunkt med GSRN $gsrn finnes allerede")

class MeteringPointNotFoundException(id: Long) :
    RuntimeException("Fant ikke målepunkt med id $id")