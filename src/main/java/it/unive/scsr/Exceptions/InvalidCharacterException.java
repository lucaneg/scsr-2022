package it.unive.scsr.Exceptions;

public class InvalidCharacterException extends RuntimeException{

    public InvalidCharacterException(char c) {
        super(c + " is not a valid alphabet character.");
    }
}
