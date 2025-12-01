package domain.Exceptions;

public abstract class DomainException extends RuntimeException{

    DomainException(String message){
        super(message);
    }

}
