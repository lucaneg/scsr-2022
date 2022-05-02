package it.unive.scsr;

public class WrongBuildStringGraphException extends RuntimeException{

    public WrongBuildStringGraphException() {
        super("String graph is not well-formed: just CONCAT nodes can have edges to father.");
    }
}
